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
    # Connect to backend (example: DuckDB in-memory)
    con = ibis.polars.connect()
    t = con.read_csv("/media/bhushan/nvme1/data/vldb/b16/s2.csv", separator=";")

    print(t.limit(5).execute())
    schema = t.schema()
    string_cols = [col for col in schema.names if schema[col].is_string()]
    t = t.mutate(**{
        col: ibis.ifelse(t[col] == "unknown", None, t[col])
        for col in string_cols
    })
    t = t.drop_null()
    t = t.rename({"previous_loan_status":"loan", "loan_status":"y"})

    print(t.limit(5).execute())
    print(t.describe().execute())
    print(t.job.nunique().execute())
    print(t.select(t.job).distinct().execute())
    print(t.group_by(t.job).count().execute())
    #print(df[df['loan_status'] == 'yes']['education'].value_counts(normalize=True))

    yes_result = t[t['loan_status'] == 'yes']['education']
    value_count_res = yes_result.value_counts()
    num_of_cols = len(value_count_res.columns)
    col_name = value_count_res.columns[num_of_cols-1]

    value_normalize = value_count_res.mutate(**{col_name: value_count_res[col_name] / value_count_res[col_name].sum().execute()}).order_by(ibis.desc(col_name))

    print(value_normalize.execute())

    print(
        t.filter(t.loan_status == "yes")
            .group_by(t.education)
            .aggregate(t.education.count())
            # .aggregate(rate=(t.education.count() / t.count()))
            .execute()
    )
    print(
        t.filter(t.loan_status == "yes")
            .group_by(t.previous_loan_status)
            .aggregate(t.previous_loan_status.count())
            .execute()
    )

    branch_1 = ibis.memtable({
        "customer_id": ["1", "2", "3", "4", "5"],
        "first_name": ["Andrew", "Alex", "Sabestian", "Hilary", "Jack"],
        "last_name": ["Ng", "Hales", "Rachaska", "Masan", "Anthony"]
    })
    print(branch_1.execute())
    # Create branch_2 table
    branch_2 = ibis.memtable({
        "customer_id": ["4", "5", "6", "7", "8"],
        "first_name": ["Brain", "Steve", "Kim", "Steve", "Ben"],
        "last_name": ["Alexander", "Jobs", "Jonas", "Fleming", "Richardsan"]
    })
    print(branch_2.execute())
    # Create credit_score table
    credit_score = ibis.memtable({
        "customer_id": ["1", "2", "3", "4", "5", "7", "8", "9", "10", "11"],
        "score": [513, 675, 165, 961, 1080, 1654, 415, 900, 610, 1116]
    })
    print(credit_score.execute())
    # Concatenate branch_1 and branch_2
    df_new = branch_1.union(branch_2)
    print(df_new.execute())
    merged = df_new.inner_join(credit_score, ["customer_id"])
    print(merged.execute())
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
    writer.writerow(["b16", "450MB", "s2.csv","polars",time2,format(ram_usage),"162B"])
    file.close()









