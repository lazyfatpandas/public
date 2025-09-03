import pandas as pd
from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *


def set_MainIndustry(ind):
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    if ind in env:
        return 'ENV'
    else:
        return 'AI'

import numpy as np # linear algebra
import pandas as pd
start = time()
# Visualisation
# load & cleanup
df = pd.read_parquet("/media/bhushan/nvme1/data/vldb/b19/s4.parquet")
print(df.info())
# LAFP: disable replication
# # -- STEFANOS -- Replicate Data
# factor = 3000
# df = pd.concat([df]*factor)
# df.info()
# df.drop('S No.',axis=1,inplace=True)
# df.dropna(inplace=True)
# df.reset_index(inplace=True,drop=True)
df = df.drop('S No.',axis=1)
df = df.dropna()
df = df.reset_index(drop=True)

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
# adding a new column
df_ea['MainIndustry'] = df_ea.Industry.apply(lambda x: set_MainIndustry(x))
# basic stats
print(f"A total of {df_ea.shape[0]} startups were started in India between 2016 & 2022, out of which {df_ea.groupby('MainIndustry').size()['ENV']} are environmental related & {df_ea.groupby('MainIndustry').size()['AI']} are AI startups.")
# # -- STEFANOS -- Disable the rest of the code because it's plotting
print(time()-start)







