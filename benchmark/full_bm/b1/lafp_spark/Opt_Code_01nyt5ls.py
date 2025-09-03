
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
  SO_columns = ["passenger_count","tpep_pickup_datetime","fare_amount"]
  SO_c_d_t = {"tpep_pickup_datetime":"str","passenger_count":"Int64","fare_amount":"float32"}
  SO_d_d_t = ["tpep_pickup_datetime"]
  df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b1/s5.csv',usecols=SO_columns,dtype=SO_c_d_t,parse_dates=SO_d_d_t)
  df = df[(df.fare_amount > 0)]
  df['day'] = df.tpep_pickup_datetime.dt.dayofweek
  df = df.groupby(['day'])['passenger_count'].sum()
  print(df)
  pd.flush()
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
print = pd.lazyPrint
if (__name__ == '__main__'):
   import os
   os.environ['PYARROW_IGNORE_TIMEZONE'] = '1'
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
   writer.writerow(['b1','12.6GB','s5.csv','lspark',time2,format(ram_usage),158])
   file.close()
pd.flush()
