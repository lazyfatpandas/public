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
    import matplotlib.pyplot as plt
    import matplotlib
    import pandas as pd
    import numpy as np
    matplotlib.use('agg')

    con = ibis.duckdb.connect()


    # read data

    city = con.read_csv('/media/bhushan/nvme1/data/vldb/b33/city_data.csv')
    ride = con.read_csv('/media/bhushan/nvme1/data/vldb/b33/s3_ride.csv')
    # merge tables, display summary
    merge_table = city.join(ride, "city", how="left")
    # LAFP: print added
    print(merge_table.limit(5).execute())

    # ## Bubble Plot of Ride Sharing Data

    ## LAFP: Syntax not supported by Dask, ValueError: Grouping by an unaligned column is unsafe and unsupported.
    ## Original CODE:
    urban_city = merge_table.filter(merge_table.type == "Urban").group_by("city")
    rural_city = merge_table.filter(merge_table.type == "Rural").group_by("city")
    suburban_city = merge_table.filter(merge_table.type == "Suburban").group_by("city")
    x_urban = urban_city.ride_id.count().execute()['count(ride_id)']
    y_urban = urban_city.fare.mean().execute()['mean(fare)']
    s_urban = urban_city.driver_count.mean().execute()['mean(driver_count)']
    x_rural = rural_city.ride_id.count().execute()['count(ride_id)']
    y_rural = rural_city.fare.mean().execute()['mean(fare)']
    s_rural = rural_city.driver_count.mean().execute()['mean(driver_count)']
    x_suburban = suburban_city.ride_id.count().execute()['count(ride_id)']
    y_suburban = suburban_city.fare.mean().execute()['mean(fare)']
    s_suburban = suburban_city.driver_count.mean().execute()['mean(driver_count)']

    plt.scatter(x_urban, y_urban, label="Urban", s=s_urban * 10, color=["pink"], edgecolor="black", alpha=1, marker="o")
    plt.scatter(x_rural, y_rural, label="Rural", s=s_rural * 10, color=["purple"], edgecolor="black", alpha=1, marker="o")
    plt.scatter(x_suburban, y_suburban, label="Suburban", s=s_suburban * 10, color=["orange"], edgecolor="black", alpha=1, marker="o")
    plt.grid()
    plt.xlabel("Total Number of Rides (Per City)")
    plt.ylabel("Average Fare ($)")
    plt.title("Pyber Ride Sharing Data - 2016")
    legend = plt.legend(fontsize=9.5, title="City Types", loc="best")
    legend.legend_handles[0]._sizes = [40]
    legend.legend_handles[1]._sizes = [40]
    legend.legend_handles[2]._sizes = [40]
    plt.text(42, 35, "Note: \nCircle size correlates with driver count per city.", fontsize=12)
    plt.savefig("PyPlot.png")

    # ---- Total Fares by City Type ----
    type_grouped = merge_table.group_by("type")
    # fare_sum = type_grouped.aggregate(total_fare=merge_table.fare.sum()).to_pandas()

    # Calculate Type Percents
    fare_sum = type_grouped.fare.sum().execute()['sum(fare)']
    # print(type(fare_sum),fare_sum.columns)

    # Build Pie Chart
    labels = ["Rural", "Suburban", "Urban"]
    explode = (0.1, 0.1, 0.1)
    colors = ["lightblue", "lightgreen", "lightcoral"]
    plt.pie(fare_sum, explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
    plt.title("% of Total Fares by City Type")

    plt.savefig("PyChartTotalFares.png")

    # Calculate Type Percents
    rides_count = type_grouped.ride_id.count().execute()['count(ride_id)']

    # Build Pie Chart
    labels = ["Rural", "Suburban", "Urban"]
    explode = (0.1, 0.1, 0.1)
    colors = ["lightblue", "lightgreen", "lightcoral"]
    plt.pie(rides_count, explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
    plt.title("% of Total Rides by City Type")

    plt.savefig("PyChartTotalRides.png")


    type_grouped_drivers = city.group_by("type")
    drivers_sum = type_grouped_drivers.driver_count.sum().execute()['sum(driver_count)']

    # Build Pie Chart
    labels = ["Rural", "Suburban", "Urban"]
    explode = (0.1, 0.1, 0.1)
    colors = ["lightblue", "lightgreen", "lightcoral"]
    plt.pie(drivers_sum, explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
    plt.title("% of Total Drivers by City Type")

    plt.savefig("PyChartTotalDrivers.png")



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
    writer.writerow(["b33", "1.4GB", "s3.csv","ibis",time2,format(ram_usage),3339])
    file.close()



