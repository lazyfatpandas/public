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
print = pd.lazyPrint
pd.BACKEND_ENGINE = pd.BackendEngines.PANDAS

from matplotlib import pyplot as plt
import seaborn as sns
SO_columns = ["salary","status","satisfaction","last_evaluation","recently_promoted","filed_complaint","department"]
# df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b31/s4.parquet',usecols=SO_columns)
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b31/s4.parquet')
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
df['status'] = pd.get_dummies(df.status).Left
print(df.status.mean())
df = pd.get_dummies(df,columns=['department','salary'])
print(df.head(10))
print(df)
pd.flush()
__builtins__.print((time() - start))



