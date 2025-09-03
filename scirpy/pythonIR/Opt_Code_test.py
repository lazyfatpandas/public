
import pandas as pd
from time import time
from matplotlib import pyplot as plt
import seaborn as sns
SO_columns = ["salary","recently_promoted","filed_complaint","department"]
df = pd.read_csv('../../../data/b31/s1.csv',usecols=SO_columns)
print(df.department.unique())
print(df.filed_complaint.unique())
print(df.recently_promoted.unique())
print(df.filed_complaint.unique())
print(df.recently_promoted.unique())
sns.countplot(y='department',data=df.compute(live_df=[df]).reset_index())
plt.savefig('fig1.png')
print(df.isnull().sum())
df = pd.get_dummies(df,columns=['department','salary'])
print(df.head(10))
print(df)
pd.flush()
print((time() - start))
