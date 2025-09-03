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
pd.analyze(comp_time=True)

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

def set_MainIndustry(ind):
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    if ind in env:
        return 'ENV'
    else:
        return 'AI'

def myfunc():
    start = time()
    import lazyfatpandas.pandas as pd
    print = pd.lazyPrint
    # load & cleanup
    df = pd.read_csv("/media/bhushan/nvme1/data/vldb/b19/s1.csv")
    # LAFP: disable replication
    # # -- STEFANOS -- Replicate Data
    # LAFP: inplace not supported
    # df.drop('S No.',axis=1,inplace=True)
    # df.dropna(inplace=True)
    # df.reset_index(inplace=True,drop=True)
    df = df.drop('S No.',axis=1)
    df = df.dropna()
    df = df.reset_index(drop=True)
    # LAFP: print added
    print(df.head())
    # Industry sub-categories for environmental & AI startups
    env = ['Agriculture','Green Technology','Renewable Energy','Waste Management']
    ai = ['AI','Robotics','Computer Vision']
    # combined df - environmental & AI startups only
    # LAFP: inplace not supported
    # df_ea = df.loc[(df['Industry'].isin(env)) | (df['Industry'].isin(ai))].reset_index(drop=True,inplace=False)
    df_ea = df.loc[(df['Industry'].isin(env)) | (df['Industry'].isin(ai))].reset_index(drop=True)
    # custom function to set Main Industry
    # LAFP: Dask error: meta keyword is expected
    # adding a new column
    df_ea['MainIndustry'] = df_ea.Industry.apply(lambda x: set_MainIndustry(x), meta=str)
    # basic stats
    print(f"A total of {df_ea.shape[0]} startups were started in India between 2016 & 2022, out of which {df_ea.groupby('MainIndustry').size()['ENV']} are environmental related & {df_ea.groupby('MainIndustry').size()['AI']} are AI startups.")
    # # -- STEFANOS -- Disable the rest of the code because it's plotting
    pd.flush()
    print(time()-start)


if __name__=='__main__':
    max_ram_usage=0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.DASK
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
    writer.writerow(["b19", "150MB", "s1.csv","ldask",time2,format(ram_usage),1916])
    file.close()


