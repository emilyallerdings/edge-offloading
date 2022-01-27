# Edge-Cloud Task Offloading - UAVs

Drone Application:

- The whole area is considered in the form of an Euclidean plane which is divided into 5 sections on x axis and 8 sections on y axis (40 zones)
- Each drone moves inside zones in x and y directions

TODO: The distance of the device from the edge device or drone is not considered while calculating or estimating delays in NetworkModel class.
As it might impact the selection of drone over edge or vice versa, we need to include it.
This needs considering the location of the closest edge or drone and the location of the device generating the task, which is completely ignored in offload destination selection. This is because in the original code, the scenario is about selecting between WLAN (edge), GSM, and RSU (cloud). But in our scenario we consider both edge devices and drones using WLAN. 