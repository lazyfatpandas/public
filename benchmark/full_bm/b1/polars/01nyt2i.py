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
    t = con.read_csv('/media/bhushan/nvme1/data/vldb/b1/s2.csv')
    # Step 2: Filter bad rows
    t = t.filter(t.fare_amount > 0)
    # Convert string to timestamp using the correct format string
    t = t.mutate(
        tpep_pickup_datetime_parsed=t.tpep_pickup_datetime.to_timestamp('%Y-%m-%d %H:%M:%S')
    )

    # Then extract the day of week as string and cast to int8
    t = t.mutate(
        day=t.tpep_pickup_datetime_parsed.strftime('%w').cast('int8')
    )
    # Step 4: Group by 'day' and sum passenger_count
    result = t.group_by(['day']).aggregate(
        passenger_count=t.passenger_count.sum()
    )

    # Step 5: Execute and print
    df = result.execute()
    print(df)
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
    writer.writerow(["b1", "450MB", "s2.csv","polars",time2,format(ram_usage),"12B"])
    file.close()



