import pandas as pd
import time
import random
import os


def get_char(i, j, drones):
    """function for getting the string representation of a drone or an obstacle

    Args:
        i (int): x position of the drone
        j (int): y position of the drone
        drones (list): list of drones

    Returns:
        str: string representation of a drone or an obstacle
    """
    for b in drones:
        if i > b['position'][0] and i-100 < b['position'][0] and j > b['position'][1] and j - 100 < b['position'][1]:
            return '{0: <3}'.format(int(b['id']))
    else:
        return '   '

def print_all(drones):
    """function for printing the whole board with the drones

    Args:
        drones (list): list of drones
    """
    for j in range(1, 2001, 100):
        for i in range(1, 3201, 100):
            print(get_char(i, j, drones), end="|")
        print()
    print()


iteration_folder = "ite1"
for i in range(100,1900,100):
    file = f"../../sim_results/{iteration_folder}/SIMRESULT_ITS_SCENARIO_AI_BASED_{i}DEVICES_DRONES_LOCATIONS.csv"
    values = pd.read_csv(file, header=0)
    

    times = values['time'].drop_duplicates().values
    for t in times:
        print(t)
        if t == 0:
            drones = []
        for idx, row in values.loc[values['time'] == t].iterrows():
            if [x for x in drones if x['id'] == row['host_id']]:
                d = drones.index([x for x in drones if x['id'] == row['host_id']][0])
                drones[d] = {'id':row['host_id'], 'position': [row['x'],row['y']]}
            else:
                drones.append({'id':row['host_id'], 'position': [row['x'],row['y']]})
    
        print_all(drones)
        
        time.sleep(0.3)  # wait for 0.1 seconds so the printed board is visible
        # clear the screan each time so the movements seem continuous. Use os.system('clear') for linux and macos and os.system('cls') for windows
        # input()
        os.system('clear') #os.system('cls')
    
    end = input("Finished! Press any key for next iteration:")