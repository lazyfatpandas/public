import lazyfatpandas.pandas as pd
from time import time
print=pd.lazyPrint
start = time()
SO_columns = ["col3","col2","col1"]
SO_c_d_t = {"col1":"float32","col2":"float32","col3":"float32"}
# df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b3/s1.parquet',usecols=SO_columns,dtype=SO_c_d_t)
df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b3/s2.parquet',columns=SO_columns)
df['col1_binary'] = (df.col1 > df.col1.quantile(0.1))
df['col2_binary'] = (df.col1 > 3)
print(df.head())
df = df[(df.col2 > 10)]
print(df.head())
group_res = df.groupby(['col1_binary']).agg({'col3':['mean']})
print(group_res.head())
pd.flush()
__builtins__.print((time() - start))