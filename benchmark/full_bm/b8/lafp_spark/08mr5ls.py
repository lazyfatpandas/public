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
    print=pd.lazyPrint
    start = time()
    src = environ.get('FILES_GLOB', "")
    # load files
    ratings_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/s5.csv', low_memory=False)
    metas_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.csv', low_memory=False)
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
    print(time()-start)
    pd.flush()


if __name__=='__main__':
    import os
    os.environ['PYARROW_IGNORE_TIMEZONE'] = '1'
    from pyspark.sql import SparkSession
    # Create a SparkSession with memory configuration
    spark = SparkSession.builder \
        .appName("StartupGrowthAnalysis") \
        .master("local[*]") \
        .config("spark.driver.memory", "24g") \
        .config("spark.sql.shuffle.partitions", "12") \
        .getOrCreate()
    max_ram_usage=0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.SPARK
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
    spark.stop()
    file = open('../../Results.csv','a',newline='')
    writer = csv.writer(file)
    writer.writerow(["b8", "12.6GB", "s5.csv","lspark",time2,format(ram_usage),858])
    file.close()


