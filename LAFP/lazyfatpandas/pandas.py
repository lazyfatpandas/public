from math import ceil
import os
import inspect
from time import time, sleep
from uuid import uuid4
from typing import Any, Dict, List, Union
from .base import BaseFrame
from .node import Node
from .utils import BackendEngines, save_as_img, set_options, is_row_selection, lafp_len
from .LazyOp import DtAccProps, Meta, LazyOp, LazyOpType
from pandas import read_csv as pandas_read_csv
from patch.static_analysis import analyze_source
from patch.static_analysis import run_optimized
from copy import deepcopy
from collections import defaultdict


BACKEND_ENGINE = BackendEngines.DASK

# Chiranmoy 30-1-24
f_string_dataframes = defaultdict(lambda: [0, FatDataFrame])  # query_node_id -> [use_count, FatDataFrame]
cached_nodes = {}  # needed for dropping cached dataframes, (query_node_id -> node)
last_print_node: [BaseFrame] = None


#changes for deployment
import yaml
import os
config_yaml = os.path.join(os.path.dirname(__file__), 'lafp_config.yaml')

with open(config_yaml, "r") as f:
    config = yaml.safe_load(f)

scirpy_name = config['scirpy_name']
scirpy_location = config['scirpy_location']
scirpy_rel_location = config['scirpy_rel_location']
scirpy_rel_location =os.path.join(os.path.dirname(__file__), scirpy_rel_location)
# jar_name = 'scirpy.jar'
# scirpy_name = "scirpy.jar"
# scirpy_location = "/home/bhushan/intellijprojects/scirpy/out/artifacts/scirpy_jar/"

import subprocess
def is_jar_running():
    # Check if the jar is already running by using the ps aux command
    result = subprocess.run(f"ps aux | grep -i '{scirpy_name}' | grep -v grep", shell=True, stdout=subprocess.PIPE, text=True)
    return result.stdout != ''
    # If the result contains an

def analyze(comp_time=False):
    # Path to your JAR file
    # jar_path = "/home/bhu/intellijprojects/scirpy/out/artifacts/scirpy_jar/scirpy.jar"
    jar_path = scirpy_rel_location +scirpy_name
    # Command to run the JAR file with Java
    command = ['java', '-jar', jar_path]
    print("command",command)

    # Run the JAR file without waiting for it to finish
    if is_jar_running():
        print("SCIRPy JAR is already running.")
        pass
        # print("SCIRPy JAR is already running.")
    else:
        subprocess.Popen(command)
        sleep(1)
        # Continue execution of your Python code here
        print("SCIRPy JAR initiated")
        # os.system("java -jar /home/bhu/intellijprojects/scirpy/out/artifacts/scirpy_jar/scirpy.jar")

    time1=time()
    abs_path = os.path.abspath((inspect.stack()[1])[1])
    file_result = analyze_source(abs_path)
    # sleep(1)
    time2=time()-time1
    if comp_time:
        print("SCRIPy time is:",time2)
    # separated, now set backend here before execution but after static analysis
    run_optimized(file_result)

# def is_jar_running():
#     # Check if the jar is already running by using the ps aux command
#     result = subprocess.run(f"ps aux | grep -i '{jar_name}' | grep -v grep", shell=True, stdout=subprocess.PIPE, text=True)
#     return result.stdout != ''
#     # If the result contains an

# def analyze(comp_time=False):
#     # Path to your JAR file
#     jar_path = "/home/bhushan/intellijprojects/scirpy/out/artifacts/scirpy_jar/scirpy.jar"
#     # Command to run the JAR file with Java
#     command = ['java', '-jar', jar_path]
#
#     # Run the JAR file without waiting for it to finish
#     if is_jar_running():
#         print("SCIRPy JAR is already running.")
#     else:
#         subprocess.Popen(command)
#         sleep(2)
#         # Continue execution of your Python code here
#         print("SCIRPy JAR initiated 2")
#         # os.system("java -jar /home/bhu/intellijprojects/scirpy/out/artifacts/scirpy_jar/scirpy.jar")
#
#     time1=time()
#     abs_path = os.path.abspath((inspect.stack()[1])[1])
#     file_result = analyze_source(abs_path)
#     # sleep(1)
#     time2=time()-time1
#     if comp_time:
#         print("SCRIPy time is:",time2)
#     # separated, now set backend here before execution but after static analysis
#     run_optimized(file_result)


class FatDataFrame(BaseFrame):
    multi_pred = {LazyOpType.CONCAT, LazyOpType.GROUPBY, LazyOpType.str.cat, LazyOpType.PRINT}
    ignore_setattr = {'id', 'pred', 'query_node', 'old_node', 'dt', 'str', 'categorical', 'filename', 'df_shape',
                      'col_dtype', 'is_empty', 'index', 'axii', 'shape', 'loc', 'getitem_parent_df'}

    def __init__(self, pred: List['FatDataFrame'], action: LazyOp = None, shape=None):
        self.id = uuid4().hex
        self.filename = ""
        self.col_dtype = []
        self.axii = []
        self.dt = DtAccess(self)
        self.str = StrAccess(self)
        self.getitem_parent_df: BaseFrame = None
        # self.pred = pred # Chiranmoy 27-1-2024: not used outside __init__, unnecessary pointers may hinder garbage collection

        query_source = []
        # self.df_shape = property(self.__get_shape, self.__set_shape)
        # self._

        if pred is not None and isinstance(pred, FatDataFrame) and action is None:
            raise NotImplementedError('Not implemented correctly. Dask doesnot support pd.DataFrame(...) like pandas does')

        elif pred is not None and isinstance(pred, list) and len(pred):
            query_source = [pred[0].query_node]
            if action.optype in FatDataFrame.multi_pred:  # Chiranmoy 30-1-24: adding if a statement to be safe
                query_source = [src.query_node for src in pred]

        self.query_node: Node = Node(query_source, action)

        # Merge all columns
        for source in query_source:
            for k, v in source.allcols.items():
                self.query_node.allcols[k] = v

        if pred is not None and isinstance(pred, list) and len(pred):
            self.query_node.shape = pred[0].query_node.shape if shape is None else shape

    def __getitem__(self, key: Union[str, int, list, 'FatDataFrame']) -> 'FatDataFrame':
        # ("Get item for",self.query_node)
        # TODO: Check for all cases
        node = FatDataFrame([self], LazyOp(LazyOpType.GET_ITEM, key=key))

        if isinstance(key, FatDataFrame):
            node = FatDataFrame([key], LazyOp(LazyOpType.GET_ITEM, key=key))

        if isinstance(key, slice):
            node.query_node.compatibility_with_dask = False
            # TODO: Implement shape for slice
        elif isinstance(key, tuple) and isinstance(key[0], slice):
            # TODO: Implement shape for slice
            node.query_node.compatibility_with_dask = False
            if len(key) > 1 and isinstance(key[1], str):
                node.query_node.gen_columns(key[1])
            elif len(key) > 1 and isinstance(key[1], list):
                for k in key[1]:
                    node.query_node.gen_columns(k)
            else:
                raise NotImplementedError('Not implemented slice get item for ', key)
        else:
            # if isinstance(key, str): 27/02/2024
            if isinstance(key, str) and len(node.query_node.shape)>0:
                node.query_node.shape = (node.query_node.shape[0], 1)
            elif isinstance(key, list):
                if node.query_node.shape and node.query_node.shape != '':
                    node.query_node.shape = (node.query_node.shape[0], len(key))
            # Key = FatFataDataframe is already updated
            node.query_node.gen_columns(key)

        # Generally, this is not required if key is a list or str
        # But if it's a DataFrame then we need it as we are
        # setting source = key dataframe (to connect graph)
        node.query_node.old_node = self.query_node  # Chiranmoy 20-4-24: df[key] = ...
        node.getitem_parent_df = self

        return node

    def __setitem__(self, key: Union[str, List[str]], value: Union[str, int, float, 'FatDataFrame']):
        # TODO: Check if this works in all cases
        # What if RHS is an integer? Like df = 0
        # commented 2 lines on 31/01 ---for df=0.
        # if not isinstance(value, FatDataFrame):
        #     raise NotImplementedError('RHS must be a FatDataFrame, got', type(value))
        #31.01.25 df=0 start
        old_node = self.query_node
        if not isinstance(value, FatDataFrame):
            # Chiranmoy 6-3-24: no need to put value in sources since it is not a FatDataFrame, otherwise error thrown in topoSort, rest of the logic in executor
            self.query_node = Node([old_node], LazyOp(LazyOpType.SET_ITEM, key=key, value=value))
        else:
            self.query_node = Node([value.query_node, old_node], LazyOp(LazyOpType.SET_ITEM, key=key, value=value.query_node))

        # 31.01.2025 df-0 end
        # print('set-item', key, value, old_node.old_node)
        self.query_node.kill_columns(key)
        self.query_node.old_node = old_node
        self.query_node.allcols = old_node.allcols.copy()
        # TODO: dask does not support df[boolean_df] = df2
        if isinstance(key, str):
            shape = old_node.shape
            rows = shape[0] if len(shape) > 0 else 0
            cols = shape[1] if len(shape) > 1 else 0
            self.query_node.shape = (rows, cols + (key not in self.columns))
            # commented below and added above 3 lines for parquet error
            # self.query_node.shape = (old_node.shape[0], old_node.shape[1] + (key not in self.columns))
            if key not in self.query_node.allcols: self.query_node.columns.append(key)
            self.query_node.allcols[key] = Meta("", 'key', type(value))
        elif  isinstance(key, list):
            self.query_node.shape = (old_node.shape[0], old_node.shape[1] + len([x for x in key if x not in self.columns]))
            for x in key:
                if self.query_node.allcols.get(x, None):
                    if x not in self.query_node.allcols: self.query_node.columns.append(x)
                    self.query_node.allcols[x] = Meta("", 'key', type(value))

    def __make_node(self, *args, **kwargs):
        """
        We won't be able to guess gen/kill set from an expression which is passed as a parameter (like function)
        So program must be rewritten to include a keyword parameter 'genset' and 'killset' representing generated/kill columns at that line
        """
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame(*args, **kwargs)
        node.query_node.gen_columns(genset)
        node.query_node.kill_columns(killset)
        return node

    def __pop_kill_gen(self, kwargs):
        return kwargs.pop('killset', []), kwargs.pop('genset', [])

    def apply(self, func, *args, **kwargs):
        killset, genset = self.__pop_kill_gen(kwargs)
        # Chiranmoy 30-3-24: meta-keyword is only needed by DASK
        if BACKEND_ENGINE != BackendEngines.DASK:
            kwargs.pop("meta", None)
        node = FatDataFrame([self], LazyOp(LazyOpType.APPLY, func, *args, **kwargs))# node = FatDataFrame([self], LazyOp(LazyOpType.APPLY, func=func, *args, **kwargs))
        node.query_node.gen_columns(genset).kill_columns(killset)
        return node

    def explode(self, column, *args, **kwargs):
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.EXPLODE, column=column, *args, **kwargs), shape=(self.query_node.shape[0] * 2, self.query_node.shape[1]))
        node.query_node.gen_columns(genset).gen_columns(column).kill_columns(killset)
        return node

    def drop(self, labels=None, axis=0, columns=None, inplace=False):
        # Force ignoring error
        # args = {'labels': labels, 'axis':axis, 'columns':columns,'inplace':inplace}
        # node = FatDataFrame([self], LazyOp(LazyOpType.DROP, *args, errors='ignore'))

        # Chiranmoy 31-1-24: throwing error when labels is a list, replacing args with kwargs fixed it
        kwargs = {'labels': labels, 'axis': axis, 'columns': columns}

        if not inplace:
            node = FatDataFrame([self], LazyOp(LazyOpType.DROP, **kwargs, errors='ignore'))
        else:
            node = self
            new_query_node = Node([self.query_node], LazyOp(LazyOpType.DROP, **kwargs, errors='ignore'))
            new_query_node.allcols = deepcopy(node.query_node.allcols)
            new_query_node.shape = deepcopy(node.query_node.shape)
            node.query_node = new_query_node

        # This may not be not exactly correct. when axis is 0, it should delete index
        # TODO: Check with labels and axis=0
        # Right now should work for labels: str or list + axis = 1, OR columns = str or list
        if axis == 1:
            columns = labels
        if isinstance(columns, str):
            node.query_node.shape = (node.query_node.shape[0], node.query_node.shape[1] - (1 if columns in node.columns else 0))
        elif isinstance(columns, (list, dict)):
            node.query_node.shape = (node.query_node.shape[0], node.query_node.shape[1] - len([x for x in columns if x in node.columns]))

        # Chiranmoy 16-1-24: remove the dropped columns from df.columns
        dropped = labels if axis == 1 else columns
        if isinstance(dropped, list):
            node.query_node.columns = list(filter(lambda col: col not in dropped, self.columns))
        elif isinstance(dropped, str):
            node.query_node.columns = list(filter(lambda col: col != dropped, self.columns))

        node.query_node.kill_columns(labels).kill_columns(columns)
        return node

    def pop(self, item):
        popped = self[item]
        self.drop(columns=item, inplace=True)
        return popped

    #bhu Sigmod
    def insert(self, loc, column, value):
        # Force ignoring error
        kwargs = {'loc': loc, 'column': column, 'value': value}
        node = self
        new_query_node = Node([self.query_node], LazyOp(LazyOpType.INSERT, **kwargs, errors='ignore'))
        new_query_node.allcols = deepcopy(node.query_node.allcols)
        new_query_node.shape = deepcopy(node.query_node.shape)
        node.query_node = new_query_node
        # TODO: Check with labels and axis=0
        node.query_node.shape = (node.query_node.shape[0], node.query_node.shape[1] + 1)

        # bhu 20-09-24: add the inserted columns column to df.columns
        node.query_node.columns.insert(loc,column)
        return node

    def to_datetime(self, dtype, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.TO_DATETIME, dtype=dtype, **kwargs))

    def dropna(self, axis=0, how='any', subset=None, inplace=False, **kwargs):
        if not inplace:
            killset, genset = self.__pop_kill_gen(kwargs)
            node = FatDataFrame([self], LazyOp(LazyOpType.DROPNA, axis=axis, how=how, subset=subset, **kwargs))
            node.query_node.gen_columns(genset).kill_columns(killset)
            return node

        self.query_node = Node([self.query_node], LazyOp(LazyOpType.DROPNA, axis=axis, how=how, subset=subset, **kwargs))
        # self.__handle_inplace_operation(Node([self.query_node], LazyOp(LazyOpType.DROPNA, axis=axis, how=how, subset=subset, **kwargs)))

    # TODO verify why it was named drop_duplicate---changed to drop_duplicates
    def drop_duplicates(self, *args, **kwargs):
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.DROP_DUPLICATE, *args, **kwargs))
        node.query_node.gen_columns(genset).kill_columns(killset)
        return node

    def groupby(self, by=None, *args, **kwargs):
        # Chiranmoy 3-2-24: parameter 'by' can be FatDataFrame or List[FatDataFrame]
        killset, genset = self.__pop_kill_gen(kwargs)

        if isinstance(by, FatDataFrame):
            node = FatDataFrame([self, by], LazyOp(LazyOpType.GROUPBY, by=by, *args, **kwargs))
        elif isinstance(by, list) and isinstance(by[0], FatDataFrame):
            node = FatDataFrame([self, *by], LazyOp(LazyOpType.GROUPBY, by=by, *args, **kwargs))
        else:
            node = FatDataFrame([self], LazyOp(LazyOpType.GROUPBY, by=by, *args, **kwargs))

        node.query_node.gen_columns(genset).kill_columns(killset).kill_columns(by).gen_columns(by)
        return node

    def sort_values(self, by=None, axis=0, ascending=True, inplace=False, *args, **kwargs):
        if not inplace:
            killset, genset = self.__pop_kill_gen(kwargs)
            # Chiranmoy 6-3-24: Dask doesn't support axis
            # node = FatDataFrame([self], LazyOp(LazyOpType.SORT_VALUES, by=by, axis=axis, ascending=ascending, *args, **kwargs))
            node = FatDataFrame([self], LazyOp(LazyOpType.SORT_VALUES, by=by, ascending=ascending, *args, **kwargs))
            node.query_node.gen_columns(genset).kill_columns(killset).gen_columns(by)
            return node

        self.__handle_inplace_operation(Node([self.query_node], LazyOp(LazyOpType.SORT_VALUES, by=by, ascending=ascending, *args, **kwargs)))

    def fillna(self, value=None, inplace=False, *args, **kwargs):
        if not inplace:
            killset, genset = self.__pop_kill_gen(kwargs)
            node = FatDataFrame([self], LazyOp(LazyOpType.FILLNA, value=value, *args, **kwargs))
            node.query_node.gen_columns(genset).kill_columns(killset)
            return node

        self.__handle_inplace_operation(Node([self.query_node], LazyOp(LazyOpType.FILLNA, value=value, *args, **kwargs)))

    def isna(self, *args, **kwargs):
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.ISNA, *args, **kwargs))
        node.query_node.gen_columns(genset).kill_columns(killset)
        return node

    def nunique(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.NUNIQUE, *args, **kwargs))

    def unique(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.UNIQUE, *args, **kwargs))

    def pivot_table(self, *args, **kwargs):
        # Chiranmoy: Dask expects the columns field to be categorical, used dask.dataframe.DataFrame.categorize to convert the type in executor
        return FatDataFrame([self], LazyOp(LazyOpType.PIVOT_TABLE, *args, **kwargs))

    def agg(self, *args, **kwargs):
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.AGGREGATE, *args, **kwargs))
        node.query_node.gen_columns(genset).kill_columns(killset)
        return node

    # def agg(self, *args, **kwargs):
    #     aggs=['min','max','count','mean']
    #     if len(kwargs)==0:
    #         killset, genset = self.__pop_kill_gen(kwargs)
    #         node = FatDataFrame([self], LazyOp(LazyOpType.AGGREGATE, *args, **kwargs))
    #         node.query_node.gen_columns(genset).kill_columns(killset)
    #         return node
    #     else:
    #         keys=[]
    #         remkeys=[]
    #         vals=[]
    #         remainingkwargs= {}
    #         removedkwargs={}
    #
    #         aggsNode=[]
    #         print(type(kwargs))
    #         for key in kwargs:
    #             keys.append(key)
    #             if type(kwargs[key]) is tuple:
    #                 for aggName in kwargs[key]:
    #                     isremkey=False
    #                     if aggName in aggs:
    #                        isremkey=True
    #                        aggsNode.append(aggName)
    #                     if isremkey: remkeys.append(key)
    #             vals.append(kwargs[key])
    #         print(aggsNode)
    #         print("Remove keys", remkeys)
    #         for key in keys:
    #             print("Key:", key);
    #             if not key in remkeys:
    #                 remainingkwargs[key]=kwargs[key]
    #         # kwargsnew=tuple(remainingkwargs)
    #         killset, genset = self.__pop_kill_gen(kwargs)
    #         node = FatDataFrame([self], LazyOp(LazyOpType.AGGREGATE, aggsNode,*args, **remainingkwargs))
    #         node.query_node.gen_columns(genset).kill_columns(killset)
    #         return node

    def rename(self, index=None, columns=None, inplace=False, *args, **kwargs):
        if not inplace:
            node = FatDataFrame([self], LazyOp(LazyOpType.RENAME, index=index, columns=columns, *args, **kwargs))
        else:
            node = self
            new_query_node = Node([self.query_node], LazyOp(LazyOpType.RENAME, index=index, columns=columns, *args, **kwargs))
            new_query_node.allcols = deepcopy(node.query_node.allcols)
            new_query_node.shape = deepcopy(node.query_node.shape)
            node.query_node = new_query_node

        killset, genset = self.__pop_kill_gen(kwargs)
        node.query_node.gen_columns(genset).kill_columns(killset)
        if isinstance(columns, Dict):
            node.query_node.gen_columns(columns.values()).kill_columns(columns.keys())

        # Chiranmoy 30-1-24: the column name was not changed in the query_node.allcols, an error was thrown when the renamed columns were subsequently used
        for old_name in columns:
            new_name = columns[old_name]
            #  and condition added by bhushan on 8-10-24: keyerror: 0
            if old_name != new_name and old_name in node.query_node.allcols:
                node.query_node.allcols[new_name] = node.query_node.allcols[old_name]
                node.query_node.allcols.pop(old_name)
                node.query_node.columns[node.query_node.columns.index(old_name)] = new_name

        return node

    def set_index(self, *args, inplace=False, **kwargs):
        if not inplace:
            return FatDataFrame([self], LazyOp(LazyOpType.SET_INDEX, keys, *args, **kwargs))
        self.__handle_inplace_operation(Node([self.query_node], LazyOp(LazyOpType.SET_INDEX, *args, **kwargs)))

    def reset_index(self, inplace=False, *args, **kwargs):
        if not inplace:
            return FatDataFrame([self], LazyOp(LazyOpType.RESET_INDEX, *args, **kwargs))
        self.__handle_inplace_operation(Node([self.query_node], LazyOp(LazyOpType.RESET_INDEX, *args, **kwargs)))

    def sort_index(self, *args, inplace=False, **kwargs):
        # Chiranmoy 3-2-24: sort_index not supported in Dask, simulated using reset_index, set_index, sort_values
        if not inplace:
            return FatDataFrame([self], LazyOp(LazyOpType.SORT_INDEX, *args, **kwargs))
        self.__handle_inplace_operation(Node([self.query_node], LazyOp(LazyOpType.SORT_INDEX, *args, **kwargs)))

    def filter(self, items, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.FILTER, items, *args, **kwargs))

    def merge(self, right:'FatDataFrame', on=None, left_on=None, right_on=None, how='inner', *args, **kwargs) -> 'FatDataFrame':
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.MERGE, on=on, left_on=left_on, right_on=right_on, how=how, *args, **kwargs))
        # right must be attached as a source to build correct task graph
        node.query_node.sources.append(right.query_node)
        node.query_node.gen_columns(genset).kill_columns(killset).gen_columns([on, left_on, right_on])
        for col in right.query_node.allcols:
            node.query_node.allcols[col] = right.query_node.allcols[col]

        # TODO: Find better estimation
        m,n = self.query_node.shape[0], right.query_node.shape[0]
        new_rows = int(min(10, max(2, min(m,n)*0.002, 0),min(m,n)*0.01))
        # Basic guess: Each row (in larger relation) will match with 0.2% number of rows of smaller relation. This is capped to (2-10)
        node.query_node.shape = (max(m,n)*new_rows, len(node.columns))
        return node

    def join(self, other, on=None, how='left', *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.JOIN, other, on=on, how=how, *args, **kwargs))

    # bhu
    # TODO check for Dask and rewrite API accordingly
    def append(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.APPEND, *args, **kwargs))

    def unstack(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.UNSTACK, *args, **kwargs))

    def stack(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.STACK, *args, **kwargs))

    def plot(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.PLOT, *args, **kwargs))

    def idxmax(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.IDXMAX, *args, **kwargs))

    def idxmin(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.IDXMIN, *args, **kwargs))

    def value_counts(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.VALUE_COUNTS, *args, **kwargs))

    def corr(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.CORR, *args, **kwargs))

    def map(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.MAP, *args, **kwargs))

    def to_string(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.TO_STRING, *args, **kwargs))

    def replace(self, to_replace=None, value=None, inplace=False, *args, **kwargs):
        if not inplace:
            killset, genset = self.__pop_kill_gen(kwargs)
            node = FatDataFrame([self], LazyOp(LazyOpType.REPLACE, to_replace=to_replace, value=value, *args, **kwargs))
            node.query_node.gen_columns(genset).kill_columns(killset).gen_columns(to_replace).kill_columns(to_replace)
            return node

        self.__handle_inplace_operation(Node([self.query_node], LazyOp(LazyOpType.REPLACE, to_replace=to_replace, value=value, *args, **kwargs)))

    def count(self, axis=0, level=None, numeric_only=False):
        args = {'level': level, 'axis': axis}

        # TODO: Make FatSeries separate from FatDataframe
        # TODO: Implement for level param. It should return DataFrame (shape)
        # BUG: Dask not supporting axis, level, numeric_only
        node = FatDataFrame([self], LazyOp(LazyOpType.COUNT))
        if level is not None:
            print('Warning: Shape is not implemented when `level` is passed')
        else:
            if len(node.query_node.shape) == 1:
                node.query_node.shape = ()
            else:
                node.query_node.shape = (self.query_node.shape[0],) if axis == 1 else (self.query_node.shape[1],)

        return node

    def __build_agg_node(self, optype, axis, args, kwargs):
        node = None

        # BUG: Because series does not have axis param
        if len(self.query_node.shape) >= 1 and self.query_node.shape[1] == 1:
            node = FatDataFrame([self], LazyOp(optype, *args, **kwargs))
        else:
            node = FatDataFrame([self], LazyOp(optype, axis=axis, *args, **kwargs))
        # 27/02/2024 node.query_node.shape error
        # if len(node.query_node.shape) == 1:
        if len(node.query_node.shape) == 1 or len(node.query_node.shape)==0:
            node.query_node.shape = ()
        else:
            node.query_node.shape = (self.query_node.shape[0],) if axis == 1 else (self.query_node.shape[1],)
        return node

    def __build_agg_node_q(self, optype, q, axis, args, kwargs):
        # BUG: Because series does not have axis param
        if len(self.query_node.shape) >= 1 and self.query_node.shape[1] == 1:
            node = FatDataFrame([self], LazyOp(optype, q=q, *args, **kwargs))
        else:
            node = FatDataFrame([self], LazyOp(optype, q=q, axis=axis, *args, **kwargs))

        if len(node.query_node.shape) == 1:
            node.query_node.shape = ()
        else:
            node.query_node.shape = ((self.query_node.shape[0] if len(self.query_node.shape) > 0 else 0),) if axis == 1 else ((self.query_node.shape[1] if len(self.query_node.shape) > 1 else 0),)
            # commented below line and added above line for parquet error in b6
            # node.query_node.shape = (self.query_node.shape[0],) if axis == 1 else (self.query_node.shape[1],)
        return node

    def sum(self, axis=0, *args, **kwargs):
        # 06-02-2024, sum() got an unexpected keyword argument 'axis'
        # return FatDataFrame([self], LazyOp(LazyOpType.SUM, *args, **kwargs))
        # Chiranmoy 7-3-24: axis is supported in dask 2024.1.0, it is needed for dias 09
        return self.__build_agg_node(LazyOpType.SUM, axis, args, kwargs)

    def max(self, axis=0, *args, **kwargs):
        return self.__build_agg_node(LazyOpType.MAX, axis, args, kwargs)

    def min(self, axis=0, *args, **kwargs):
        return self.__build_agg_node(LazyOpType.MIN, axis, args, kwargs)

    def mode(self, axis=0, *args, **kwargs):
        return self.__build_agg_node(LazyOpType.MODE, axis, args, kwargs)

    def mean(self, axis=0, *args, **kwargs):
        return self.__build_agg_node(LazyOpType.MEAN, axis, args, kwargs)

    def quantile(self, q=0.5, axis=0, *args, **kwargs):
        return self.__build_agg_node_q(LazyOpType.QUANTILE, q, axis, args, kwargs)

    def between(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.BETWEEN, *args, **kwargs))

    def isin(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.ISIN, *args, **kwargs))

    def to_csv(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.TO_CSV, *args, **kwargs))

    def to_dict(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.TO_DICT, *args, **kwargs))

    def to_list(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.TO_LIST, *args, **kwargs))

    tolist = to_list  # Chiranmoy: tolist and to_list are alias

    def __and__(self, other):
        return self.__binaryop(other, LazyOp(LazyOpType.BIT_AND))

    def __or__(self, other):
        return self.__binaryop(other, LazyOp(LazyOpType.BIT_OR))

    def __add__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.ADD, *args, **kwargs))

    def __lt__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.LT, *args, **kwargs))

    def __gt__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.GT, *args, **kwargs))

    def __ge__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.GE, *args, **kwargs))

    def __le__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.LE, *args, **kwargs))

    def __eq__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.EQ, *args, **kwargs))

    def __ne__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.NE, *args, **kwargs))

    def __add__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.ADD, *args, **kwargs))

    def __sub__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.SUB, *args, **kwargs))

    def __mul__(self, other, *args, **kwargs):
        return self.__binaryop(other, LazyOp(LazyOpType.MUL, *args, **kwargs))

    def __truediv__(self, other, *args, **kwargs):
        # __div__ is only for Python 2.x
        return self.__binaryop(other, LazyOp(LazyOpType.DIV, *args, **kwargs))

    # __repr__ is used by print and intellij debugger
    def __repr__(self):
        """
        Chiranmoy 30-1-24
        Update 6-3-24: return statement change for df.info
        """
        node = self.query_node
        if node.cache is not None:
            return node.cache if isinstance(node.cache, str) else repr(node.cache)
        return repr(node.action)  # placeholder

    def __format__(self, format_spec):
        # Chiranmoy 30-1-24: Called when f-string or .format() is used
        node = self.query_node
        if node.cache is not None:
            return repr(node.cache)

        f_string_dataframes[node.get_id()][0] += 1
        f_string_dataframes[node.get_id()][1] = self

        # NOTE: $_# is assumed to be special escape sequence, not used by users
        return f'$_#{node.get_id()}|{format_spec}$_#'

    def __len__(self):
        """
        Chiranmoy 14-5-24: __len__ is called when len(df) is used
        since len func must return an int, we return the estimated length here
        for accurate length use df.len (a LAFP specific API)
        """
        return self.query_node.shape[0]

    # Chiranmoy 16-3-24: For loops should be handled in SCIRPy
    # def __iter__(self):
    #     raise "ForceCompute in for loop, not Implemented in SCIRPy"
    #     return iter(self.compute())

    def sample(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.SAMPLE, *args, **kwargs))

    def copy(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.COPY, *args, **kwargs))

    def duplicated(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.DUPLICATED, *args, **kwargs))

    def any(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.ANY, *args, **kwargs))

    def isnull(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.ISNULL, *args, **kwargs))

    def __binaryop(self, other: 'FatDataFrame', action: LazyOp):
        node = FatDataFrame([self], action)

        if isinstance(other, FatDataFrame):
            # node.pred.append(other)  # Never Used
            node.query_node.sources.append(other.query_node)
        else:
            # Assuming it's a literal...str int
            # assert isinstance(other, (str, int, float)), 'Not implemented for other literals'
            query_node = Node([], LazyOp(LazyOpType.LITERAL, other))
            node.query_node.sources.append(query_node)

        return node

    # bhu
    def to_frame(self, *args, **kwargs):
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.TO_FRAME, *args, **kwargs))
        node.query_node.gen_columns(genset).kill_columns(killset)
        return node

    def compute(self, prod=True, column_selection=None, row_selection=None, merge_filter=None, remove_deadcode=None,
                persist_dataframes=None, dead_df_removal=None, default_to_pandas=None, output_hash=None, live_df=None):
        """
        Optimizes and collects results.
        When `prod` is `True`, it won't create any graph file
        Keyword arguments:

            column_selection    = False [Default] - Perform column selection optimization
            row_selection       = True [Default] - Perform row selection (moving filter operations) optimization
            merge_filter        = True [Default] - Merge filter operations whenever possible
            remove_deadcode     = False [Default] - Merge filter operations whenever possible
            persist_dataframes    = True [Default] - Check for common nodes with the DFs passed in live_df, persist common nodes
            dead_df_removal       = True [Default] - Remove persisted DataFrames once the usefulness ends.
            live_df - DataFrames live in the program, check for common sub-expressions

        Return Type: Pandas DataFrame | List(Pandas DataFrame)
            Pandas DataFrame when called as df.compute()
            Tuple(Pandas DataFrame) when called using pd.compute(args)

        Chiranmoy 30-1-24: Added support for persisting common nodes, caching and removing dead dataframes
        """

        if prod:
            return compute(self, prod=prod, column_selection=column_selection, row_selection=row_selection,
                           merge_filter=merge_filter, remove_deadcode=remove_deadcode, persist_dataframes=persist_dataframes,
                           dead_df_removal=dead_df_removal, default_to_pandas=default_to_pandas, output_hash=output_hash,
                           live_df=live_df)


        set_options(column_selection=column_selection, row_selection=True, merge_filter=False,
                          remove_deadcode=remove_deadcode, persist_dataframes=persist_dataframes, dead_df_removal=dead_df_removal,
                          default_to_pandas=default_to_pandas, output_hash=output_hash, __default=False)

        t1 = 0
        t2 = 0
        optimization_time = 0
        print('Non prod')
        # save_as_img(self.query_node, True, 'out_with_old_node.png')
        save_as_img(self.query_node, False, 'out.png')
        optimized_node = self.query_node.optimize()
        save_as_img(optimized_node, False, 'out-after.png')

        start = time()
        # res1 = self.query_node.to_dask()
        # op = self.query_node.strong_lively_test()
        # save_as_img(op, False, 'out-op.png')

        t1 = time() - start
        print(t1)
        return
        return self.query_node.to_dask()
        # self.query_node.clear_results()
        start = time()
        # optimized_node = self.query_node.optimize()
        optimized_node = self.query_node.optimize()
        optimization_time = time() - start
        save_as_img(optimized_node, False, 'out_after.png')
        # save_as_img(optimized_node, True, 'out_after_old.png')
        start = time()
        res2 = optimized_node.to_dask()

        t2 = time() - start
        # if isinstance(res1, (pd.DataFrame, pd.Series)) and isinstance(res2, (pd.DataFrame, pd.Series)):
        #     print('Correctness1:', res1.equals(res2))
        #     # print(res1.columns)
        #     # print(res2.columns)
        # elif type(res1) == type(res2):
        #     print('Correctness2', res1 == res2)
        # else:
        #     print('Incorrect:')

        print('Original', t1, 'Optimized', t2, 'Processing time', optimization_time)
        # print(res1)
        # print('-'*40)
        # print(res2)
        return res2
        # return self.query_node.to_pandas_code()

    def __getattr__(self, attr: str) -> Any:
        # Chiranmoy 27-3-24: special case for get_dummies, new columns are created by get_dummies
        if self.query_node.action.optype == LazyOpType.GET_DUMMIES and self.query_node.allcols.get(attr, None) is None:
            self.query_node.allcols[attr] = Meta("", attr, bool)
        if self.query_node.allcols.get(attr, None) is not None:
            return self.__getitem__(attr)

        return super().__getattribute__(attr)

    def __setattr__(self, __name: str, __value: Any) -> None:
        if __name in FatDataFrame.ignore_setattr:
            return super().__setattr__(__name, __value)

        # query_node must be set before accessing it
        # Thats why if __name is present in whitelist (ignore_setattr) then call default setattr
        if self.query_node.allcols.get(__name, None) is not None:
            return self.__setitem__(__name, __value)

        return super().__setattr__(__name, __value)

    def head(self, *args, **kwargs):
        # Chiranmoy 19-3-24: informational API like head, tail, info and describe should not generate/kill liveness
        # killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.HEAD, compute=False, *args, **kwargs))
        # node.query_node.gen_columns(genset).kill_columns(killset).gen_columns(self.columns)
        return node

    def tail(self, *args, **kwargs):
        # Chiranmoy 19-3-24: informational API like head, tail, info and describe should not generate/kill liveness
        # killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.TAIL, compute=False, *args, **kwargs))
        # node.query_node.gen_columns(genset).kill_columns(killset).gen_columns(self.columns)
        return node

    def describe(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.DESCRIBE, *args, **kwargs))

    def nlargest(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.NLARGEST, *args, **kwargs))

    def nsmallest(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.NSMALLEST, *args, **kwargs))

    def round(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.ROUND, *args, **kwargs))

    def info(self, *args, **kwargs):
        """
        Chiranmoy: info by default prints directly to screen, the problem is because backend (Dask) does the printing
        the print ordering is messed up
        Solution: Provide a buffer where the data is dumped, and we can print it later
        NOTE: output of info may differ from that of Pandas since some columns may not be fetched
        """
        killset, genset = self.__pop_kill_gen(kwargs)
        node = FatDataFrame([self], LazyOp(LazyOpType.INFO, *args, **kwargs))
        node.query_node.gen_columns(genset).kill_columns(killset).gen_columns(self.columns)
        lazyPrint(node)

    def memory_usage(self, deep=False, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.MEMORY_USAGE, deep=deep, *args, **kwargs))

    def mean(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.MEAN, *args, **kwargs))

    def droplevel(self, level, axis=0, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.DROPLEVEL, level=level, axis=axis, *args, **kwargs))

    def query(self, q, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.QUERY, q, *args, **kwargs))

    def astype(self, dtype):
        return FatDataFrame([self], LazyOp(LazyOpType.ASTYPE, dtype))

    def ewm(self, *args, **kwargs):
        return FatDataFrame([self], LazyOp(LazyOpType.EWM, *args, **kwargs))

    @property
    def values(self):
        # return self.compute()  # Chiranmoy 30-1-24: automatically called by intellij debugger
        return None

    @property
    def size(self):
        """
        Chiranmoy 12-3-24
        1. df.size returns the number of elements in object
        2. df.groupby(...).size() returns the size of each group
        """
        class GroupBySize:
            def __init__(self, df):
                self.df = df

            def __call__(self, *args, **kwargs):
                return FatDataFrame([self.df], LazyOp(LazyOpType.SIZE, *args, **kwargs))

        if self.query_node.action.optype == LazyOpType.GROUPBY:
            return GroupBySize(self)
        return FatDataFrame([self], LazyOp(LazyOpType.CELL_COUNT))

    @property
    def shape(self):
        # NOTE: "return FatDataFrame([self], LazyOp(LazyOpType.SHAPE))" causes debugger to glitch
        #  use "return self.query_node.shape" for debugging and "return FatDataFrame(...)" for matching outputs

        return self.query_node.shape
        # return FatDataFrame([self], LazyOp(LazyOpType.SHAPE))

    # @shape.setter
    # def shape(self, value):
    #     self.query_node.shape = value

    @property
    def len(self):
        """
        Chiranmoy 14-5-24: LAFP property to replace len(df)
        len(df) should return an integer (python constraint)
        for LAFP it is impossible to return the actual len without forcing compute
        """
        return FatDataFrame([self], LazyOp(LazyOpType.LEN))

    @property
    def index(self):
        return FatDataFrame([self], LazyOp(LazyOpType.INDEX))

    @index.setter
    def index(self, value):
        query_node = Node([self.query_node], LazyOp(LazyOpType.INDEX_RENAME, index=value))
        query_node.shape = self.query_node.shape
        self.query_node = query_node

    @property
    def columns(self):
        """
        Chiranmoy 16-3-24: return a deepcopy when metadata is read
        this way, if the metadata is updated later in the program, lazyPrint is not affected since it has its own copy
        """
        # return list(self.query_node.allcols.keys())  # order compromised
        return deepcopy(self.query_node.columns)

    @columns.setter
    def columns(self, new_names):
        """
        Chiranmoy 30-1-24
        df.columns = [...], is used to rename all columns together, thus the column ordering must be preserved
        """
        new_allcols = {}
        old_names = self.query_node.columns
        old_allcols = self.query_node.allcols

        assert len(old_names) == len(new_names), f'Length mismatch: Expected {len(old_names)} names, have {len(new_names)} names'

        for i, new_name in enumerate(new_names):
            old_name = old_names[i]
            new_allcols[new_name] = old_allcols[old_name]

        query_node = Node([self.query_node], LazyOp(LazyOpType.COL_RENAME))
        query_node.columns = new_names
        query_node.allcols = new_allcols
        query_node.shape = self.query_node.shape

        self.query_node = query_node

    @property
    def dtypes(self):
        types = []
        for col in self.columns:
            # print(dtypes.column, '\t', dtypes.dtype)
            types.append(self.query_node.allcols[col].dtype)

        return types

    @property
    def axes(self):
        raise NotImplementedError()
        self.axii.append(self.index)
        self.axii.append(self.columns)
        return self.axii

    @property
    def empty(self):
        return len(self.query_node.shape) == 0 or (self.query_node.shape[0] == 0)

    @property
    def loc(self):
        return IndexSlice(self, LazyOpType.LOC)

    @property
    def iloc(self):
        return IndexSlice(self, LazyOpType.ILOC)

    @property
    def at(self):
        return IndexSlice(self, LazyOpType.AT)

    @property
    def iat(self):
        return IndexSlice(self, LazyOpType.IAT)

    def __handle_inplace_operation(self, inplace_node: Node):
        """
        Chiranmoy 14-4-24: supporting inplace
        """
        # syntax of format df[key].replace(..., inplace=True), i.e., the source is GET_ITEM but not a filter
        if self.query_node.action.optype == LazyOpType.GET_ITEM and not is_row_selection(self.query_node):
            base_dataframe = self.getitem_parent_df  # df in df[key]
            old_node = base_dataframe.query_node
            base_dataframe.query_node = Node([inplace_node, old_node], LazyOp(LazyOpType.SET_ITEM, key=self.query_node.action.kwargs['key'], value=inplace_node))
            base_dataframe.query_node.old_node = old_node

        self.query_node = inplace_node


def DataFrame(*args, **kwargs):
    """
    Chiranmoy 30-1-24
    """
    df = FatDataFrame([], LazyOp(LazyOpType.DATAFRAME))

    if BACKEND_ENGINE == BackendEngines.MODIN:
        from modin.pandas import DataFrame
    else:
        from pandas import DataFrame

    df.query_node.cache = DataFrame(*args, **kwargs)
    df.query_node.shape = df.query_node.cache.shape
    df.query_node.columns = list(df.query_node.cache.columns)
    for col in df.query_node.columns:
        df.query_node.allcols[col] = Meta("", col, df.query_node.cache.dtypes[col])

    return df

def pivot_table(data, *args, **kwargs):
    # if BACKEND_ENGINE != BackendEngines.DASK:
    # check if separate implementation required for dask as dask supports Dataframe.pivot_table
        # Chiranmoy: Dask expects the columns field to be categorical, used dask.dataframe.DataFrame.categorize to convert the type in executor
    sources = [data] if isinstance(data, FatDataFrame) else []
    node = FatDataFrame(sources, LazyOp(LazyOpType.SERIES, *args, **kwargs))
    node.query_node.shape = len(data)
    return node


def Series(data=None, *args, **kwargs):
    # Chiranmoy 9-4-24
    kwargs['data'] = data
    sources = [data] if isinstance(data, FatDataFrame) else []
    node = FatDataFrame(sources, LazyOp(LazyOpType.SERIES, *args, **kwargs))
    node.query_node.shape = len(data)
    return node


def get_dummies(data: FatDataFrame, prefix=None, prefix_sep='_', **kwargs):
    if data.query_node.shape[1] == 1:
        prefix = prefix_sep = ''  # for series, no need to add prefix
    return FatDataFrame([data], LazyOp(LazyOpType.GET_DUMMIES, prefix, prefix_sep, **kwargs))

def __read_meta(filepath, *args, **kwargs):
    pass
    if not os.path.isfile(f'{filepath}.lafp.json'):
        print("Meta data does not exists")
        return args, kwargs
    if  os.path.getmtime(f'{filepath}.lafp.json') <= os.path.getmtime(filepath):
        print("Meta data file is older than actual data file")
        return args, kwargs
    
    print("Process meta file", f'{filepath}.lafp.json')
    import json
    with open(f'{filepath}.lafp.json') as metafile:
        metadata = json.load(metafile)
        for property in metadata:
            # Merge every property in meta file with user passed arguments
            # Note: This removes duplicates
            # Note: Meta file will take precendence for each property
            if isinstance(metadata.get(property), (list, set, tuple)):
                kwargs[property] = type(metadata.get(property))(set().union(kwargs.get(property, []), metadata.get(property)))
            elif isinstance(metadata.get(property), dict):
                kwargs[property] = {**kwargs.get(property, {}), **metadata.get(property)}
            else:
                kwargs[property] = metadata.get(property)

        print('Identified properties', kwargs)
        return args, kwargs



def read_csv(filepath_or_buffer, *args, **kwargs):
    # print('Reading config file: ', analysis[id])
    # print('----- pd.read_csv -----', filepath)
    from glob import glob
    all_files = glob(filepath_or_buffer) if isinstance(filepath_or_buffer, str) else filepath_or_buffer
    print(all_files)
    columns_meta = []
    columns_ordered = None  # column list ordered
    # Read to get column list

    # Reading single file is enough for now
    # In future we can read all meta files and build a common repository
    args, kwargs =__read_meta(all_files[0], *args, **kwargs)

    delimiter = kwargs['delimiter'] if 'delimiter' in kwargs else ','
    encoding = kwargs['encoding'] if 'encoding' in kwargs else 'utf-8'
    sample_nrows = 500

    for chunk in pandas_read_csv(filepath_or_buffer=all_files[0], chunksize=sample_nrows, delimiter=delimiter, encoding=encoding):
        columns_ordered = list(chunk.columns)
        for column in chunk:
            columns_meta.append([column, chunk[column].dtype, 0, 0])
        break
    
    from csv import reader as csv_reader
    with open(all_files[0], 'r', encoding=encoding) as f:
        reader = csv_reader(f, delimiter=delimiter)
        for index, line in enumerate(reader):
            if index == 0:
                continue

            for (k,v) in enumerate(columns_meta):
                columns_meta[k][2] += len(line[k])
                columns_meta[k][3] += 1

            if index > sample_nrows:
                break
    
    df = FatDataFrame([], LazyOp(LazyOpType.READ_CSV, filepath_or_buffer=all_files, *args, **kwargs))
    df.query_node.columns = columns_ordered
    avg_row_size = len(columns_meta) # Adds number of comma's
    for (column, dtype, total_len, size) in columns_meta:
        avg_row_size += ceil((total_len/size))
        df.query_node.update_col(column, Meta(all_files, column, dtype))
        df.query_node.kill_columns(column)

    total_file_size = sum([os.stat(f).st_size for f in all_files])
    # print(total_file_size/(1024*1024))
    # print(avg_row_size)
    # print('Estimated rows', ceil(total_file_size/avg_row_size))

    # Update the estimate
    df.query_node.shape = (ceil(total_file_size/avg_row_size), len(columns_meta))
    # print(df.query_node.shape)
    # df.df_shape = (100, 100)
    if kwargs.get('index_col', None) is not None:
        df.query_node.gen_columns(kwargs.get('index_col'))
    elif kwargs.get('index', None) is not None:
        df.query_node.gen_columns(kwargs.get('index'))

    return df


def read_parquet(filepath_or_buffer, engine='auto', columns=None, storage_options={}, use_nullable_dtypes=False, **kwargs):
    from glob import glob
    filepath_or_buffer = glob(filepath_or_buffer) if isinstance(filepath_or_buffer, str) else filepath_or_buffer
    print(filepath_or_buffer)
    from pyarrow.parquet import ParquetFile
    import pyarrow as pa

    # Partially read the files
    pf = ParquetFile(filepath_or_buffer[0])
    first_100_rows = next(pf.iter_batches(batch_size = 100))
    df = pa.Table.from_batches([first_100_rows]).to_pandas()
    df_columns = {}
    # Read to get column list
    for column in df:
        df_columns[column] = df[column].dtype

    if columns is None:
        columns = []
    elif isinstance(columns, str):
        columns = [columns]

    df = FatDataFrame([], LazyOp(LazyOpType.READ_PARQUET, filepath_or_buffer=filepath_or_buffer, engine=engine, columns=columns, storage_options=storage_options, use_nullable_dtypes=use_nullable_dtypes, **kwargs))
    # added for parquet as it was not getting column names as defined earlier
    df.query_node.columns = list(pf.schema.names)
    num_rows = pf.metadata.num_rows
    num_columns = pf.metadata.num_columns
    df.query_node.shape = (num_rows,num_columns)
    #     end added for parquet
    for column, dtype in df_columns.items():
        df.query_node.update_col(column, Meta(filepath_or_buffer, column, dtype))
        df.query_node.kill_columns(column)

    return df


# General functions
def merge(left: FatDataFrame, right: FatDataFrame, on=None, left_on=None, right_on=None, *args, **kwargs):
    return left.merge(right, on=on, left_on=left_on, right_on=right_on, *args, **kwargs)


def to_numeric(arg, errors="raise", downcast=None):
    # if isinstance(arg, FatDataFrame):
    return FatDataFrame([arg], LazyOp(LazyOpType.TO_NUMERIC, errors=errors, downcast=downcast))


def to_datetime(*args, **kwargs):
    # changed for dias netflix
    # return FatDataFrame([self], LazyOp(LazyOpType.TO_DATETIME, args,kwargs))
    return FatDataFrame(args[0], LazyOp(LazyOpType.CONCAT, *args[1:], **kwargs))


def concat(*args, **kwargs):
    return FatDataFrame(args[0], LazyOp(LazyOpType.CONCAT, *args[1:], **kwargs))


def to_timedelta(*args,**kwargs):
    return FatDataFrame(args[0], LazyOp(LazyOpType.TO_TIMEDELTA, *args[1:], **kwargs))


def compute(*args, prod=True, column_selection=None, row_selection=None, merge_filter=None, remove_deadcode=None,
            persist_dataframes=None, dead_df_removal=None, default_to_pandas=None, live_df=None, output_hash=None):
    """
    Assumption: All args are of type 'FatDataFrame'
    Return Type:    len(args) == 0:  returns None
                                 1:  returns DataFrame
                                 2+: returns List[DataFrame]
    Side Effect: Pending print statements are processed.
    Chiranmoy 30-1-24: Added support for lazy print, persisting common nodes, caching and removing dead dataframes
    """
    global last_print_node


    if len(args) == 0:
        return None

    if not all([isinstance(x, FatDataFrame) for x in args]):
        raise NotImplementedError('Not implemented for non FatDataFrame object')

    # set the flags for this call to compute, wherever the flags are needed, import utils.options
    options = set_options(column_selection=column_selection, row_selection=row_selection, merge_filter=merge_filter,
                          remove_deadcode=remove_deadcode, persist_dataframes=persist_dataframes, dead_df_removal=dead_df_removal,
                          default_to_pandas=default_to_pandas, output_hash=output_hash, __default=False)

    compute_node = FatDataFrame([*args], LazyOp(LazyOpType.COMPUTE))

    # evaluate lazy prints when a dataframe is force computed
    if last_print_node is not None:
        compute_node.query_node.sources.append(last_print_node.query_node)
        last_print_node = None

    if options.persist_dataframes:
        compute_node.query_node.mark_live_subexpressions(live_df, cached_nodes, BACKEND_ENGINE)

    # save_as_img(compute_node.query_node, True, f'before_{compute_node.query_node.get_id()[:5]}.png')

    optimized_node = compute_node.query_node.optimize()

    # save_as_img(optimized_node, True, f'after_{optimized_node.get_id()[:5]}.png')
    # bhushan 10-10-24
    # save_as_img(optimized_node, False, 'out.png')

    if BACKEND_ENGINE == BackendEngines.MODIN:
        result = optimized_node.to_modin()
    elif BACKEND_ENGINE == BackendEngines.PANDAS:
        result = optimized_node.to_pandas()
    else:
        result = optimized_node.to_dask()

    # Chiranmoy: save the computed dataframes in the node.cache field, NOTE: not saving pandas dataframe
    # if options.persist_dataframes:
    #     for index, node in enumerate(compute_node.query_node.sources):
    #         cached_nodes[node.get_id()] = node
    #         node.cache = result[index]

    if options.dead_df_removal:
        _remove_dead_dataframes(live_df)

    if compute_node.query_node.sources[-1].action.optype == LazyOpType.PRINT:
        result.pop()  # no need to return a result for print nodes
    return result[0] if len(result) == 1 else result


def _remove_dead_dataframes(live_df):
    """ Chiranmoy 17-4-24: removes dead dataframes from cache """
    live_nodes_ids = set(live_node_id for df in live_df for live_node_id in df.query_node.get_live_nodes_ids()) if live_df else set()

    for node_id in list(cached_nodes.keys()):
        if node_id not in live_nodes_ids:
            cached_nodes[node_id].cache = None
            cached_nodes[node_id].result = None
            cached_nodes.pop(node_id)


def lazyPrint(*print_args, sep=' ', end='\n'):
    """
    Chiranmoy 1-4-24: lazy print as nodes in the task graph
    """
    global last_print_node
    sources: List[FatDataFrame] = [] if last_print_node is None else [last_print_node]
    print_args = list(print_args)

    # find dataframes used in the print
    for index, arg in enumerate(print_args):
        if isinstance(arg, FatDataFrame):
            sources.append(arg)
            print_args[index] = arg.query_node

        elif isinstance(arg, list):
            for i, ele in enumerate(arg):
                if isinstance(ele, FatDataFrame):
                    sources.append(ele)
                    print_args[index][i] = ele.query_node

        elif isinstance(arg, dict):
            for key in arg:
                if isinstance(arg[key], FatDataFrame):
                    sources.append(arg[key])
                    print_args[index][key] = arg[key].query_node

        elif isinstance(arg, str) and '$_#' in arg:
            arr, i = arg.split('$_#'), 1
            while i < len(arr):
                key, format_spec = arr[i].split('|')
                sources.append(f_string_dataframes[key][1])
                f_string_dataframes[key][0] -= 1
                if f_string_dataframes[key][0] == 0:
                    f_string_dataframes.pop(key)
                i += 2

    node = FatDataFrame(sources, LazyOp(LazyOpType.PRINT, print_args=print_args, sep=sep, end=end))
    last_print_node = node

    from lazyfatpandas.utils import default_options
    if default_options.lazy_print is False:
        flush()


def flush(prod=True, column_selection=None, row_selection=None, merge_filter=None, remove_deadcode=None, persist_dataframes=None, dead_df_removal=None, default_to_pandas=None, output_hash=None):
    """
    Chiranmoy 4-1-24: evaluate remaining lazyPrint nodes
    """
    global last_print_node
    print_node = last_print_node
    last_print_node = None

    if print_node is not None:
        compute(print_node, prod=prod, column_selection=column_selection, row_selection=row_selection, merge_filter=merge_filter,
                remove_deadcode=remove_deadcode, persist_dataframes=persist_dataframes, dead_df_removal=dead_df_removal, default_to_pandas=default_to_pandas, output_hash=output_hash)


# To make uniform API
# DataFrame = FatDataFrame # Chiranmoy 30-1-24: commented because it's conflicting with pd.DataFrame API
# pd.DataFrame.compute = lambda x: x
# pd.Series.compute = lambda x: x


# TODO
# 1. Simplify graph API (delete operation)
# 2. Manage metadata store and add properties like
#       index, columns, dtypes,
#       values, axes, ndim, size, shape, empty
# 4. Testcases and benchmark

class DtAccess:
    def __init__(self,parent):
        self.parent=parent
        # self.add_properties()

    @property
    def minute(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.minute, *args, **kwargs))

    @property
    def seconds(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.seconds, *args, **kwargs))

    @property
    def dayofweek(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.dayofweek, *args, **kwargs))

    @property
    def year(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.year, *args, **kwargs))

    @property
    def hour(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.hour, *args, **kwargs))

    @property
    def date(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.date, *args, **kwargs))

    @property
    def time(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.time, *args, **kwargs))

    @property
    def time(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.timez, *args, **kwargs))

    @property
    def day(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.day, *args, **kwargs))

    @property
    def month(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.month, *args, **kwargs))

    @property
    def month_name(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.month_name, *args, **kwargs))

    @property
    def nanosecond(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.nanosecond, *args, **kwargs))

    @property
    def week(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.week, *args, **kwargs))

    @property
    def second(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.second, *args, **kwargs))

    @property
    def microsecond(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.microsecond, *args, **kwargs))

    @property
    def day_of_week(self, *args,**kwargs): return FatDataFrame([self.parent], LazyOp(LazyOpType.dt.day_of_week, *args, **kwargs))

    def add_properties(self):
        '''
        Dynamicall adds properties
        '''
        for value in DtAccProps:
            setattr(DtAccess, value.value, property(lambda x: FatDataFrame([x.parent], LazyOp(value))))


class StrAccess:
    def __init__(self, parent):
        self.parent = parent

    def slice(self, *args, **kwargs):
        return FatDataFrame([self.parent], LazyOp(LazyOpType.str.slice, *args, **kwargs))

    def split(self, *args, **kwargs):
        return FatDataFrame([self.parent], LazyOp(LazyOpType.str.split, *args, **kwargs))

    def len(self, *args, **kwargs):
        return FatDataFrame([self.parent], LazyOp(LazyOpType.str.len, *args, **kwargs))

    def lower(self, *args, **kwargs):
        return FatDataFrame([self.parent], LazyOp(LazyOpType.str.lower, *args, **kwargs))

    def contains(self, *args, **kwargs):
        return FatDataFrame([self.parent], LazyOp(LazyOpType.str.contains, *args, **kwargs))

    def replace(self, *args, **kwargs):
        return FatDataFrame([self.parent], LazyOp(LazyOpType.str.replace, *args, **kwargs))

    def cat(self, others=None, sep=None, na_rep=None):
        kwargs = {'others': others, 'sep': sep, 'na_rep': na_rep}
        return FatDataFrame([self.parent, others], LazyOp(LazyOpType.str.cat, **kwargs))

    def startswith(self, *args, **kwargs):
        return FatDataFrame([self.parent], LazyOp(LazyOpType.str.startswith, *args, **kwargs))

    def __getitem__(self, key, *args):
        return self.slice(key.start, key.stop, key.step)


class IndexSlice:
    """
    Chiranmoy 30-1-24: support for loc, iloc, at, iat APIs
    iloc is deprecated in Pandas 2.2.0

    Dask issues:
    Assignment using loc like: df.loc['cobra'] = 10 not supported
    iloc syntax must be of format df.iloc[:, column_indexer] so syntax like df.iloc[0] and df.iloc[[0, 1]]
    Doesn't support at and iat, trying to simulate using other APIS
    """
    def __init__(self, parent, op_type):
        self.parent = parent
        self.op_type = op_type

    def __getitem__(self, key):
        if self.op_type == LazyOpType.LOC or self.op_type == LazyOpType.ILOC:
            if isinstance(key, slice) or isinstance(key, FatDataFrame) or isinstance(key, str) or isinstance(key, list) or isinstance(key, int):
                row_key, col_key = key, None
            elif isinstance(key, tuple):
                row_key, col_key = key if len(key) > 1 else (key[0], None)
            else:
                raise NotImplementedError(f'Key type {type(key)} not implemented in IndexSlice')

            node = FatDataFrame([self.parent], LazyOp(self.op_type, row_key=row_key, col_key=col_key))

            if isinstance(row_key, FatDataFrame):
                node.query_node.sources.append(row_key.query_node)
            # Chiranmoy 9-4-24: col_key is a column name or List[column name] but never a DataFrame
            # if isinstance(col_key, FatDataFrame):
            #     node.query_node.sources.append(col_key.query_node)

        elif self.op_type == LazyOpType.AT:
            row_key, col_key = key
            if BACKEND_ENGINE == BackendEngines.DASK:
                node = FatDataFrame([self.parent], LazyOp(LazyOpType.LOC, row_key=row_key, col_key=col_key))
            else:
                node = FatDataFrame([self.parent], LazyOp(LazyOpType.AT, row_key=row_key, col_key=col_key))

        elif self.op_type == LazyOpType.IAT:
            row_key, col_key = key
            if BACKEND_ENGINE == BackendEngines.DASK:
                node = FatDataFrame([self.parent], LazyOp(LazyOpType.ILOC, row_key=row_key, col_key=col_key))
            else:
                node = FatDataFrame([self.parent], LazyOp(LazyOpType.IAT, row_key=row_key, col_key=col_key))
        else:
            raise NotImplementedError(f'Operation {self.op_type} not implemented in IndexSlice')

        return node

    def __setitem__(self, key, value):
        if self.op_type == LazyOpType.LOC or self.op_type == LazyOpType.ILOC:
            if isinstance(key, slice) or isinstance(key, FatDataFrame) or isinstance(key, str) or isinstance(key, list) or isinstance(key, int):
                row_key, col_key = key, None
            elif isinstance(key, tuple):
                row_key, col_key = key if len(key) > 1 else (key[0], None)
            else:
                raise NotImplementedError(f'Key type {type(key)} not implemented in IndexSlice')

            new_query_node = Node([self.parent.query_node], LazyOp(LazyOpType.LOC_INDEXER_SETITEM, row_key=row_key, col_key=col_key, value=value))
            self.parent.query_node = new_query_node

            if isinstance(row_key, FatDataFrame):
                new_query_node.sources.append(row_key.query_node)
            if isinstance(value, FatDataFrame):
                new_query_node.sources.append(value.query_node)
        else:
            raise NotImplementedError("Not implemented assignment using at and iat.")


# add_properties()

# class FatSeries(FatDataFrame):
#     def Series:
