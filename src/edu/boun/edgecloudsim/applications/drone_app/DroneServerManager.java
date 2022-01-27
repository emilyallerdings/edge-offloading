package edu.boun.edgecloudsim.applications.drone_app;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeVmAllocationPolicy_Custom;
import edu.boun.edgecloudsim.utils.Location;

public class DroneServerManager extends EdgeServerManager {
    private int hostIdCounter;
    private int[] areaBounds;
    protected List<List<DroneVM>> vmList;

    public DroneServerManager() {
    }

    @Override
    public void initialize() {
        localDatacenters = new ArrayList<Datacenter>();
        hostIdCounter = 0;
        vmList = new ArrayList<List<DroneVM>>();
    }

    @Override
    public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> hostList, int dataCenterIndex) {
        return new EdgeVmAllocationPolicy_Custom(hostList, dataCenterIndex);
    }

    public void startDatacenters() throws Exception {
        Document doc = SimSettings.getInstance().getDronesDocument();
        NodeList datacenterList = doc.getElementsByTagName("datacenter");
        for (int i = 0; i < datacenterList.getLength(); i++) {
            Node datacenterNode = datacenterList.item(i);
            Element datacenterElement = (Element) datacenterNode;
            localDatacenters.add(createDatacenter(i, datacenterElement));
        }
    }

    public List<DroneVM> getDroneVmList(int hostId) {
        return vmList.get(hostId);
    }

    public void createVmList(DroneHost host, Node hostNode, int hostCounter, int hostNodeLength) {
        int vmCounter = 0;
        vmList.add(hostCounter, new ArrayList<DroneVM>());

        Element hostElement = (Element) hostNode;
        NodeList vmNodeList = hostElement.getElementsByTagName("VM");
        for (int k = 0; k < vmNodeList.getLength(); k++) {
            Node vmNode = vmNodeList.item(k);
            Element vmElement = (Element) vmNode;

            String vmm = vmElement.getAttribute("vmm");
            int numOfCores = Integer.parseInt(vmElement.getElementsByTagName("core").item(0).getTextContent());
            double mips = Double.parseDouble(vmElement.getElementsByTagName("mips").item(0).getTextContent());
            int ram = Integer.parseInt(vmElement.getElementsByTagName("ram").item(0).getTextContent());
            long storage = Long.parseLong(vmElement.getElementsByTagName("storage").item(0).getTextContent());
            long bandwidth = SimSettings.getInstance().getWlanBandwidth() / (hostNodeLength + vmNodeList.getLength());

            //VM Parameters
            DroneVM vm = new DroneVM(vmCounter, SimManager.getInstance().getMobileDeviceManager().getId(), mips, numOfCores, ram, bandwidth, storage, vmm, new CloudletSchedulerTimeShared());
            vm.setHost(host);
            vmList.get(hostCounter).add(vm);
            vmCounter++;
        }
    }

    public void createVmList(int brockerId) {
//        int hostCounter=0;
//        int vmCounter=0;
//
//        //Create VMs for each hosts
//        Document doc = SimSettings.getInstance().getDronesDocument();
//        NodeList datacenterList = doc.getElementsByTagName("datacenter");
//        for (int i = 0; i < datacenterList.getLength(); i++) {
//            Node datacenterNode = datacenterList.item(i);
//            Element datacenterElement = (Element) datacenterNode;
//            NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
//            for (int j = 0; j < hostNodeList.getLength(); j++) {
//
//                vmList.add(hostCounter, new ArrayList<DroneVM>());
//
//                Node hostNode = hostNodeList.item(j);
//                Element hostElement = (Element) hostNode;
//                NodeList vmNodeList = hostElement.getElementsByTagName("VM");
//                for (int k = 0; k < vmNodeList.getLength(); k++) {
//                    Node vmNode = vmNodeList.item(k);
//                    Element vmElement = (Element) vmNode;
//
//                    String vmm = vmElement.getAttribute("vmm");
//                    int numOfCores = Integer.parseInt(vmElement.getElementsByTagName("core").item(0).getTextContent());
//                    double mips = Double.parseDouble(vmElement.getElementsByTagName("mips").item(0).getTextContent());
//                    int ram = Integer.parseInt(vmElement.getElementsByTagName("ram").item(0).getTextContent());
//                    long storage = Long.parseLong(vmElement.getElementsByTagName("storage").item(0).getTextContent());
//                    long bandwidth = SimSettings.getInstance().getWlanBandwidth() / (hostNodeList.getLength()+vmNodeList.getLength());
//
//                    //VM Parameters
//                    DroneVM vm = new DroneVM(vmCounter, brockerId, mips, numOfCores, ram, bandwidth, storage, vmm, new CloudletSchedulerTimeShared());
//                    vmList.get(hostCounter).add(vm);
//                    vmCounter++;
//                }
//
//                hostCounter++;
//            }
//        }
    }

    public void terminateDatacenters() {
        for (Datacenter datacenter : localDatacenters) {
            datacenter.shutdownEntity();
        }
    }

    //average utilization of all VMs
    public double getAvgUtilization() {
        double totalUtilization = 0;
        int hostCounter = 0;
        int vmCounter = 0;

        // for each datacenter...
        for (int i = 0; i < localDatacenters.size(); i++) {
            List<? extends Host> list = localDatacenters.get(i).getHostList();
            // for each host...
            for (int hostIndex = 0; hostIndex < list.size(); hostIndex++) {
                List<DroneVM> vmArray = getDroneVmList(hostCounter);
                //for each vm...
                for (int vmIndex = 0; vmIndex < vmArray.size(); vmIndex++) {
                    totalUtilization += vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
                    vmCounter++;
                }
                hostCounter++;
            }
        }
        return totalUtilization / (double) vmCounter;
    }

    private Datacenter createDatacenter(int index, Element datacenterElement) throws Exception {
        String arch = datacenterElement.getAttribute("arch");
        String os = datacenterElement.getAttribute("os");
        String vmm = datacenterElement.getAttribute("vmm");
        double costPerBw = Double.parseDouble(datacenterElement.getElementsByTagName("costPerBw").item(0).getTextContent());
        double costPerSec = Double.parseDouble(datacenterElement.getElementsByTagName("costPerSec").item(0).getTextContent());
        double costPerMem = Double.parseDouble(datacenterElement.getElementsByTagName("costPerMem").item(0).getTextContent());
        double costPerStorage = Double.parseDouble(datacenterElement.getElementsByTagName("costPerStorage").item(0).getTextContent());

        List<DroneHost> hostList = createHosts(datacenterElement);

        String name = "DroneDatacenter_" + Integer.toString(index);
        double time_zone = 3.0;         // time zone this resource located
        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, costPerSec, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;

        VmAllocationPolicy vm_policy = getVmAllocationPolicy(hostList, index);
        datacenter = new Datacenter(name, characteristics, vm_policy, storageList, 0);

        return datacenter;
    }

    private List<DroneHost> createHosts(Element datacenterElement) {
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more Machines
        List<DroneHost> hostList = new ArrayList<DroneHost>();

        Element location = (Element) datacenterElement.getElementsByTagName("location").item(0);
        String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
        int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
        int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
        int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());
        int placeTypeIndex = Integer.parseInt(attractiveness);

        NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
        for (int j = 0; j < hostNodeList.getLength(); j++) {
            Node hostNode = hostNodeList.item(j);

            Element hostElement = (Element) hostNode;
            int numOfCores = Integer.parseInt(hostElement.getElementsByTagName("core").item(0).getTextContent());
            double mips = Double.parseDouble(hostElement.getElementsByTagName("mips").item(0).getTextContent());
            int ram = Integer.parseInt(hostElement.getElementsByTagName("ram").item(0).getTextContent());
            long storage = Long.parseLong(hostElement.getElementsByTagName("storage").item(0).getTextContent());
            long bandwidth = SimSettings.getInstance().getWlanBandwidth() / hostNodeList.getLength();
            int droneSpeed = Integer.parseInt(datacenterElement.getElementsByTagName("speed").item(0).getTextContent());

            // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
            //    create a list to store these PEs before creating
            //    a Machine.
            List<Pe> peList = new ArrayList<Pe>();

            // 3. Create PEs and add these into the list.
            //for a quad-core machine, a list of 4 PEs is required:
            for (int i = 0; i < numOfCores; i++) {
                peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
            }

            //4. Create Hosts with its id and list of PEs and add them to the list of machines
            DroneHost host = new DroneHost(
                    hostIdCounter,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bandwidth), //kbps
                    storage,
                    peList,
                    new VmSchedulerSpaceShared(peList),
                    droneSpeed
            );

            host.setPlace(new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
            hostList.add(host);

            createVmList(host, hostNode, hostIdCounter, hostNodeList.getLength());

            hostIdCounter++;
        }

        return hostList;
    }
}