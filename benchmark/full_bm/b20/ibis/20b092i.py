import ibis
from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *


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

    # Connect to DuckDB in-memory database
    con = ibis.duckdb.connect()
    # Load CSVs into Ibis tables
    train_df = con.read_csv("/media/bhushan/nvme1/data/vldb/b20/s2_train.csv")
    print(train_df.schema())
    test_df = con.read_csv("/media/bhushan/nvme1/data/vldb/b20/s2_test.csv")

    # Show head (like pandas.head())
    print(train_df.limit(5).execute())
    print(test_df.limit(5).execute())
    # Then the size of each dataset.
    # Then the size of each dataset.
    print(train_df.count().execute(), test_df.count().execute())

    LABEL_COLUMNS = ['cohesion', 'syntax', 'vocabulary', 'phraseology', 'grammar', 'conventions']
    # # Text Examples
    # ## Random Examples
    # Random Examples (sample 4 rows)
    texts = train_df.order_by(ibis.random()).limit(4)
    # print(texts.execute())
    # Lowest Scoring Examples
    train_with_score = train_df.mutate(
        total_score=sum(train_df[col] for col in LABEL_COLUMNS)
    )
    lowest_df = train_with_score.order_by("total_score").limit(4)
    print(lowest_df.execute())
    # Highest Scoring Examples
    highest_df = train_with_score.order_by(ibis.desc("total_score")).limit(4)
    print(highest_df.execute())
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
    writer.writerow(["b20", "450MB", "s2.csv","ibis",time2,format(ram_usage),2029])
    file.close()



