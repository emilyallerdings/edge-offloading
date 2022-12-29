package edu.boun.edgecloudsim.applications.drone_app;

// in our use case (drone app) task sources are not mobile
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import org.cloudbus.cloudsim.core.CloudSim;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;

import java.util.ArrayList;
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

		int y_bound = (int)SimSettings.getInstance().getNorthernBound();
		int x_bound = (int)SimSettings.getInstance().getEasternBound();

		for (int i = 0; i < numberOfMobileDevices; i++) {
			int x_pos = SimUtils.getRandomNumber(0, x_bound-1);
			int y_pos = SimUtils.getRandomNumber(0, y_bound-1);

			//TODO: This is hard-coded! It's better to get these values from the edge device in the region.
			int wlan_id = x_pos / 400 + (y_pos / 400) * SimSettings.getInstance().getNumColumns();
			int placeTypeIndex = SimSettings.getInstance().getPlaceTypeIndex(wlan_id);
			location[i] = new Location(placeTypeIndex, wlan_id, x_pos, y_pos);
		}
	}

	@Override
	public Location getLocation(int deviceId, double time) {
		// devices are not mobile
		return location[deviceId];
	}

	public DroneHost getClosestDrone(int mobileID) {
		List<Integer> hosts = new ArrayList<Integer>();
		double minDist = Integer.MAX_VALUE;
		Location deviceLoc = SimManager.getInstance().getMobilityModel().getLocation(mobileID, CloudSim.clock());

		//TODO: choose host randomly within a wlan
		for (int i = 0; i < SimManager.getInstance().getDroneServerManager().getDatacenterList().size(); i++) {
			List<? extends DroneHost> list = SimManager.getInstance().getDroneServerManager().getDatacenterList().get(i).getHostList();
			Location hostLoc = list.get(0).getLocation(CloudSim.clock());
			if (hostLoc.getServingWlanId() == deviceLoc.getServingWlanId() &&
					list.get(0).getDestination() == hostLoc.getServingWlanId()) {
				hosts.add(i);
			}
		}
		if(hosts.size() == 0) {
			for (int i = 0; i < SimManager.getInstance().getDroneServerManager().getDatacenterList().size(); i++) {
				List<? extends DroneHost> list = SimManager.getInstance().getDroneServerManager().getDatacenterList().get(i).getHostList();
				Location hostLoc = list.get(0).getLocation(CloudSim.clock());
				if (list.get(0).getDestination() == hostLoc.getServingWlanId() &&
						SimSettings.getInstance().checkNeighborCells(hostLoc.getServingWlanId(), deviceLoc.getServingWlanId())) {
					hosts.add(i);
				} else {
					double dist = Math.sqrt(Math.pow((hostLoc.getXPos() - deviceLoc.getXPos()), 2) + Math.pow((hostLoc.getYPos() - deviceLoc.getYPos()), 2));
					if (dist < minDist) {
						minDist = dist;
						hosts.add(i);
					}
				}
			}
		}
		if(hosts.size() == 0) {
			SimLogger.printLine("Could not find closest drone! Terminating simulation...");
			System.exit(1);
		}
		int rand = SimUtils.getRandomNumber(0, hosts.size()-1);
		DroneHost host = (DroneHost)(SimManager.getInstance().getDroneServerManager().getDatacenterList().get(rand).getHostList().get(0));
		return host;
	}
}
