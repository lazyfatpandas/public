import dask.dataframe as pd
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
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b3/s4.parquet')
quantile = df.col1.quantile(0.1).compute()
#add a new column and calculate 10th quantile of a column
df['col1_binary'] = df.col1 > quantile
df['col2_binary']= df.col1 >3
print(df.head())
#Filter data
df = df[df.col2 > 10]
print(df.head())

#group by and aggregate
group_res = df.groupby(['col1_binary']).agg({'col3': ['mean']})
print(group_res.head())

# #Sum of all columns
# sums=df.sum(axis=0)
# print(sums.compute())

print(time()-start)











