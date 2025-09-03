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
pd.analyze()

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
    print=pd.lazyPrint
    src = environ.get('FILES_GLOB', "/media/bhushan/nvme1/data/vldb/b2")
    # load files
    ratings_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/s2.csv', low_memory=False)
    metas_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.csv', low_memory=False,dtype={'budget': 'object',
                                                                                                           'id': 'object',
                                                                                                           'popularity': 'object',
                                                                                                           'revenue': 'float64',
                                                                                                           'vote_count': 'float64'})
    links_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/links.csv', low_memory=False)
    # 1st join
    merged_df = pd.merge(links_df[['movieId','imdbId']],
                         ratings_df[['movieId','rating']], on='movieId', how='right')
    metas_df['imdb_id']=pd.to_numeric(metas_df['imdb_id'].str.slice(2), errors='coerce')
    # 2nd join
    merged_df = pd.merge(merged_df, metas_df[['title','imdb_id']],left_on='imdbId', right_on='imdb_id', how='inner')
    # # group-by having
    grouped_df = merged_df[['title','rating']].groupby('title').agg(Mean=('rating', 'mean' ), Count=('rating','count'))
    result = grouped_df.query('Mean > 4.5 and Count > 2')
    print(result)
    pd.flush()
    print(time()-start)


if __name__=='__main__':
    max_ram_usage=0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.MODIN
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
    writer.writerow(["b8", "450MB", "s2.csv","lmodin",time2,format(ram_usage),824])
    file.close()


