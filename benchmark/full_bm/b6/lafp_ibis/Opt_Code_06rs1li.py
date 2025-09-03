
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
  print=pd.lazyPrint
  SO_columns = ["col3","col2","col1"]
  df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b3/s1.csv',usecols=SO_columns)
  df['col1_binary'] = (df.col1 > df.col1.quantile(0.1))
  df['col2_binary'] = (df.col1 > 3)
  print(df.head())
  df = df[(df.col2 > 10)]
  print(df.head())
  group_res = df.groupby(['col1_binary']).agg({'col3':['mean']})
  print(group_res.head())
  pd.flush()
  __builtins__.print((time() - start))

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
   pd.BACKEND_ENGINE = pd.BackendEngines.IBIS
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
   writer.writerow(['b6','150MB','s1.csv','libis',time2,format(ram_usage),'61a'])
   file.close()
