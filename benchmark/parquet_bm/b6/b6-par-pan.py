# import dask.dataframe as pd
import pandas as pd
from time import time
start = time()
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b3/s4.parquet')
# df = pd.read_parquet('/home/bhushan/nvme1/data/vldb/b3/s1.parquet')
df['col1_binary'] = (df.col1 > df.col1.quantile(0.1))
df['col2_binary'] = (df.col1 > 3)
# print(df.head())
df = df[(df.col2 > 10)]
print(df.head())
group_res = df.groupby(['col1_binary']).agg({'col3':['mean']})
print(group_res.head())
# pd.flush()
__builtins__.print((time() - start))