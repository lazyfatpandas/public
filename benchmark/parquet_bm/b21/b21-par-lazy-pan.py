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
pd.BACKEND_ENGINE=pd.BackendEngines.PANDAS
start = time()
print = pd.lazyPrint
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b21/s4_city.parquet')
# df = pd.read_parquet('/home/bhushan/nvme1/data/vldb/b21/s4_city.parquet')
print(df.head())
df = df.drop(['UNIT','DISTRICT','REGION','CITY','HASH','ID'],axis=1)
df['POSTCODE'] = df['POSTCODE'].astype(str)
df['ADDRESS'] = df['NUMBER'].str.cat(df['STREET'],sep='')
df = df.drop(['STREET','NUMBER'],axis=1)
df = df.rename(columns={'LON':'long','LAT':'lat','ADDRESS':'address','POSTCODE':'zipcode'})
df['zipcode'] = df['zipcode'].map(lambda x: str(x)[:-(2)])
df['address'] = df['address'].replace('  ',' ')
df['address'] = df['address'].replace('   ',' ')
df['address'] = df['address'].replace('    ',' ')
manzips = [10026,10027,10030,10037,10039,10001,10011,10018,10019,10020,10036,10029,10035,10010,10016,10017,10022,10012,10013,10014,10004,10005,10006,10007,10038,10280,10002,10003,10009,10021,10028,10044,10065,10075,10128,10023,10024,10025,10031,10032,10033,10034,10040]
bkzips = [11212,11213,11216,11233,11238,11209,11214,11228,11204,11218,11219,11230,11234,11236,11239,11223,11224,11229,11235,11201,11205,11215,11217,11231,11203,11210,11225,11226,11207,11208,11211,11222,11220,11232,11206,11221,11237]
man_zips = [ str(i) for i in manzips ]
bk_zips = [ str(i) for i in bkzips ]
manbk_zips = (man_zips + bk_zips)
df = df[df.zipcode.isin(manbk_zips)]
print(df)
SO_c_d_t = {"zipcode":"int64","address":"str"}
# df2 = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b21/s4_pluto.parquet')
df2 = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b21/s4_pluto.parquet')

print(df2.head(1))
print(df2.groupby('zipcode')['address'].apply(lambda q: q.values.tolist()))
final_df = pd.concat([df,df2])
print(final_df.dtypes)
final_df['zipcode'] = final_df['zipcode'].astype(str)
print(final_df)
ad_zip = final_df.groupby('zipcode')['address'].apply(lambda q: q.values.tolist())
print(ad_zip)
pd.flush()
print((time() - start))
__builtins__.print((time() - start))


