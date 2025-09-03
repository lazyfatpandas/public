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
pd.BACKEND_ENGINE = pd.BackendEngines.PANDAS
start = time()
print = pd.lazyPrint
SO_columns = ["State","Industry","Year"]
SO_c_d_t = {"Year":"category","State":"category","Industry":"category"}
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b12/s4.parquet',columns=SO_columns)
# df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b12/s4.parquet')
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
pd.flush()
__builtins__.print((time() - start))



