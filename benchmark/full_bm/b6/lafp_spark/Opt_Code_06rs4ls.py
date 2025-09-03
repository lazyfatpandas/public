
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

def myfunc():
  start = time()
  print = pd.lazyPrint
  SO_columns = ["col3","col2","col1"]
  SO_c_d_t = {"col1":"float32","col2":"float32","col3":"float32"}
  df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b3/s4.csv',usecols=SO_columns,dtype=SO_c_d_t)
  quantile = df.col1.quantile(0.1)
  df['col1_binary'] = (df.col1 > df.col1.quantile(0.1))
  print(quantile)
  print(df.head())
  df = df[(df.col2 > 10)]
  print(df.head())
  group_res = df.groupby(['col1_binary']).agg({'col3':['mean']})
  print(group_res.head())
  sums = df.sum(axis=0)
  print(sums)
  print((time() - start))
  pd.flush()

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
print = pd.lazyPrint
if (__name__ == '__main__'):
   from pyspark.sql import SparkSession
   spark = SparkSession.builder.appName('StartupGrowthAnalysis').master('local[*]').config('spark.driver.memory','24g').config('spark.sql.shuffle.partitions','12').getOrCreate()
   max_ram_usage = 0.0
   pd.BACKEND_ENGINE = pd.BackendEngines.SPARK
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
   spark.stop()
   file = open('../../Results.csv','a',newline='')
   writer = csv.writer(file)
   writer.writerow(['b6','4.2GB','s4.csv','lspark',time2,format(ram_usage),648])
   file.close()
pd.flush()
