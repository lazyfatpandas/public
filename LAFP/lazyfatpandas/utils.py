from enum import Enum
from typing import Dict, List, Set
from lazyfatpandas.LazyOp import LazyOpType
from .base import BaseNode, BaseFrame


class BackendEngines(object):
    DASK = 'dask'
    MODIN = 'modin'
    PANDAS = 'pandas'


class Options:
    # Chiranmoy: Set the default values in the __init__ below
    def __init__(self, column_selection=False, row_selection=True, merge_filter=True, remove_deadcode=False,
                 lazy_print=True, persist_dataframes=True, dead_df_removal=True, default_to_pandas=True, output_hash=False):
        self.column_selection = column_selection
        self.row_selection = row_selection
        self.merge_filter = merge_filter
        self.remove_deadcode = remove_deadcode
        self.lazy_print = lazy_print
        self.persist_dataframes = persist_dataframes
        self.default_to_pandas = default_to_pandas
        self.dead_df_removal = dead_df_removal
        self.output_hash = output_hash


default_options = Options()  # default options
options = default_options  # options for current call to compute


def get_options_kwargs(column_selection=None, row_selection=None, merge_filter=None, remove_deadcode=None, lazy_print=None, 
                       persist_dataframes=None, dead_df_removal=None, default_to_pandas=None, output_hash=None):
    """
    Chiranmoy 12-4-24
    If option is not set (i.e., value is None) then take the value from default_options
    """
    column_selection = default_options.column_selection if column_selection is None else column_selection
    row_selection = default_options.row_selection if row_selection is None else row_selection
    merge_filter = default_options.merge_filter if merge_filter is None else merge_filter
    remove_deadcode = default_options.remove_deadcode if remove_deadcode is None else remove_deadcode
    lazy_print = default_options.lazy_print if lazy_print is None else lazy_print
    persist_dataframes = default_options.persist_dataframes if persist_dataframes is None else persist_dataframes
    default_to_pandas = default_options.default_to_pandas if default_to_pandas is None else default_to_pandas
    dead_df_removal = default_options.dead_df_removal if dead_df_removal is None else dead_df_removal
    output_hash = default_options.output_hash if output_hash is None else output_hash

    return {'column_selection': column_selection, 'row_selection': row_selection, 'merge_filter': merge_filter,
            'remove_deadcode': remove_deadcode, 'lazy_print': lazy_print, 'persist_dataframes': persist_dataframes,
            'dead_df_removal': dead_df_removal, 'default_to_pandas': default_to_pandas, 'output_hash': output_hash}


def set_options(column_selection=None, row_selection=None, merge_filter=None, remove_deadcode=None, lazy_print=None,
                persist_dataframes=None, dead_df_removal=None, default_to_pandas=None, output_hash=None, __default=True):
    global default_options, options

    _option = Options(**get_options_kwargs(column_selection=column_selection, row_selection=row_selection, merge_filter=merge_filter, remove_deadcode=remove_deadcode,
                                           lazy_print=lazy_print, persist_dataframes=persist_dataframes, dead_df_removal=dead_df_removal,
                                           default_to_pandas=default_to_pandas, output_hash=output_hash))
    if __default:
        default_options = _option
    else:
        options = _option
    return _option


def topological_sort(root: BaseNode) -> List[BaseNode]:
    visited = {}
    stack: List[BaseNode] = []

    def __topological_sort_util(v: BaseNode, visited: Dict[str, bool], stack: List[BaseNode]) -> None:
        # Mark the current node as visited.
        visited[v.get_id()] = True

        # Recur for all the vertices adjacent to this vertex
        for i in v.sources:
            if not visited.get(i.get_id(), False):
                __topological_sort_util(i, visited, stack)

        # Push current vertex to stack which stores result
        stack.append(v)

    __topological_sort_util(root, visited, stack)

    return stack


def save_as_img(root: BaseNode, draw_old_edges: bool = True, filename: str = 'out.png') -> None:
    print(f'Generating {filename} with Root {root}')
    from networkx.drawing.nx_agraph import graphviz_layout
    import networkx as nx
    from os.path import basename
    import matplotlib.pyplot as plt

    G = nx.DiGraph()
    nodeslabels = {}
    edges: Dict[str, List[str]] = {}
    oldedges = {}
    visited = {}
    plt.close()

    def dfs(node: BaseNode, indent=0):
        # print(' '*indent, node)
        if visited.get(node.get_id(), False) is True:
            return
        visited[node.get_id()] = True

        optype = repr(node.action)
        if isinstance(node.action.optype, Enum):
            optype = node.action.optype.value
        if node.action.optype == LazyOpType.SET_ITEM:
            optype = f"{optype} {node.action.kwargs['key']}"

        nodeslabels[node.get_id()] = optype + ' ' + '-'.join([str(key) for key in node.genset.keys()])

        # Chiranmoy 4-2-24: better graph labeling
        if node.action.optype == LazyOpType.READ_CSV:
            nodeslabels[node.get_id()] = optype + ' ' + basename(node.action.kwargs['filepath_or_buffer'][0])
        elif is_row_selection(node):
            nodeslabels[node.get_id()] = optype + ' ' + '[filter]'  # filter operation

        # nodeslabels[node.get_id()] = nodeslabels[node.get_id()] + ' (' + str(node.in_deg) + ')'  # in_deg of each node

        for source in node.sources:
            edges[source.get_id()] = edges.get(source.get_id(), [])
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
        for k, v in oldedges.items():
            G.add_edge(k, v, color='red')

    # nx.draw(G, with_labels=True, labels=nodeslabels)

    nx.nx_agraph.write_dot(G, 'test.dot')
    # TODO: Find tree depth and with and set figure size accordingly
    plt.figure(1, figsize=(15, 10))
    pos = graphviz_layout(G, prog='dot')
    # update and revert back
    # nx.draw(G, pos, with_labels=True, labels=nodeslabels, node_color="skyblue", node_shape="s")
    nx.draw_networkx(G, pos, with_labels=True, labels=nodeslabels, node_color="skyblue", node_shape="s")
    # plt.figure(3,figsize=(24,24))
    plt.savefig(filename)
def find_usecols_in_subgraph(root: BaseNode,  stop_node: BaseNode, result: Set[str]) -> List[str]:
    '''
    Scans the graph from `root` node upto `stop_node` and computes columns used
    '''
    if root is None or (stop_node is not None and root.get_id() == stop_node.get_id()):
        return []
    #TODO: Bhu Check why genset and not usecols, potential bug
    result += root.genset.keys()
    for source in root.sources:
        find_usecols_in_subgraph(source, stop_node, result)

    return result


def push_down(prev: BaseNode, first_head: BaseNode, second_head: BaseNode, second_tail: BaseNode, genkill: set):
    # print('push', prev, first_head, second_head, second_tail, genkill, second_head.action, is_safe_for_push_down(second_head))
    if first_head is None or second_head is None or second_tail is None:
        return first_head, False
    if not is_safe_for_push_down(second_head):
        print('Not whitelisted op for push down', first_head, second_head, second_tail)
        return first_head, False

    subgraph_genkill = set(find_usecols_in_subgraph(second_head, second_tail, []))
    conflict = genkill.intersection(subgraph_genkill)

    # If there's no common columns between 2 successive filters, then also we cannot swap them as it can change te result in somecases
    # Example
    ''' 
        df = df[df.total_amount > 0] 
        df = df[(df.passenger_count > 0) & (df.passenger_count < 7)]
        max_trip_distance = df.trip_distance.max()
        airport_trips = df[df.airport_fee > 0]['airport_fee'].count()

        In this case airport_fee filter cannot be swapped with passenget_count filter as it will change max_trip_distance and airport_trips aggregates
    '''

    if len(conflict) != 0 or second_head.expanding_action or is_row_selection(second_head):
        # print('>> Conflict with', second_head, 'Extending first', first_head, 'to', second_head, genkill.union(subgraph_genkill))
        # Has conflict, merge first and second head
        return push_down(prev, first_head, second_tail, second_tail.old_node, genkill.union(subgraph_genkill))
    else:
        # print('>>>', 'No conflict', first_head, second_head, second_tail, genkill)
        # No conflict
        change = not is_row_selection(second_head)
        # print('>> Swapping', first_head, second_head, change)
        new_root = swap_nodes(prev, first_head, second_head, second_tail)
        if prev is None:
            prev = new_root

        # print('>> >> after', first_head, second_tail, second_tail.old_node)
        _, second_change = push_down(prev, first_head, second_tail, second_tail.old_node, genkill)
        return (prev, change or second_change)


def move_edges(start_node: BaseNode, stop_node: BaseNode, target_node: BaseNode, visited: Dict[str, bool]):
    '''
    Reassign edges to stop_node to target_node. All edges pointing to `stop_node` will now point to `target_node`
    '''
    if start_node is None or start_node.action.optype == LazyOpType.LITERAL or start_node.get_id() == stop_node.get_id():
        return

    if visited.get(start_node.get_id(), False) is True:
        return
        raise RecursionError(f'Cycle detected while visiting {start_node}, stop_node: {stop_node}')

    visited[start_node.get_id()] = True

    for source in start_node.sources:
        move_edges(source, stop_node, target_node, visited)

    if start_node.old_node is not None and start_node.old_node.get_id() == stop_node.get_id():
        start_node.old_node = target_node

    if len(start_node.sources) > 0 and start_node.sources[0].get_id() == stop_node.get_id():
        start_node.sources[0] = target_node

    if len(start_node.sources) > 1 and start_node.sources[1].get_id() == stop_node.get_id():
        start_node.sources[1] = target_node


def swap_nodes(prev_node: BaseNode, first_head: BaseNode, second_head: BaseNode, second_tail: BaseNode) -> BaseNode:
    """
        ...nodes....
        first_head
        ...nodes...
        second_head
        ...nodes...
        second_tail
        ...nodes...

        Scan from first head to second_head, nodes pointing to second_head should now point to second_tail.
        Scan from second_head to second_tail, nodes pointing to second_tail should now point to first head.
        Scan from prev_node to first_head, nodes pointing to first_head should now point to second_head.
    """
    # print('connecting', first_head, second_head, second_tail)
    move_edges(first_head, second_head, second_tail, {})
    # print('connecting', second_head, second_tail, first_head)
    move_edges(second_head, second_tail, first_head, {})

    if prev_node is not None:
        # print('connecting', prev_node, first_head, second_head)
        move_edges(prev_node, first_head, second_head, {})

    return second_head


def is_row_selection(node: BaseNode):
    if node is None or len(node.sources) != 1:
        return False

    get_item = node.action.optype == LazyOpType.GET_ITEM
    operation = node.sources[0].action.optype in [*LazyOpType.CONDITION_CONNECTORS, *LazyOpType.COMPARATORS]
    # print('>>>', node, node.sources[0], node.sources[0].action.optype)
    return get_item and operation

def is_safe_for_push_down(node: BaseNode):
    if node is None:
        return True
    
    return is_row_selection(node) or  node.action.optype in LazyOpType.PUSH_DOWN_WHITELIST_OP

def __printer(sources, print_args, sep, end):
    """
    Chiranmoy 1-4-24: prints the lazyPrint node
    If options.output_hash is true, we output the hash rather than the actual data.
    """
    if options.output_hash:
        from r_suite.r_suite import dataframe_hasher as print
    else:
        from builtins import print

    def get_computed_result(node_id):
        return list(filter(lambda src: src.original.get_id() == node_id, sources))[0].result

    for arg_no, arg in enumerate(print_args, 1):
        if isinstance(arg, str) and '$_#' in arg:
            arr, i = arg.split('$_#'), 1
            print(arr[0], sep='', end='')

            while i < len(arr):
                query_node_id, format_spec = arr[i].split('|')

                if format_spec:
                    print(f'%{format_spec}' % get_computed_result(query_node_id), arr[i + 1], sep='', end='')
                else:
                    print(get_computed_result(query_node_id), arr[i + 1], sep='', end='')

                i += 2

        elif isinstance(arg, BaseNode):
            print(get_computed_result(arg.get_id()), end='')

        elif isinstance(arg, list):
            print([get_computed_result(ele.get_id()) if isinstance(ele, BaseNode) else ele for ele in arg], end='')

        elif isinstance(arg, dict):
            print({key: get_computed_result(arg[key].get_id()) if isinstance(arg[key], BaseNode) else arg[key] for key in arg}, end='')

        else:
            print(arg, end='')

        if arg_no < len(print_args) and not options.output_hash:
            print(sep, end='')

    print(end=end)


def lafp_len(obj):
    if isinstance(obj, BaseFrame):
        return obj.len
    return len(obj)


class DaskUnsupported(Exception):
    def __init__(self, message, unsupported_op=None, **kwargs):
        super().__init__(message)
        self.unsupported_op = unsupported_op
        self.kwargs = kwargs
