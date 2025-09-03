from typing import List
import pandas as pd
import os
import csv
import json
import ast
import inspect
from .static_analysis import analyze_source
from .static_analysis import run_optimized


analysis = {}
work_dir = f'{os.getcwd()}/work'

# Backup routes
pd._read_csv = pd.read_csv
pd._DataFrame = pd.DataFrame


def analyze():
    abs_path = os.path.abspath((inspect.stack()[1])[1])
    file_result=analyze_source(abs_path)
    #separated, now set backend here befor execution but after static analysis
    run_optimized(file_result)


def setup():
    global analysis
    # TODO: fix me! Changed the directory path, should be a configurable parameter
    # with open('sa.json', 'r') as config:
    with open('../sa.json', 'r') as config:
        analysis = json.loads(''.join(config.readlines()))

    # Create work directory
    if not os.path.exists(work_dir):
        os.mkdir(work_dir)
    print(work_dir)


setup()


class DataFrame(pd.DataFrame):
    _id = 1
    _chunksize = 100
    __cols = {

    }

    __unique_no = 1

    _filters = []

    @property
    def unique_no(self):
        prev = self.__unique_no
        self.__unique_no += 1
        return prev

    def _update_col(self, col, data):
        self.__cols[col] = data

    def update(self, cols_to_update, cols_of_df, expression):
        '''
        LHS colums: cols_to_update
        RHS columns: cols_of_df

        Right now materializing all columns to work directory
        '''
        new_file = f'{work_dir}/{"_".join(cols_to_update)}_{self.unique_no}.csv'

        sources = self.__get_source(cols_of_df)

        # Write header row
        with open(new_file, 'w') as file:
            writer = csv.writer(file, quoting=csv.QUOTE_MINIMAL)
            writer.writerow(cols_to_update)

            # TODO: Implement multi file read
            # Find out file names of columns, zip read them and process at once
            for chunk in self.__multichunked_read(sources):
                chunk = expression(chunk)  # .reset_index(drop=True)
                # BUG: Don't know why but csv writer is quoting individual elements of int array

                # chunk.to_csv(new_file, header=False, mode='a',index=False)
                for row in chunk:
                    writer.writerow([row])

        # Update new source of column for subsequent program
        for col in cols_to_update:
            if col in self.__cols.keys():
                self.__cols[col]['path'] = new_file
            else:
                self.__cols[col] = {'path': new_file}

        # print('='*30)
        # print(self.__get_source(cols_of_df))
        sources = self.__get_source(cols_of_df)
        print(cols_to_update, 'updated and written to:', new_file)

        return self

    def explode2(self, column, *args, **kargs):

        # Should read whole dataframe
        sources = self.__get_source(self.__cols)

        def ll(x):
            print(x)
            return ast.literal_eval(x)
            # print(json.loads(x))

        for chunk in self.__multichunked_read(sources):
            chunk[column].apply(ast.literal_eval)
            # print([x for x in chunk[column]]
            print()
            print('=' * 60)
            # print(chunk['Card'].tolist())
            chunk = chunk.explode('Card')
            print(chunk)
            # chunk = super().explode(column, *args, **kargs)
            # print(chunk.info())

            break

    def groupby2(self, column):
        # TODO: Implement groupby
        super
        if type(column) is str:
            column = [column]

        sources = self.__get_source(self.__cols)
        i = 0
        for chunk in self.__multichunked_read(sources):
            chunk = chunk.groupby(column)
            print(chunk.groups)

            i += 1
            if i > 1:
                break

        return self.groupby(column)

    def loc2(self, filter):
        '''Added filter to dataframe'''
        df = DataFrame(self)
        df._filters.append(filter)
        return df

    def print(self):
        sources = self.__get_source(self.__cols)
        result = pd.DataFrame(columns=self.__cols)
        for chunk in self.__multichunked_read(sources, chunksize=1000):
            for filter in self._filters:
                chunk = chunk[filter(chunk)]
            result = pd.concat([result, chunk])

        print('=' * 50)
        print(result)

    def get(self, cols, agg, axis=0):
        '''
        If cols is a 1D list (should have 1 col)
            It should returns single primitive element
            But for now returning pd.Series

        Else returns pd.Series

        There's one more case... pd.get exists and it returns a column (Series). TODO: Handle pd.get
        '''

        # Cases
        # Single column(str) with single (str) or multiple (list(str)) aggregates
        # Single cols (list(str)) with single (str) or multiple (list(str)) aggregates
        # Multiple cols (list(list(str))) with single (str) or multiple (list(str)) aggregates

        if type(cols) is type([]) and len(cols) > 0:
            # Single or multiple cols
            if type(cols[0]) is str and type(agg) is str:
                # 1 column and 1 aggregate
                # print('Will compute aggregate on single column', cols[0], ' with 1 agg', agg)
                result = self.__chunked_read_update(cols, [agg], axis=axis)
                return result.loc[agg]
            elif type(cols[0]) is str and type(agg) is type([]):
                # 1 column and multiple aggregate
                # print('Will compute aggregate on single column', cols[0], ' with multiple agg', agg)
                result = self.__chunked_read_update(cols, agg, axis=axis)
                return result
            elif type(cols[0]) is type([]) and type(agg) is str:
                # Multi column with 1 aggregate
                # print('Multi column operation', *cols, 'single agg', agg)
                result = self.__chunked_read_update(*cols, [agg], axis=axis)
                return result.loc[agg]
            elif type(cols[0]) is type([]) and type(agg) is type([]):
                # print('Multi column operation', *cols, 'multiple agg', agg)
                result = self.__chunked_read_update(*cols, agg, axis=axis)
                return result
            else:
                raise NotImplemented

        elif type(cols) is str:
            # Single col
            if type(agg) is str:
                # print('Single col with 1 agg')
                result = self.__chunked_read_update([cols], [agg], axis=axis)
                return result.loc[agg].values[0]
            elif type(agg) is type([]):
                # print('Single col with multiple agg')
                result = self.__chunked_read_update([cols], agg, axis=axis)
                return result[cols]
            else:
                raise NotImplemented

        raise NotImplemented

    def __get_source(self, cols):
        '''
        Returns file <> columns mapping.
        '''
        grouped = {}
        for key in cols:
            if self.__cols.get(key, None) is None:
                # This column does not exists in out 
                continue

            if grouped.get(self.__cols[key]['path'], None) is None:
                grouped[self.__cols[key]['path']] = [key]
            else:
                grouped[self.__cols[key]['path']].append(key)

        return grouped

    def __multichunked_read(self, sources, chunksize=100, *args, **kargs):
        '''
        Chunked read from multisource
        sources is dict({filename: [col1, col2, col3...]})
        '''
        # DONE: Read from multiple file
        for chunk in zip(
                *[self.__chunked_read(file, usecols=cols, chunksize=chunksize) for file, cols in sources.items()]):
            merged = DataFrame(pd.concat(chunk, axis=1))
            yield merged
            # yield DataFrame(chunk)

        # for chunk in pd.read_csv(csv_file,chunksize=chunksize, *args, **kargs):
        #     yield DataFrame(chunk)

    def __chunked_read(self, csv_file, chunksize=100, *args, **kargs):
        '''Chunked read from single source'''

        for chunk in pd.read_csv(csv_file, chunksize=chunksize, *args, **kargs):
            yield DataFrame(chunk)

    def __chunked_read_update(self, cols, aggregates, axis: int = 0, chunksize=100) -> pd.Series:
        '''
        Read datafile as chunk with cols, computes the aggregates and update the df
        Returns pd.Series | pd.DataFrame | Single element
        '''
        result = None

        # Mapping of aggregate function to chunked aggregate
        # sum of complete data == sum(sum(chunk1), sum(chunk2), ...)
        # But count(all) != count(count(chunk1), count(chunk2), ...)
        #   This should be count(all) == sum(count(chunk1), count(chunk2), ...)
        # 
        # Some aggregates like median, mode, cumsum need more thinking and implementation
        chunk_agg_map = {
            'sum': 'sum',
            'count': 'sum',
            'min': 'min',
            'max': 'max',
            'product': 'product'
        }

        sources = self.__get_source(cols)

        for chunk in self.__multichunked_read(sources, chunksize=chunksize):
            chunked_result = chunk[cols].agg(aggregates, axis=axis)
            if result is None:
                result = chunked_result
            else:
                result = pd.concat([result, chunked_result])

        # TODO: result[cols].agg(aggregates) with only work for simple aggs liks sum, max, min
        # For operations like mean, mode

        # DONE: Fix axis=1 computation
        if axis == 1:
            return result

        data = []
        groups = result.reset_index().groupby('index')
        #          1 2 3
        # 'sum':   ad
        # 'count':

        for group_name in aggregates:
            p = None
            p = groups.get_group(group_name)[cols].agg(chunk_agg_map[group_name])
            data.append(p.values)

        result = pd.DataFrame(index=aggregates, columns=cols, data=data)
        # result = result[cols].agg(aggregates, axis=1)

        # TODO: Cache result
        # for c in cols:
        #     for p in result[c]:
        #         print(c, ' => ' , p)
        #     print()

        return result

    # def __getitem__(self, key):
    #     print ('I will get ', key)

    #     return self[key]

    # def __getattribute__(self, name: str):
    #     # print('I will get atr', name)
    #     # return 0

    #     return super().__getattribute__(name)


# dataframes = {}


def read_csv(csv_path, id, precompute={}, *arg, **karg):
    # print('Reading config file: ', analysis[id])

    # TODO: Store
    # dataframes[id] = {
    #     "csv_path": csv_path,
    #     "id": id,
    #     "precompute": {}
    # }

    # if precompute.get('agg', None):
    #     for column in precompute['agg']:
    #         dataframes[id]['precompute'][column] = {}

    #         for agg in precompute['agg'][column]:
    #             dataframes[id]['precompute'][column][agg] = None

    # print(dataframes)

    # df = DataFrame(pd._read_csv(csv_path,*arg, **karg))
    # Bhu start

    # Bhu end
    df = None

    # Read to get column list
    for chunk in pd._read_csv(csv_path, chunksize=1, *arg, **karg):
        df = DataFrame(chunk)
        break

    df._id = id
    for col in df.columns:
        df._update_col(col, {'path': csv_path})
    print('Read', df.columns)
    return df


# def block1():
#     pass
#     # df["col1_binary"] = (df.col1 > df.quantile(0.1)["col1"])
#     # DataFrame df

#     # df.groupby(["col1_binary"]).agg({"col3":["mean"]})

#     col1_binary = None
#     for chunk in pd.read_csv("../../datasets/18GB.csv",usecols=SO_columns,dtype=SO_c_d_t, chunksize=10000):
#         print('Chunk')
#         chunk['col1_binary'] = (chunk.col1 > chunk.quantile(0.1)['col1'])
#         if col1_binary is None:
#             col1_binary = chunk[['col1_binary']]
#         else:
#             col1_binary = pd.concat([col1_binary, chunk[['col1_binary']]])

#     col1_binary.to_csv('col1_binary.csv')


# def exec(df_id, statement):
#     print('Will execute runtime for', statement)
#     # 2 ways

#     # 1. Parse and execute
#     # print(ast.parse(statement))

#     # 2. Generate executor during SA and just call up the method here
#     # Generated executor can exactly contain which columns should be loaded
#     # May require some identifier
#     block1()
#     # CAN internally use DASK for complex/groupby distributed operation


# def get_aggegate(id: int, col: str, agg: str):
#     '''
#     id: dataframe ID
#     col: column name
#     agg: Aggregate function
#     '''
#     # Check if it exists in our cache
#     # One caveat, If for some reason column values changes then this cache must be invalidated
#     if dataframes.get(id, None) and dataframes[id].get('precompute', None):
#         if dataframes[id]['precompute'].get(col, None):
#             if dataframes[id]['precompute'][col].get(agg, None) is not None:
#                 return dataframes[id]['precompute'][col][agg]

#     # Otherwise compute and cache it
#     return 0


# Overwrite library references
pd.DataFrame = DataFrame
