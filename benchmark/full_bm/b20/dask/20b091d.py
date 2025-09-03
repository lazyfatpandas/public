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

def get_sys_stats():
    global max_ram_usage
    max_ram_usage=0.0
    max_ram = 0.0
    max_cpu = 0.0
    while 1 < 2:
        ram_usage = (psutil.virtual_memory()[3] / 1000000)
        if (max_ram_usage < ram_usage):
            max_ram_usage = ram_usage
        sleep(0.2)

def myfunc():
    start = time()
    dask.config.set({'dataframe.query-planning-warning': False})
    train_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b20/s1_train.csv')
    test_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b20/s1_test.csv')
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
    print("Time:",time()-start)


if __name__=='__main__':
    max_ram_usage=0.0
    time1 = time()
    init_ram = (psutil.virtual_memory()[3] / 1000000)
    # print('init ram is',init_ram)
    t1 = threading.Thread(target=get_sys_stats, daemon=True)
    t1.start()
    t2 = threading.Thread(target=myfunc)
    t2.start()
    t2.join()
    ram_usage = (max_ram_usage - init_ram)
    print('Maximum memory used: {} MiB'.format(ram_usage))
    time2 = (time() - time1)
    file = open('../../Results.csv','a',newline='')
    writer = csv.writer(file)
    writer.writerow(["b20", "150MB", "s1.csv","dask",time2,format(ram_usage),2015])
    file.close()




