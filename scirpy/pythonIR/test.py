import pandas as pd

def weighted_rating(x, m=m, C=C):
    v = x['vote_count']
    R = x['vote_average']
    return (v/(v+m) * R) + (m/(m+v) * C)

df = pd.read_csv("data.csv")
df.apply(weighted_rating, axis=1)