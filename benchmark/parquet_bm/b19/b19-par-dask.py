import dask.dataframe as pd
import pandas as ppd
from time import time
from os import environ
import csv
import os
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *

def set_MainIndustry(ind):
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    # added to avoid NA error
    if ppd.isna(ind):
        return 'Unknown'
    if ind in env:
        return 'ENV'
    else:
        return 'AI'

start = time()
import numpy as np # linear algebra
# In[2]:
# load & cleanup
df = pd.read_parquet("/media/bhushan/nvme1/data/vldb/b19/s4.parquet")
print(df.info())
# LAFP: disable replication
# # -- STEFANOS -- Replicate Data
# LAFP: inplace not supported
# df.drop('S No.',axis=1,inplace=True)
# df.dropna(inplace=True)
# df.reset_index(inplace=True,drop=True)
df = df.drop('S No.',axis=1)
df = df.dropna()
df = df.reset_index(drop=True)
#view
# LAFP: print added
print(df.head())
# Industry sub-categories for environmental & AI startups
env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
ai = ['AI','Robotics','Computer Vision']
# combined df - environmental & AI startups only
# LAFP: inplace not supported
# df_ea = df.loc[(df['Industry'].isin(env)) | (df['Industry'].isin(ai))].reset_index(drop=True,inplace=False)
df_ea = df.loc[(df['Industry'].isin(env)) | (df['Industry'].isin(ai))].reset_index(drop=True)
# custom function to set Main Industry
# LAFP: Dask error: meta keyword is expected
# adding a new column
# df_ea['MainIndustry'] = df_ea.Industry.apply(lambda x: set_MainIndustry(x), meta=str)
df_ea['MainIndustry'] = df_ea.Industry.apply(lambda x: set_MainIndustry(x), meta=str)
df_ea=df_ea.compute()
# basic stats
# print(f"A total of {df_ea.shape[0].compute()} startups were started in India between 2016 & 2022, out of which {df_ea.groupby('MainIndustry').compute().size()['ENV']} are environmental related & {df_ea.groupby('MainIndustry').size()['AI']} are AI startups.")
print(f"A total of {df_ea.shape[0]} startups were started in India between 2016 & 2022, out of which {df_ea.groupby('MainIndustry').size()['ENV']} are environmental related & {df_ea.groupby('MainIndustry').size()['AI']} are AI startups.")
# # -- STEFANOS -- Disable the rest of the code because it's plotting
print(time()-start)

