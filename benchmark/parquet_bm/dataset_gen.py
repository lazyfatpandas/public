import pandas as dd
import pyarrow as pa
print("pyarrow version:", pa.__version__)
print("pandas version:", dd.__version__)
# Read CSV file(s) into a Dask DataFrame
# df = dd.read_csv('/media/bhushan/nvme1/data/vldb/b20/s4_train.csv')
# df1 = dd.read_csv('/media/bhushan/nvme1/data/vldb/b20/s4_test.csv')
# df.to_parquet('/media/bhushan/nvme1/data/vldb/b20/s4_train.parquet', engine='pyarrow', index=False)
# df1.to_parquet('/media/bhushan/nvme1/data/vldb/b20/s4_test.parquet', engine='pyarrow', index=False)

df = dd.read_csv('/media/bhushan/nvme1/data/vldb/b33/s4_ride.csv')
df.to_parquet('/media/bhushan/nvme1/data/vldb/b33/s4_ride.parquet', engine='pyarrow', index=False)



# df = dd.read_csv('/media/bhushan/nvme1/data/vldb/b3/s4.csv')
# df1 = dd.read_csv('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.csv',dtype={'budget': 'object',
#                                                                                 'id': 'object',
#                                                                                 'popularity': 'object',
#                                                                                 'revenue': 'float64',
#                                                                                 'vote_count': 'float64'})
# df1['popularity'] = dd.to_numeric(df1['popularity'], errors='coerce')  # Converts strings like '0.837228' to float

# df2 = dd.read_csv('/home/bhushan/nvme1/data/vldb/b2/links.csv')
# df = dd.read_csv('/home/bhushan/nvme1/data/vldb/b2/s1.csv')


# Write to Parquet format
# df.to_parquet('/media/bhushan/nvme1/data/vldb/b19/s4.parquet', engine='pyarrow', index=False)
# df1.to_parquet('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.parquet', engine='pyarrow', index=False)
# df2.to_parquet('/home/bhushan/nvme1/data/vldb/b2/links.parquet', engine='pyarrow', index=False)
