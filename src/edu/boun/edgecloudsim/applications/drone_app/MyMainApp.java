package edu.boun.edgecloudsim.applications.drone_app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class MyMainApp {

	/**
	 * Creates main() to run this example
	 */
	static String configName = "default";
	static String  wekaModelsFolder = "scripts/drone_app/config/weka/" + configName + "/";
	
	public static void main(String[] args) {
		//disable console output of cloudsim library
		Log.disable();

		//enable console output and file output of this application
		SimLogger.enablePrintLog();

		int iterationNumber = 5;
		String configFile = "";
		String outputFolder = "";
		String edgeDevicesFile = "";
		String dronesFile = "";
		String applicationsFile = "";
		
		
		if (args.length == 5){
			configFile = args[0];
			edgeDevicesFile = args[1];
			dronesFile = args[5];
			applicationsFile = args[2];
			outputFolder = args[3];
			iterationNumber = Integer.parseInt(args[4]);
		}
		else {
			SimLogger.printLine("Simulation setting file, output folder and iteration number are not provided! Using default ones...");

			configFile = "scripts/drone_app/config/" + configName + "/config.properties";
			applicationsFile = "scripts/drone_app/config/" + configName + "/applications.xml";
			edgeDevicesFile = "scripts/drone_app/config/" + configName + "/edge_devices.xml";
			dronesFile = "scripts/drone_app/config/" + configName + "/drones.xml";
		}

		//load settings from configuration file
		SimSettings SS = SimSettings.getInstance();
		if(SS.initialize(configFile, edgeDevicesFile, dronesFile, applicationsFile) == false) {
			SimLogger.printLine("cannot initialize simulation settings!");
			System.exit(1);
		}

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date SimulationStartDate = Calendar.getInstance().getTime();
		String now = df.format(SimulationStartDate);
		SimLogger.printLine("Simulation started at " + now);
		SimLogger.printLine("----------------------------------------------------------------------");

		if (Arrays.asList(SS.getOrchestratorPolicies()).contains("AI_BASED")) {
			
			WekaWrapper.getInstance().initialize("MultilayerPerceptron", "LinearRegression", wekaModelsFolder, configName);
//			WekaWrapper.getInstance().initialize("NaiveBayes", "SMOreg", wekaModelsFolder, configName);
		}

		// To run the experiments in a loop for all iterations
		for (int itNum = 1; itNum <= iterationNumber; itNum++) {
			outputFolder = "sim_results/ite" + itNum;
			if(SS.getFileLoggingEnabled()){
				SimUtils.cleanOutputFolder(outputFolder);
				SimLogger.enableFileLog();
			}
			for (int i = SS.getMinNumOfMobileDev(); i <= SS.getMaxNumOfMobileDev(); i += SS.getMobileDevCounterSize())
				for (int s = 0; s < SS.getSimulationScenarios().length; s++)
					for (int p = 0; p < SS.getOrchestratorPolicies().length; p++)
						mainHelper(outputFolder, SS.getSimulationScenarios()[s], SS.getOrchestratorPolicies()[p], itNum, i);
		}
		
		Date SimulationEndDate = Calendar.getInstance().getTime();
		now = df.format(SimulationEndDate);
		SimLogger.printLine("Simulation finished at " + now +  ". It took " + SimUtils.getTimeDifference(SimulationStartDate,SimulationEndDate));
	}

	public static void mainHelper(String outputFolder, String simulationScenario, String orchestratorPolicy, int iterationNumber, int numOfMobileDevice){
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date ScenarioStartDate = Calendar.getInstance().getTime();
		String now = df.format(ScenarioStartDate);
		SimSettings SS = SimSettings.getInstance();

		SimLogger.printLine("Scenario started at " + now);
		SimLogger.printLine("Scenario: " + simulationScenario + " - Policy: " + orchestratorPolicy + " - #iteration: " + iterationNumber + " - Drones movement strategy: " + SimSettings.getInstance().getDronesMovementStrategy());
		SimLogger.printLine("Duration: " + SS.getSimulationTime()/60 + " min (warm up period: "+ SS.getWarmUpPeriod()/60 +" min) - #devices: " + numOfMobileDevice);
		SimLogger.getInstance().simStarted(outputFolder, "SIMRESULT_" + simulationScenario + "_"  + orchestratorPolicy + "_" + numOfMobileDevice + "DEVICES");

		try
		{
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 2;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag, 0.01);

			// Generate EdgeCloudsim Scenario Factory
			ScenarioFactory sampleFactory = new MyScenarioFactory(numOfMobileDevice, SS.getSimulationTime(), orchestratorPolicy, simulationScenario);

			// Generate EdgeCloudSim Simulation Manager
			SimManager manager = new SimManager(sampleFactory, numOfMobileDevice, simulationScenario, orchestratorPolicy);

			if(orchestratorPolicy.equals("AI_TRAINER")){
				SimLogger.disableFileLog();
				((MyEdgeOrchestrator)manager.getEdgeOrchestrator()).openTrainerOutputFile();
			}

			// Start simulation
			manager.startSimulation();

			if(orchestratorPolicy.equals("AI_TRAINER"))
				((MyEdgeOrchestrator)manager.getEdgeOrchestrator()).closeTrainerOutputFile();
		}
		catch (Exception e)
		{
			SimLogger.printLine("The simulation has been terminated due to an unexpected error");
			e.printStackTrace();
			System.exit(1);
		}

		Date ScenarioEndDate = Calendar.getInstance().getTime();
		now = df.format(ScenarioEndDate);
		SimLogger.printLine("Scenario finished at " + now +  ". It took " + SimUtils.getTimeDifference(ScenarioStartDate,ScenarioEndDate));
		SimLogger.printLine("----------------------------------------------------------------------");

		System.gc();
	}//End of scenarios loop
}
