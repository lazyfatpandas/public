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
    from matplotlib import pyplot as plt
    # Seaborn for easier visualization
    import seaborn as sns
    # Next, let's import the dataset.
    # * The file path is <code style="color:crimson">'project_files/employee_data.csv'</code>
    # Load employee data from CSV
    con = ibis.connect("duckdb://")

    # Load CSV into Ibis table
    df = con.read_csv("/media/bhushan/nvme1/data/vldb/b31/s4.csv")

    # Drop duplicates - Ibis uses .distinct()
    # df = df.distinct()  # Uncomment if you want this

    print(df.count().execute())  # shape equivalent: only row count here

    # Unique values of department
    print(df.select(df.department).distinct().execute())

    # Drop temp department or keep nulls
    df = df.filter(
        (df.department != "temp") | df.department.isnull()
    )
    print(df.count().execute())

    # Unique values of filed_complaint and recently_promoted
    print(df.select(df.filed_complaint).distinct().execute())
    print(df.select(df.recently_promoted).distinct().execute())

    # Fill missing filed_complaint & recently_promoted with 0
    df = df.mutate(
        filed_complaint=df.filed_complaint.fill_null(0),
        recently_promoted=df.recently_promoted.fill_null(0),
    )

    # Replace 'information_technology' with 'IT'
    df = df.mutate(
        department=(
            ibis.case()
                .when(df.department == "information_technology", "IT")
                .else_(df.department)
                .end()
        )
    )

    # Missing values count
    null_counts = df.aggregate(
        **{col: df[col].isnull().sum() for col in df.columns}
    )
    print(null_counts.execute())

    # Fill missing department with 'Missing'
    df = df.mutate(
        department=df.department.fill_null("Missing")
    )

    # Indicator for missing last_evaluation
    df = df.mutate(
        last_evaluation_missing=df.last_evaluation.isnull().ifelse(1, 0)
    )

    # Fill missing last_evaluation with 0
    df = df.mutate(
        last_evaluation=df.last_evaluation.fill_null(0)
    )

    null_counts = df.aggregate(
        **{col: df[col].isnull().sum() for col in df.columns}
    )
    print(null_counts.execute())

    # Create indicator features
    df = df.mutate(
        underperformer=((df.last_evaluation < 0.6) & (df.last_evaluation_missing == 0)).ifelse(1, 0),
        unhappy=(df.satisfaction < 0.2).ifelse(1, 0),
        overachiever=((df.last_evaluation > 0.8) & (df.satisfaction > 0.7)).ifelse(1, 0),
    )

    # Mean of indicators
    means = df.aggregate(
        **{col: df[col].mean() for col in ["underperformer", "unhappy", "overachiever"]}
    )
    print(means.execute())


    # Convert status to indicator variable
    # df = df.mutate(
    #     status=(df.status == "Left").ifelse(1, 0)
    # )

    # Proportion of "Left"
    # print(df.status.mean().execute())

    # Dummy variables for department and salary
    # df = df.join(
    #     df.department.one_hot(),
    #     how="left"
    # ).join(
    #     df.salary.one_hot(),
    #     how="left"
    # ).drop(["department", "salary"])

    # Display first 10 rows
    print(df.limit(10).execute())

    # Save final ABT
    df.execute().to_csv("analytical_base_table.csv", index=False)

    print(time() - start)

    # print( df.filed_complaint.unique() )
    # # Print unique values of 'recently_promoted'
    # print( df.recently_promoted.unique() )
    # # **Replace any instances of <code style="color:crimson">'information_technology'</code> with <code style="color:crimson">'IT'</code> instead.**
    # # * Remember to do it **inplace**.
    # # * Then, plot the **bar chart** for <code style="color:steelblue">'department'</code> to see its new distribution.
    # # 'information_technology' should be 'IT'
    # # LAFP: inplace not supported by dask
    # # df.department.replace('information_technology', 'IT', inplace=True)
    # df['department'] = df.department.replace('information_technology', 'IT')
    # # Plot class distributions for 'department'
    # sns.countplot(y='department', data=df)
    # # LAFP: inline not support in iPython, added plt.savefig
    # plt.savefig("fig1.png")
    # # # 3. Handle missing data
    # # Next, it's time to handle **missing data**.
    # # **Display the <span style="color:royalblue">number of missing values</span> for each feature (both categorical and numeric).**
    # # Display number of missing values by feature
    # # LAFP: print added
    # print(df.isnull().sum())
    # # **Label missing values in <code style="color:steelblue">'department'</code> as <code style="color:crimson">'Missing'</code>.**
    # # * By the way, the <code style="color:steelblue">.fill_null()</code> function also has an <code style="color:steelblue">inplace=</code> argument, just like the <code style="color:steelblue">.replace()</code> function.
    # # * In the previous project, we just overwrote that column. This time, try using the <code style="color:steelblue">inplace=</code> argument instead.
    # # Fill missing values in department with 'Missing'
    # # LAFP: inplace not supported by dask
    # # df['department'].fill_null('Missing', inplace=True)
    # df['department'] = df['department'].fill_null('Missing')
    # # **First, let's flag <code style="color:steelblue">'last_evaluation'</code> with an indicator variable of missingness.**
    # # * <code style="color:crimson">0</code> if not missing.
    # # * <code style="color:crimson">1</code> if missing.
    # # Let's name the new indicator variable <code style="color:steelblue">'last_evaluation_missing'</code>.
    # # * We can use the <code style="color:steelblue">.isnull()</code> function.
    # # * Also, remember to convert it with <code style="color:steelblue">.astype(int)</code>
    # # Indicator variable for missing last_evaluation
    # df['last_evaluation_missing'] = df.last_evaluation.isnull().astype(int)
    # # **Then, simply fill in the original missing value with <code style="color:crimson">0</code> just so your algorithms can run properly.**
    # # Fill missing values in last_evaluation with 0
    # # LAFP: inplace not supported by dask
    # # df.last_evaluation.fill_null(0, inplace=True)
    # df['last_evaluation'] = df.last_evaluation.fill_null(0)
    # # **Display the number of missing values for each feature (both categorical and numeric) again, just to confirm.**
    # # Display number of missing values by feature
    # print(df.isnull().sum())
    # # For this project, we're going to have an abbreviated version of feature engineering, since we've already covered many tactics in Project 2.
    # # Do you remember the scatterplot of <code style="color:steelblue">'satisfaction'</code> and <code style="color:steelblue">'last_evaluation'</code> for employees who have <code style="color:crimson">'Left'</code>?
    # # **Let's reproduce it here, just so we have it in front of us.**
    # # Scatterplot of satisfaction vs. last_evaluation, only those who have left
    # sns.lmplot(x='satisfaction', y='last_evaluation', data=df[df.status == 'Left'], fit_reg=False)
    # # LAFP: inline not support in iPython, added plt.savefig
    # plt.savefig("fig2.png")
    # # These roughly translate to 3 **indicator features** we can engineer:
    # # **Create those 3 indicator features.**
    # # * Use boolean masks.
    # # * **Important:** For <code style="color:steelblue">'underperformer'</code>, it's important to include <code style="color:steelblue">'last_evaluation_missing' == 0</code> to avoid those originally missing observations that we flagged and filled.
    # # Create indicator features
    # df['underperformer'] = ((df.last_evaluation < 0.6) & (df.last_evaluation_missing == 0)).astype(int)
    # df['unhappy'] = (df.satisfaction < 0.2).astype(int)
    # df['overachiever'] = ((df.last_evaluation > 0.8) & (df.satisfaction > 0.7)).astype(int)
    # # **Next, run this code to check that you created the features correctly.**
    # # The proportion of observations belonging to each group
    # # LAFP: print added
    # print(df[['underperformer', 'unhappy', 'overachiever']].mean())
    # # # 5. Save the ABT
    # # Finally, let's save the **analytical base table**.
    # # Convert status to an indicator variable
    # df['status'] = pd.get_dummies( df.status ).Left
    # # **To confirm we did that correctly, display the proportion of people in our dataset who left.**
    # # The proportion of observations who 'Left'
    # # LAFP: print added
    # print(df.status.mean())
    # # **Overwrite your dataframe with a version that has <span style="color:royalblue">dummy variables</span> for the categorical features.**
    # # * Then, display the first 10 rows to confirm all of the changes we've made so far in this module.
    # # Create new dataframe with dummy features
    # df = pd.get_dummies(df, columns=['department', 'salary'])
    # # Display first 10 rows
    # # LAFP: print added
    # print(df.head(10))
    # # **Save this dataframe as your <span style="color:royalblue">analytical base table</span> to use in later modules.**
    # # * Remember to set the argument <code style="color:steelblue">index=None</code> to save only the data.
    # # Save analytical base table
    # # LAFP: dask outputs the csv into parts (one csv for each partition), printing the result for consistency and ease in benchmarking
    # # df.to_csv('analytical_base_table.csv', index=None)
    # print(df)
    # # Congratulations for making through Project 3's ABT Construction module!
    # # As a reminder, here are a few things you did in this module:
    # # * You cleaned dropped irrelevant observations from the dataset.
    # # * You fixed various structural errors, such as wannabe indicator variables.
    # # * You handled missing data.
    # # * You engineered features by leveraging your exploratory analysis.
    # # * And you created dummy variables before saving the ABT.
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
    writer.writerow(["b31", "4.2GB", "s4.csv","ibis",time2,format(ram_usage),3149])
    file.close()



