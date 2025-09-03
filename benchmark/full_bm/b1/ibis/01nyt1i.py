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
    con = ibis.duckdb.connect()
    t = con.read_csv('/media/bhushan/nvme1/data/vldb/b1/s1.csv')
    t = t.filter(t.fare_amount > 0)
    t = t.mutate(day=t.tpep_pickup_datetime.strftime('%w').cast('int8'))
    result = t.group_by(t.day).aggregate(passenger_count=t.passenger_count.sum())
    df = result.execute()
    print(df)
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
    writer.writerow(["b1", "150MB", "s1.csv","ibis",time2,format(ram_usage),119])
    file.close()



