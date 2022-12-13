package edu.boun.edgecloudsim.applications.drone_app;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.Host;
import edu.boun.edgecloudsim.utils.Location;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;
import java.lang.Math;

public class DroneHost extends Host {
    private Location location;
    private int speed;
    private int destination;

    public DroneHost(int id, RamProvisioner ramProvisioner,
                     BwProvisioner bwProvisioner, long storage,
                     List<? extends Pe> peList, VmScheduler vmScheduler, int speed) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        this.speed = speed;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setPlace(Location _location) {
        location = _location;
        this.destination = _location.getServingWlanId();
        SimLogger.getInstance().addDroneLocationLog(this.getId(), CloudSim.clock(), location.getXPos(), location.getYPos(), location.getServingWlanId(), 0.0);
    }

    public void setDestination(int _destination) {
        this.destination = _destination;
    }

    public int getDestination() {
        return destination;
    }

    public Location getLocation(double time) {
        if(location.getServingWlanId() != destination) {
            // x and y values based on destination wlan id
            int y_dest = destination / SimSettings.getInstance().getNumColumns() * 400 + 200;
            int x_dest = destination % SimSettings.getInstance().getNumColumns() * 400 + 200;
            double h = Math.sqrt(Math.pow(Math.abs(location.getYPos() - y_dest), 2) + Math.pow(Math.abs(location.getXPos() - x_dest), 2));
            double dist = (this.speed * time) / 3.6;
            int x_dir = location.getXPos() < x_dest ? 1 : -1;
            int y_dir = location.getYPos() < y_dest ? 1 : -1;
            int x = location.getXPos() + x_dir * (int) (Math.abs(location.getXPos() - x_dest) / h * dist);
            int y = location.getYPos() + y_dir * (int) (Math.abs(location.getYPos() - y_dest) / h * dist);
            if (x < SimSettings.getInstance().getEasternBound() && y < SimSettings.getInstance().getNorthernBound()) {
                int Wlan = x / 400 + (y / 400) * SimSettings.getInstance().getNumColumns();
                int placeTypeIndex = SimSettings.getInstance().getPlaceTypeIndex(Wlan);
                location = new Location(placeTypeIndex, Wlan, x, y);
                double vmutil = 0;
                if (this.getVmList().size() > 0)
                    vmutil = this.getVmList().get(0).getTotalUtilizationOfCpu(time) + this.getVmList().get(1).getTotalUtilizationOfCpu(time);
                SimLogger.getInstance().addDroneLocationLog(this.getId(), time, location.getXPos(), location.getYPos(), location.getServingWlanId(), vmutil);
            }
        }
        return location;
    }

    public int moveToWlan(double prob, int wlan) {
        if(SimUtils.getRandomDoubleNumber(0,1) < prob) {
//            System.err.println("\n" + this.getLocation().getServingWlanId() + " --> " + wlan);
            this.destination = wlan;
            return this.location.getServingWlanId();
        }
        else
            return -1;
    }
}
