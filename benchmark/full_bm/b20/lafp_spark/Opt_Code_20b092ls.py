
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
  import os
  os.environ['PYARROW_IGNORE_TIMEZONE'] = '1'
  print = pd.lazyPrint
  start = time()
  import string
  start = time()
  SO_columns = ["full_text","cohesion","syntax","vocabulary","phraseology","grammar","conventions"]
  train_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b20/s2_train.csv',usecols=SO_columns)
  print(train_df.dtypes)
  test_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b20/s2_test.csv')
  print(train_df.head())
  print(test_df.head())
  print(len(train_df),len(test_df))
  LABEL_COLUMNS = ['cohesion','syntax','vocabulary','phraseology','grammar','conventions']
  texts = train_df.sample(frac=1.0,random_state=420).head(4)
  train_df['total_score'] = train_df[LABEL_COLUMNS].sum(axis=1)
  lowest_df = train_df.sort_values('total_score').head(4)
  print(lowest_df)
  highest_df = train_df.sort_values('total_score',ascending=False).head(4)
  print(highest_df)
  train_df['word_count'] = train_df.full_text.fillna('').apply(lambda x: len(x.split()))
  print(train_df['word_count'].mean())
  print(train_df['word_count'].max())
  print('Time:',(time() - start))
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
   from os import environ
   environ['PYARROW_IGNORE_TIMEZONE'] = '1'
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
   writer.writerow(['b20','450MB','s2.csv','lspark',time2,format(ram_usage),2028])
   file.close()
pd.flush()
