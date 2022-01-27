package edu.boun.edgecloudsim.applications.drone_app;

// in our use case (drone app) task sources are not mobile
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import org.cloudbus.cloudsim.core.CloudSim;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;

import java.util.List;

public class MyMobilityModel extends MobilityModel {
	private Location[] location;

	public MyMobilityModel(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
	}

	@Override
	public void initialize() {
		//randomly choose a location for each mobile device
		location = new Location[numberOfMobileDevices];

		int x_bound = (int)SimSettings.getInstance().getNorthernBound();
		int y_bound = (int)SimSettings.getInstance().getEasternBound();

		for (int i = 0; i < numberOfMobileDevices; i++) {
			int x_pos = SimUtils.getRandomNumber(0, x_bound);
			int y_pos = SimUtils.getRandomNumber(0, y_bound);

			//TODO: This is hard-coded! It's better to get these values from the edge device in the region.
			int wlan_id = (x_pos-1)/1000 + ((y_pos-1)/1000) * 3 ;
			int placeTypeIndex = (x_pos-1)/1000;
			location[i] = new Location(placeTypeIndex, wlan_id, x_pos, y_pos);
		}
	}

	@Override
	public Location getLocation(int deviceId, double time) {
		// devices are not mobile
		return location[deviceId];
	}

	public DroneHost getClosestDrone(int mobileID) {
		DroneHost host = null;
		double minDist = Integer.MAX_VALUE;

		for (int i = 0; i < SimManager.getInstance().getDroneServerManager().getDatacenterList().size(); i++) {
			List<? extends DroneHost> list = SimManager.getInstance().getDroneServerManager().getDatacenterList().get(i).getHostList();
			for (int j = 0; j < list.size(); j++) {
				Location hostLoc = list.get(j).getLocation(CloudSim.clock());
				Location deviceLoc = SimManager.getInstance().getMobilityModel().getLocation(mobileID, CloudSim.clock());
				double dist = Math.sqrt(Math.pow((hostLoc.getXPos() - deviceLoc.getXPos()), 2) + Math.pow((hostLoc.getYPos() - deviceLoc.getYPos()), 2));
				if (dist < minDist) {
					minDist = dist;
					host = list.get(j);
				}
			}
		}
		if (host == null) {
			SimLogger.printLine("Could not find closest drone! Terminating simulation...");
			System.exit(1);
		}
		return host;
	}
}
