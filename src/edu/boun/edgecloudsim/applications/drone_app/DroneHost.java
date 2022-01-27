package edu.boun.edgecloudsim.applications.drone_app;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import org.cloudbus.cloudsim.Host;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

public class DroneHost extends Host {
    private Location location;
    private int speed;

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
    }

    public Location getLocation(double time) {
        Location currentLocation = location;
        boolean locationFound = false;
        int x, y;
        x = y = 0;
        double[] bounds = {SimSettings.getInstance().getNorthernBound(), SimSettings.getInstance().getEasternBound()};

        while (!locationFound) {
            int angle = SimUtils.getRandomNumber(0, 90);
            int direction = SimUtils.getRandomNumber(1, 4);
            double dist = (double) (this.speed * time) / 3.6;

            if (direction == 1) {
                x = currentLocation.getXPos() + (int) (Math.cos(angle) * dist);
                y = currentLocation.getYPos() + (int) (Math.sin(angle) * dist);
            } else if (direction == 2) {
                x = currentLocation.getXPos() - (int) (Math.cos(angle) * dist);
                y = currentLocation.getYPos() + (int) (Math.sin(angle) * dist);
            } else if (direction == 3) {
                x = currentLocation.getXPos() - (int) (Math.cos(angle) * dist);
                y = currentLocation.getYPos() - (int) (Math.sin(angle) * dist);
            } else if (direction == 4) {
                x = currentLocation.getXPos() + (int) (Math.cos(angle) * dist);
                y = currentLocation.getYPos() - (int) (Math.sin(angle) * dist);
            }
            if (x > 0 && x < bounds[0] && y > 0 && y < bounds[1])
                locationFound = true;
        }

        // wlanid specifies the sub-area in the whole area which is used to choose the closest edge device.
        // We use getClosestDrone method in the MobilityModel class. Yet, here we find the new sub-area that dron has entered by moving.
        int WlanId = (x - 1) / 1000 + ((y - 1) / 1000) * 3;
        int placeTypeIndex = (x - 1) / 1000;
        Location futureLocation = new Location(placeTypeIndex, WlanId, x, y);
        location = futureLocation;

        return futureLocation;
    }
}
