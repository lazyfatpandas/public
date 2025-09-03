import pandas as pd
from time import time
from os import environ
import csv
from tqdm import tqdm
from time import sleep
import psutil
import threading
from threading import *
start = time()
import matplotlib.pyplot as plt
import matplotlib
import pandas as pd
import numpy as np
matplotlib.use('agg')

city_data_to_load = "/data/city_data.csv"
ride_data_to_load = "/data/ride_data.csv"

# read data

# df_city_data_to_load = pd.read_csv('/media/bhushan/nvme1/data/vldb/b33/s4_city.csv')
# df_riders_data_to_load = pd.read_csv('/media/bhushan/nvme1/data/vldb/b33/ride_data.csv')
df_city_data_to_load = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b33/city_data.parquet')
df_riders_data_to_load = pd.read_parquet('/media/bhushan/nvme1/data/vldb/b33/s4_ride.parquet')

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
plt.scatter(x_urban, y_urban, label="Urban", s=s_urban * 10, color=["pink"], edgecolor="black", alpha=1, marker="o")
plt.scatter(x_rural, y_rural, label="Rural", s=s_rural * 10, color=["purple"], edgecolor="black", alpha=1, marker="o")
plt.scatter(x_suburban, y_suburban, label="Suburban", s=s_suburban * 10, color=["orange"], edgecolor="black", alpha=1, marker="o")
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
plt.text(42, 35, "Note: \nCircle size correlates with driver count per city.", fontsize=12)

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
plt.pie(fare_sum, explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
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
plt.pie(rides_count, explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
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
plt.pie(drivers_sum, explode=explode, labels=labels, colors=colors, autopct="%1.1f%%", shadow=True, startangle=150)
plt.title("% of Total Drivers by City Type")

plt.savefig("PyChartTotalDrivers.png")

# Show Figure
# plt.show()

print(time()-start)




