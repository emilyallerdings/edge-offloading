package edu.boun.edgecloudsim.applications.drone_app;

import java.util.List;

import edu.boun.edgecloudsim.edge_server.EdgeHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import edu.boun.edgecloudsim.cloud_server.CloudVM;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;
import edu.boun.edgecloudsim.core.SimSettings.VM_TYPES;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.TaskProperty;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimLogger;

public class MyMobileDeviceManager extends MobileDeviceManager {
	private static final int BASE = 100000; //start from base in order not to conflict cloudsim tag!
	private static final int UPDATE_MM1_QUEUE_MODEL = BASE + 1;
	private static final int SET_DELAY_LOG = BASE + 2;
	private static final int READY_TO_SELECT_VM = BASE + 3;
	private static final int REQUEST_RECEIVED_BY_CLOUD = BASE + 4;
	private static final int REQUEST_RECEIVED_BY_MOBILE_DEVICE = BASE + 5;
	private static final int REQUEST_RECEIVED_BY_EDGE_DEVICE = BASE + 6;
	private static final int REQUEST_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_CLOUD = BASE + 7;
	private static final int REQUEST_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_NEIGHBOR = BASE + 8;
	private static final int RESPONSE_RECEIVED_BY_MOBILE_DEVICE = BASE + 9;
	private static final int RESPONSE_RECEIVED_BY_EDGE_DEVICE = BASE + 10;
	private static final int RESPONSE_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_MOBILE_DEVICE = BASE + 11;

	private static final int REQUEST_RECEIVED_BY_DRONE = BASE + 12;
	private static final int REQUEST_RECEIVED_BY_DRONE_TO_RELAY_NEIGHBOR = BASE + 13;
	private static final int RESPONSE_RECEIVED_BY_DRONE_TO_RELAY_MOBILE_DEVICE = BASE + 14;

	private static final double MM1_QUEUE_MODEL_UPDATE_INTERVAL = 0.5; //seconds
	private int taskIdCounter=0;

	public MyMobileDeviceManager() throws Exception{
	}

	@Override
	public void initialize() {
	}

	@Override
	public UtilizationModel getCpuUtilizationModel() {
		return new MyCpuUtilizationModel();
	}

	@Override
	public void startEntity() {
		super.startEntity();
		schedule(getId(), SimSettings.CLIENT_ACTIVITY_START_TIME +
				MM1_QUEUE_MODEL_UPDATE_INTERVAL, UPDATE_MM1_QUEUE_MODEL);

		if(SimSettings.getInstance().getApDelayLogInterval() != 0)
			schedule(getId(), SimSettings.getInstance().getApDelayLogInterval(), SET_DELAY_LOG);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		//do nothing!
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		MyNetworkModel networkModel = (MyNetworkModel)SimManager.getInstance().getNetworkModel();
		MyEdgeOrchestrator edgeOrchestrator = (MyEdgeOrchestrator)SimManager.getInstance().getEdgeOrchestrator();

		Task task = (Task) ev.getData();

		SimLogger.getInstance().taskExecuted(task.getCloudletId());

		if(task.getAssociatedDatacenterId() == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM) {
			NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.GSM_DELAY;
			double gsmDelay = networkModel.getDownloadDelay(delayType, task);
			if(gsmDelay > 0)
			{
				SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), gsmDelay, delayType);
				schedule(getId(), gsmDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
			}
			else
			{
				SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), delayType);
				edgeOrchestrator.taskFailed(task);
			}
		}
		else if(task.getAssociatedDatacenterId() == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU) {
			NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.WAN_DELAY;
			double wanDelay = networkModel.getDownloadDelay(delayType, task);
			if(wanDelay > 0)
			{
				SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), wanDelay, delayType);
				schedule(getId(), wanDelay, RESPONSE_RECEIVED_BY_EDGE_DEVICE, task);
			}
			else
			{
				SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), delayType);
				edgeOrchestrator.taskFailed(task);
			}
		}
		else if(task.getAssociatedDatacenterId() == MyEdgeOrchestrator.EDGE_DATACENTER) {
			NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.WLAN_DELAY;
			double wlanDelay = networkModel.getDownloadDelay(delayType, task);
			if (wlanDelay > 0) {
				SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), wlanDelay, delayType);
				schedule(getId(), wlanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
			} else {
				SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), delayType);
				edgeOrchestrator.taskFailed(task);
			}

		}
		else if(task.getAssociatedDatacenterId() == MyEdgeOrchestrator.DRONE_DATACENTER) {
			DroneHost host = (DroneHost) SimManager.getInstance().getDroneServerManager().getDatacenterList().get(task.getAssociatedHostId()).getHostList().get(0);
			Location currentLocation = host.getLocation(CloudSim.clock());
			if(SimSettings.getInstance().checkNeighborCells(task.getSubmittedLocation().getServingWlanId(), currentLocation.getServingWlanId()))
			{
				NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.WLAN_DELAY;
				double wlanDelay = networkModel.getDownloadDelay(delayType, task);
				if(wlanDelay > 0)
				{
					Location futureLocation = host.getLocation(CloudSim.clock() + wlanDelay);
					if(SimSettings.getInstance().checkNeighborCells(task.getSubmittedLocation().getServingWlanId(), futureLocation.getServingWlanId()))
					{
						SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), wlanDelay, delayType);
						schedule(getId(), wlanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
					}
					else
					{
						SimLogger.getInstance().failedDueToMobility(task.getCloudletId(), CloudSim.clock());
						//edgeOrchestrator.taskFailed(task);
					}
				}
				else
				{
					SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), delayType);
					edgeOrchestrator.taskFailed(task);
				}
			}
			else {
				NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.MAN_DELAY;
				double manDelay = networkModel.getDownloadDelay(delayType, task);
				if(manDelay > 0)
				{
					SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), manDelay, delayType);
					schedule(getId(), manDelay, RESPONSE_RECEIVED_BY_DRONE_TO_RELAY_MOBILE_DEVICE, task);
				}
				else
				{
					SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), delayType);
					edgeOrchestrator.taskFailed(task);
				}
			}
		}
		else {
			SimLogger.printLine("Unknown datacenter id! Terminating simulation...");
			System.exit(1);
		}

	}

	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null! Terminating simulation...");
			System.exit(1);
			return;
		}

		MyNetworkModel networkModel = (MyNetworkModel) SimManager.getInstance().getNetworkModel();
		MyEdgeOrchestrator edgeOrchestrator = (MyEdgeOrchestrator) SimManager.getInstance().getEdgeOrchestrator();

		switch (ev.getTag()) {
			case UPDATE_MM1_QUEUE_MODEL: {
				((MyNetworkModel) networkModel).updateMM1QueeuModel();
				schedule(getId(), MM1_QUEUE_MODEL_UPDATE_INTERVAL, UPDATE_MM1_QUEUE_MODEL);

				break;
			}
			case SET_DELAY_LOG: {
				int[] indices = SimSettings.getInstance().getRandomAttrIndices();
				double apUploadDelays[] = new double[indices.length];
				double apDownloadDelays[] = new double[indices.length];
				for (int i = 0; i < indices.length; i++) {
					apUploadDelays[i] = networkModel.estimateWlanUploadDelay(indices[i]);
					apDownloadDelays[i] = networkModel.estimateWlanDownloadDelay(indices[i]);
				}
				SimLogger.getInstance().addApDelayLog(CloudSim.clock(), apUploadDelays, apDownloadDelays);

				schedule(getId(), SimSettings.getInstance().getVmLoadLogInterval(), SET_DELAY_LOG);
				break;
			}
			// task is just created and assigned to a Datacenter type based on the delay values for different network types.
			case READY_TO_SELECT_VM: {
				Task task = (Task) ev.getData();

				// The task has already an associated datacenter which is decided based on delays and orchestration strategy (AssociatedDatacenterId is set in getDeviceToOffload function in MyEdgeOrchestrator)
				//The VM on the host which is closest to the task (based on location for Drones, and WLAN Id for Edge devices)
				Vm selectedVM = SimManager.getInstance().getEdgeOrchestrator().getVmToOffload(task, task.getAssociatedDatacenterId());

				int nextHopId = task.getAssociatedDatacenterId();
				int nextEvent = 0;
				VM_TYPES vmType = null;

				//
				if (nextHopId == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM) {
					nextEvent = REQUEST_RECEIVED_BY_CLOUD;
					vmType = VM_TYPES.CLOUD_VM;
				}
				//task is sent to cloud over RSU via 2 hops
				else if (nextHopId == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU) {
					nextEvent = REQUEST_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_CLOUD;
					vmType = VM_TYPES.CLOUD_VM;
				}
				//task is sent to best edge device via 2 hops (unless the best edge is the nearest one)
				else if (nextHopId == MyEdgeOrchestrator.EDGE_DATACENTER) {
					vmType = VM_TYPES.EDGE_VM;
				} else if (nextHopId == MyEdgeOrchestrator.DRONE_DATACENTER) {
					vmType = VM_TYPES.DRONE_VM;
				} else {
					SimLogger.printLine("Unknown nextHopId! Terminating simulation...");
					System.exit(1);
				}

				if (selectedVM != null) {
					//set related host id
					task.setAssociatedHostId(selectedVM.getHost().getId());

					//set related vm id
					task.setAssociatedVmId(selectedVM.getId());

					//bind task to related VM
					getCloudletList().add(task);
					bindCloudletToVm(task.getCloudletId(), selectedVM.getId());
					getCloudletList().clear();

					if (selectedVM instanceof DroneVM) {
						DroneHost host = (DroneHost) (selectedVM.getHost());
						// if nearest drone is selected
						if (host.getLocation(CloudSim.clock()).getServingWlanId() == task.getSubmittedLocation().getServingWlanId())
							nextEvent = REQUEST_RECEIVED_BY_DRONE;
						else
						// if the drone moves and exits the wlan which the mobile device resides in, we should relay the task to the cloud.
							nextEvent = REQUEST_RECEIVED_BY_DRONE_TO_RELAY_NEIGHBOR;
					} else if (selectedVM instanceof EdgeVM) {
						EdgeHost host = (EdgeHost) (selectedVM.getHost());
						//if nearest edge device is selected
						if (host.getLocation().getServingWlanId() == task.getSubmittedLocation().getServingWlanId())
							nextEvent = REQUEST_RECEIVED_BY_EDGE_DEVICE;
						else
							// if the mobile device moves and exits the wlan which the Edge resides in, we should relay the task to the next edge.
							nextEvent = REQUEST_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_NEIGHBOR;
					}

					scheduleNow(getId(), nextEvent, task);
				} else {
					//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
					SimLogger.getInstance().rejectedDueToVMCapacity(task.getCloudletId(), CloudSim.clock(), vmType.ordinal());
					edgeOrchestrator.taskFailed(task);
				}
				break;
			}
			case REQUEST_RECEIVED_BY_CLOUD: {
				Task task = (Task) ev.getData();
				submitTaskToVm(task, SimSettings.VM_TYPES.CLOUD_VM);
				break;
			}
			case REQUEST_RECEIVED_BY_EDGE_DEVICE: {
				Task task = (Task) ev.getData();
				submitTaskToVm(task, SimSettings.VM_TYPES.EDGE_VM);
				break;
			}
			case REQUEST_RECEIVED_BY_MOBILE_DEVICE: {
				Task task = (Task) ev.getData();
				submitTaskToVm(task, SimSettings.VM_TYPES.MOBILE_VM);
				break;
			}
			case REQUEST_RECEIVED_BY_DRONE: {
				Task task = (Task) ev.getData();
				submitTaskToVm(task, SimSettings.VM_TYPES.DRONE_VM);
				break;
			}
			// if cloud_rsu is selected as associated datacenter, then the task will be relayed to the cloud by edge.
			case REQUEST_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_CLOUD: {
				Task task = (Task) ev.getData();
				NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.WAN_DELAY;

				double wanDelay = networkModel.getUploadDelay(delayType, task);
				if (wanDelay > 0) {
					SimLogger.getInstance().setUploadDelay(task.getCloudletId(), wanDelay, delayType);
					schedule(getId(), wanDelay, REQUEST_RECEIVED_BY_CLOUD, task);
				} else {
					//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
					SimLogger.getInstance().rejectedDueToBandwidth(
							task.getCloudletId(),
							CloudSim.clock(),
							SimSettings.VM_TYPES.CLOUD_VM.ordinal(),
							delayType);

					edgeOrchestrator.taskFailed(task);
				}
				break;
			}
			case REQUEST_RECEIVED_BY_DRONE_TO_RELAY_NEIGHBOR: {
				Task task = (Task) ev.getData();
				NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.MAN_DELAY;

				double manDelay = networkModel.getUploadDelay(delayType, task);
				if (manDelay > 0) {
					SimLogger.getInstance().setUploadDelay(task.getCloudletId(), manDelay, delayType);
					schedule(getId(), manDelay, REQUEST_RECEIVED_BY_DRONE, task);
				} else {
					//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
					SimLogger.getInstance().rejectedDueToBandwidth(
							task.getCloudletId(),
							CloudSim.clock(),
							VM_TYPES.DRONE_VM.ordinal(),
							delayType);

					edgeOrchestrator.taskFailed(task);
				}
				break;
			}
			case REQUEST_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_NEIGHBOR: {
				Task task = (Task) ev.getData();
				NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.MAN_DELAY;

				double manDelay = networkModel.getUploadDelay(delayType, task);
				if (manDelay > 0) {
					SimLogger.getInstance().setUploadDelay(task.getCloudletId(), manDelay, delayType);
					schedule(getId(), manDelay, REQUEST_RECEIVED_BY_EDGE_DEVICE, task);
				} else {
					//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
					SimLogger.getInstance().rejectedDueToBandwidth(
							task.getCloudletId(),
							CloudSim.clock(),
							SimSettings.VM_TYPES.EDGE_VM.ordinal(),
							delayType);

					edgeOrchestrator.taskFailed(task);
				}
				break;
			}
			case RESPONSE_RECEIVED_BY_EDGE_DEVICE: {
				Task task = (Task) ev.getData();
				scheduleNow(getId(), RESPONSE_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_MOBILE_DEVICE, task);
				break;
			}
			case RESPONSE_RECEIVED_BY_DRONE_TO_RELAY_MOBILE_DEVICE:
			case RESPONSE_RECEIVED_BY_EDGE_DEVICE_TO_RELAY_MOBILE_DEVICE: {
				Task task = (Task) ev.getData();
				NETWORK_DELAY_TYPES delayType = NETWORK_DELAY_TYPES.WLAN_DELAY;

				//SimLogger.printLine(CloudSim.clock() + ": " + getName() + ": task #" + task.getCloudletId() + " received from edge");
				double wlanDelay = networkModel.getDownloadDelay(delayType, task);

				if (wlanDelay > 0) {
					SimLogger.getInstance().setDownloadDelay(task.getCloudletId(), wlanDelay, delayType);
					schedule(getId(), wlanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
				} else {
					SimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), delayType);
					edgeOrchestrator.taskFailed(task);
				}
				break;
			}
			case RESPONSE_RECEIVED_BY_MOBILE_DEVICE: {
				Task task = (Task) ev.getData();

				String taskName = SimSettings.getInstance().getTaskName(task.getTaskType());
				double taskProperty[] = SimSettings.getInstance().getTaskProperties(taskName);
				double serviceTime = CloudSim.clock() - task.getCreationTime();
				double delaySensitivity = taskProperty[13];
				double maxDelayRequirement = taskProperty[14];

				double QoE = 100;
				if (serviceTime > maxDelayRequirement) {
					QoE = (Math.min(2 * maxDelayRequirement, serviceTime) - maxDelayRequirement) / maxDelayRequirement;
					QoE = 100 * (1 - QoE) * (1 - delaySensitivity);
				}

				SimLogger.getInstance().setQoE(task.getCloudletId(), QoE);

				edgeOrchestrator.taskCompleted(task, serviceTime);

				//SimLogger.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " is received");
				SimLogger.getInstance().taskEnded(task.getCloudletId(), CloudSim.clock());

				break;
			}
			default:
				SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker. Terminating simulation...");
				System.exit(1);
				break;
		}
	}

	public synchronized void submitTask(TaskProperty edgeTask) {
		MyNetworkModel networkModel = (MyNetworkModel)SimManager.getInstance().getNetworkModel();
		MyEdgeOrchestrator edgeOrchestrator = (MyEdgeOrchestrator)SimManager.getInstance().getEdgeOrchestrator();

		//create a task
		Task task = createTask(edgeTask);

		Location currentLocation = SimManager.getInstance().getMobilityModel().
				getLocation(task.getMobileDeviceId(),CloudSim.clock());

		//set location of the mobile device which generates this task
		task.setSubmittedLocation(currentLocation);

		//add related task to log list
		SimLogger.getInstance().addLog(task.getMobileDeviceId(),
				task.getCloudletId(),
				task.getTaskType(),
				(int)task.getCloudletLength(),
				(int)task.getCloudletFileSize(),
				(int)task.getCloudletOutputSize());

		long startTime = System.nanoTime();   
		int nextHopId = SimManager.getInstance().getEdgeOrchestrator().getDeviceToOffload(task);
		long estimatedTime = System.nanoTime() - startTime;

		SimLogger.getInstance().setOrchestratorOverhead(task.getCloudletId(), estimatedTime);

		double delay = 0;
		VM_TYPES vmType = null;
		NETWORK_DELAY_TYPES delayType = null;

		if(nextHopId == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM){
			vmType = VM_TYPES.CLOUD_VM;
			delayType = NETWORK_DELAY_TYPES.GSM_DELAY;
			delay = networkModel.getUploadDelay(delayType, task);
		}
		//task is sent to cloud over RSU via 2 hops
		else if(nextHopId == MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU){
			vmType = VM_TYPES.CLOUD_VM;
			delayType = NETWORK_DELAY_TYPES.WLAN_DELAY;
			delay = networkModel.getUploadDelay(delayType, task);
		}
		//task is sent to best edge device via 2 hops (unless the best edge is the nearest one)
		else if(nextHopId == MyEdgeOrchestrator.EDGE_DATACENTER){
			vmType = VM_TYPES.EDGE_VM;
			delayType = NETWORK_DELAY_TYPES.WLAN_DELAY;
			delay = networkModel.getUploadDelay(delayType, task);
		}
		else if(nextHopId == MyEdgeOrchestrator.DRONE_DATACENTER){
			vmType = VM_TYPES.DRONE_VM;
			delayType = NETWORK_DELAY_TYPES.WLAN_DELAY;
			delay = networkModel.getUploadDelay(delayType, task);
		}
		else {
			SimLogger.printLine("Unknown nextHopId! Terminating simulation...");
			System.exit(1);
		}

		task.setAssociatedDatacenterId(nextHopId);

		if(delay>0){
			//set related host id
			schedule(getId(), delay, READY_TO_SELECT_VM, task);

			SimLogger.getInstance().taskStarted(task.getCloudletId(), CloudSim.clock());
			SimLogger.getInstance().setUploadDelay(task.getCloudletId(), delay, delayType);
		}
		else
		{
			//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
			SimLogger.getInstance().rejectedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), vmType.ordinal(), delayType);
			edgeOrchestrator.taskFailed(task);
		}
	}

	private void submitTaskToVm(Task task, SimSettings.VM_TYPES vmType) {
		MyEdgeOrchestrator edgeOrchestrator = (MyEdgeOrchestrator)SimManager.getInstance().getEdgeOrchestrator();

		Vm targetVM = null;
		if(vmType == VM_TYPES.EDGE_VM) {
			int numberOfHost = SimSettings.getInstance().getNumOfEdgeHosts();
			for (int hostIndex = 0; hostIndex < numberOfHost; hostIndex++) {
				List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(hostIndex);
				for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
					if(vmArray.get(vmIndex).getId() == task.getAssociatedVmId()) {
						targetVM = vmArray.get(vmIndex);
						break;
					}
				}
				if(targetVM != null)
					break;
			}
		}
		else if(vmType == VM_TYPES.DRONE_VM) {
			int numberOfHost = SimSettings.getInstance().getNumOfDroneHosts();
			for (int hostIndex = 0; hostIndex < numberOfHost; hostIndex++) {
				List<DroneVM> vmArray = SimManager.getInstance().getDroneServerManager().getVmList(hostIndex);
				for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
					if(vmArray.get(vmIndex).getId() == task.getAssociatedVmId()) {
						targetVM = vmArray.get(vmIndex);
						break;
					}
				}
				if(targetVM != null)
					break;
			}
		}
		else if(vmType == VM_TYPES.CLOUD_VM) {
			List<Host> list = SimManager.getInstance().getCloudServerManager().getDatacenter().getHostList();
			for (int hostIndex = 0; hostIndex < list.size(); hostIndex++) {
				List<CloudVM> vmArray = SimManager.getInstance().getCloudServerManager().getVmList(hostIndex);
				for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
					if(vmArray.get(vmIndex).getId() == task.getAssociatedVmId()) {
						targetVM = vmArray.get(vmIndex);
						break;
					}
				}
				if(targetVM != null)
					break;
			}
		}
		else {
			SimLogger.printLine("Unknown vm type! Terminating simulation...");
			System.exit(1);
		}

		double targetVmCapacity = (double) 100 - targetVM.getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());

		double requiredCapacity = ((MyCpuUtilizationModel) task.getUtilizationModelCpu()).predictUtilization(vmType);

		if (requiredCapacity > targetVmCapacity) {
			// TODO: iterate over netxHopId which is array of offload devices
			SimLogger.getInstance().rejectedDueToVMCapacity(task.getCloudletId(), CloudSim.clock(), vmType.ordinal());
			edgeOrchestrator.taskFailed(task);
		} else {
			// SimLogger.printLine(CloudSim.clock() + ": Cloudlet#" + task.getCloudletId() +
			// " is submitted to VM#" + task.getVmId());
			schedule(getVmsToDatacentersMap().get(task.getVmId()), 0, CloudSimTags.CLOUDLET_SUBMIT, task);

			SimLogger.getInstance().taskAssigned(task.getCloudletId(), task.getAssociatedDatacenterId(),
					task.getAssociatedHostId(), task.getAssociatedVmId(), vmType.ordinal());
		}
	}

	private Task createTask(TaskProperty edgeTask){
		UtilizationModel utilizationModel = new UtilizationModelFull(); /*UtilizationModelStochastic*/
		UtilizationModel utilizationModelCPU = getCpuUtilizationModel();

		Task task = new Task(edgeTask.getMobileDeviceId(), ++taskIdCounter,
				edgeTask.getLength(), edgeTask.getPesNumber(),
				edgeTask.getInputFileSize(), edgeTask.getOutputFileSize(),
				utilizationModelCPU, utilizationModel, utilizationModel);

		//set the owner of this task
		task.setUserId(this.getId());
		task.setTaskType(edgeTask.getTaskType());

		if (utilizationModelCPU instanceof MyCpuUtilizationModel) {
			((MyCpuUtilizationModel)utilizationModelCPU).setTask(task);
		}

		return task;
	}
}
