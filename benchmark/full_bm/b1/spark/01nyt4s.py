import pyspark.pandas as pd
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


def myfunc():
    start = time()
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b1/s4.csv') # fetch data
    # df.memory_usage(deep=True)
    print(df.dtypes) # use dataframe
    df = df[df.fare_amount > 0] # filter bad rows
    df['day'] = df.tpep_pickup_datetime.dt.dayofweek # add features
    print(df.info())
    df = df.groupby(['day'])['passenger_count'].sum() # aggregationgit pgitgit add .
    print(df) # use dataframe
    print(time()-start)



if __name__=='__main__':
    from pyspark.sql import SparkSession
    # Create a SparkSession with memory configuration
    spark = SparkSession.builder \
        .appName("StartupGrowthAnalysis") \
        .master("local[*]") \
        .config("spark.driver.memory", "24g") \
        .config("spark.sql.shuffle.partitions", "12") \
        .getOrCreate()
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
    spark.stop()
    file = open('../../Results.csv','a',newline='')
    writer = csv.writer(file)
    writer.writerow(["b1", "4.2GB", "s4.csv","spark",time2,format(ram_usage),147])
    file.close()



