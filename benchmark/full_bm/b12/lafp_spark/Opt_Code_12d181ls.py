
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
  print = pd.lazyPrint
  start = time()
  import string
  SO_columns = ["State","Industry","Year"]
  SO_c_d_t = {"Year":"int64","State":"category","Industry":"category"}
  df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b12/s1.csv',usecols=SO_columns,dtype=SO_c_d_t)
  print(df.head())
  df_year = df['Year'].value_counts().sort_index()
  print(df_year)
  print(df.groupby('Industry').size().sort_values(ascending=False))
  df_ind = df['Industry'].value_counts().iloc[:20]
  print(df_ind)
  X = df['State'].value_counts().iloc[:20]
  print(X)
  ds_list = ['Internet of Things','AI','Robotics','Analytics','Computer Vision']
  ds_df = df[df['Industry'].isin(ds_list)]
  print(ds_df)
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
   writer.writerow(['b12','150MB','s1.csv','lspark',time2,format(ram_usage),1218])
   file.close()
pd.flush()
