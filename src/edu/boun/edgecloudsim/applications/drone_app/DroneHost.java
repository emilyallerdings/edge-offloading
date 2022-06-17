package edu.boun.edgecloudsim.applications.drone_app;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.Host;
import edu.boun.edgecloudsim.utils.Location;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

public class DroneHost extends Host {
    private Location location;
    private int speed;
    private int destination;
//    private List<Location> locations;
//    private List<Double> locationTime;

    public DroneHost(int id, RamProvisioner ramProvisioner,
                     BwProvisioner bwProvisioner, long storage,
                     List<? extends Pe> peList, VmScheduler vmScheduler, int speed) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        this.speed = speed;
//        this.locations = new ArrayList<Location>();
//        this.locationTime = new ArrayList<Double>();
    }

    public Location getLocation() {
        return this.location;
    }

    public void setPlace(Location _location) {
        location = _location;
        this.destination = _location.getServingWlanId();
//        this.locations.add(_location);
//        this.locationTime.add(CloudSim.clock());
    }

    public void setDestination(int _destination) {
        this.destination = _destination;
    }

    public Location getLocation(double time) {
        if(location.getServingWlanId() != destination){
            // x and y values based on destination wlan id
            int y_dest = destination / SimSettings.getInstance().getNumColumns() * 400 + 200;
            int x_dest = destination % SimSettings.getInstance().getNumColumns() * 400 + 200;
            double h = Math.sqrt(Math.pow(Math.abs(location.getYPos() - y_dest), 2) + Math.pow(Math.abs(location.getXPos() - x_dest), 2));
            double dist = (this.speed * time) / 3.6;
            int x = location.getXPos() + (int) (Math.abs(location.getXPos() - x_dest) / h * dist);
            int y = location.getYPos() + (int) (Math.abs(location.getYPos() - y_dest) / h * dist);
            int Wlan = x / 400 + (y / 400) * SimSettings.getInstance().getNumColumns();
            int placeTypeIndex = SimSettings.getInstance().getPlaceTypeIndex(Wlan);
            location = new Location(placeTypeIndex, Wlan, x, y);
//            this.locations.add(location);
//            this.locationTime.add(time);
        }
        return location;
    }

    public void moveToWlan(double prob, int wlan) {
        if(SimUtils.getRandomDoubleNumber(0,1) < prob)
            this.destination = wlan;
    }

//    public List<Location> getLocations() {
//        return locations;
//    }
//
//    public List<Double> getLocationTime() {
//        return locationTime;
//    }
}
