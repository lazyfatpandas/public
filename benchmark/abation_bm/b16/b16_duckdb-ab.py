
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
    import numpy as np
    pd.BACKEND_ENGINE = pd.BackendEngines.IBIS
    print = pd.lazyPrint
    # SO_c_d_t = {"age":"int64","job":"category","marital":"category","education":"category","default":"category","balance":"int64","housing":"category","loan":"category","contact":"category","day":"int64","month":"category","duration":"int64","campaign":"int64","pdays":"int64","previous":"int64","poutcome":"category","y":"category"}
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b16/s3.csv',delimiter=';')
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
    writer.writerow(['b16','abl','s3.csv','libis',time2,format(ram_usage),"163A"])
    file.close()
