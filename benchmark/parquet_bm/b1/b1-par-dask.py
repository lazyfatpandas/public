import dask.dataframe as pd
from time import time
start = time()
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b1/s4.parquet')
# df = pd.read_parquet('/home/bhushan/nvme1/data/vldb/b1/s4.parquet')
df = df[(df.fare_amount > 0)]
df['day'] = df.tpep_pickup_datetime.dt.dayofweek
df = df.groupby(['day'])['passenger_count'].sum()
print(df.compute())
print((time() - start))