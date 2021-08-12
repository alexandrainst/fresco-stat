from faas import stat
import pandas as pd
import numpy as np
from sklearn import preprocessing

# Connect to a locally running fresco-as-a-service
faas = stat.connect(5556)

# Load dataset as data frame
df = pd.read_csv('real-estate.csv')

# Take relevant columns and normalize
df = df[['X2 house age', 'X3 distance to the nearest MRT station', 
         'X4 number of convenience stores', 'Y house price of unit area']]
scaler = preprocessing.MinMaxScaler().fit_transform(df)
df = pd.DataFrame(scaler, columns=df.columns, index=df.index)

# Party 1 doesn't know the price. Party 2 should replace all values 
# for 'X2 house age', 'X3 distance to the nearest MRT station' and
# 'X4 number of convenience stores' with None.
df[['X2 house age', 'X3 distance to the nearest MRT station', 'X4 number of convenience stores']] = None

# Run linear regression
myData = df.to_numpy()
result = faas.linreg(myData[:,0:3], myData[:,3])

print(result)