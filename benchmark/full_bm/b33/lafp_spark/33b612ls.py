import lazyfatpandas.pandas as pd
from time import time
from os import environ
import csv
import os
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *
pd.analyze()

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
    import os
    os.environ['PYARROW_IGNORE_TIMEZONE'] = '1'
    print=pd.lazyPrint
    start = time()
    import matplotlib.pyplot as plt
    import matplotlib
    # import pandas as pd
    import numpy as np
    matplotlib.use('agg')

    city_data_to_load = "/data/city_data.csv"
    ride_data_to_load = "/data/ride_data.csv"

    # read data

    df_city_data_to_load = pd.read_csv('/media/bhushan/nvme1/data/vldb/b33/city_data.csv')
    df_riders_data_to_load = pd.read_csv('/media/bhushan/nvme1/data/vldb/b33/s1_ride.csv')

    # merge tables, display summary

    merge_table_df = pd.merge(df_city_data_to_load, df_riders_data_to_load, on="city", how="left")
    # LAFP: print added
    print(merge_table_df.head(5))

    # ## Bubble Plot of Ride Sharing Data

    ## LAFP: Syntax not supported by Dask, ValueError: Grouping by an unaligned column is unsafe and unsupported.
    ## Original CODE:
    ## urban_city = merge_table_df[merge_table_df["type"] == "Urban"].groupby([merge_table_df["city"]])
    ## rural_city = merge_table_df[merge_table_df["type"] == "Rural"].groupby([merge_table_df["city"]])
    ## suburban_city = merge_table_df[merge_table_df["type"] == "Suburban"].groupby([merge_table_df["city"]])

    # LAFP: Modified for Dask
    urban_city = merge_table_df[merge_table_df['type'] == 'Urban'].groupby(['city'])
    rural_city = merge_table_df[merge_table_df['type'] == 'Rural'].groupby(['city'])
    suburban_city = merge_table_df[merge_table_df['type'] == 'Suburban'].groupby(['city'])

    # Obtain the x and y coordinates for each of the three city types
    x_urban = urban_city["ride_id"].count()
    y_urban = urban_city["fare"].mean()
    s_urban = urban_city["driver_count"].mean()

    x_rural = rural_city["ride_id"].count()
    y_rural = rural_city["fare"].mean()
    s_rural = rural_city["driver_count"].mean()

    x_suburban = suburban_city["ride_id"].count()
    y_suburban = suburban_city["fare"].mean()
    s_suburban = suburban_city["driver_count"].mean()

    # create scatter plot
    plt.scatter(x_urban.to_numpy(), y_urban.to_numpy(), label="Urban", s=s_urban.to_numpy() * 10, color=["pink"], edgecolor="black", alpha=1, marker="o")
    plt.scatter(x_rural.to_numpy(), y_rural.to_numpy(), label="Rural", s=s_rural.to_numpy() * 10, color=["purple"], edgecolor="black", alpha=1, marker="o")
    plt.scatter(x_suburban.to_numpy(), y_suburban.to_numpy(), label="Suburban", s=s_suburban.to_numpy() * 10, color=["orange"], edgecolor="black", alpha=1, marker="o")
    plt.grid()

    # Incorporate the other graph properties
    plt.xlabel("Total Number of Rides (Per City)")
    plt.ylabel("Average Fare ($)")
    plt.title("Pyber Ride Sharing Data - 2016")

    # Create a legend
    legend = plt.legend(fontsize=9.5, title="City Types", loc="best")

    # legend scaling
    #Bhushan MatplotlibDeprecationWarning: The legendHandles attribute was deprecated in Matplotlib 3.7 and will be removed two minor releases later. Use legend_handles instead.
    # legend.legendHandles[2]._sizes = [40]
    legend.legend_handles[0]._sizes = [40]
    legend.legend_handles[1]._sizes = [40]
    legend.legend_handles[2]._sizes = [40]
    # legend.legendHandles[0]._sizes = [40]
    # legend.legendHandles[1]._sizes = [40]
    # legend.legendHandles[2]._sizes = [40]
    # Incorporate a text label regarding circle size
    plt.text(42, 35, "Note: Circle size correlates with driver count per city.", fontsize=12)

    # Save figure with the parameter bbox_inches to fit the whole image
    plt.savefig("PyPlot.png")

    # Show plot
    # plt.show()

    # ## Total Fares by City Type

    # In[ ]:


    # Calculate Type Percents
    type_grouped = merge_table_df.groupby(['type'])
    fare_sum = type_grouped['fare'].sum()

    # Build Pie Chart
    labels = ["Rural", "Suburban", "Urban"]
    explode = (0.1, 0.1, 0.1)
    colors = ["lightblue", "lightgreen", "lightcoral"]
    plt.pie(fare_sum.to_numpy(), explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
    plt.title("% of Total Fares by City Type")

    plt.savefig("PyChartTotalFares.png")

    # Show Figure
    # plt.show()

    # ## Total Rides by City Type

    # In[6]:


    # Calculate Type Percents
    rides_count = type_grouped['ride_id'].count()

    # Build Pie Chart
    labels = ["Rural", "Suburban", "Urban"]
    explode = (0.1, 0.1, 0.1)
    colors = ["lightblue", "lightgreen", "lightcoral"]
    plt.pie(rides_count.to_numpy(), explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
    plt.title("% of Total Rides by City Type")

    plt.savefig("PyChartTotalRides.png")

    # Show Figure
    # plt.show()

    # ## Total Drivers by City Type


    # Calculate Type Percents
    type_grouped_drivers = df_city_data_to_load.groupby(['type'])
    drivers_sum = type_grouped_drivers['driver_count'].sum()

    # Build Pie Chart
    labels = ["Rural", "Suburban", "Urban"]
    explode = (0.1, 0.1, 0.1)
    colors = ["lightblue", "lightgreen", "lightcoral"]
    plt.pie(drivers_sum.to_numpy(), explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
    plt.title("% of Total Drivers by City Type")

    plt.savefig("PyChartTotalDrivers.png")

    # Show Figure
    # plt.show()

    print(time()-start)
    pd.flush()


if __name__=='__main__':
    from pyspark.sql import SparkSession
    # Create a SparkSession with memory configuration
    spark = SparkSession.builder \
        .appName("StartupGrowthAnalysis") \
        .master("local[*]") \
        .config("spark.driver.memory", "24g") \
        .config("spark.sql.shuffle.partitions", "12") \
        .getOrCreate()
    max_ram_usage=0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.SPARK
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
    spark.stop()
    file = open('../../Results.csv','a',newline='')
    writer = csv.writer(file)
    writer.writerow(["b33", "150MB", "s1.csv","lspark",time2,format(ram_usage),3318])
    file.close()


