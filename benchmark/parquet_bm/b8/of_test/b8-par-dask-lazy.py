import lazyfatpandas.pandas as pd
from time import time
pd.BACKEND_ENGINE = pd.BackendEngines.DASK

start = time()
print = pd.lazyPrint
SO_c_d_t = {"userId":"int64","movieId":"int64","rating":"float32","timestamp":"int64"}
ratings_df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b2/s4.parquet')
metas_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.csv', low_memory=False,dtype={'budget': 'object',
                                                                                                      'id': 'object',
                                                                                                      'popularity': 'object',
                                                                                                      'revenue': 'float64',
                                                                                                      'vote_count': 'float64'})
SO_c_d_t = {"movieId":"int64","imdbId":"int64","float32":"int64"}
links_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/links.csv')
merged_df = pd.merge(links_df[['movieId','imdbId']],ratings_df[['movieId','rating']],on='movieId',how='right')
# print(merged_df)
metas_df['imdb_id'] = pd.to_numeric(metas_df['imdb_id'].str.slice(2),errors='coerce')
# print(metas_df)
merged_df = pd.merge(merged_df,metas_df[['title','imdb_id']],left_on='imdbId',right_on='imdb_id',how='inner')
print(merged_df)
# grouped_df = merged_df[['title','rating']].groupby('title').agg(Mean=('rating','mean'),Count=('rating','count'))
# print(grouped_df)
# result = grouped_df.query('Mean > 4.5 and Count > 2')
# print(result)
pd.flush()
__builtins__.print((time() - start))
