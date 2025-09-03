
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
  from matplotlib import pyplot as plt
  import seaborn as sns
  SO_columns = ["status","satisfaction","last_evaluation","recently_promoted","filed_complaint","department"]
  SO_columns = ["satisfaction","last_evaluation","recently_promoted","filed_complaint","department"]
  df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b31/s5.csv',usecols=SO_columns)
  print(df.shape)
  print(df.department.unique())
  df = df[((df.department != 'temp') | df.department.isnull())]
  print(df.shape)
  print(df.filed_complaint.unique())
  print(df.recently_promoted.unique())
  df['filed_complaint'] = df.filed_complaint.fillna(0)
  df['recently_promoted'] = df.recently_promoted.fillna(0)
  print(df.filed_complaint.unique())
  print(df.recently_promoted.unique())
  df['department'] = df.department.replace('information_technology','IT')
  sns.countplot(y='department',data=df.compute(live_df=[df]))
  plt.savefig('fig1.png')
  print(df.isnull().sum())
  df['department'] = df['department'].fillna('Missing')
  df['last_evaluation_missing'] = df.last_evaluation.isnull().astype(int)
  df['last_evaluation'] = df.last_evaluation.fillna(0)
  print(df.isnull().sum())
  sns.lmplot(x='satisfaction',y='last_evaluation',data=df[(df.status == 'Left')].compute(live_df=[df]),fit_reg=False)
  plt.savefig('fig2.png')
  df['underperformer'] = ((df.last_evaluation < 0.6) & (df.last_evaluation_missing == 0)).astype(int)
  df['unhappy'] = (df.satisfaction < 0.2).astype(int)
  df['overachiever'] = ((df.last_evaluation > 0.8) & (df.satisfaction > 0.7)).astype(int)
  print(df[['underperformer','unhappy','overachiever']].mean())
  print(df.head(10))
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
   max_ram_usage = 0.0
   pd.BACKEND_ENGINE = pd.BackendEngines.PANDAS
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
   writer.writerow(['b31','12.6GB','s5.csv','libis',time2,format(ram_usage),'315a'])
   file.close()
pd.flush()
