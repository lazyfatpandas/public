import ibis
import pandas as pd
from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from ibis import literal

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
    con = ibis.polars.connect()
    # load files
    ratings = con.read_csv('/media/bhushan/nvme1/data/vldb/b2/s4.csv')
    # metas = con.read_csv('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.csv')
    links = con.read_csv('/media/bhushan/nvme1/data/vldb/b2/links.csv')

    # Step 1: Read CSV with lenient options
    metas_raw = con.read_csv('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.csv',
                             ignore_errors=True)

    links3 = links.mutate(
        imdbId_int= links.imdbId.try_cast("int64") #.fill_null(literal(-1))
    )
    # First join: links + ratings on movieId (right join)
    step1 = links3.select("movieId", "imdbId_int").join(
        ratings.select("movieId", "rating"),
        predicates=[links3.movieId == ratings.movieId],
        how="right"
    ).select(
        ratings.movieId,   # or links.movieId — both are the same due to the join condition
        ratings.rating,
        links3.imdbId_int
    )
    # step11 = step1.mutate(
    #     imdbId_int= step1.imdbId.try_cast("int64") #.fill_null(literal(-1))
    # )
    # step11=step11.select(
    #     step11.movieId,   # or links.movieId — both are the same due to the join condition
    #     step11.rating,
    #     step11.imdbId_int
    # )

    # print(step1.execute())
    metas = metas_raw.mutate(
        imdb_id_int = metas_raw.imdb_id.substr(3).try_cast("int64") #.fill_null(literal(-1))
    )
    # print(metas.execute())

    # Step 2: join with metas on imdbId == imdb_id
    step2 = step1.join(
        metas.select("title", "imdb_id_int"),
        predicates=[step1.imdbId_int == metas.imdb_id_int],
        how="inner"
    )
    # print(step2.execute())
    # # 2nd join
    # Step 3: group by title
    grouped = step2.group_by("title").aggregate(
        Mean=step2.rating.mean(),
        Count=step2.rating.count()
    )
    # print(grouped.execute())
    # Step 4: filter on Mean > 4.5 and Count > 2
    result = grouped.filter((grouped.Mean > 4.5) & (grouped.Count > 2))
    # Execute and show result
    print(result.execute())
    print(time()-start)

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
    writer.writerow(["b8", "4.2GB", "s4.csv","polars",time2,format(ram_usage),"84B"])
    file.close()



