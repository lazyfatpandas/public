import pandas as pd
from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *

start = time()
# ## This notebook analyses growth of startsup's in India during 2016 to 2022.
# # Import Libraries
import numpy as np
import os
import seaborn as sns
import matplotlib.pyplot as plt
import plotly.express as px
# from wordcloud import WordCloud
import string
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b12/s4.parquet')
print(df.head())
# ## Year wise Startup Growth
df_year = df['Year'].value_counts().sort_index()
print(df_year)
# ## Various Industries and Its Count
# Bhushan comment - since computed and to be printed, emulated print
# df.groupby('Industry').size().sort_values(ascending=False).to_frame().style.background_gradient(cmap='coolwarm')
print(df.groupby('Industry').size().sort_values(ascending=False))#.to_frame().style.background_gradient(cmap='coolwarm')
# ## Top 20 Startup Industries from 2016
# fig, ax = plt.subplots(1,1, figsize=(20, 6))
df_ind = df['Industry'].value_counts().iloc[:20]
print(df_ind)
# ## Top 20 States with Max Startup's
X = df['State'].value_counts().iloc[:20]
print(X)
# ## AI related Startup's from 2016-2022
ds_list=['Internet of Things','AI','Robotics','Analytics','Computer Vision']
ds_df = df[df['Industry'].isin(ds_list)]
print(ds_df)
print(time()-start)
# __builtins__.print((time() - start))





