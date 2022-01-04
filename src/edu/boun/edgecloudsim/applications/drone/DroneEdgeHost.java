package edu.boun.edgecloudsim.applications.drone;

import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

public class DroneEdgeHost extends EdgeHost {
    private boolean type;

    public DroneEdgeHost(int id, RamProvisioner ramProvisioner,
    BwProvisioner bwProvisioner, long storage,
    List<? extends Pe> peList, VmScheduler vmScheduler, boolean type) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        this.type = type;
    }

    public Location getLocation(){
        if (type) //edge
        {
            return super.getLocation();
        } else { // drone
            // randomly move the drone then return new location
            int x_bound = super.getLocation().getServingWlanId() % 3;
            int y_bound = super.getLocation().getServingWlanId() / 3;
            int x = SimUtils.getRandomNumber(x_bound * 1000, (x_bound + 1) * 1000);
            int y = SimUtils.getRandomNumber(y_bound * 1000, (y_bound + 1) * 1000);
            return (new Location(super.getLocation().getPlaceTypeIndex(), super.getLocation().getServingWlanId(), x, y));
        }

    }
}
