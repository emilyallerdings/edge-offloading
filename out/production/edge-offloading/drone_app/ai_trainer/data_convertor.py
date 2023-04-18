import pandas as pd
import json
import sys

if len (sys.argv) != 5:
    print('invalid arguments. Usage:')
    print('python data_conventor.py config.json [edge|cloud_rsu|cloud_gsm|drone] [classifier|regression] [train|test]')
    sys.exit(1)
    
with open(sys.argv[1]) as json_data_file:
    data = json.load(json_data_file)
    
target = sys.argv[2]
method = sys.argv[3]
datatype = sys.argv[4]

print("conversion started with args " + target + ", " + method + ", " + datatype)

# output_path = data['arff_foder'] + data['config_name'] + "/"
output_path = "C:\GIT Repo/edge-offloading/sim_results/arff/"

sim_result_folder = data["sim_result_folder"]
statFilePath = output_path + "stat.txt"
 
num_iterations = data["num_iterations"]
train_data_ratio = data["train_data_ratio"]
min_mobile = data["min_mobile"]
max_mobile = data["max_mobile"]
mobile_step_size = data["mobile_step_size"]

def getDecisionColumnName(target):
    if target == "edge":
        COLUMN_NAME  = "EDGE"
    elif target == "cloud_rsu":
        COLUMN_NAME  = "CLOUD_DATACENTER_VIA_RSU"
    elif target == "cloud_gsm":
        COLUMN_NAME  = "CLOUD_DATACENTER_VIA_GSM"
    elif target == "drone":
        COLUMN_NAME  = "DRONE"
    return COLUMN_NAME

def getClassifierColumns(target):
    if target == "edge":
        result  = ["TaskLength","TaskInput","TaskOutput",
                    "WLANUploadDelay","WLANDownloadDelay","AvgEdgeUtilization","NumOffloadedTask", "Result"]
    elif target == "cloud_rsu":
        result  = ["TaskLength","TaskInput","TaskOutput","WANUploadDelay","WANDownloadDelay",
                   "AvgCloudUtilization","NumOffloadedTask", "Result"]
    elif target == "cloud_gsm":
        result  = ["TaskLength","TaskInput","TaskOutput","GSMUploadDelay","GSMDownloadDelay",
                   "AvgCloudUtilization","NumOffloadedTask", "Result"]
    elif target == "drone":
        result  = ["TaskLength","TaskInput","TaskOutput",
                    "WLANUploadDelay","WLANDownloadDelay","AvgDroneUtilization","NumOffloadedTask", "Result"]
    return result

def getRegressionColumns(target):
        if target == "edge":
            result  = ["ServiceTime","TaskLength","TaskInput","TaskOutput",
                        "WLANUploadDelay","WLANDownloadDelay","AvgEdgeUtilization","NumOffloadedTask"]
        elif target == "cloud_rsu":
            result  = ["ServiceTime","TaskLength","TaskInput","TaskOutput","WANUploadDelay","WANDownloadDelay",
                       "AvgCloudUtilization","NumOffloadedTask"]
        elif target == "cloud_gsm":
            result  = ["ServiceTime","TaskLength","TaskInput","TaskOutput","GSMUploadDelay","GSMDownloadDelay",
                       "AvgCloudUtilization","NumOffloadedTask"]
        elif target == "drone":
            result  = ["ServiceTime","TaskLength","TaskInput","TaskOutput",
                        "WLANUploadDelay","WLANDownloadDelay","AvgDroneUtilization","NumOffloadedTask"]
        return result

def znorm(column):
    column = (column - column.mean()) / column.std()
    return column

data_set =  []

for ite in range(num_iterations):
    for mobile in range(min_mobile, max_mobile+1, mobile_step_size):
        # if (datatype == "train" and ite < testDataStartIndex) or (datatype == "test" and ite >= testDataStartIndex):
            file_name = sim_result_folder + "/ite" + str(ite + 1) + "/" + str(mobile) + "_learnerOutputFile.csv"
            df = [pd.read_csv(file_name, na_values = "?", comment='\t', sep=",")]
            df[0]['MobileCount'] = mobile
            # print(file_name)
            data_set += df

data_set = pd.concat(data_set, ignore_index=True)
data_set = data_set[data_set['Decision'] == getDecisionColumnName(target)]

if datatype == "train":
    data_set = data_set.sample(frac=train_data_ratio/100)
else:
    data_set = data_set.sample(frac=1-train_data_ratio/100)

if method == "classifier":
    targetColumns = getClassifierColumns(target)
else:
    targetColumns= getRegressionColumns(target)

if datatype == "train":
    print ("##############################################################")
    print ("Stats for " + target + " - " + method)
    print ("Please use relevant information from below table in java side:")
    train_stats = data_set[targetColumns].describe()
    train_stats = train_stats.transpose()
    print(train_stats)
    
    with open(statFilePath, "a") as myfile:
        for ind in range(0,len(targetColumns)-1):
            myfile.write(str(train_stats['mean'][:,][ind]))
            myfile.write('\n')
                    
    with open(statFilePath, "a") as myfile:
        for ind in range(0,len(targetColumns)-1):
            myfile.write(str(train_stats['std'][:,][ind]))
            myfile.write('\n')
        
    print ("##############################################################")

#print("balancing " + target + " for " + method)

#BALANCE DATA SET
if method == "classifier":
    df0 = data_set[data_set['Result']=="fail"]
    df1 = data_set[data_set['Result']=="success"]
    
    #size = min(len(df0[df0['MobileCount']==max_mobile]), len(df1[df1['MobileCount']==min_mobile]))
    
    size = len(df0[df0['MobileCount']==max_mobile]) // 2
    
    df1 = df1.groupby('MobileCount').apply(lambda x: x if len(x) < size else x.sample(size))
    df0 = df0.groupby('MobileCount').apply(lambda x: x if len(x) < size else x.sample(size))

    data_set = pd.concat([df0, df1], ignore_index=True)
else:        
    data_set = data_set[data_set['Result'] == 'success']
    
    #size = min(len(data_set[data_set['MobileCount']==min_mobile]), len(data_set[data_set['MobileCount']==max_mobile]))
    
    size = len(data_set[data_set['MobileCount']==max_mobile]) // 3
    data_set = data_set.groupby('MobileCount').apply(lambda x: x if len(x.index) < size else x.sample(size))

#EXTRACT RELATED ATTRIBUTES
df = pd.DataFrame(columns=targetColumns)
for column in targetColumns:
    if column == 'Result' or column == 'ServiceTime':
        df[column] = data_set[column]
    else:
        df[column] = znorm(data_set[column])

f = open(output_path + target + "_" + method + "_" + datatype + ".arff", 'w')
f.write('@relation ' + target + '\n\n')
for column in targetColumns:
    if column == 'Result':
        f.write('@attribute class {fail,success}\n')
    else:
        f.write('@attribute ' + column + ' REAL\n')
f.write('\n@data\n')
df.to_csv(f, header=False, index=False)
f.close()

print ("##############################################################")
print ("Operation completed!")
print (".arff file is generated for weka.")
print ("##############################################################")

