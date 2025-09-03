def get_sys_stats():
    global max_ram_usage
    max_ram_usage = 0.0
    max_ram = 0.0
    max_cpu = 0.0
    while (1 < 2):
        ram_usage = (psutil.virtual_memory()[3] / 1000000)
        if (max_ram_usage < ram_usage):
            max_ram_usage = ram_usage
        sleep(0.2)

def set_MainIndustry(ind):
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    if (ind in env):
        return 'ENV'
    else:
        return 'AI'

def myfunc():
    start = time()
    print = pd.lazyPrint
    # SO_c_d_t = {"S No.":"int64","Year":"category","State":"category","Industry":"category","Count":"category"}
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b19/s3.csv')
    print(df.info())
    df = df.drop('S No.',axis=1)
    df = df.dropna()
    df = df.reset_index(drop=True)
    print(df.head())
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    df_ea = df.loc[(df['Industry'].isin(env) | df['Industry'].isin(ai))].reset_index(drop=True)
    df_ea['MainIndustry'] = df_ea.Industry.apply(lambda x: set_MainIndustry(x))
    print(f'A total of {df_ea.shape[0]} startups were started in India between 2016 & 2022, out of which {df_ea.groupby("MainIndustry").size()["ENV"]} are environmental related & {df_ea.groupby("MainIndustry").size()["AI"]} are AI startups.')
    print((time() - start))
    pd.flush(row_selection=False)
    print((time() - start))

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
if (__name__ == '__main__'):
    max_ram_usage = 0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.SPARK
    os.environ['PYARROW_IGNORE_TIMEZONE'] = '1'
    from pyspark.sql import SparkSession
    spark = SparkSession.builder.appName('StartupGrowthAnalysis').master('local[*]').config('spark.driver.memory','24g').config('spark.sql.shuffle.partitions','12').getOrCreate()
    time1 = time()
    init_ram = (psutil.virtual_memory()[3] / 1000000)
    t1 = threading.Thread(target=get_sys_stats,daemon=True)
    t1.start()
    t2 = threading.Thread(target=myfunc)
    t2.start()
    t2.join()
    ram_usage = (max_ram_usage - init_ram)
    print('Maximum memory used: {} MiB'.format(ram_usage))
    time2 = (time() - time1)
    file = open('../../Results.csv','a',newline='')
    writer = csv.writer(file)
    writer.writerow(['b19','abl','s3.csv','lspark',time2,format(ram_usage),1938])
    file.close()
