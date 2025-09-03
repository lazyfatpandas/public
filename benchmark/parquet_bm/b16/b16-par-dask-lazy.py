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
start = time()
import numpy as np
pd.BACKEND_ENGINE = pd.BackendEngines.DASK
print = pd.lazyPrint
# SO_c_d_t = {"age":"int64","job":"category","marital":"category","education":"category","default":"category","balance":"int64","housing":"category","loan":"category","contact":"category","day":"int64","month":"category","duration":"int64","campaign":"int64","pdays":"int64","previous":"int64","poutcome":"category","y":"category"}
# df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b16/s1.parquet')
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b16/s4.parquet')
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
# loan_stat = df[df['marital'].apply(lambda marital: (marital == 'married'))]
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
pd.flush()
__builtins__.print((time() - start))

