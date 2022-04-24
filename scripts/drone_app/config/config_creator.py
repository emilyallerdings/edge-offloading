import xml.etree.cElementTree as ET
import pandas as pd

NUM_AREAS = 40
NUM_ROWS = 5
NUM_COLUMNS = 8
NUM_VMS_PER_DEVICE = {"drones": 2, "edge_devices": 2}
ARCH = {"drones": "x86", "edge_devices": "x86"}
OS = {"drones": "Linux", "edge_devices": "Linux"}
VMM = {"drones": "Xen", "edge_devices": "Xen"}

# area wlan Id's mapped to attractiveness
ATTRACTIVNESS = {0: [0, 1, 2, 3, 4, 5, 6, 7, 8, 15, 16, 23, 24, 31, 32, 33, 34, 35, 36, 37, 38, 39],
                 1: [9, 10, 11, 12, 13, 14, 17, 22, 23, 24, 25, 26, 27, 28, 29, 30],
                 2: [18, 19, 20, 21]}


def getLocationInfo(col, idx):
    info = {}

    info["x_pos"] = (idx % NUM_COLUMNS) * 400 + 200
    info["y_pos"] = (idx//NUM_COLUMNS) * 400 + 200
    info["wlan_id"] = idx

    att2 = [18, 19, 20, 21]
    if idx in ATTRACTIVNESS[0]:
        info["attractiveness"] = 0
    elif idx in ATTRACTIVNESS[1]:
        info["attractiveness"] = 1
    else:
        info["attractiveness"] = 2
    return info[col]


count = {"edge_devices": NUM_AREAS, "drones": NUM_AREAS}
values = pd.DataFrame({c: pd.Series(dtype=t) for c, t in {"costPerBw": "float",
                                                          "costPerSec": "float",
                                                          "costPerMem": "float",
                                                          "costPerStorage": "float",
                                                          "core": "int",
                                                          "mips": "float",
                                                          "ram": "int",
                                                          "storage": "int",
                                                          "speed": "float"}.items()},
                      index=["edge_devices", "drones"])

values.loc["edge_devices"] = {"costPerBw": 0.1,
                              "costPerSec": 3,
                              "costPerMem": 0.05,
                              "costPerStorage": 0.1,
                              "core": 4,
                              "mips": 20000,
                              "ram": 16000,
                              "storage": 250000,
                              "speed": 0}
values.loc["drones"] = {
    "costPerBw": 0.1,
    "costPerSec": 3,
    "costPerMem": 0.05,
    "costPerStorage": 0.1,
    "core": 4,
    "mips": 20000,
    "ram": 16000,
    "storage": 250000,
    "speed": 5}

for config in ["edge_devices", "drones"]:
    root = ET.Element(config)
    for i in range(count[config]):
        doc = ET.SubElement(root, "drone" if config ==
                            "drones" else "datacenter", arch=ARCH[config], os=OS[config], vmm=VMM[config])
        for c in ["costPerBw", "costPerSec", "costPerMem", "costPerStorage"]:
            ET.SubElement(doc, c).text = str(values.loc[config][c])

        location = ET.SubElement(doc, 'location')
        for c in ["x_pos", "y_pos", "wlan_id", "attractiveness"]:
            ET.SubElement(location, c).text = str(getLocationInfo(c, i))

        if config == "drones":
            ET.SubElement(doc, 'speed').text = str(
                int(values.loc[config]["speed"]))

        hosts = ET.SubElement(doc, 'hosts')
        host = ET.SubElement(hosts, 'host')
        for c in ["core", "mips", "ram", "storage"]:
            ET.SubElement(host, c).text = str(int(values.loc[config][c]))
        vms = ET.SubElement(host, 'VMs')
        for i in range(NUM_VMS_PER_DEVICE[config]):
            vm = ET.SubElement(vms, 'VM', vmm="Xen")
            for c in ["core", "mips", "ram", "storage"]:
                ET.SubElement(vm, c).text = str(
                    int(values.loc[config][c] / NUM_VMS_PER_DEVICE[config]))

    tree = ET.ElementTree(root)
    tree.write(f"{config}.xml")
