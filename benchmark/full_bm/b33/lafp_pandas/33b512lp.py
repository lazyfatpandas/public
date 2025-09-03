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
    start = time()
    print=pd.lazyPrint
    import matplotlib.pyplot as plt
    import numpy as np
    import matplotlib
    matplotlib.use('agg')
    print = pd.lazyPrint
    city_data_to_load = '/data/city_data.csv'
    ride_data_to_load = '/data/ride_data.csv'
    # df_city_data_to_load = pd.read_csv('data/city_data.csv')
    # df_riders_data_to_load = pd.read_csv('data/ride_data.csv')
    df_city_data_to_load = pd.read_csv('/media/bhushan/nvme1/data/vldb/b33/city_data.csv')
    df_riders_data_to_load = pd.read_csv('/media/bhushan/nvme1/data/vldb/b33/s2_ride.csv')
    merge_table_df = pd.merge(df_city_data_to_load,df_riders_data_to_load,on='city',how='left')
    print(merge_table_df.head(5))
    urban_city = merge_table_df[(merge_table_df['type'] == 'Urban')].groupby(['city'])
    rural_city = merge_table_df[(merge_table_df['type'] == 'Rural')].groupby(['city'])
    suburban_city = merge_table_df[(merge_table_df['type'] == 'Suburban')].groupby(['city'])
    x_urban = urban_city['ride_id'].count()
    y_urban = urban_city['fare'].mean()
    s_urban = urban_city['driver_count'].mean()
    x_rural = rural_city['ride_id'].count()
    y_rural = rural_city['fare'].mean()
    s_rural = rural_city['driver_count'].mean()
    x_suburban = suburban_city['ride_id'].count()
    y_suburban = suburban_city['fare'].mean()
    s_suburban = suburban_city['driver_count'].mean()
    plt.scatter(x_urban.compute(live_df=[df_city_data_to_load, merge_table_df, x_suburban, y_suburban, s_suburban, x_rural, y_rural, s_rural, s_urban, y_urban]),y_urban.compute(live_df=[df_city_data_to_load, merge_table_df, x_suburban, y_suburban, s_suburban, x_rural, y_rural, s_rural, s_urban]),label='Urban',s=(s_urban.compute(live_df=[df_city_data_to_load, merge_table_df, x_suburban, y_suburban, s_suburban, x_rural, y_rural, s_rural]) * 10),color=['pink'],edgecolor='black',alpha=1,marker='o')
    plt.scatter(x_rural.compute(live_df=[df_city_data_to_load, merge_table_df, x_suburban, y_suburban, s_suburban, s_rural, y_rural]),y_rural.compute(live_df=[df_city_data_to_load, merge_table_df, x_suburban, y_suburban, s_suburban, s_rural]),label='Rural',s=(s_rural.compute(live_df=[df_city_data_to_load, merge_table_df, x_suburban, y_suburban, s_suburban]) * 10),color=['purple'],edgecolor='black',alpha=1,marker='o')
    plt.scatter(x_suburban.compute(live_df=[df_city_data_to_load, merge_table_df, s_suburban, y_suburban]),y_suburban.compute(live_df=[df_city_data_to_load, merge_table_df, s_suburban]),label='Suburban',s=(s_suburban.compute(live_df=[df_city_data_to_load, merge_table_df]) * 10),color=['orange'],edgecolor='black',alpha=1,marker='o')
    plt.grid()
    plt.xlabel('Total Number of Rides (Per City)')
    plt.ylabel('Average Fare ($)')
    plt.title('Pyber Ride Sharing Data - 2016')
    legend = plt.legend(fontsize=9.5,title='City Types',loc='best')
    legend.legend_handles[0]._sizes = [40]
    legend.legend_handles[1]._sizes = [40]
    legend.legend_handles[2]._sizes = [40]
    plt.text(42,35,'Note: Circle size correlates with driver count per city.',fontsize=12)
    plt.savefig('PyPlot.png')
    type_grouped = merge_table_df.groupby(['type'])
    fare_sum = type_grouped['fare'].sum()
    labels = ['Rural','Suburban','Urban']
    explode = (0.1,0.1,0.1)
    colors = ['lightblue','lightgreen','lightcoral']
    plt.pie(fare_sum.compute(live_df=[df_city_data_to_load, type_grouped]),explode=explode,labels=labels,colors=colors,autopct='%1.1f%%',shadow=True,startangle=150)
    plt.title('% of Total Fares by City Type')
    plt.savefig('PyChartTotalFares.png')
    rides_count = type_grouped['ride_id'].count()
    labels = ['Rural','Suburban','Urban']
    explode = (0.1,0.1,0.1)
    colors = ['lightblue','lightgreen','lightcoral']
    plt.pie(rides_count.compute(live_df=[df_city_data_to_load]),explode=explode,labels=labels,colors=colors,autopct='%1.1f%%',shadow=True,startangle=150)
    plt.title('% of Total Rides by City Type')
    plt.savefig('PyChartTotalRides.png')
    type_grouped_drivers = df_city_data_to_load.groupby(['type'])
    drivers_sum = type_grouped_drivers['driver_count'].sum()
    labels = ['Rural','Suburban','Urban']
    explode = (0.1,0.1,0.1)
    colors = ['lightblue','lightgreen','lightcoral']
    plt.pie(drivers_sum.compute(live_df=[]),explode=explode,labels=labels,colors=colors,autopct='%1.1f%%',shadow=True,startangle=150)
    plt.title('% of Total Drivers by City Type')
    plt.savefig('PyChartTotalDrivers.png')
    pd.flush()
    print(time()-start)


if __name__=='__main__':
    max_ram_usage=0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.PANDAS
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
    writer.writerow(["b33", "450MB", "s2.csv","lpandas",time2,format(ram_usage),3322])
    file.close()



