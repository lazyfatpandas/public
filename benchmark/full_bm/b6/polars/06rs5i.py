import ibis
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
    con = ibis.polars.connect()
    # Read the CSV file
    table = con.read_csv('/media/bhushan/nvme1/data/vldb/b3/s5.csv')
    # Compute 10th quantile of col1
    quantile_val = table.aggregate(quant=table.col1.quantile(0.1)).execute().iloc[0, 0]

    # Add new columns
    table = table.mutate(
        col1_binary = table.col1 > quantile_val,
        col2_binary = table.col1 > 3
    )

    # Show first few rows
    print(table.head().execute())

    # Filter on col2 > 10
    filtered = table.filter(table.col2 > 10)
    print(filtered.head().execute())

    # Group by col1_binary and compute mean of col3
    grouped = filtered.group_by(filtered.col1_binary).aggregate(
        col3_mean = filtered.col3.mean()
    )
    print(grouped.execute())

    print(time() - start)

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
    writer.writerow(["b6", "12.6GB", "s5.csv","polars",time2,format(ram_usage),"65B"])
    file.close()









