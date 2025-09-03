import dask.dataframe as pd
from time import time
from os import environ
import csv
import os
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *




start = time()
src = environ.get('FILES_GLOB', "/home/bhushan/nvme1/data/vldb/b2")
# load files
ratings_df = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b2/s4.parquet')
metas_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/movies_metadata.csv', low_memory=False,dtype={'budget': 'object',
                                                                                                       'id': 'object',
                                                                                                       'popularity': 'object',
                                                                                                       'revenue': 'float64',
                                                                                                       'vote_count': 'float64'})
links_df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b2/links.csv',dtype={'tmdbId': 'float64'}, low_memory=False)
# 1st join
merged_df = pd.merge(links_df[['movieId','imdbId']],
                     ratings_df[['movieId','rating']], on='movieId', how='right')
# print(merged_df.compute())
metas_df['imdb_id']=pd.to_numeric(metas_df['imdb_id'].str.slice(2), errors='coerce')
# print(metas_df.compute())

# # 2nd join
merged_df = pd.merge(merged_df, metas_df[['title','imdb_id']],left_on='imdbId', right_on='imdb_id', how='inner')
print(merged_df.compute())
# # # group-by having
# grouped_df = merged_df[['title','rating']].groupby('title').agg(Mean=('rating', 'mean' ), Count=('rating','count'))
# result = grouped_df.query('Mean > 4.5 and Count > 2')
# print(result.compute())
print(time()-start)

