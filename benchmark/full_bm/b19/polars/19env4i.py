import ibis
from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *


def get_sys_stats():
    global max_ram_usage
    max_ram_usage=0.0
    max_ram = 0.0
    max_cpu = 0.0
    while 1 < 2:
        ram_usage = (psutil.virtual_memory()[3] / 1000000)
        if (max_ram_usage < ram_usage):
            max_ram_usage = ram_usage
        sleep(0.2)

def set_MainIndustry(ind):
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    if ind in env:
        return 'ENV'
    else:
        return 'AI'

def myfunc():
    import numpy as np # linear algebra
    start = time()
    # Visualisation
    # load & cleanup
    con = ibis.polars.connect()
    t = con.read_csv("/media/bhushan/nvme1/data/vldb/b19/s4.csv")
    print(t.limit(5).execute())
    # Drop 'S No.' column
    t = t.drop("S No.")

    # Drop NA rows
    t = t.drop_null()
    # df = df.reset_index(drop=True) no need to reset index in duckdb
    print(t.limit(5).execute())

    # Environmental and AI subcategories
    env = ['Agriculture', 'Green Technology', 'Renewable Energy', 'Waste Management']
    ai = ['AI', 'Robotics', 'Computer Vision']

    # Filter for environmental & AI startups
    t_ea = t.filter(t["Industry"].isin(env) | t["Industry"].isin(ai))
    print(t_ea.execute())
    print(time() - start)

if __name__=='__main__':
    max_ram_usage=0.0
    time1 = time()
    init_ram = (psutil.virtual_memory()[3] / 1000000)
    # print('init ram is',init_ram)
    t1 = threading.Thread(target=get_sys_stats, daemon=True)
    t1.start()
    t2 = threading.Thread(target=myfunc)
    t2.start()
    t2.join()
    ram_usage = (max_ram_usage - init_ram)
    print('Maximum memory used: {} MiB'.format(ram_usage))
    time2 = (time() - time1)
    file = open('../../Results.csv','a',newline='')
    writer = csv.writer(file)
    writer.writerow(["b19", "4.2GB", "s4.csv","polars",time2,format(ram_usage),"194B"])
    file.close()



