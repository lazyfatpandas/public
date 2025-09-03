import modin.pandas as pd
from time import time
from os import environ
import csv
import os
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
        sleep(0.5)

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
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b12/s4.csv')
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
    writer.writerow(["b12", "4.2GB", "s4.csv","modin",time2,format(ram_usage),1243])
    file.close()




