package edu.boun.edgecloudsim.applications.drone_app;

import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.utils.SimLogger;

public class OrchestratorStatisticLogger {
	public static final int NUMBER_OF_HISTORY_WINDOW = 4;
	public static final double PREDICTION_WINDOW_UPDATE_INTERVAL = 0.125; //seconds

	private StatisticWrapper statForPreviousWindow[];
	private StatisticWrapper statForCurrentWindow;

	class StatItem {
		long numOfCompletedTasks;
		long numOfFailedTasks;
		double totalServiceTime;

		double getFailureRate() {
			double failureRate = 0.1;
			if(numOfFailedTasks != 0)
				failureRate = ((double)100 * numOfFailedTasks) / (numOfCompletedTasks + numOfFailedTasks);

			return failureRate;
		}

		double getAvgServiceTime() {
			double serviceTime = 0.01;
			if(numOfCompletedTasks != 0)
				serviceTime = totalServiceTime/numOfCompletedTasks;

			return serviceTime;
		}
	}

	class StatisticWrapper {
		StatItem edgeStat;
		StatItem cloudViaRsuStat;
		StatItem cloudViaGsmStat;
		StatItem droneStat;

		double getFailureRate(int targetDatacenter) {
			double failureRate = 0;

			switch (targetDatacenter) {
				case MyEdgeOrchestrator.EDGE_DATACENTER:
					failureRate = edgeStat.getFailureRate();
					break;
				case MyEdgeOrchestrator.DRONE_DATACENTER:
					failureRate = droneStat.getFailureRate();
					break;
				case MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU:
					failureRate = cloudViaRsuStat.getFailureRate();
					break;
				case MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM:
					failureRate = cloudViaGsmStat.getFailureRate();
					break;
				default:
					SimLogger.printLine("Unknow target datacenter in predictive orchestration policy! Terminating simulation...");
					System.exit(1);
					break;
			}

			return failureRate;
		}

		double getAvgServiceTime(int targetDatacenter) {
			double serviceTime = 0.01;

			switch (targetDatacenter) {
				case MyEdgeOrchestrator.EDGE_DATACENTER:
					serviceTime = edgeStat.getAvgServiceTime();
					break;
				case MyEdgeOrchestrator.DRONE_DATACENTER:
					serviceTime = droneStat.getAvgServiceTime();
					break;
				case MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU:
					serviceTime = cloudViaRsuStat.getAvgServiceTime();
					break;
				case MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM:
					serviceTime = cloudViaGsmStat.getAvgServiceTime();
					break;
				default:
					SimLogger.printLine("Unknow target datacenter in predictive orchestration policy! Terminating simulation...");
					System.exit(1);
					break;
			}

			return serviceTime;
		}
	}

	public OrchestratorStatisticLogger() {
		statForCurrentWindow = new StatisticWrapper();
		statForCurrentWindow.cloudViaRsuStat = new StatItem();
		statForCurrentWindow.cloudViaGsmStat = new StatItem();
		statForCurrentWindow.edgeStat = new StatItem();
		statForCurrentWindow.droneStat = new StatItem();

		statForPreviousWindow = new StatisticWrapper[NUMBER_OF_HISTORY_WINDOW];
		for(int i = 0; i< NUMBER_OF_HISTORY_WINDOW; i++){
			statForPreviousWindow[i] = new StatisticWrapper();

			statForPreviousWindow[i].cloudViaRsuStat = new StatItem();
			statForPreviousWindow[i].cloudViaGsmStat = new StatItem();
			statForPreviousWindow[i].edgeStat = new StatItem();
			statForPreviousWindow[i].droneStat = new StatItem();
		}
	}

	public synchronized void addSuccessStat(Task task, double serviceTime) {
		addStat(true, serviceTime, task.getAssociatedDatacenterId());
	}

	public synchronized void addFailStat(Task task) {
		addStat(false, 0, task.getAssociatedDatacenterId());
	}

	private synchronized void addStat(boolean isCompleted, double serviceTime, int targetDatacenter) {
		StatItem statItem = null;

		switch (targetDatacenter) {
			case MyEdgeOrchestrator.EDGE_DATACENTER:
				statItem = statForCurrentWindow.edgeStat;
				break;
			case MyEdgeOrchestrator.DRONE_DATACENTER:
				statItem = statForCurrentWindow.droneStat;
				break;
			case MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_RSU:
				statItem = statForCurrentWindow.cloudViaRsuStat;
				break;
			case MyEdgeOrchestrator.CLOUD_DATACENTER_VIA_GSM:
				statItem = statForCurrentWindow.cloudViaGsmStat;
				break;
			default:
				SimLogger.printLine("Unknow target datacenter in predictive orchestration policy! Terminating simulation...");
				System.exit(1);
				break;
		}

		if(isCompleted) {
			statItem.numOfCompletedTasks++;
			statItem.totalServiceTime += serviceTime;
		}
		else {
			statItem.numOfFailedTasks++;
		}
	}

	public double getFailureRate(int targetDatacenter){
		double result = 0;

		for (int i=0; i<NUMBER_OF_HISTORY_WINDOW; i++) {
			result += (statForPreviousWindow[i].getFailureRate(targetDatacenter) * (double)(NUMBER_OF_HISTORY_WINDOW - i));
		}

		return result;
	}

	public double getServiceTime(int targetDatacenter){
		double result = 0;

		for (int i=0; i<NUMBER_OF_HISTORY_WINDOW; i++) {
			result += (statForPreviousWindow[i].getAvgServiceTime(targetDatacenter) * (double)(NUMBER_OF_HISTORY_WINDOW - i));
		}

		return result;
	}

	public void switchNewStatWindow() {
		for(int i = NUMBER_OF_HISTORY_WINDOW -2; i>=0; i--){
			statForPreviousWindow[i+1].cloudViaRsuStat.numOfCompletedTasks = statForPreviousWindow[i].cloudViaRsuStat.numOfCompletedTasks;
			statForPreviousWindow[i+1].cloudViaRsuStat.numOfFailedTasks = statForPreviousWindow[i].cloudViaRsuStat.numOfFailedTasks;
			statForPreviousWindow[i+1].cloudViaRsuStat.totalServiceTime = statForPreviousWindow[i].cloudViaRsuStat.totalServiceTime;

			statForPreviousWindow[i+1].cloudViaGsmStat.numOfCompletedTasks = statForPreviousWindow[i].cloudViaGsmStat.numOfCompletedTasks;
			statForPreviousWindow[i+1].cloudViaGsmStat.numOfFailedTasks = statForPreviousWindow[i].cloudViaGsmStat.numOfFailedTasks;
			statForPreviousWindow[i+1].cloudViaGsmStat.totalServiceTime = statForPreviousWindow[i].cloudViaGsmStat.totalServiceTime;

			statForPreviousWindow[i+1].edgeStat.numOfCompletedTasks = statForPreviousWindow[i].edgeStat.numOfCompletedTasks;
			statForPreviousWindow[i+1].edgeStat.numOfFailedTasks = statForPreviousWindow[i].edgeStat.numOfFailedTasks;
			statForPreviousWindow[i+1].edgeStat.totalServiceTime = statForPreviousWindow[i].edgeStat.totalServiceTime;

			statForPreviousWindow[i+1].droneStat.numOfCompletedTasks = statForPreviousWindow[i].droneStat.numOfCompletedTasks;
			statForPreviousWindow[i+1].droneStat.numOfFailedTasks = statForPreviousWindow[i].droneStat.numOfFailedTasks;
			statForPreviousWindow[i+1].droneStat.totalServiceTime = statForPreviousWindow[i].droneStat.totalServiceTime;
		}

		statForPreviousWindow[0].cloudViaRsuStat.numOfCompletedTasks = statForCurrentWindow.cloudViaRsuStat.numOfCompletedTasks;
		statForPreviousWindow[0].cloudViaRsuStat.numOfFailedTasks = statForCurrentWindow.cloudViaRsuStat.numOfFailedTasks;
		statForPreviousWindow[0].cloudViaRsuStat.totalServiceTime = statForCurrentWindow.cloudViaRsuStat.totalServiceTime;
		statForCurrentWindow.cloudViaRsuStat.numOfCompletedTasks = 0;
		statForCurrentWindow.cloudViaRsuStat.numOfFailedTasks = 0;
		statForCurrentWindow.cloudViaRsuStat.totalServiceTime = 0;

		statForPreviousWindow[0].cloudViaGsmStat.numOfCompletedTasks = statForCurrentWindow.cloudViaGsmStat.numOfCompletedTasks;
		statForPreviousWindow[0].cloudViaGsmStat.numOfFailedTasks = statForCurrentWindow.cloudViaGsmStat.numOfFailedTasks;
		statForPreviousWindow[0].cloudViaGsmStat.totalServiceTime = statForCurrentWindow.cloudViaGsmStat.totalServiceTime;
		statForCurrentWindow.cloudViaGsmStat.numOfCompletedTasks = 0;
		statForCurrentWindow.cloudViaGsmStat.numOfFailedTasks = 0;
		statForCurrentWindow.cloudViaGsmStat.totalServiceTime = 0;

		statForPreviousWindow[0].edgeStat.numOfCompletedTasks = statForCurrentWindow.edgeStat.numOfCompletedTasks;
		statForPreviousWindow[0].edgeStat.numOfFailedTasks = statForCurrentWindow.edgeStat.numOfFailedTasks;
		statForPreviousWindow[0].edgeStat.totalServiceTime = statForCurrentWindow.edgeStat.totalServiceTime;
		statForCurrentWindow.edgeStat.numOfCompletedTasks = 0;
		statForCurrentWindow.edgeStat.numOfFailedTasks = 0;
		statForCurrentWindow.edgeStat.totalServiceTime = 0;

		statForPreviousWindow[0].droneStat.numOfCompletedTasks = statForCurrentWindow.droneStat.numOfCompletedTasks;
		statForPreviousWindow[0].droneStat.numOfFailedTasks = statForCurrentWindow.droneStat.numOfFailedTasks;
		statForPreviousWindow[0].droneStat.totalServiceTime = statForCurrentWindow.droneStat.totalServiceTime;
		statForCurrentWindow.droneStat.numOfCompletedTasks = 0;
		statForCurrentWindow.droneStat.numOfFailedTasks = 0;
		statForCurrentWindow.droneStat.totalServiceTime = 0;
	}
}
