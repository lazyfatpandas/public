import lazyfatpandas.pandas as pd
from time import time
from os import environ
import csv
import os
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *
pd.analyze()

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


def myfunc():
    start = time()
    print=pd.lazyPrint
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b1/s5.csv',  parse_dates=['tpep_pickup_datetime']) # fetch data
    df = df[df.fare_amount > 0] # filter bad rows
    df['day'] = df.tpep_pickup_datetime.dt.dayofweek # add features
    df = df.groupby(['day'])['passenger_count'].sum() # aggregationgit pgitgit add .
    print(df) # use dataframe
    pd.flush()
    print(time()-start)


if __name__=='__main__':
    max_ram_usage=0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.POLARS
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
    writer.writerow(["b1", "12.6GB", "s5.csv","lpolars",time2,format(ram_usage),'15C'])
    file.close()


