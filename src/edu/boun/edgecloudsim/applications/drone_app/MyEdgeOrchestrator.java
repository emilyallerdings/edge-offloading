package edu.boun.edgecloudsim.applications.drone_app;

import edu.boun.edgecloudsim.applications.sample_app5.WekaWrapper;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.SimEvent;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class MyEdgeOrchestrator extends EdgeOrchestrator {
	private static final int BASE = 100000; //start from base in order not to conflict cloudsim tag!
	private static final int UPDATE_PREDICTION_WINDOW = BASE+1;

	public static final int CLOUD_DATACENTER_VIA_GSM = 1;
	public static final int CLOUD_DATACENTER_VIA_RSU = 2;
	public static final int EDGE_DATACENTER = 3;
	public static final int DRONE_DATACENTER = 4;

	private int cloudVmCounter;
	private int edgeVmCounter;
	private int droneVmCounter;
	private int numOfMobileDevice;

	private OrchestratorStatisticLogger statisticLogger;
	private OrchestratorTrainerLogger trainerLogger;

	private MultiArmedBanditHelper MAB;
	private GameTheoryHelper GTH;

	public MyEdgeOrchestrator(int _numOfMobileDevices, String _policy, String _simScenario) {
		super(_policy, _simScenario);
		this.numOfMobileDevice = _numOfMobileDevices;
	}

	@Override
	public void initialize() {
		cloudVmCounter = 0;
		edgeVmCounter = 0;
		droneVmCounter = 0;

		statisticLogger = new OrchestratorStatisticLogger();
		trainerLogger = new OrchestratorTrainerLogger();

		double lookupTable[][] = SimSettings.getInstance().getTaskLookUpTable();
		//assume the first app has the lowest and the last app has the highest task length value
		double minTaskLength = lookupTable[0][7];
		double maxTaskLength = lookupTable[lookupTable.length-1][7];
		MAB = new MultiArmedBanditHelper(minTaskLength, maxTaskLength);

		//assume the first app has the lowest and the last app has the highest task arrival rate
		//double minTaskArrivalRate = lookupTable[0][2];
		//double maxTaskArrivalRate = lookupTable[lookupTable.length-1][2];
		GTH = new GameTheoryHelper(0, 20, numOfMobileDevice);
	}

	@Override
	public int getDeviceToOffload(Task task) {
		int result = 0;

		double avgEdgeUtilization = SimManager.getInstance().getEdgeServerManager().getAvgUtilization();
		double avgDroneUtilization = SimManager.getInstance().getDroneServerManager().getAvgUtilization();

		MyNetworkModel networkModel = (MyNetworkModel)SimManager.getInstance().getNetworkModel();
		double wanUploadDelay = networkModel.estimateUploadDelay(NETWORK_DELAY_TYPES.WAN_DELAY, task);
		double wanDownloadDelay = networkModel.estimateDownloadDelay(NETWORK_DELAY_TYPES.WAN_DELAY, task);

		double gsmUploadDelay = networkModel.estimateUploadDelay(NETWORK_DELAY_TYPES.GSM_DELAY, task);
		double gsmDownloadDelay = networkModel.estimateDownloadDelay(NETWORK_DELAY_TYPES.GSM_DELAY, task);

		double wlanUploadDelay = networkModel.estimateUploadDelay(NETWORK_DELAY_TYPES.WLAN_DELAY, task);
		double wlanDownloadDelay =networkModel.estimateDownloadDelay(NETWORK_DELAY_TYPES.WLAN_DELAY, task);

		int options[] = {
				EDGE_DATACENTER,
				CLOUD_DATACENTER_VIA_RSU,
				CLOUD_DATACENTER_VIA_GSM,
				DRONE_DATACENTER
		};

		if(policy.startsWith("AI_") || policy.equals("MAB")) {
			if(wanUploadDelay == 0)
				wanUploadDelay = WekaWrapper.MAX_WAN_DELAY;

			if(wanDownloadDelay == 0)
				wanDownloadDelay = WekaWrapper.MAX_WAN_DELAY;

			if(gsmUploadDelay == 0)
				gsmUploadDelay = WekaWrapper.MAX_GSM_DELAY;

			if(gsmDownloadDelay == 0)
				gsmDownloadDelay = WekaWrapper.MAX_GSM_DELAY;

			if(wlanUploadDelay == 0)
				wlanUploadDelay = WekaWrapper.MAX_WLAN_DELAY;

			if(wlanDownloadDelay == 0)
				wlanDownloadDelay = WekaWrapper.MAX_WLAN_DELAY;
		}

		if (policy.equals("AI_BASED")) {
			WekaWrapper weka = WekaWrapper.getInstance();

			boolean predictedResultForDrone = weka.handleClassification(DRONE_DATACENTER,
					new double[] {trainerLogger.getOffloadStat(DRONE_DATACENTER-1),
							task.getCloudletLength(), wlanUploadDelay,
							wlanDownloadDelay, avgDroneUtilization});

			boolean predictedResultForEdge = weka.handleClassification(EDGE_DATACENTER,
					new double[] {trainerLogger.getOffloadStat(EDGE_DATACENTER-1),
							task.getCloudletLength(), wlanUploadDelay,
							wlanDownloadDelay, avgEdgeUtilization});

			boolean predictedResultForCloudViaRSU = weka.handleClassification(CLOUD_DATACENTER_VIA_RSU,
					new double[] {trainerLogger.getOffloadStat(CLOUD_DATACENTER_VIA_RSU-1),
							wanUploadDelay, wanDownloadDelay});

			boolean predictedResultForCloudViaGSM = weka.handleClassification(CLOUD_DATACENTER_VIA_GSM,
					new double[] {trainerLogger.getOffloadStat(CLOUD_DATACENTER_VIA_GSM-1),
							gsmUploadDelay, gsmDownloadDelay});

			double predictedServiceTimeForEdge = Double.MAX_VALUE;
			double predictedServiceTimeForDrone = Double.MAX_VALUE;
			double predictedServiceTimeForCloudViaGSM = Double.MAX_VALUE;
			double predictedServiceTimeForCloudViaRSU = Double.MAX_VALUE;

			if(predictedResultForEdge)
				predictedServiceTimeForEdge = weka.handleRegression(EDGE_DATACENTER,
						new double[] {task.getCloudletLength(), avgEdgeUtilization});

			if(predictedResultForDrone)
				predictedServiceTimeForDrone = weka.handleRegression(DRONE_DATACENTER,
						new double[] {task.getCloudletLength(), avgDroneUtilization});

			if(predictedResultForCloudViaRSU)
				predictedServiceTimeForCloudViaRSU = weka.handleRegression(CLOUD_DATACENTER_VIA_RSU,
						new double[] {task.getCloudletLength(), wanUploadDelay, wanDownloadDelay});

			if(predictedResultForCloudViaGSM)
				predictedServiceTimeForCloudViaGSM = weka.handleRegression(CLOUD_DATACENTER_VIA_GSM,
						new double[] {task.getCloudletLength(), gsmUploadDelay, gsmDownloadDelay});

			if(!predictedResultForEdge && !predictedResultForDrone && !predictedResultForCloudViaRSU && !predictedResultForCloudViaGSM) {
				double probabilities[] = {0.25, 0.25, 0.25, 0.25};

				double randomNumber = SimUtils.getRandomDoubleNumber(0, 1);
				double lastPercentagte = 0;
				boolean resultFound = false;
				for(int i=0; i<probabilities.length; i++) {
					if(randomNumber <= probabilities[i] + lastPercentagte) {
						result = options[i];
						resultFound = true;
						break;
					}
					lastPercentagte += probabilities[i];
				}

				if(!resultFound) {
					SimLogger.printLine("Unexpected probability calculation! Terminating simulation...");
					System.exit(1);
				}
			}
			else if(predictedServiceTimeForEdge <= Math.min(predictedServiceTimeForCloudViaRSU, Math.min(predictedServiceTimeForCloudViaGSM, predictedServiceTimeForDrone)))
				result = EDGE_DATACENTER;

			else if(predictedServiceTimeForCloudViaRSU <= Math.min(predictedServiceTimeForEdge, Math.min(predictedServiceTimeForCloudViaGSM, predictedServiceTimeForDrone)))
				result = CLOUD_DATACENTER_VIA_RSU;

			else if(predictedServiceTimeForCloudViaGSM <= Math.min(predictedServiceTimeForEdge, Math.min(predictedServiceTimeForCloudViaRSU, predictedServiceTimeForDrone)))
				result = CLOUD_DATACENTER_VIA_GSM;

			else if(predictedServiceTimeForDrone <= Math.min(predictedServiceTimeForEdge, Math.min(predictedServiceTimeForCloudViaRSU, predictedServiceTimeForCloudViaGSM)))
				result = DRONE_DATACENTER;

			else{
				SimLogger.printLine("Impossible occurred in AI based algorithm! Terminating simulation...");
				System.exit(1);
			}

			trainerLogger.addOffloadStat(result-1);
		}
		else if (policy.equals("AI_TRAINER")) {
			double probabilities[] = null;
			if(task.getTaskType() == 0)
				// TODO: dunno where these numbers come from. changed them randomly
				probabilities = new double[] {0.50, 0.20, 0.17, 13};
			else if(task.getTaskType() == 1)
				probabilities = new double[] {0.25, 0.43, 0.16, 0.16};
			else
				probabilities = new double[] {0.20, 0.50, 0.15, 0.15};

			double randomNumber = SimUtils.getRandomDoubleNumber(0, 1);
			double lastPercentagte = 0;
			boolean resultFound = false;
			for(int i=0; i<probabilities.length; i++) {
				if(randomNumber <= probabilities[i] + lastPercentagte) {
					result = options[i];
					resultFound = true;

					trainerLogger.addStat(task.getCloudletId(), result,
							wanUploadDelay, wanDownloadDelay,
							gsmUploadDelay, gsmDownloadDelay,
							wlanUploadDelay, wlanDownloadDelay);

					break;
				}
				lastPercentagte += probabilities[i];
			}

			if(!resultFound) {
				SimLogger.printLine("Unexpected probability calculation for AI based orchestrator! Terminating simulation...");
				System.exit(1);
			}
		}

		else if (policy.equals("MAB")) {
			if(!MAB.isInitialized()){
				double expectedProcessingDealyOnCloud = task.getCloudletLength() /
						SimSettings.getInstance().getMipsForCloudVM();

				//All Edge VMs are identical, just get MIPS value from the first VM
				//TODO: might need to change this! not all vm's are identical
				double expectedProcessingDelayOnEdge = task.getCloudletLength() /
						SimManager.getInstance().getEdgeServerManager().getVmList(0).get(0).getMips();

				double expectedProcessingDelayOnDrone = task.getCloudletLength() /
						((DroneServerManager)SimManager.getInstance().getDroneServerManager()).getDroneVmList(0).get(0).getMips();

				double[] expectedDelays = {
						wlanUploadDelay + wlanDownloadDelay + expectedProcessingDelayOnEdge,
						wanUploadDelay + wanDownloadDelay + expectedProcessingDealyOnCloud,
						gsmUploadDelay + gsmDownloadDelay + expectedProcessingDealyOnCloud,
						wlanUploadDelay + wlanDownloadDelay + expectedProcessingDelayOnDrone
				};

				MAB.initialize(expectedDelays, task.getCloudletLength());
			}

			result = options[MAB.runUCB(task.getCloudletLength())];
		}
		else {
			SimLogger.printLine("Unknow edge orchestrator policy! Terminating simulation...");
			System.exit(1);
		}

		return result;
	}

	@Override
	public Vm getVmToOffload(Task task, int deviceId) {
		Vm selectedVM = null;

		if (deviceId == CLOUD_DATACENTER_VIA_GSM || deviceId == CLOUD_DATACENTER_VIA_RSU) {
			int numOfCloudHosts = SimSettings.getInstance().getNumOfCloudHost();
			int hostIndex = (cloudVmCounter / numOfCloudHosts) % numOfCloudHosts;
			int vmIndex = cloudVmCounter % SimSettings.getInstance().getNumOfCloudVMsPerHost();;

			selectedVM = SimManager.getInstance().getCloudServerManager().getVmList(hostIndex).get(vmIndex);

			cloudVmCounter++;
			cloudVmCounter = cloudVmCounter % SimSettings.getInstance().getNumOfCloudVMs();

		}
		else if (deviceId == EDGE_DATACENTER) {
			int numOfEdgeVMs = SimSettings.getInstance().getNumOfEdgeVMs();
			int numOfEdgeHosts = SimSettings.getInstance().getNumOfEdgeHosts();
			int vmPerHost = numOfEdgeVMs / numOfEdgeHosts;

			int hostIndex = (edgeVmCounter / vmPerHost) % numOfEdgeHosts;
			int vmIndex = edgeVmCounter % vmPerHost;

			selectedVM = SimManager.getInstance().getEdgeServerManager().getVmList(hostIndex).get(vmIndex);

			edgeVmCounter++;
			edgeVmCounter = edgeVmCounter % numOfEdgeVMs;
		}
		else if (deviceId == DRONE_DATACENTER) {
			int numOfDroneVMs = SimSettings.getInstance().getNumOfDroneVMs();
			int numOfDroneHosts = SimSettings.getInstance().getNumOfDroneHosts();
			int vmPerHost = numOfDroneVMs / numOfDroneHosts;
			int vmIndex = droneVmCounter % vmPerHost;

			DroneHost host = ((MyMobilityModel)(SimManager.getInstance().getMobilityModel())).getClosestDrone(task.getMobileDeviceId());

			selectedVM = ((DroneServerManager)SimManager.getInstance().getDroneServerManager()).getDroneVmList(host.getId()).get(vmIndex);

			droneVmCounter++;
			droneVmCounter = droneVmCounter % numOfDroneVMs;
		}

		else {
			SimLogger.printLine("Unknow device id! Terminating simulation...");
			System.exit(1);
		}
		return selectedVM;
	}

	@Override
	public void startEntity() {
	}

	@Override
	public void shutdownEntity() {
	}


	@Override
	public void processEvent(SimEvent ev) {
		if (ev == null) {
			SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null! Terminating simulation...");
			System.exit(1);
			return;
		}

		switch (ev.getTag()) {
		case UPDATE_PREDICTION_WINDOW:
		{
			statisticLogger.switchNewStatWindow();
			schedule(getId(), OrchestratorStatisticLogger.PREDICTION_WINDOW_UPDATE_INTERVAL,
					UPDATE_PREDICTION_WINDOW);
			break;
		}
		default:
			SimLogger.printLine(getName() + ": unknown event type");
			break;
		}
	}

	public void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			SimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null! Terminating simulation...");
			System.exit(1);
			return;
		}
	}

	public void taskCompleted(Task task, double serviceTime) {
		if(policy.equals("AI_TRAINER"))
			trainerLogger.addSuccessStat(task, serviceTime);

		if(policy.equals("MAB"))
			MAB.updateUCB(task, serviceTime);
	}

	public void taskFailed(Task task) {
		if(policy.equals("AI_TRAINER"))
			trainerLogger.addFailStat(task);

		if(policy.equals("MAB"))
			MAB.updateUCB(task, 0);
	}

	public void openTrainerOutputFile() {
		trainerLogger.openTrainerOutputFile();
	}

	public void closeTrainerOutputFile() {
		trainerLogger.closeTrainerOutputFile();
	}
}