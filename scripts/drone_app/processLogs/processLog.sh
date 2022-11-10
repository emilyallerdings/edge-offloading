# Run this from inside the sim_result/iteX folder:
# e.g., bash ../../scripts/drone_app/processLogs/processLog.sh

min_number_of_mobile_devices=100
max_number_of_mobile_devices=1800
mobile_device_counter_size=100

echo "=========================================="
echo "Here are the configuration parameters used in this script. If needed, please update them:"
echo "min_number_of_mobile_devices: 100"$min_number_of_mobile_devices
echo "max_number_of_mobile_devices: 1800"$max_number_of_mobile_devices
echo "mobile_device_counter_size: 100"$mobile_device_counter_size
echo "------------------------------------------"
echo "WARNING: The concatenated files except for ALL_APPS_GENERIC.csv are not in order."
echo "=========================================="

mkdir AI_Based
mkdir MAB
mkdir Predictive
mkdir Random

mv *AI_BASED*.csv AI_Based
mv *MAB*.csv MAB
mv *PREDICTIVE*.csv Predictive
mv *RANDOM*.csv Random


cd AI_Based

mkdir DOWNLOAD_DELAY
mkdir ALL_APPS_GENERIC
mkdir UPLOAD_DELAY
mkdir INFOTAINMENT_GENERIC
mkdir DEVICES_LOCATION
mkdir DANGER_ASSESSMENT_GENERIC
mkdir TRAFFIC_MANAGEMENT_GENERIC
mkdir DEVICES_VM_LOAD
mkdir DEVICES_DRONES_LOCATIONS

mv *DOWNLOAD_DELAY*.csv DOWNLOAD_DELAY
mv *ALL_APPS_GENERIC*.csv ALL_APPS_GENERIC 
mv *UPLOAD_DELAY*.csv UPLOAD_DELAY 
mv *INFOTAINMENT_GENERIC*.csv INFOTAINMENT_GENERIC
mv *DEVICES_LOCATION*.csv DEVICES_LOCATION
mv *DANGER_ASSESSMENT_GENERIC*.csv DANGER_ASSESSMENT_GENERIC
mv *TRAFFIC_MANAGEMENT_GENERIC*.csv TRAFFIC_MANAGEMENT_GENERIC 
mv *DEVICES_VM_LOAD*.csv DEVICES_VM_LOAD 
mv *DEVICES_DRONES_LOCATIONS*.csv DEVICES_DRONES_LOCATIONS 


cd ALL_APPS_GENERIC/;
#cat *.csv > ../ALL_APPS_GENERIC.csv;
for (( c=$min_number_of_mobile_devices; c<=$max_number_of_mobile_devices; c=c+mobile_device_counter_size ))
do
	file="SIMRESULT_ITS_SCENARIO_AI_BASED_"$c"DEVICES_ALL_APPS_GENERIC.csv"
	cat $file >> ../ALL_APPS_GENERIC_AI.csv;
done

cd ../DANGER_ASSESSMENT_GENERIC;
cat *.csv > ../DANGER_ASSESSMENT_GENERIC_AI.csv;
cd ../DEVICES_LOCATION;
cat *.csv > ../DEVICES_LOCATION_AI.csv;
cd ../DEVICES_VM_LOAD;
cat *.csv > ../DEVICES_VM_LOAD_AI.csv;
cd ../DOWNLOAD_DELAY;
cat *.csv > ../DOWNLOAD_DELAY_AI.csv;
cd ../INFOTAINMENT_GENERIC;
cat *.csv > ../INFOTAINMENT_GENERIC_AI.csv;
cd ../TRAFFIC_MANAGEMENT_GENERIC;
cat *.csv > ../TRAFFIC_MANAGEMENT_GENERIC_AI.csv;
cd ../UPLOAD_DELAY;
cat *.csv > ../UPLOAD_DELAY_AI.csv;
cd ../DEVICES_DRONES_LOCATIONS;
cat *.csv > ../DEVICES_DRONES_LOCATIONS_AI.csv;

cd ..
#=================================

cd ../MAB
mkdir DOWNLOAD_DELAY
mkdir ALL_APPS_GENERIC
mkdir UPLOAD_DELAY
mkdir INFOTAINMENT_GENERIC
mkdir DEVICES_LOCATION
mkdir DANGER_ASSESSMENT_GENERIC
mkdir TRAFFIC_MANAGEMENT_GENERIC
mkdir DEVICES_VM_LOAD
mkdir DEVICES_DRONES_LOCATIONS

mv *DOWNLOAD_DELAY*.csv DOWNLOAD_DELAY
mv *ALL_APPS_GENERIC*.csv ALL_APPS_GENERIC 
mv *UPLOAD_DELAY*.csv UPLOAD_DELAY 
mv *INFOTAINMENT_GENERIC*.csv INFOTAINMENT_GENERIC
mv *DEVICES_LOCATION*.csv DEVICES_LOCATION
mv *DANGER_ASSESSMENT_GENERIC*.csv DANGER_ASSESSMENT_GENERIC
mv *TRAFFIC_MANAGEMENT_GENERIC*.csv TRAFFIC_MANAGEMENT_GENERIC 
mv *DEVICES_VM_LOAD*.csv DEVICES_VM_LOAD 
mv *DEVICES_DRONES_LOCATIONS*.csv DEVICES_DRONES_LOCATIONS 


cd ALL_APPS_GENERIC/;
#cat *.csv > ../ALL_APPS_GENERIC.csv;
for (( c=$min_number_of_mobile_devices; c<=$max_number_of_mobile_devices; c=c+mobile_device_counter_size ))
do
	file="SIMRESULT_ITS_SCENARIO_MAB_"$c"DEVICES_ALL_APPS_GENERIC.csv"
	cat $file >> ../ALL_APPS_GENERIC_MAB.csv;
done

cd ../DANGER_ASSESSMENT_GENERIC;
cat *.csv > ../DANGER_ASSESSMENT_GENERIC_MAB.csv;
cd ../DEVICES_LOCATION;
cat *.csv > ../DEVICES_LOCATION_MAB.csv;
cd ../DEVICES_VM_LOAD;
cat *.csv > ../DEVICES_VM_LOAD_MAB.csv;
cd ../DOWNLOAD_DELAY;
cat *.csv > ../DOWNLOAD_DELAY_MAB.csv;
cd ../INFOTAINMENT_GENERIC;
cat *.csv > ../INFOTAINMENT_GENERIC_MAB.csv;
cd ../TRAFFIC_MANAGEMENT_GENERIC;
cat *.csv > ../TRAFFIC_MANAGEMENT_GENERIC_MAB.csv;
cd ../UPLOAD_DELAY;
cat *.csv > ../UPLOAD_DELAY_MAB.csv;
cd ../DEVICES_DRONES_LOCATIONS;
cat *.csv > ../DEVICES_DRONES_LOCATIONS_MAB.csv;

cd ..
# ------------------------------------

cd ../Predictive

mkdir DOWNLOAD_DELAY
mkdir ALL_APPS_GENERIC
mkdir UPLOAD_DELAY
mkdir INFOTAINMENT_GENERIC
mkdir DEVICES_LOCATION
mkdir DANGER_ASSESSMENT_GENERIC
mkdir TRAFFIC_MANAGEMENT_GENERIC
mkdir DEVICES_VM_LOAD
mkdir DEVICES_DRONES_LOCATIONS

mv *DOWNLOAD_DELAY*.csv DOWNLOAD_DELAY
mv *ALL_APPS_GENERIC*.csv ALL_APPS_GENERIC 
mv *UPLOAD_DELAY*.csv UPLOAD_DELAY 
mv *INFOTAINMENT_GENERIC*.csv INFOTAINMENT_GENERIC
mv *DEVICES_LOCATION*.csv DEVICES_LOCATION
mv *DANGER_ASSESSMENT_GENERIC*.csv DANGER_ASSESSMENT_GENERIC
mv *TRAFFIC_MANAGEMENT_GENERIC*.csv TRAFFIC_MANAGEMENT_GENERIC 
mv *DEVICES_VM_LOAD*.csv DEVICES_VM_LOAD 
mv *DEVICES_DRONES_LOCATIONS*.csv DEVICES_DRONES_LOCATIONS 


cd ALL_APPS_GENERIC/;
#cat *.csv > ../ALL_APPS_GENERIC.csv;
for (( c=$min_number_of_mobile_devices; c<=$max_number_of_mobile_devices; c=c+mobile_device_counter_size ))
do
	
	file="SIMRESULT_ITS_SCENARIO_PREDICTIVE_"$c"DEVICES_ALL_APPS_GENERIC.csv"
	cat $file >> ../ALL_APPS_GENERIC_Predictive.csv;
done

cd ../DANGER_ASSESSMENT_GENERIC;
cat *.csv > ../DANGER_ASSESSMENT_GENERIC_Predictive.csv;
cd ../DEVICES_LOCATION;
cat *.csv > ../DEVICES_LOCATION_Predictive.csv;
cd ../DEVICES_VM_LOAD;
cat *.csv > ../DEVICES_VM_LOAD_Predictive.csv;
cd ../DOWNLOAD_DELAY;
cat *.csv > ../DOWNLOAD_DELAY_Predictive.csv;
cd ../INFOTAINMENT_GENERIC;
cat *.csv > ../INFOTAINMENT_GENERIC_Predictive.csv;
cd ../TRAFFIC_MANAGEMENT_GENERIC;
cat *.csv > ../TRAFFIC_MANAGEMENT_GENERIC_Predictive.csv;
cd ../UPLOAD_DELAY;
cat *.csv > ../UPLOAD_DELAY_Predictive.csv;
cd ../DEVICES_DRONES_LOCATIONS;
cat *.csv > ../DEVICES_DRONES_LOCATIONS_Predictive.csv;

cd ..

#--------------------------------
cd ../Random

mkdir DOWNLOAD_DELAY
mkdir ALL_APPS_GENERIC
mkdir UPLOAD_DELAY
mkdir INFOTAINMENT_GENERIC
mkdir DEVICES_LOCATION
mkdir DANGER_ASSESSMENT_GENERIC
mkdir TRAFFIC_MANAGEMENT_GENERIC
mkdir DEVICES_VM_LOAD
mkdir DEVICES_DRONES_LOCATIONS

mv *DOWNLOAD_DELAY*.csv DOWNLOAD_DELAY
mv *ALL_APPS_GENERIC*.csv ALL_APPS_GENERIC 
mv *UPLOAD_DELAY*.csv UPLOAD_DELAY 
mv *INFOTAINMENT_GENERIC*.csv INFOTAINMENT_GENERIC
mv *DEVICES_LOCATION*.csv DEVICES_LOCATION
mv *DANGER_ASSESSMENT_GENERIC*.csv DANGER_ASSESSMENT_GENERIC
mv *TRAFFIC_MANAGEMENT_GENERIC*.csv TRAFFIC_MANAGEMENT_GENERIC 
mv *DEVICES_VM_LOAD*.csv DEVICES_VM_LOAD 
mv *DEVICES_DRONES_LOCATIONS*.csv DEVICES_DRONES_LOCATIONS 


cd ALL_APPS_GENERIC/;
#cat *.csv > ../ALL_APPS_GENERIC.csv;
for (( c=$min_number_of_mobile_devices; c<=$max_number_of_mobile_devices; c=c+mobile_device_counter_size ))
do
	file="SIMRESULT_ITS_SCENARIO_RANDOM_"$c"DEVICES_ALL_APPS_GENERIC.csv"
	cat $file >> ../ALL_APPS_GENERIC_Random.csv;
done

cd ../DANGER_ASSESSMENT_GENERIC;
cat *.csv > ../DANGER_ASSESSMENT_GENERIC_Random.csv;
cd ../DEVICES_LOCATION;
cat *.csv > ../DEVICES_LOCATION_Random.csv;
cd ../DEVICES_VM_LOAD;
cat *.csv > ../DEVICES_VM_LOAD_Random.csv;
cd ../DOWNLOAD_DELAY;
cat *.csv > ../DOWNLOAD_DELAY_Random.csv;
cd ../INFOTAINMENT_GENERIC;
cat *.csv > ../INFOTAINMENT_GENERIC_Random.csv;
cd ../TRAFFIC_MANAGEMENT_GENERIC;
cat *.csv > ../TRAFFIC_MANAGEMENT_GENERIC_Random.csv;
cd ../UPLOAD_DELAY;
cat *.csv > ../UPLOAD_DELAY_Random.csv;
cd ../DEVICES_DRONES_LOCATIONS;
cat *.csv > ../DEVICES_DRONES_LOCATIONS_Random.csv;

cd ..

echo "All done!"

