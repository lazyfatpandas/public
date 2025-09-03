import pandas as pd
from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *


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
    import numpy as np
    import pandas as pd
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b16/s5.csv',delimiter=';')
    print(df.head())
    print(df.describe())
    # replace the unknown words with nan
    df = df.replace('unknown', np.nan)
    print(df.isnull().sum())

    df = df.dropna()
    # check the null values
    print(df.isnull().sum())
    # ### Replace the column name from `loan` to `previous_loan_status` and `y` to `loan_status`
    # df.rename(columns={'loan': 'previous_loan_status', 'y': 'loan_status'}, inplace=True)
    df = df.rename(columns={'loan': 'previous_loan_status', 'y': 'loan_status'})
    print(df.head())
    # ## Find out the information of the `job` column.
    # How many different variants of job are there?
    print(df['job'].nunique())
    #Total different types of job
    print(df['job'].unique())
    # Counts for different types of `job`
    print(df['job'].value_counts())
    # ## Check the `loan_status`  approval rate by `job`
    # In[8]:
    # LAFP: print added
    #Bhushan dask error ---> TypeError: SeriesGroupBy.value_counts() got an unexpected keyword argument 'normalize'
    # print(df.groupby('job').loan_status.value_counts(normalize = True))
    # ## Check the percentage of loan approved by `education`
    # LAFP: print added
    print(df[df['loan_status'] == 'yes']['education'].value_counts(normalize=True))
    # ## Check the percentage of loan approved by `previous loan status`
    print(df[df['loan_status'] == 'yes']['previous_loan_status'].value_counts(normalize=True))
    # ## Create a pivot table between `loan_status` and `marital ` with values form `age`
    #Bhushan Modified: Dask error - operation not supported by dask -- code commented
    # pivot = df.pivot_table(index='loan_status', values='age', columns='marital')
    # print(pivot)
    # ## Loan status based on marital status whose status is married
    loan_stat = df[df['marital'].apply(lambda marital: marital == 'married')]
    marital_status_summery = loan_stat['loan_status'].value_counts()
    print(marital_status_summery)
    # ## Create a  Dataframes
    #
    # class 1
    branch_1 = {
        'customer_id': ['1', '2', '3', '4', '5'],
        'first_name': ['Andrew', 'Alex', 'Sabestian', 'Hilary', 'Jack'],
        'last_name': ['Ng', 'Hales', 'Rachaska', 'Masan', 'Anthony']}
    df_branch_1 = pd.DataFrame(branch_1, columns = ['customer_id', 'first_name', 'last_name'])
    print(df_branch_1)

    # class 2
    branch_2 = {
        'customer_id': ['4', '5', '6', '7', '8'],
        'first_name': ['Brain', 'Steve', 'Kim', 'Steve', 'Ben'],
        'last_name': ['Alexander', 'Jobs', 'Jonas', 'Fleming', 'Richardsan']}
    df_branch_2 = pd.DataFrame(branch_2, columns = ['customer_id', 'first_name', 'last_name'])
    print(df_branch_2)

    # test_score
    credit_score = {
        'customer_id': ['1', '2', '3', '4', '5', '7', '8', '9', '10', '11'],
        'score': [513, 675, 165, 961, 1080, 1654, 415, 900, 610, 1116]}
    df_credit_score = pd.DataFrame(credit_score, columns = ['customer_id','score'])
    print(df_credit_score)
    # ## Concatenate the dataframe `df_branch_1` and `df_branch_2` along the rows
    df_new = pd.concat([df_branch_1, df_branch_2])
    print(df_new)
    # ## Merge two dataframes `df_new` and `df_credit_score` with both the left and right dataframes using the `customer_id` key
    #
    print(pd.merge(df_new, df_credit_score, left_on='customer_id', right_on='customer_id'))
    print(time()-start)




if __name__=='__main__':
    max_ram_usage=0.0
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
    writer.writerow(["b16", "12.6GB", "s5.csv","pandas",time2,format(ram_usage),1651])
    file.close()









