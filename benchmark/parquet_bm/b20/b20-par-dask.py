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
import dask
start1 = time()
dask.config.set({'dataframe.query-planning-warning': False})
train_df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b20/s4_train.parquet')
test_df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b20/s4_test.parquet')
# LAFP: remove duplication
# # -- STEFANOS -- Replicate Data
# factor = 500
# Let's see a row from each dataset.
# LAFP: print added
print(train_df.head())
# LAFP: print added
print(test_df.head())
# Then the size of each dataset.
# LAFP: print added
print(len(train_df), len(test_df))
LABEL_COLUMNS = ['cohesion', 'syntax', 'vocabulary', 'phraseology', 'grammar', 'conventions']
# # Text Examples
# ## Random Examples
texts = train_df.sample(frac=1, random_state=420).head(4)
# ## Lowest Scoring Examples
train_df['total_score'] = train_df[LABEL_COLUMNS].sum(axis=1)
lowest_df = train_df.sort_values('total_score').head(4)
print(lowest_df)
# ## Highest Scoring Examples
train_df['total_score'] = train_df[LABEL_COLUMNS].sum(axis=1)
highest_df = train_df.sort_values('total_score', ascending=False).head(4)
print(highest_df)
# # Text Overview
# ## Word Count
#bhushan  Added str to avoid error
# train_df['word_count'] = train_df.full_text.apply(lambda x: len(str(x).split()))
train_df['word_count'] = train_df.full_text.apply(lambda x: len(str(x).split()),meta=('full_text','int64'))
train_df=train_df.compute()
# Mean word count:
# LAFP: print added
# print(train_df['word_count'].mean().compute())
print(train_df['word_count'].mean())
# Max word count:
# print(train_df['word_count'].max().compute())
print(train_df['word_count'].max())
print("Time:",time()-start1)







