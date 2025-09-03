
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
  SO_d_d_t = ["tpep_pickup_datetime"]
  SO_c_d_t = {"passenger_count":"Int64","fare_amount":"float32"}
  df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b1/s2.csv',usecols=SO_columns,dtype=SO_c_d_t,parse_dates=SO_d_d_t)
  df = df[(df.fare_amount > 0)]
  df['day'] = df.tpep_pickup_datetime.dt.dayofweek
  df = df.groupby(['day'])['passenger_count'].sum()
  print(df)
  pd.flush()
  print((time() - start))

from memory_profiler import memory_usage
from memory_profiler import profile
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
   pd.BACKEND_ENGINE = pd.BackendEngines.MODIN
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
   writer.writerow(['b1','450MB','s2.csv','lmodin',time2,format(ram_usage),124])
   file.close()
