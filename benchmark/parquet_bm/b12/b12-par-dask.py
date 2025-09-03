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
# ## This notebook analyses growth of startsup's in India during 2016 to 2022.
# # Import Libraries
import numpy as np
import os
import seaborn as sns
import matplotlib.pyplot as plt
import plotly.express as px
# from wordcloud import WordCloud
import string
import dask.dataframe as pd
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b12/s4.parquet')
print(df.head())
# ## Year wise Startup Growth
df_year = df['Year'].value_counts().compute().sort_index()
print(df_year)
# ## Various Industries and Its Count
# Bhushan comment - since computed and to be printed, emulated print
# df.groupby('Industry').size().sort_values(ascending=False).to_frame().style.background_gradient(cmap='coolwarm')
# Dask error AttributeError: 'Series' object has no attribute 'sort_values'
# print(df.groupby('Industry').size().sort_values(ascending=False))
df_grouped = df.groupby('Industry').size().compute().sort_values(ascending=False)
print(df_grouped)
# ## Top 20 Startup Industries from 2016
# fig, ax = plt.subplots(1,1, figsize=(20, 6))
# Dask Error AttributeError: 'Series' object has no attribute 'iloc'. Did you mean: 'loc'?
# df_ind = df['Industry'].value_counts().iloc[:20]
df_ind=df['Industry'].value_counts().nlargest(20)
print(df_ind.compute())
# ## Top 20 States with Max Startup's
# Dask Error AttributeError: 'Series' object has no attribute 'iloc'. Did you mean: 'loc'?
# X = df['State'].value_counts().iloc[:20]
X = df['State'].value_counts().nlargest(20)
print(X.compute())
# ## AI related Startup's from 2016-2022
ds_list=['Internet of Things','AI','Robotics','Analytics','Computer Vision']
ds_df = df[df['Industry'].isin(ds_list)]
print(ds_df.compute())
print(time()-start)







