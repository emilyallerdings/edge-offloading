package edu.boun.edgecloudsim.applications.drone;

import org.cloudbus.cloudsim.UtilizationModel;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.SimLogger;

public class DroneCpuUtilizationModel implements UtilizationModel {
	private Task task;

	public DroneCpuUtilizationModel(){
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		int datacenterId = task.getAssociatedDatacenterId();
		int index = 0;

		if(datacenterId == DroneEdgeOrchestrator.EDGE_DATACENTER)
			index = 9;
		else if(datacenterId == DroneEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM ||
				datacenterId == DroneEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU)
			index = 10;

		return SimSettings.getInstance().getTaskLookUpTable()[task.getTaskType()][index];
	}

	public void setTask(Task _task){
		task=_task;
	}

	public double predictUtilization(SimSettings.VM_TYPES _vmType){
		int index = 0;
		if(_vmType == SimSettings.VM_TYPES.EDGE_VM)
			index = 9;
		else if(_vmType == SimSettings.VM_TYPES.CLOUD_VM)
			index = 10;
		else if(_vmType == SimSettings.VM_TYPES.MOBILE_VM)
			index = 11;
		else{
			SimLogger.printLine("Unknown VM Type! Terminating simulation...");
			System.exit(1);
		}
		return SimSettings.getInstance().getTaskLookUpTable()[task.getTaskType()][index];
	}
}
