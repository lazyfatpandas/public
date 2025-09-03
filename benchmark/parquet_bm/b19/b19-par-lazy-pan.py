import lazyfatpandas.pandas as pd
from time import time
from time import time
from os import environ
import csv
import os
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *
pd.BACKEND_ENGINE=pd.BackendEngines.PANDAS

def set_MainIndustry(ind):
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    if (ind in env):
        return 'ENV'
    else:
        return 'AI'

start = time()
print = pd.lazyPrint
# SO_c_d_t = {"S No.":"int64","Year":"category","State":"category","Industry":"category","Count":"category"}
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b19/s4.parquet')
print(df.info())
df = df.drop('S No.',axis=1)
df = df.dropna()
df = df.reset_index(drop=True)
print(df.head())
env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
ai = ['AI','Robotics','Computer Vision']
df_ea = df.loc[(df['Industry'].isin(env) | df['Industry'].isin(ai))].reset_index(drop=True)
df_ea['MainIndustry'] = df_ea.Industry.apply(lambda x: set_MainIndustry(x))
print(f"A total of {df_ea.shape[0]} startups were started in India between 2016 & 2022, out of which {df_ea.groupby('MainIndustry').size()['ENV']} are environmental related & {df_ea.groupby('MainIndustry').size()['AI']} are AI startups.")
print((time() - start))
pd.flush()
__builtins__.print((time() - start))




