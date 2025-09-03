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
    print = pd.lazyPrint
    # Matplotlib for visualization
    from matplotlib import pyplot as plt
    # Seaborn for easier visualization
    import seaborn as sns
    pd.BACKEND_ENGINE = pd.BackendEngines.MODIN

    # Load employee data from CSV
    df = pd.read_csv('/media/bhushan/nvme1/data/vldb/b31/s4.csv')


    # Now we're ready to jump into cleaning the data!

    # <span id="drop"></span>
    # # 1. Drop wanted observations
    #
    # The first step to data cleaning is removing samples from your dataset that you don't want to include in the model.
    #
    # <br>
    # **First, <span style="color:royalblue">drop duplicates</span> from the dataset.**
    # * Then, print the shape of the new dataframe.

    # In[5]:


    # LAFP: Disabling drop_duplicates for benchmarking
    # Drop duplicates
    # df = df.drop_duplicates()
    print( df.shape )


    # **Display all of the unique classes of the <code style="color:steelblue">'department'</code> feature**

    # In[88]:


    # Unique classes of 'department'
    # LAFP: print added
    print(df.department.unique())


    # **Drop all observations that belong to the <span style="color:crimson">'temp'</span> department.**
    # * **Hint:** This is the same as keeping all that don't belong to that department.
    # * **Hint:** Remember to overwrite your original dataframe.
    # * Then, print the shape of the new dataframe.

    # In[7]:

    # LAFP: added df.department.isnull() in the filter condition to make it consistent with pandas. It can be automated in Runtime or via SCIRPy
    # Drop temporary workers
    # df = df[df.department != 'temp']
    df = df[(df.department != 'temp') | (df.department.isnull())]
    print( df.shape )

    # <p style="text-align:center; margin: 40px 0 40px 0; font-weight:bold">
    # <a href="#toc">Back to Contents</a>
    # </p>

    # <span id="structural"></span>
    # # 2. Fix structural errors
    #
    # The next bucket under data cleaning involves fixing structural errors, which arise during measurement, data transfer, or other types of "poor housekeeping."
    #
    # <br>
    # **Print the unique values of <code style="color:steelblue">'filed_complaint'</code> and <code style="color:steelblue">'recently_promoted'</code>.**

    # In[8]:


    # Print unique values of 'filed_complaint'
    print( df.filed_complaint.unique() )

    # Print unique values of 'recently_promoted'
    print( df.recently_promoted.unique() )


    # **Fill missing <code style="color:steelblue">'filed_complaint'</code> and <code style="color:steelblue">'recently_promoted'</code> values with <code style="color:crimson">0</code>.**

    # In[9]:


    # Missing filed_complaint values should be 0
    df['filed_complaint'] = df.filed_complaint.fillna(0)

    # Missing recently_promoted values should be 0
    df['recently_promoted'] = df.recently_promoted.fillna(0)


    # **Print the unique values of <code style="color:steelblue">'filed_complaint'</code> and <code style="color:steelblue">'recently_promoted'</code> again, just to confirm.**

    # In[10]:


    # Print unique values of 'filed_complaint'
    print( df.filed_complaint.unique() )

    # Print unique values of 'recently_promoted'
    print( df.recently_promoted.unique() )


    # **Replace any instances of <code style="color:crimson">'information_technology'</code> with <code style="color:crimson">'IT'</code> instead.**
    # * Remember to do it **inplace**.
    # * Then, plot the **bar chart** for <code style="color:steelblue">'department'</code> to see its new distribution.

    # In[11]:


    # 'information_technology' should be 'IT'
    # LAFP: inplace not supported by dask
    # df.department.replace('information_technology', 'IT', inplace=True)
    df['department'] = df.department.replace('information_technology', 'IT')

    # Plot class distributions for 'department'
    sns.countplot(y='department', data=df.compute(live_df=[df]))
    # LAFP: inline not support in iPython, added plt.savefig
    plt.savefig("fig1.png")


    # <p style="text-align:center; margin: 40px 0 40px 0; font-weight:bold">
    # <a href="#toc">Back to Contents</a>
    # </p>

    # <span id="missing-data"></span>
    # # 3. Handle missing data
    #
    # Next, it's time to handle **missing data**.
    #
    # <br>
    # **Display the <span style="color:royalblue">number of missing values</span> for each feature (both categorical and numeric).**

    # In[12]:


    # Display number of missing values by feature
    # LAFP: print added
    print(df.isnull().sum())


    # **Label missing values in <code style="color:steelblue">'department'</code> as <code style="color:crimson">'Missing'</code>.**
    # * By the way, the <code style="color:steelblue">.fillna()</code> function also has an <code style="color:steelblue">inplace=</code> argument, just like the <code style="color:steelblue">.replace()</code> function.
    # * In the previous project, we just overwrote that column. This time, try using the <code style="color:steelblue">inplace=</code> argument instead.

    # In[13]:


    # Fill missing values in department with 'Missing'
    # LAFP: inplace not supported by dask
    # df['department'].fillna('Missing', inplace=True)
    df['department'] = df['department'].fillna('Missing')


    # **First, let's flag <code style="color:steelblue">'last_evaluation'</code> with an indicator variable of missingness.**
    # * <code style="color:crimson">0</code> if not missing.
    # * <code style="color:crimson">1</code> if missing.
    #
    # Let's name the new indicator variable <code style="color:steelblue">'last_evaluation_missing'</code>.
    # * We can use the <code style="color:steelblue">.isnull()</code> function.
    # * Also, remember to convert it with <code style="color:steelblue">.astype(int)</code>

    # In[14]:


    # Indicator variable for missing last_evaluation
    df['last_evaluation_missing'] = df.last_evaluation.isnull().astype(int)


    # **Then, simply fill in the original missing value with <code style="color:crimson">0</code> just so your algorithms can run properly.**

    # In[15]:


    # Fill missing values in last_evaluation with 0
    # LAFP: inplace not supported by dask
    # df.last_evaluation.fillna(0, inplace=True)
    df['last_evaluation'] = df.last_evaluation.fillna(0)


    # **Display the number of missing values for each feature (both categorical and numeric) again, just to confirm.**

    # In[16]:


    # Display number of missing values by feature
    # LAFP: print added
    print(df.isnull().sum())


    # <p style="text-align:center; margin: 40px 0 40px 0; font-weight:bold">
    # <a href="#toc">Back to Contents</a>
    # </p>

    # <span id="engineer-features"></span>
    # # 4. Engineer features
    #
    # For this project, we're going to have an abbreviated version of feature engineering, since we've already covered many tactics in Project 2.
    #
    # <br>
    # Do you remember the scatterplot of <code style="color:steelblue">'satisfaction'</code> and <code style="color:steelblue">'last_evaluation'</code> for employees who have <code style="color:crimson">'Left'</code>?
    #
    # **Let's reproduce it here, just so we have it in front of us.**

    # In[17]:

    # Scatterplot of satisfaction vs. last_evaluation, only those who have left
    sns.lmplot(x='satisfaction', y='last_evaluation', data=df[df.status == 'Left'].compute(live_df=[df]), fit_reg=False)
    # LAFP: inline not support in iPython, added plt.savefig
    plt.savefig("fig2.png")


    # These roughly translate to 3 **indicator features** we can engineer:
    #
    # * <code style="color:steelblue">'underperformer'</code> - last_evaluation < 0.6 and last_evaluation_missing == 0
    # * <code style="color:steelblue">'unhappy'</code> - satisfaction_level < 0.2
    # * <code style="color:steelblue">'overachiever'</code> - last_evaluation > 0.8 and satisfaction > 0.7
    #
    # <br>
    # **Create those 3 indicator features.**
    # * Use boolean masks.
    # * **Important:** For <code style="color:steelblue">'underperformer'</code>, it's important to include <code style="color:steelblue">'last_evaluation_missing' == 0</code> to avoid those originally missing observations that we flagged and filled.

    # In[18]:


    # Create indicator features
    df['underperformer'] = ((df.last_evaluation < 0.6) & (df.last_evaluation_missing == 0)).astype(int)

    df['unhappy'] = (df.satisfaction < 0.2).astype(int)

    df['overachiever'] = ((df.last_evaluation > 0.8) & (df.satisfaction > 0.7)).astype(int)


    # <br>
    # **Next, run this code to check that you created the features correctly.**

    # In[19]:


    # The proportion of observations belonging to each group
    # LAFP: print added
    print(df[['underperformer', 'unhappy', 'overachiever']].mean())


    # <p style="text-align:center; margin: 40px 0 40px 0; font-weight:bold">
    # <a href="#toc">Back to Contents</a>
    # </p>

    # <span id="save-abt"></span>
    # # 5. Save the ABT
    #
    # Finally, let's save the **analytical base table**.
    #
    # <br>
    # **Convert <code style="color:steelblue">'status'</code> into an indicator variable.**
    # * <code style="color:crimson">'Left'</code> should be <code style="color:crimson">1</code>
    # * <code style="color:crimson">'Employed'</code> should be <code style="color:crimson">0</code>
    # * There's also a really easy way you can use <code style="color:steelblue">pd.get_dummies()</code> here. See below.

    # In[20]:


    # Convert status to an indicator variable
    df['status'] = pd.get_dummies( df.status ).Left


    # **To confirm we did that correctly, display the proportion of people in our dataset who left.**

    # In[21]:


    # The proportion of observations who 'Left'
    # LAFP: print added
    print(df.status.mean())


    # **Overwrite your dataframe with a version that has <span style="color:royalblue">dummy variables</span> for the categorical features.**
    # * Then, display the first 10 rows to confirm all of the changes we've made so far in this module.

    # In[22]:


    # Create new dataframe with dummy features
    df = pd.get_dummies(df, columns=['department', 'salary'])

    # Display first 10 rows
    # LAFP: print added
    print(df.head(10))


    # **Save this dataframe as your <span style="color:royalblue">analytical base table</span> to use in later modules.**
    # * Remember to set the argument <code style="color:steelblue">index=None</code> to save only the data.

    # In[23]:


    # Save analytical base table
    # LAFP: dask outputs the csv into parts (one csv for each partition), printing the result for consistency and ease in benchmarking
    # df.to_csv('analytical_base_table.csv', index=None)
    print(df)
    pd.flush()
    print(time()-start)


if __name__=='__main__':
    max_ram_usage=0.0
    pd.BACKEND_ENGINE = pd.BackendEngines.MODIN
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
    writer.writerow(["b31", "4.2GB", "s4.csv","lmodin",time2,format(ram_usage),3144])
    file.close()



