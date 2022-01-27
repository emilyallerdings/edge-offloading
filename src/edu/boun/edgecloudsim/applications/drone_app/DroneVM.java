package edu.boun.edgecloudsim.applications.drone_app;

import edu.boun.edgecloudsim.core.SimSettings;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.CloudletScheduler;

import java.util.ArrayList;
import java.util.List;

public class DroneVM extends Vm {

    private SimSettings.VM_TYPES type;
    public DroneVM(int id, int userId, double mips, int numberOfPes, int ram,
                  long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);

        type = SimSettings.VM_TYPES.DRONE_VM;
    }

    public SimSettings.VM_TYPES getVmType(){
        return type;
    }

    /**
     *  dynamically reconfigures the mips value of a  VM in CloudSim
     *
     * @param mips new mips value for this VM.
     */
    public void reconfigureMips(double mips){
        super.setMips(mips);
        super.getHost().getVmScheduler().deallocatePesForVm(this);

        List<Double> mipsShareAllocated = new ArrayList<Double>();
        for(int i= 0; i<getNumberOfPes(); i++)
            mipsShareAllocated.add(mips);

        super.getHost().getVmScheduler().allocatePesForVm(this, mipsShareAllocated);
    }
}
