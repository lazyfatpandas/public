from pandas import DataFrame, Series
from lazyfatpandas.executor import get_result
from typing import Dict, List, Tuple, Union
from .LazyOp import LazyOp, LazyOpType
from .base import BaseNode
from .utils import BackendEngines, find_usecols_in_subgraph, is_row_selection, move_edges, push_down, topological_sort
from copy import deepcopy


class Node(BaseNode):
    def __init__(self, sources: List['Node'], action: LazyOp, compatibility_with_dask=True, compatibility_with_modin=True, cache=None):
        super().__init__()

        # print('New node', action, 'with sources', [x for x in sources])

        if sources is None or not isinstance(sources, list):
            raise Exception(f'Expected source to be a list, got {sources}')

        self.sources: List[Node] = sources
        self.action: LazyOp = action

        if self.action.optype in [LazyOpType.READ_CSV, LazyOpType.EXPLODE]:
            self.expanding_action = True

        self.compatibility_with_dask = compatibility_with_dask
        self.compatibility_with_modin = compatibility_with_modin

        # Reference to node in case of complex operations, like setitem or getitem(key=dataframe or series)
        self.old_node: Node = None if len(sources) == 0 else sources[0]

        # Chiranmoy 30-1-24
        self.cache = cache  # cached pandas dataframe
        self.persist = False  # if true save the backend dataframe (in result field)
        self.original = None  # useful for storing results in executor
        self.columns = deepcopy(self.old_node.columns) if self.old_node else []  # do deepcopy for metadata
        if self.old_node is not None:
            self.allcols = deepcopy(self.old_node.allcols)
            self.shape = deepcopy(self.old_node.shape)

        # SIGMOD REDUNDANT PERSIST temp fix
        self.redundant_persist = False  # : df is being persisted due to get_item [filter] or set_item

        # Translation stuff
        # self.__equivalent_code = r''
        # self.__df_counter = 0

    def __eq__(self, other):
        # Chiranmoy:
        if self.action.optype != other.action.optype:
            return False
        if self.old_node.get_id() != other.old_node.get_id():
            return False
        if len(self.sources) != len(other.sources) or not all([s1.get_id() == s2.get_id() for s1, s2 in zip(self.sources, other.sources)]):
            return False
        if (len(self.action.kwargs) != len(other.action.kwargs) or
                not all([other.action.kwargs.get(key, '$_#DUMMY_VAL#_$') == self.action.kwargs[key] for key in self.action.kwargs])):
            return False
        return True

    def optimize(self) -> BaseNode:
        """
        Returns a new copy of task graph with following optimizations
        1. Deadcode elimination
        2. Row selection and merge row selection
        3. Column selection
        4 Chiranmoy 30-1-24: remove sub-graphs which are already computed

        # Planned optimizations
        3. Add drop statements (MSDF) ## to check
        4. Equivalent SQL (loops to function)
        5. COBRA (the least cost alternative)

        @deprecated
        Mutates current object. However, any reference of this object
        must be updated by assignment operations, otherwise you may
        get partial task graph.

        """
        # return self.remove_deadcode()
        # return self
        # copy_node = self.remove_deadcode()
        # copy_node = self.copy_from().strong_live_test()
        from lazyfatpandas.utils import options

        copy_node = self.copy_from()

        if options.persist_dataframes:
            copy_node.prune()
        if options.remove_deadcode:
            copy_node = copy_node.remove_deadcode(debug=False)
        if options.row_selection:
            copy_node = copy_node.row_selection()
        if options.merge_filter:
            copy_node = copy_node.merge_filters()
        if options.column_selection:
            copy_node = copy_node.column_selection()

        copy_node.calc_in_deg()

        return copy_node

        # self.__infer_objects()
        # self.__least_cost_alternatives()

    def calc_in_deg(self):
        """
        Chiranmoy 19-3-24
        during execution in_deg > 0 => node is still useful, otherwise clear the node results
        """
        for node in topological_sort(self):
            for src in node.sources:
                src.in_deg += 1
            # count 'old_node' if it is not present in the 'sources' list (to avoid double counting)
            if node.old_node and node.old_node.get_id() not in [src.get_id() for src in node.sources]:
                node.old_node.in_deg += 1

            # SIGMOD REDUNDANT PERSIST temp fix
            if is_row_selection(node) or node.action.optype == LazyOpType.SET_ITEM:
                node.old_node.redundant_persist = True

    def to_pandas_code(self):
        seq = self.__topological_sort()
        with open('generated.py', 'w') as output:

            # try https://svn.python.org/projects/python/trunk/Demo/parser/unparse.py
            output.write('''import pandas as pd\n\n''')
            for block in seq:
                counter = 0
                if block.action.optype == LazyOpType.READ_CSV:
                    counter = block.__df_counter
                    block.__equivalent_code = rf'df{counter+1} = pd.read_csv({",".join(map(str,block.action.args))},{*block.action.kwargs,})'
                elif block.action.optype == LazyOpType.GET_ITEM:
                    counter = block.source[0].__df_counter
                    block.__equivalent_code = rf'df{counter+1} = df{counter}[{*block.action.args,}]'
                elif block.action.optype == LazyOpType.SET_ITEM:
                    counter = block.source[0].__df_counter
                    block.__equivalent_code = f'df{counter+1}[\'{block.action.kwargs.get("key", "ERROR")}\'] = df{counter}'
                elif block.action.optype in [LazyOpType.APPLY, LazyOpType.EXPLODE, LazyOpType.GROUPBY]:
                    counter = block.source[0].__df_counter
                    block.__equivalent_code = rf'df{counter+1} = df{counter}.{block.action.optype}({*block.action.args,},{*block.action.kwargs,})'

                block.__df_counter = counter+1
                # print(block, [x.__str__() for x in block.source])

                print(block, block.__equivalent_code)
                if block.__equivalent_code:
                    output.write(block.__equivalent_code + "\n")
                else:
                    output.write(repr(output).__str__() + "\n")

    def __add_drop_statements(self):
        """
        # Need optimization gain confirmation
        Adds drop statements in-between nodes to decrease data movements.
        """
        return self

    def get_result(self):
        # self.estimate_cost()
        return self.to_dask()

    def to_dask(self) -> List[Union[DataFrame, Series]]:
        import dask.dataframe as dd
        # DataFrame.repartition(divisions=None, npartitions=None, partition_size=None, freq=None, force=False)
        # s = time()
        # r = get_result(self, dd, executor=BackendEngines.DASK)
        # # print('Internal time', time()-s)
        # return r
        # profiler
        # from dask.diagnostics import ResourceProfiler
        # rprof = ResourceProfiler(dt=0.5)
        # with ResourceProfiler(dt=0.5) as rprof:
        # s = time()
        r = get_result(self, dd, executor=BackendEngines.DASK)
        # rprof.visualize()
        # print('Internal time from Dask', time()-s)
        return list(r)

    def to_modin(self) -> List[Union[DataFrame, Series]]:
        import modin.pandas as mo
        # s = time()
        r = get_result(self, mo, executor=BackendEngines.MODIN)
        # print('Internal time from Modin', time()-s)
        return list(r)

    def to_pandas(self) -> List[Union[DataFrame, Series]]:
        import pandas as pd
        # s = time()
        r = get_result(self, pd, executor=BackendEngines.PANDAS)
        # print('Internal time from Pandas', time()-s)
        return list(r)

    def column_selection(self,  outcols: Dict[str, bool] = {}):
        # Mark all usecols in outcols
        for col in self.usecols:
            self.outcols[col] = True

        for col in outcols:
            self.outcols[col] = True
        for source in self.sources:
            source.column_selection(self.outcols.copy())
        return self

    def __replace_with_equivalent_code__(self):
        raise NotImplementedError('Equivalent code not implemented')

    def __least_cost_alternatives(self):
        raise NotImplementedError('COBRA not implemented')

    def clear_results(self):
        '''
        Invalidates result attributes for all trailing subgraph
        '''

        self.result = None
        for source in self.sources:
            source.clear_results()

    def row_selection(self: 'Node') -> BaseNode:
        '''
        Attempts to move row selection operations closer to read_csv operation.
        By definition, row selection operations have format:
            get-item (conditional or connectors)


        It mutates the task graph, therefore, any all references must be updated
        with the return value from this method

        '''
        def explore_and_push(prev: BaseNode, node: BaseNode) -> Tuple[BaseNode, bool]:
            change = False
            if node is None:
                return prev, change

            # Chiranmoy 2-1-24: we are trying to push node over node.old_node, but if node.old_node should be persisted
            # i.e., needed in the future, then don't push down
            if is_row_selection(node) and not node.old_node.persist:
                # This is filter node
                # print('filter:', node, node.sources[0])
                usecols = set(find_usecols_in_subgraph(node, node.old_node, []))
                n1, change = push_down(prev, node, node.old_node, node.old_node.old_node, usecols)
                if prev is None:
                    _, second_change = explore_and_push(None, node.old_node)
                    return (n1, change or second_change)
            # else:
            # print('============Not filter', node)
            n1, second_change = explore_and_push(node, node.old_node)
            change = change or second_change

            if prev is None:
                return (n1, change)
            return (prev, change)

        # print(None, self, self.old_node, self.old_node.sources[0])

        haschanged = True
        root = self
        while haschanged:
            root, haschanged = explore_and_push(None, root)
            # if haschanged:
            #     print('Running another round of row selection optimization')
            # haschanged = False
        return root

    def merge_filters(self: BaseNode):
        """
        Merge 2 or more consecutive filter nodes

        # BUG
        We cannot merge 2 filters if first filter is used by some other computation as it will affect results of the latter one

        # BUG
        Cannot merge 2 filters if second (latter) one is using any aggregate that requires parsing of whole data
            Example. Following 2 filters cannot be merged or swapped as secondone is using max()
            aggregate which depends on result of first filter

            df = df[(df.fare_amount > 0) & (df.tip_amount > 0)] # Get cheap
            df = df[(df.fare_amount > df.fare_amount.max()*0.1)]
        """
        def merge(root: BaseNode):
            if not root:
                return
                #
            b1 = is_row_selection(root)
            b2 = is_row_selection(root.old_node)

            if b1 and b2:
                # print('Will merge', root, 'and', root.old_node)
                and_node = Node([root.old_node.sources[0]], LazyOp(LazyOpType.BIT_AND))
                and_node.sources.append(root.sources[0])
                new_old_node = root.old_node.old_node
                move_edges(root, root.old_node, new_old_node, {})
                root.sources[0] = and_node  # Append and node
                root.old_node = new_old_node
                return merge(root)

            if root.old_node:
                merge(root.old_node)

        merge(self)
        return self

    def remove_deadcode(self: BaseNode, debug=False):
        # print('-'*50)

        def dfs(root: BaseNode):

            if not root:
                return
            # print('Check', root, root.usefull, root.live, root.result)
            root.genset2 = set(find_usecols_in_subgraph(root, root.old_node, []))
            # for k in root.allcols:
            #     root.live[k] = False

            if root.old_node:
                dfs(root.old_node)
                if root.action.optype in LazyOpType.COMPUTATIONAL:
                    root.genset2 = root.genset2.union(root.old_node.genset2)

                # if operation is filter or explode
                # then it might affect future aggregates. so we need to
                # keep track of those columns
                if is_row_selection(root) or root.action.affects_order():
                    root.possible_use_in_agg.update(root.genset2)

                if root.old_node.possible_use_in_agg:
                    root.possible_use_in_agg.update(root.old_node.possible_use_in_agg)
                # print('Row selection', root, is_row_selection(root) or root.action.affects_order(), root.possible_use_in_agg)

        dfs(self)
        # print('-'*50)
        if debug:
            print('-'*50)

        def dfs2(root: BaseNode, liveset, is_start=False):
            if not root:
                return
            if root.action.optype in [LazyOpType.READER]:
                if debug:
                    print(root, 'Live')
                root.usefull = True
                return

            if is_start:
                # If root is aggregate,
                # if root.action.optype in LazyOpType.COMPUTATIONAL:
                root.live = root.genset2.union(root.possible_use_in_agg)
                root.usefull = True
                # print(root, root.live)
                # print(root, root.genset2, root.live, 'Live' if root.usefull else 'Not live')
                dfs2(root.old_node, root.live)
                return

            if root.old_node:

                root.live = liveset

                if root.killset:
                    # This is updating column
                    # print('>>', root, liveset, root.genset2,root.killset.keys())
                    if liveset.intersection(set(root.killset.keys())):
                        # Updated colum is used later, so generated columns should be marked live
                        root.live = root.genset2.union(liveset)
                        root.usefull = True
                    elif root.action.optype == LazyOpType.RENAME and liveset.intersection(root.genset2):
                        root.live = root.genset2.union(liveset)
                        root.usefull = True
                    else:
                        # print('Marking not live', root.killset.keys())
                        pass
                else:
                    # Else this is only reading/using columns
                    # This operation will be strongly live only when any of the gen column is strongly live later
                    if root.genset2.intersection(liveset):
                        root.live = liveset.union(root.genset2)
                        root.usefull = True
                    elif root.action.optype in [LazyOpType.QUANTILE, LazyOpType.MERGE]:
                        # TODO BUG Make if proper. One way is to make tree using DLL
                        root.usefull = True

                    # Another case is if this operation is an aggregation
                    # then root.possible_use_in_agg cols can affect this result
                    # so we need to update liveset accordingly

                # set(root.genset2).union(set(root.old_node.genset2)) - set(root.old_node.killset.keys()).union(root.killset.keys())
                # if is_start:
                if debug:
                    print(root, root.genset2, root.live, 'Live' if root.usefull else 'Not live')
                dfs2(root.old_node, root.live)

        dfs2(self, set(), True)

        # print('-'*50)
        return self.copy_live_code()

    def copy_live_code(self: BaseNode) -> BaseNode:
        # Steps
        # 1. Create copy of live nodes and make hex_to_node map, old_to_new_hex_map
        # 2. Traverse again, for each live node
        #       find next node and update its sources

        def get_live_after(root: BaseNode) -> BaseNode:
            if not root or root.usefull:
                return root

            return get_live_after(root.old_node)

        old_to_new_hex_map = {}
        hex_to_node_map = {}

        def mark_live_and_copy(root: BaseNode, target: BaseNode):
            if root == target:
                return

            root.usefull = True
            for source in root.sources:
                mark_live_and_copy(source, target)

        def fill_live(root: BaseNode) -> None:

            if root and root.usefull:
                mark_live_and_copy(root, root.old_node)

            if root.old_node:
                fill_live(root.old_node)

        fill_live(self)
        visited = {}
        # print('Performing live code copy ---------------------------')

        def live_copy(root: BaseNode, visited):
            if root is None or visited.get(root.get_id(), None) is not None:
                return
            visited[root.get_id()] = True
            for source in root.sources:
                live_copy(source, visited)

            # print(root, root.get_id(), root.usefull)
            if root.usefull:
                node = Node([], root.action, root.compatibility_with_dask, root.compatibility_with_modin)

                old_to_new_hex_map[root.get_id()] = node.get_id()
                hex_to_node_map[node.get_id()] = node

                for source in root.sources:
                    next = get_live_after(source)
                    # print('1>>',root, next)
                    node.sources.append(hex_to_node_map[old_to_new_hex_map.get(next.get_id())])

                # NOTE: new node may have reference to original node as argument in action (set item rhs value).
                # But we are not using it in executor, instead old_node reference is used
                if root.old_node:
                    next = get_live_after(root.old_node)
                    # print('2>>',root, next, next.get_id(), old_to_new_hex_map.get(next.get_id(), None))
                    node.old_node = hex_to_node_map[old_to_new_hex_map[next.get_id()]]

                node.allcols = root.allcols
                node.expanding_action = root.expanding_action
                node.genset = root.genset
                node.killset = root.killset
                node.usecols = root.usecols
                node.shape = root.shape
                pass

        live_copy(self, visited)
        result = hex_to_node_map[old_to_new_hex_map[self.get_id()]]
        del hex_to_node_map
        del old_to_new_hex_map
        return result

    def copy_from(self: BaseNode) -> 'Node':
        """
        Returns a new copy from current node till the end
        """
        order = topological_sort(self)
        old_to_new_hex_map = {}
        hex_to_node_map = {}

        for statement in order:
            node = Node([], statement.action, statement.compatibility_with_dask, statement.compatibility_with_modin, statement.cache)

            old_to_new_hex_map[statement.get_id()] = node.get_id()
            hex_to_node_map[node.get_id()] = node

            for source in statement.sources:
                node.sources.append(hex_to_node_map[old_to_new_hex_map.get(source.get_id())])

            # NOTE: new node may have reference to original node as argument in action (set item rhs value).
            # But we are not using it in executor, instead old_node reference is used
            if statement.old_node:
                node.old_node = hex_to_node_map[old_to_new_hex_map[statement.old_node.get_id()]]

            # copy other properties
            node.allcols = statement.allcols
            node.expanding_action = statement.expanding_action
            node.genset = statement.genset
            node.killset = statement.killset
            node.usecols = statement.usecols
            # print('copying', node, statement.shape)
            node.shape = statement.shape

            # Chiranmoy 30-1-24
            node.cache = statement.cache
            node.result = statement.result  # saved backend dataframes are reused
            node.persist = statement.persist  # whether to persist or not
            node.original = statement
            node.columns = statement.columns

            # print('Created', node, node.action.kwargs, 'with', len(node.sources))

        result = hex_to_node_map[old_to_new_hex_map[order[-1].get_id()]]
        del hex_to_node_map
        del old_to_new_hex_map
        return result

    def strong_lively_test(self):
        '''
        ## Idea
        1. For each node `u` in topological order
            Find a node `v` which has a conflict with node `u`. That is in original program we have,

                >>> (start) s1..., *v* , s4... , *u* , s6... (end)

                Conflict means
                    1. Removing `v` affects result of `u` (need concrete definition)
                    2. Kill(v) interset with Gen (u) (row selection, assignment, explosive operations)
        2. Now identify the terminal node `t` (first node in reverse topological order) and gets its all conflicting nodes transitively. This should give operations that are required to compute `t` without any redundant/dead operation.

        Worst Case O(N^2)  where N is number of operations (No code is live or only terminal code is live)
        Best case O(N) - All operation is live


        Node properties/conditions
            1. Read_csv always live
            2. Row selection: Add all cols in kill set
            3. Groupby: Make immediately next operation depend on groupby
            4. Aggregate/Computational: Make it dependent on immediately previous statement

        '''

        def find_conflict(node: BaseNode, genset: set, killset: set):
            if node is None:
                return None

            # subgraph_genkill = set(find_usecols_in_subgraph(node, node.old_node, [])).union(set(node.killset))
            # conflict = genset.union(killset).intersection(subgraph_genkill)
            conflict = genset.intersection(set(node.killset))
            if node.action.optype == LazyOpType.RENAME:
                conflict = conflict.union(genset.intersection(node.genset2))

            if len(conflict) != 0 or node.expanding_action:
                return node

            return find_conflict(node.old_node, genset, killset)

        def find_dependent(node: 'BaseNode'):
            # print('Finding depends of ', node)
            if node is None:
                return node

            if node.action.optype == LazyOpType.READ_CSV:
                return None
            if node.action.optype in LazyOpType.COMPUTATIONAL:
                return node.old_node

            if node.action.optype == LazyOpType.GROUPBY:
                node.old_node_reverse.depends_on.append(node)

            return find_conflict(node.old_node, node.genset2, set(node.killset))

        print('*'*80)
        order = topological_sort(self)
        # Add old_node_reverse
        # Add sources_reverse

        for statement in order:
            if statement.old_node:
                statement.old_node.old_node_reverse = statement

        def dfs(node: BaseNode):
            if node is None:
                return

            node.genset2 = set(find_usecols_in_subgraph(node, node.old_node, []))
            if is_row_selection(node):
                node.killset = node.allcols

            if node.action.optype == LazyOpType.READ_CSV:
                node.usefull = True

            if node.old_node:
                dfs(node.old_node)

            dependent = find_dependent(node)
            if dependent:  # and len(node.depends_on) == 0:
                node.depends_on.append(dependent)

            # print(node, [x.action for x in node.depends_on], is_row_selection(node), node.old_node_reverse, node.genset2, set(node.killset.keys()))
            # print()

        dfs(self)

        print('*'*80)

        def mark_live(node: BaseNode):
            if node is None:
                return

            node.usefull = True
            print(node)
            for n in node.depends_on:
                mark_live(n)

        mark_live(self)

        # Small Bug: Transitive dependency
        return self.copy_live_code()

    def estimate_cost(self):
        return
        order = topological_sort(self)
        print('='*50)
        for statement in order:
            # if statement.action.optype in LazyOpType.READER:
            #     print(statement.action)
            print(statement, statement.shape)

    def prune(self) -> BaseNode:
        """
        Chiranmoy 30-1-24
        remove the sub-graphs which are already computed
        """
        if self.cache is not None or self.result is not None:
            self.action = LazyOp(LazyOpType.FROM_CACHE, *self.action.args, **self.action.kwargs)

            # severing connections to the nodes below
            self.sources = []
            self.old_node = None
            self.old_node_reverse = None
            self.sources_reverse = None

            return self

        for i in range(len(self.sources)):
            self.sources[i] = self.sources[i].prune()

        if self.old_node and self.old_node.cache is not None:
            self.old_node = self.old_node.prune()

        return self

    def mark_live_subexpressions(self, live_df, cached_nodes, executor) -> None:
        """
        Chiranmoy 30-1-24
        Find and mark the common sub-expressions (common nodes). Set node.persist to True for common nodes.
        """
        if not live_df or len(live_df) == 0:
            return

        nodes_to_be_computed = {}  # nodes in the task graph to be computed

        def populate(node: Node):
            if (node.cache is not None) or (node.result is not None):
                return
            nodes_to_be_computed[node.get_id()] = node
            for child in node.sources:
                populate(child)

        populate(self)

        def dfs(node: Node):
            if (node.cache is not None) or (node.result is not None):  # already cached
                return
            elif node.get_id() in nodes_to_be_computed:
                # groupby without aggregation can't be persisted/computed in dask
                # persist the dataframe on top of which groupby is executed for dask, problem: liveness of this dataframe
                if node.action.optype != LazyOpType.GROUPBY or executor != BackendEngines.DASK:
                    cached_nodes[node.get_id()] = node
                    node.persist = True
                    return

            for child in node.sources:
                dfs(child)

        for df in live_df:
            dfs(df.query_node)

    def get_live_nodes_ids(self) -> List[str]:
        """ Chiranmoy 16-4-24: returns the node ids in the pruned task graph """
        live_nodes_ids: List[str] = []

        def dfs(node):
            live_nodes_ids.append(node.get_id())
            if node.result is not None or node.cache is not None:
                return

            for src in node.sources:
                dfs(src)

        dfs(self)

        return live_nodes_ids
