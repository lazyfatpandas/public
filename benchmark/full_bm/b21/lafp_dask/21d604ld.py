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
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b21/s4_city.csv', dtype={'NUMBER': 'object', 'UNIT': 'object'})  # NOTE: dtype
    print(df.head())
    #drop unneeded columns
    # LAFP: inplace not supported
    # df.drop(['UNIT','DISTRICT','REGION','CITY','HASH','ID'],axis=1,inplace=True)
    df = df.drop(['UNIT','DISTRICT','REGION','CITY','HASH','ID'],axis=1)
    df['POSTCODE'] = df['POSTCODE'].astype(str)
    df['ADDRESS'] = df['NUMBER'].str.cat(df['STREET'], sep='')
    # LAFP: inplace not supported
    # df.drop(['STREET','NUMBER'], axis=1,inplace=True)
    df = df.drop(['STREET','NUMBER'], axis=1)
    df = df.rename(columns={'LON': 'long','LAT':'lat','ADDRESS':'address','POSTCODE':'zipcode'})
    #convert zipcode column into strings
    df['zipcode'] = df['zipcode'].map(lambda x: str(x)[:-2])
    #replacing excessive whitespace
    df['address'] = df['address'].replace('  ', ' ')
    df['address'] = df['address'].replace('   ',' ')
    df['address'] = df['address'].replace('    ',' ')
    #lsit of zips for each borough
    manzips = [10026, 10027, 10030, 10037, 10039,10001, 10011, 10018, 10019, 10020, 10036,10029,
               10035,10010, 10016, 10017, 10022,10012, 10013, 10014,
               10004, 10005, 10006, 10007, 10038, 10280,
               10002, 10003, 10009,
               10021, 10028, 10044, 10065, 10075, 10128,
               10023, 10024, 10025,
               10031, 10032, 10033, 10034, 10040]

    bkzips = [11212, 11213, 11216, 11233, 11238,11209, 11214, 11228, 11204, 11218, 11219, 11230,
              11234, 11236, 11239, 11223, 11224, 11229, 11235,
              11201, 11205, 11215, 11217, 11231,
              11203, 11210, 11225, 11226,
              11207, 11208,
              11211, 11222,
              11220, 11232,
              11206, 11221, 11237]
    #convert to strings, create list
    man_zips = [str(i) for i in manzips]
    bk_zips = [str(i) for i in bkzips]
    manbk_zips = man_zips+bk_zips
    df = df[df.zipcode.isin(manbk_zips)]
    # In[17]:
    # LAFP: dask outputs the csv into parts (one csv for each partition), printing the result for consistency and ease in benchmarking
    print(df)
    df2 = pd.read_csv('/media/bhushan/nvme1/data/vldb/b21/s4_pluto.csv')
    # LAFP: print added
    print(df2.head(1))
    #create a dictionary of addresses grouped by zipcode
    # LAFP: print added
    # print(df2.groupby('zipcode')['address'].apply(lambda q: q.values.tolist()).to_dict())
    print(df2.groupby('zipcode')['address'].apply(lambda q: q.values.tolist()))

    final_df = pd.concat([df,df2])
    # LAFP: print added
    print(final_df.dtypes)
    final_df['zipcode'] = final_df['zipcode'].astype(str)
    # LAFP: dask outputs the csv into parts (one csv for each partition) by default, printing the result for consistency and ease in benchmarking
    # final_df.to_csv('final_df.csv')
    print(final_df)
    # ad_zip = final_df.groupby('zipcode')['address'].apply(lambda q: q.values.tolist()).to_dict()
    ad_zip = final_df.groupby('zipcode')['address'].apply(lambda q: q.values.tolist())
    pd.flush(default_to_pandas=True)
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
    writer.writerow(["b21", "4.2GB", "s4.csv","ldask",time2,format(ram_usage),2146])
    file.close()


