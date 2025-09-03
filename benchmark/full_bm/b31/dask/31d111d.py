import dask.dataframe as pd
from dask.dataframe import compute
from time import time
from os import environ
import csv
import os
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *
pprint = print

def print(arg):
    pprint(*compute(arg))

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
    #!/usr/bin/env python
    # coding: utf-8
    # <h1 style="font-size:42px; text-align:center; margin-bottom:30px;"><span style="color:SteelBlue">Module 2
    # :</span> ABT Construction</h1>
    # Welcome to the workbook for <span style="color:royalblue">Module 2: ABT Construction</span>!
    # In this module, we're going to combine the **Data Cleaning** and **Feature Engineering** steps from Project 2.
    # Remember, **better data beats better algorithms**.
    # In this module, we'll cover the essential steps for building your analytical base table:
    # 1. [Drop unwanted observations](#drop)
    # 2. [Fix structural errors](#structural)
    # 3. [Handle missing data](#missing-data)
    # 4. [Engineer features](#engineer-features)
    # 5. [Save the ABT](#save-abt)
    # Finally, we'll save the ABT to a new file so we can use it in other modules.
    # ### First, let's import libraries and load the dataset.
    # In general, it's good practice to keep all of your library imports at the top of your notebook or program.
    # We've provided comments for guidance.
    # print_function for compatibility with Python 3
    # NumPy for numerical computing
    import numpy as np
    # Pandas for DataFrames
    # pd.set_option('display.max_columns', 100)
    import dask.dataframe as pd
    # Matplotlib for visualization
    from matplotlib import pyplot as plt
    # Seaborn for easier visualization
    import seaborn as sns
    # Next, let's import the dataset.
    # * The file path is <code style="color:crimson">'project_files/employee_data.csv'</code>
    # Load employee data from CSV
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b31/s1.csv')
    print(df.shape)
    print(df.department.unique())
    df = df[((df.department != 'temp') | df.department.isnull())]
    print(df.shape)
    print(df.filed_complaint.unique())
    print(df.recently_promoted.unique())
    df['filed_complaint'] = df.filed_complaint.fillna(0)
    df['recently_promoted'] = df.recently_promoted.fillna(0)
    print(df.filed_complaint.unique())
    print(df.recently_promoted.unique())
    df['department'] = df.department.replace('information_technology','IT')
    sns.countplot(y='department',data=df.compute().reset_index(drop=True))
    plt.savefig('fig1.png')
    print(df.isnull().sum())
    df['department'] = df['department'].fillna('Missing')
    df['last_evaluation_missing'] = df.last_evaluation.isnull().astype(int)
    df['last_evaluation'] = df.last_evaluation.fillna(0)
    print(df.isnull().sum())
    sns.lmplot(x='satisfaction',y='last_evaluation',data=df[(df.status == 'Left')].compute(),fit_reg=False)
    plt.savefig('fig2.png')
    df['underperformer'] = ((df.last_evaluation < 0.6) & (df.last_evaluation_missing == 0)).astype(int)
    df['unhappy'] = (df.satisfaction < 0.2).astype(int)
    df['overachiever'] = ((df.last_evaluation > 0.8) & (df.satisfaction > 0.7)).astype(int)
    print(df[['underperformer','unhappy','overachiever']].mean())
    df['status'] = pd.reshape.get_dummies(pd.DataFrame.categorize(df).status).Left
    print(df.status.mean())
    df = pd.get_dummies(pd.DataFrame.categorize(df),columns=['department','salary'])
    pprint(df.head(10))
    print(df)
    # Congratulations for making through Project 3's ABT Construction module!
    # As a reminder, here are a few things you did in this module:
    # * You cleaned dropped irrelevant observations from the dataset.
    # * You fixed various structural errors, such as wannabe indicator variables.
    # * You handled missing data.
    # * You engineered features by leveraging your exploratory analysis.
    # * And you created dummy variables before saving the ABT.
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
    writer.writerow(["b31", "150MB", "s1.csv","dask",time2,format(ram_usage),3115])
    file.close()




