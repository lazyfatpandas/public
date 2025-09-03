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
    print = pd.lazyPrint
    pd.BACKEND_ENGINE=pd.BackendEngines.MODIN
    start = time()
    import numpy as np
    import os
    print = pd.lazyPrint
    import modin.config as cfg
    cfg.Engine.put('dask')
    cfg.StorageFormat.put('pandas')
    # ## This notebook analyses growth of startsup's in India during 2016 to 2022.
    import os
    import seaborn as sns
    import matplotlib.pyplot as plt
    import plotly.express as px
    # from wordcloud import WordCloud
    import string
    # # Visualisations
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b12/s3.csv')
    # df.head().style.background_gradient(cmap='coolwarm')
    print(df.info())
    # ## Industry Vs Year
    # STEFANOS: Disable plotting
    # fig = px.scatter(df,x="Industry", y="Year", size="Count", color="Count",template='plotly_dark', title="Industry Vs Year")
    # fig.show()
    # ## Year wise Startup Growth
    # fig, ax = plt.subplots(1,1, figsize=(15, 6))
    df_year = df['Year'].value_counts().sort_index()
    # STEFANOS: Disable plotting
    # ## Various Industries and Its Count
    #disable Bhushan--not printing
    # df.groupby('Industry').size().sort_values(ascending=False).to_frame().style.background_gradient(cmap='coolwarm')
    # ## Top 20 Startup Industries from 2016
    df_ind = df['Industry'].value_counts().iloc[:20]
    # ## Top 20 States with Max Startup's
    # fig, ax = plt.subplots(1,1, figsize=(20, 6))
    X = df['State'].value_counts().iloc[:20]
    # ## AI related Startup's from 2016-2022
    ds_list=['Internet of Things','AI','Robotics','Analytics','Computer Vision']
    ds_df = df[df['Industry'].isin(ds_list)]
    print(ds_df)
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
    writer.writerow(["b12", "1.4GB", "s3.csv","lmodin",time2,format(ram_usage),1234])
    file.close()




