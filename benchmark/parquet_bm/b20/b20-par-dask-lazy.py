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
pd.BACKEND_ENGINE = pd.BackendEngines.DASK
start = time()
SO_columns = ["full_text","cohesion","syntax","vocabulary","phraseology","grammar","conventions"]
train_df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b20/s4_train.parquet',columns=SO_columns)
test_df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b20/s4_test.parquet')
print(train_df.head())
print(test_df.head())
print(len(train_df),len(test_df))
LABEL_COLUMNS = ['cohesion','syntax','vocabulary','phraseology','grammar','conventions']
texts = train_df.sample(frac=1,random_state=420).head(4)
train_df['total_score'] = train_df[LABEL_COLUMNS].sum(axis=1)
lowest_df = train_df.sort_values('total_score').head(4)
print(lowest_df)
train_df['total_score'] = train_df[LABEL_COLUMNS].sum(axis=1)
highest_df = train_df.sort_values('total_score',ascending=False).head(4)
print(highest_df)
train_df['word_count'] = train_df.full_text.apply(lambda x: len(x.split()),meta=('full_text','int64'))
print(train_df['word_count'].mean())
print(train_df['word_count'].max())
pd.flush()
__builtins__.print((time() - start))

