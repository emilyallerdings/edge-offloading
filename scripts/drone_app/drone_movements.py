import pandas as pd
import time
import os
from tqdm import tqdm 

CONFIG = 'default'

X_BOUND=3200
Y_BOUND=2000

NUM_COLUMNS=8
NUM_ROWS=5

def get_drone(i, j, drones):
    """function for getting the string representation of a drone or an obstacle

    Args:
        i (int): x position of the drone
        j (int): y position of the drone
        drones (list): list of drones

    Returns:
        str: string representation of a drone or an obstacle
    """
    for d in drones:
        if i > d['position'][0] and i-100 < d['position'][0] and j > d['position'][1] and j - 100 < d['position'][1]:
            return '{0:<2}'.format(int(d['id']))
    else:
        return '  '

def get_task(i, j, tasks):
    """function for getting the string representation of a drone or an obstacle

    Args:
        i (int): x position of the drone
        j (int): y position of the drone
        drones (list): list of drones

    Returns:
        str: string representation of a drone or an obstacle
    """
    # for _, t in tasks.iterrows():
    if (i-1) % 400 == 200 and (j-1) % 400 == 200:
        w = i // 400 + (j // 400) * NUM_COLUMNS
        return '~{0:<2}'.format(int(tasks[f'num users in WLAN {w}']))
    else:
        return '   '

def print_all(drones, tasks):
    """function for printing the whole board with the drones

    Args:
        drones (list): list of drones
    """
    result = ""
    for j in range(1, Y_BOUND+1, 100):
        for i in range(1, X_BOUND+1, 100):
            result += get_drone(i, j, drones) + get_task(i, j, tasks) + "|"
        result += "\n"
    result += "\n"
    return result


iteration_folder = "ite1"
for i in range(1800,1900,100):
    output = []

    drones_file = f"../../sim_results/{iteration_folder}/SIMRESULT_ITS_SCENARIO_AI_BASED_{i}DEVICES_DRONES_LOCATIONS.csv"
    drones_values = pd.read_csv(drones_file, header=0)

    drones = []
    drone_id = 0
    for y in range(200, Y_BOUND+1, 400):
        for x in range(200, X_BOUND+1, 400):
            drones.append({'id':drone_id, 'position': [x, y]})
            drone_id += 1

    tasks_file = f"../../sim_results/{iteration_folder}/SIMRESULT_ITS_SCENARIO_AI_BASED_{i}DEVICES_LOCATION.csv"
    tasks_values = pd.read_csv(tasks_file, header=0)
    tasks = tasks_values.loc[0]
    
    times = drones_values['time'].round(0).drop_duplicates().values
    for t in tqdm(times,total=len(times)):

        for idx, row in drones_values.loc[drones_values['time'] <= t].iterrows():
            if [x for x in drones if x['id'] == row['host_id']]:
                d = drones.index([x for x in drones if x['id'] == row['host_id']][0])
                drones[d] = {'id':row['host_id'], 'position': [row['x'],row['y']]}
            else:
                drones.append({'id':row['host_id'], 'position': [row['x'],row['y']]})
        
        output.append({'time':t, 'resutl':print_all(drones, tasks)})
        drones_values = drones_values.loc[drones_values['time'] > t].copy()

    for o in output:
        print("time:", o['time'])
        print("num devices:", i)

        print(o['resutl'])

        time.sleep(0.2)  # wait for 0.1 seconds so the printed board is visible
        # clear the screan each time so the movements seem continuous. Use os.system('clear') for linux and macos and os.system('cls') for windows
        # input()
        os.system('clear') #os.system('cls')
    
    input(f"Finished for {i} devices! Press any key for {i+100} devices:")