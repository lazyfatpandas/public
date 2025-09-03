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

def set_MainIndustry(ind):
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    if ind in env:
        return 'ENV'
    else:
        return 'AI'

def myfunc():
    start = time()
    # Visualisation
    # load & cleanup
    df = pd.read_csv("/media/bhushan/nvme1/data/vldb/b19/s2.csv")
    print(df.info())
    # LAFP: disable replication
    # # -- STEFANOS -- Replicate Data
    # factor = 3000
    # df = pd.concat([df]*factor)
    # df.info()
    # df.drop('S No.',axis=1,inplace=True)
    # df.dropna(inplace=True)
    # df.reset_index(inplace=True,drop=True)
    df = df.drop('S No.',axis=1)
    df = df.dropna()
    df = df.reset_index(drop=True)

    # LAFP: print added
    print(df.head())
    # Industry sub-categories for environmental & AI startups
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    # combined df - environmental & AI startups only
    # LAFP: inplace not supported
    # df_ea = df.loc[(df['Industry'].isin(env)) | (df['Industry'].isin(ai))].reset_index(drop=True,inplace=False)
    df_ea = df.loc[(df['Industry'].isin(env)) | (df['Industry'].isin(ai))].reset_index(drop=True)
    # custom function to set Main Industry
    # adding a new column
    df_ea['MainIndustry'] = df_ea.Industry.apply(lambda x: set_MainIndustry(x))
    # basic stats
    print(f"A total of {df_ea.shape[0]} startups were started in India between 2016 & 2022, out of which {df_ea.groupby('MainIndustry').size()['ENV']} are environmental related & {df_ea.groupby('MainIndustry').size()['AI']} are AI startups.")
    # # -- STEFANOS -- Disable the rest of the code because it's plotting
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
    writer.writerow(["b19", "450MB", "s2.csv","spark",time2,format(ram_usage),1927])
    file.close()



