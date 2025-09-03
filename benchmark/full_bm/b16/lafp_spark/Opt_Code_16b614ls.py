
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
  import numpy as np
  SO_c_d_t = {"age":"int64","job":"category","marital":"category","education":"category","default":"category","balance":"int64","housing":"category","loan":"category","contact":"category","day":"int64","month":"category","duration":"int64","campaign":"int64","pdays":"int64","previous":"int64","poutcome":"category","y":"category"}
  df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b16/s4.csv',sep=';',dtype=SO_c_d_t)
  print(df.head())
  print(df.describe())
  df = df.replace('unknown',np.nan)
  print(df.isnull().sum())
  df = df.dropna()
  print(df.isnull().sum())
  df = df.rename(columns={'loan':'previous_loan_status','y':'loan_status'})
  print(df.head())
  print(df['job'].nunique())
  print(df['job'].unique())
  print(df['job'].value_counts())
  print(df[(df['loan_status'] == 'yes')]['education'].value_counts(normalize=True))
  print(df[(df['loan_status'] == 'yes')]['previous_loan_status'].value_counts(normalize=True))
  loan_stat = df[df['marital'].apply(lambda marital: (marital == 'married'))]
  marital_status_summery = loan_stat['loan_status'].value_counts()
  print(marital_status_summery)
  branch_1 = {'customer_id':['1','2','3','4','5'],'first_name':['Andrew','Alex','Sabestian','Hilary','Jack'],'last_name':['Ng','Hales','Rachaska','Masan','Anthony']}
  df_branch_1 = pd.DataFrame(branch_1,columns=['customer_id','first_name','last_name'])
  print(df_branch_1)
  branch_2 = {'customer_id':['4','5','6','7','8'],'first_name':['Brain','Steve','Kim','Steve','Ben'],'last_name':['Alexander','Jobs','Jonas','Fleming','Richardsan']}
  df_branch_2 = pd.DataFrame(branch_2,columns=['customer_id','first_name','last_name'])
  print(df_branch_2)
  credit_score = {'customer_id':['1','2','3','4','5','7','8','9','10','11'],'score':[513,675,165,961,1080,1654,415,900,610,1116]}
  df_credit_score = pd.DataFrame(credit_score,columns=['customer_id','score'])
  print(df_credit_score)
  df_new = pd.concat([df_branch_1,df_branch_2])
  print(df_new)
  print(pd.merge(df_new,df_credit_score,left_on='customer_id',right_on='customer_id'))
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
   writer.writerow(['b16','4.2GB','s4.csv','lspark',time2,format(ram_usage),1648])
   file.close()
pd.flush()
