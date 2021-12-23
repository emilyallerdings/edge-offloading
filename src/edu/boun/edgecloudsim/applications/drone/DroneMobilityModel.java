package edu.boun.edgecloudsim.applications.drone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;

public class DroneMobilityModel extends MobilityModel {
	private final double SPEED_FOR_PLACES[] = {20, 40, 60}; //km per hour

	private int lengthOfSegment;
	private double totalTimeForLoop; //seconds
	private int[][] locationTypes;

	//prepare following arrays to decrease computation on getLocation() function
	//NOTE: if the number of clients is high, keeping following values in RAM
	//      may be expensive. In that case sacrifice computational resources!
	private int[][] initialLocationIndexArray;
	private int[][] initialPositionArray; //in meters unit
	private double[] timeToDriveLocationArray;//in seconds unit
	private double[] timeToReachNextLocationArray; //in seconds unit

	public DroneMobilityModel(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize() {
		//Find total length of the road
		Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
		NodeList datacenterList = doc.getElementsByTagName("datacenter");
		Element location = (Element) ((Element) datacenterList.item(0)).getElementsByTagName("location").item(0);
		int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
		lengthOfSegment = x_pos * 2; //assume that all segments have the same length
		int totalLengthOfRoad = lengthOfSegment * datacenterList.getLength();

		//prepare locationTypes array to store attractiveness level of the locations
		locationTypes = new int[5][8];
		timeToDriveLocationArray = new double[datacenterList.getLength()];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 8; j++) {
				Node datacenterNode = datacenterList.item(i);
				Element datacenterElement = (Element) datacenterNode;
				Element locationElement = (Element) datacenterElement.getElementsByTagName("location").item(0);
				locationTypes[i][j] = Integer.parseInt(locationElement.getElementsByTagName("attractiveness").item(0).getTextContent());

				//(3600 * lengthOfSegment) / (SPEED_FOR_PLACES[x] * 1000);
				timeToDriveLocationArray[i] = ((double) 3.6 * (double) lengthOfSegment) /
						(SPEED_FOR_PLACES[locationTypes[i][j]]);

				//find the time required to loop in the road
				totalTimeForLoop += timeToDriveLocationArray[i];
			}
		}

		//assign a random x position as an initial position for each device
		initialPositionArray = new int[numberOfMobileDevices][2];
		initialLocationIndexArray = new int[numberOfMobileDevices][2];
		timeToReachNextLocationArray = new double[numberOfMobileDevices];
		for (int i = 0; i < numberOfMobileDevices; i++) {
			initialPositionArray[i][0] = SimUtils.getRandomNumber(0, totalLengthOfRoad - 1);
			initialPositionArray[i][1] = SimUtils.getRandomNumber(0, totalLengthOfRoad - 1);
			initialLocationIndexArray[i][0] = initialPositionArray[i][0] / lengthOfSegment;
			initialLocationIndexArray[i][1] = initialPositionArray[i][1] / lengthOfSegment;
			timeToReachNextLocationArray[i] = ((double) 3.6 *
					((double) (lengthOfSegment - (initialPositionArray[i][0] % lengthOfSegment))
							+ (double) ((lengthOfSegment - (initialPositionArray[i][1] % lengthOfSegment))))) /
					(SPEED_FOR_PLACES[locationTypes[initialLocationIndexArray[i][0]][initialLocationIndexArray[i][1]]]);
		}
	}

	@Override
	public Location getLocation(int deviceId, double time) {
		int ofset_x = 0, ofset_y = 0;
		double remainingTime = 0;

		int locationIndex_x = initialLocationIndexArray[deviceId][0];
		int locationIndex_y = initialLocationIndexArray[deviceId][1];
		double timeToReachNextLocation = timeToReachNextLocationArray[deviceId];

		if(time < timeToReachNextLocation){
			ofset_x = initialPositionArray[deviceId][0];
			ofset_y = initialPositionArray[deviceId][1];
			remainingTime = time;
		}
		else{
			remainingTime = (time - timeToReachNextLocation) % totalTimeForLoop;
			locationIndex_x = (locationIndex_x + 1) % 5;
			locationIndex_y = (locationIndex_y + 1) % 8;

			while(remainingTime > timeToDriveLocationArray[locationIndex_x]) {
				remainingTime -= timeToDriveLocationArray[locationIndex_x];
				locationIndex_x =  (locationIndex_x + 1) % 5;
				locationIndex_y =  (locationIndex_y + 1) % 8;
			}

			ofset_x = locationIndex_x * lengthOfSegment;
		}

		int x_pos = (int) (ofset_x + ( (SPEED_FOR_PLACES[locationTypes[locationIndex_x][locationIndex_y]] * remainingTime) / (double)3.6));
		int y_pos = (int) (ofset_y + ( (SPEED_FOR_PLACES[locationTypes[locationIndex_x][locationIndex_y]] * remainingTime) / (double)3.6));
		int locationIndex =  locationIndex_x + locationIndex_y;
		return new Location(locationTypes[locationIndex_x][locationIndex_y], locationIndex, x_pos, y_pos);
	}

}
