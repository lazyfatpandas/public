import lazyfatpandas.pandas as pd
# pd.BACKEND_ENGINE=pd.BackendEngines.DASK
from time import time
start=time()
print = pd.lazyPrint
SO_columns = ["passenger_count","tpep_pickup_datetime","fare_amount"]
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b1/s4.parquet',columns=SO_columns)
# df = pd.read_parquet('/home/bhushan/nvme1/data/vldb/b1/s1.parquet',columns=SO_columns)
df = df[(df.fare_amount > 0)]
df['day'] = df.tpep_pickup_datetime.dt.dayofweek
df = df.groupby(['day'])['passenger_count'].sum()
print(df)
pd.flush()
print((time() - start))
__builtins__.print((time() - start))