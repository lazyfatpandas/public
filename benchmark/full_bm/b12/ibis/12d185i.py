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
    # ## This notebook analyses growth of startsup's in India during 2016 to 2022.
    # # Import Libraries
    import numpy as np
    import os
    import seaborn as sns
    import matplotlib.pyplot as plt
    import plotly.express as px
    # from wordcloud import WordCloud
    import string
    con = ibis.connect("duckdb://")
    # Read CSV lazily
    df = con.read_csv("/media/bhushan/nvme1/data/vldb/b12/s5.csv")
    # Show first few rows
    print(df.limit(5).execute())
    # ## Year wise Startup Growth
    df_year = df.group_by(df["Year"]).aggregate(count=df.count()).order_by("Year")
    print(df_year.execute())
    # ## Various Industries and Its Count
    # Bhushan comment - since computed and to be printed, emulated print
    # Various Industries and Its Count
    df_industry_counts = (
        df.group_by(df["Industry"])
            .aggregate(count=df.count())
            .order_by(ibis.desc("count"))
    )
    print(df_industry_counts.execute())
    # ## Top 20 Startup Industries from 2016
    # Top 20 Startup Industries from 2016
    df_top20_industries = df_industry_counts.limit(20)
    print(df_top20_industries.execute())
    # ## Top 20 States with Max Startup's
    # Top 20 States with Max Startups
    df_state_counts = (
        df.group_by(df["State"])
            .aggregate(count=df.count())
            .order_by(ibis.desc("count"))
            .limit(20)
    )
    print(df_state_counts.execute())

    # AI-related Startups from 2016-2022
    ds_list = ['Internet of Things', 'AI', 'Robotics', 'Analytics', 'Computer Vision']
    ds_df = df.filter(df["Industry"].isin(ds_list))
    print(ds_df.execute())
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
    writer.writerow(["b12", "12.6GB", "s5.csv","ibis",time2,format(ram_usage),1259])
    file.close()





