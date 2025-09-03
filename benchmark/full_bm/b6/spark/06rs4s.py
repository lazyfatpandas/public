import pyspark.pandas as pd
from pyspark.sql import SparkSession

from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *
spark = SparkSession.builder.appName("Data Processing") .config("spark.executor.memory", "14g").config("spark.driver.memory", "14g").getOrCreate()



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
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b3/s4.csv')
    quantile = df.col1.quantile(0.1)
    #add a new column and calculate 10th quantile of a column
    df['col1_binary'] = df.col1 > df.col1.quantile(0.1)
    #add a new column and calculate 10th quantile of a column
    print(quantile)
    # df['col2_binary']= df.col1 >3
    print(df.head())

    #Filter data
    df = df[df.col2 > 10]
    print(df.head())

    #group by and aggregate
    group_res = df.groupby(['col1_binary']).agg({'col3': ['mean']})
    print(group_res.head())

    #Sum of all columns
    sums=df.sum(axis=0)
    print(sums)

    print(time()-start)
    spark.stop()


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
    writer.writerow(["b6", "4.2GB", "s4.csv","spark",time2,format(ram_usage),647])
    file.close()









