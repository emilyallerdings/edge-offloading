package edu.boun.edgecloudsim.applications.drone;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.cloud_server.DefaultCloudServerManager;
import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;
import edu.boun.edgecloudsim.network.NetworkModel;

public class DroneScenarioFactory implements ScenarioFactory {
	private int numOfMobileDevice;
	private double simulationTime;
	private String orchestratorPolicy;
	private String simScenario;

	DroneScenarioFactory(int _numOfMobileDevice,
						 double _simulationTime,
						 String _orchestratorPolicy,
						 String _simScenario){
		orchestratorPolicy = _orchestratorPolicy;
		numOfMobileDevice = _numOfMobileDevice;
		simulationTime = _simulationTime;
		simScenario = _simScenario;
	}

	@Override
	public LoadGeneratorModel getLoadGeneratorModel() {
		return new DroneLoadGenerator(numOfMobileDevice, simulationTime, simScenario);
	}

	@Override
	public EdgeOrchestrator getEdgeOrchestrator() {
		return new DroneEdgeOrchestrator(numOfMobileDevice, orchestratorPolicy, simScenario);
	}

	@Override
	public MobilityModel getMobilityModel() {
		return new DroneMobilityModel(numOfMobileDevice,simulationTime);
	}

	@Override
	public NetworkModel getNetworkModel() {
		return new DroneNetworkModel(numOfMobileDevice, simScenario, orchestratorPolicy);
	}

	@Override
	public EdgeServerManager getEdgeServerManager() {
		return new DroneEdgeServerManager();
	}
	@Override
	public CloudServerManager getCloudServerManager() {
		return new DefaultCloudServerManager();
	}

	@Override
	public MobileDeviceManager getMobileDeviceManager() throws Exception {
		return new DroneMobileDeviceManager();
	}

	@Override
	public MobileServerManager getMobileServerManager() {
		return new DroneMobileServerManager(numOfMobileDevice);
	}
}
