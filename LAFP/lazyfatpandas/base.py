from typing import Callable, Dict, Iterable, List
from uuid import uuid4
# import uuid

from lazyfatpandas.LazyOp import LazyOp, Meta, LazyOpType
from abc import ABCMeta, abstractmethod, abstractproperty


class BackendEngines(object):
    DASK = 'dask'
    MODIN = 'modin'
    PANDAS = 'pandas'


BACKEND_ENGINE = BackendEngines.DASK


class BaseNode:
    ignore = {'sources', 'actions', 'usecols', 'allcols', 'columns', 'genset', 'killset', 'expanding_action',
              'compatibility_with_dask', 'compatibility_with_modin', 'result', 'old_node', '__id', 'outcols', 'shape'}

    def __init__(self) -> None:
        self.sources: List['BaseNode'] = []
        self.action: LazyOp = None
        self.usecols: Dict[str, bool] = {}

        self.allcols: Dict[str, Meta] = {}
        # Attributes/columns generated i.e. read in this node
        self.genset: Dict[str, bool] = {}
        self.killset: Dict[str, bool] = {}

        # True: dataframe will expand. It acts as a barrier for moving filter statements
        self.expanding_action = False

        self.compatibility_with_dask = True
        self.compatibility_with_modin = True
        self.result = None
        self.old_node: 'BaseNode' = None
        self.old_node_reverse: 'BaseNode' = None
        self.sources_reverse: List['BaseNode'] = []
        self.shape = ()

        self.__id = uuid4().hex

        # List of columns that are required in future (remainder program from this point)
        self.outcols: Dict[str, bool] = {}

        self.usefull = False
        self.live = set()
        self.genset2 = set()
        self.possible_use_in_agg = set()

        self.depends_on = []

        # Chiranmoy
        self.columns = []  # column ordering is needed, allcols is a dict and doesn't maintain ordering
        self.in_deg = 0   # number of nodes which uses this node as a source

    def update_col(self, col, data):
        self.allcols[col] = data

    def __mark_gen(self, col):
        self.usecols[col] = True
        self.genset[col] = True

    def __mark_kill(self, col):
        self.killset[col] = True
        if self.usecols.get(col, None) is not None:
        # TODO verify this as indentation error removed on 05/06/2023
            del self.usecols[col]

    def __update_gen_kill_column(self, key_or_keys: List[str], updater: Callable):
        if key_or_keys is None:
            return self
        
        columns = []
        # print('Gen', key_or_keys)
        if isinstance(key_or_keys, str):
            columns = [key_or_keys]
        elif isinstance(key_or_keys, list) and key_or_keys and isinstance(key_or_keys[0], BaseFrame):
            # Chiranmoy 3-2-24: keys can be of type List[FatDataFrame], like in groupby
            columns = [k for df in key_or_keys for k in df.query_node.usecols.keys()]
        elif (isinstance(key_or_keys, list) and all(isinstance(k, str) for k in key_or_keys)) or isinstance(key_or_keys, Iterable):
            columns = key_or_keys
        elif isinstance(key_or_keys, BaseFrame):
            # Assumption, now mark all cols of key dataframe as gen
            columns = key_or_keys.query_node.usecols.keys()
        elif isinstance(key_or_keys, dict):
            columns = key_or_keys.keys()
        #bhu: check if this has any side effect. This is implemented for get(0)
        elif isinstance(key_or_keys, int):
            # print("key or keys is:", key_or_keys)
            columns = [key_or_keys]
        else:
            raise NotImplementedError('Not implemented column for ', key_or_keys)

        for key in columns:
            # TODO: Check in allcols before marking gen or kill. Right now allcols is not proporagated across nodes
            # if key in self.allcols:
            if key is not None:
                updater(key)

        return self

    def gen_columns(self, key_or_keys):
        return self.__update_gen_kill_column(key_or_keys, self.__mark_gen)

    def kill_columns(self, key_or_keys):
        return self.__update_gen_kill_column(key_or_keys, self.__mark_kill)

    def get_id(self):
        return self.__id

    def __str__(self):
        return repr(self.action)

    @abstractmethod
    def optimize(self, prune=True, column_selection=True, row_selection=True,
                 merge_filter=True, remove_deadcode=True): pass

    @abstractmethod
    def to_pandas_code(self): pass

    @abstractmethod
    def to_dask(self): pass

    @abstractmethod
    def copy_from(self): pass

    @abstractmethod
    def prune(self): pass

    @abstractmethod
    def to_modin(self): pass

    @abstractmethod
    def to_pandas(self): pass


class BaseFrame:
    ignore_setattr = {'pred', 'query_node', 'old_node', 'dt'}

    def __init__(self, pred: List['BaseFrame'], action: LazyOp):
        # self.pred = pred  # Chiranmoy 30-1-224: Not used outside __init__
        query_source = []
        if pred is not None and isinstance(pred, list) and len(pred):
            query_source = [pred[0].query_node]

        self.query_node: BaseNode = BaseNode()

        # Merge allcols
        for source in query_source:
            for k, v in source.allcols.items():
                self.query_node.allcols[k] = v

    def compute(self, prod=True):
        if prod:
            self.query_node = self.query_node.optimize()
            if BACKEND_ENGINE == BackendEngines.MODIN:
                return self.query_node.to_modin()

            if BACKEND_ENGINE == BackendEngines.PANDAS:
                return self.query_node.to_pandas()

            return self.query_node.to_dask()

        from time import time
        t1 = 0
        t2 = 0
        print('Non prod')
        save_as_img(self.query_node, True, 'out_with_old_node.png')
        save_as_img(self.query_node, False, 'out.png')
        start = time()
        res1 = self.query_node.column_selection().to_dask()
        t1 = time() - start
        self.query_node.clear_results()

        # This assignment is necessary as optimize returns modified head node
        # And query_node reference must be updated to get complete correct node
        start = time()
        self.query_node = self.query_node.optimize()
        optimization_time = time() - start
        save_as_img(self.query_node, False, 'out_after.png')
        save_as_img(self.query_node, True, 'out_after_old.png')
        start = time()
        res2 = self.query_node.to_dask()
        t2 = time() - start
        # if isinstance(res1, (pd.DataFrame, pd.Series)) and isinstance(res2, (pd.DataFrame, pd.Series)):
        #     print('Correctness1:', res1.equals(res2))
        #     # print(res1.columns)
        #     # print(res2.columns)
        # elif type(res1) == type(res2):
        #     print('Correctness2', res1 == res2)
        # else:
        #     print('Incorrect:')

        print('Base: Original', t1, 'Optimized', t2, 'Processing time', optimization_time)
        # print(res1)
        # print('-'*40)
        # print(res2)
        # return res2
        # return self.query_node.to_pandas_code()


def save_as_img(root: BaseNode, draw_old_edges: bool=True, filename: str='out.png') -> None:
    print('Generating', filename, 'Root is', root)
    from networkx.drawing.nx_agraph import graphviz_layout
    import networkx as nx
    import matplotlib.pyplot as plt
    G = nx.DiGraph()
    nodeslabels = {}
    edges: Dict[str, List[str]] = {}
    oldedges = {}
    visited = {}

    def dfs(node: BaseNode, indent=0):
        # print(' '*indent, node, node.source)
        if visited.get(node.get_id(), False) is True:
            return
        visited[node.get_id()] = True

        nodeslabels[node.get_id()] = repr(node.action)
        nodeslabels[node.get_id()] = node.action.optype + ' ' + '-'.join(node.usecols.keys())

        for source in node.sources:
            edges[source.get_id()] = edges.get(source.get_id(),[])
            edges[source.get_id()].append(node.get_id())
            dfs(source, indent+4)

        if node.old_node is not None:
            oldedges[node.get_id()] = node.old_node.get_id()

    dfs(root)

    G.add_nodes_from(nodeslabels.keys())
    for u, vs in edges.items():
        for v in vs:
            G.add_edge(v,u)
    if draw_old_edges:
        for k,v in oldedges.items():
            G.add_edge(k,v, color='red')

    # nx.draw(G, with_labels=True, labels=nodeslabels)
    nx.nx_agraph.write_dot(G,'test.dot')
    pos=graphviz_layout(G, prog='dot')
    nx.draw(G, pos, with_labels=True, labels=nodeslabels)

    plt.savefig(filename)
    plt.clf()

