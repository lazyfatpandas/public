from warnings import catch_warnings, simplefilter
from lazyfatpandas.LazyOp import DtAccProps, LazyOp, LazyOpType, StrAccFunc
from lazyfatpandas.base import BaseFrame, BaseNode
from lazyfatpandas.utils import BackendEngines, topological_sort, __printer, DaskUnsupported
from copy import deepcopy
from os import cpu_count
from time import time
from memory_profiler import profile
from gc import collect as collect_garbage

# Chiranmoy
DASK_PARTITIONS = 6  # cpu_count()  # Number of partitions when converting from Pandas DF to Dask DF using from_pandas
CLEAR_INTERMEDIATE_NODES = True  # clear a node's result when no longer needed


def get_result(root, library, executor=BackendEngines.DASK):
    """
    # Common executor for dask/modin.
    It expects all will have atleast `read_csv` and corresponding
    method as LazyOpType. Pass appropriate module as library parameter
    """
    # s = time()
    # print('Executing with', executor)
    from lazyfatpandas.utils import options
    order = topological_sort(root)

    with catch_warnings():
        simplefilter("ignore")

        for statement in order:
            try:
                ret = execute_statement(statement, library, executor)
                if statement.action.optype == LazyOpType.COMPUTE:
                    return ret
            except AttributeError as err:
                if not options.default_to_pandas:
                    raise AttributeError(err)
                elif executor == BackendEngines.DASK:
                    default_to_pandas_dask(statement, library)
                elif executor == BackendEngines.MODIN:
                    raise NotImplementedError("NOT IMPLEMENTED: Default to Pandas for Modin")
                else:
                    print("statement:",statement)
                    raise NotImplementedError("Unreachable, Pandas should support its own APIs")

            except DaskUnsupported as err:
                if not options.default_to_pandas:
                    raise DaskUnsupported(err)
                # Chiranmoy 16-4-24: these errors might need special handling
                default_to_pandas_dask(statement, library)

            # Chiranmoy 30-1-24: if statement.persist is true, then this node is needed again
            if statement.persist and executor == BackendEngines.DASK:
                statement.original.result = statement.result = dask_persist(statement.result)
            elif statement.persist:
                statement.original.result = statement.result

            if CLEAR_INTERMEDIATE_NODES:
                clear_intermediate_results(statement)

    # print('Task graph time', time()-s)

    # assert order[-1].action.optype in [*LazyOpType.NO_RETURN, LazyOpType.PRINT] or order[-1].result is not None, 'Missed some statements, Result is None'
    #
    # if executor != BackendEngines.DASK or root.action.optype == LazyOpType.PRINT:
    #     return order[-1].result
    # return dask_compute(order[-1].result)


def execute_statement(statement, library, executor):
    if executor == BackendEngines.DASK:
        dask_compatibility_check(statement)
    if executor == BackendEngines.MODIN:
        modin_compatibility_check(statement)

    if statement.result is not None:
        # Chiranmoy 30-1-24: result persisted from previous execution
        return
    elif statement.action.optype == LazyOpType.PRINT:
        execute_lazy_print(statement, library, executor)

    elif statement.action.optype == LazyOpType.READ_CSV:
        execute_read_csv(library, executor, statement)

    elif statement.action.optype == LazyOpType.READ_PARQUET:
        execute_read_parquet(library, executor, statement)

    elif statement.action.optype in [LazyOpType.FROM_CACHE, LazyOpType.DATAFRAME]:
        # Chiranmoy 30-1-24, convert pandas' DF to backend DF
        if executor == BackendEngines.DASK:
            statement.result = library.from_pandas(statement.cache, npartitions=DASK_PARTITIONS)
        else:
            statement.result = statement.cache

    elif statement.action.optype == LazyOpType.SERIES:
        if isinstance(statement.action.kwargs['data'], BaseFrame):
            statement.action.kwargs['data'] = statement.sources[0].result
        statement.result = library.Series(*statement.action.args, **statement.action.kwargs)

    else:
        assert statement.action.optype == LazyOpType.LITERAL or len(statement.sources) > 0, f'Was expecting a source for this node, Got {statement}'
        if statement.action.optype == LazyOpType.LITERAL:
            statement.result = statement.action.args[0]
            return

        if statement.action.optype == LazyOpType.GET_ITEM:
            assert statement.action.kwargs.get('key', None) is not None, f'Was expecting a "key" for getitem, got {statement.action.kwargs}'
            if isinstance(statement.action.kwargs.get('key'), BaseFrame):
                # print('getitem', statement.old_node.result, statement.action.kwargs)
                # print(statement.sources[0].__result.compute())
                # Things going messy here. Since key is a dataframe, source0 will be a key
                # And we need original source for row selection
                statement.result = statement.old_node.result[statement.sources[0].result]
            elif isinstance(statement.old_node.result, tuple) or isinstance(statement.old_node.result, list):
                statement.result = statement.sources[0].result[statement.action.kwargs['key']]
            else:
                # statement.result = statement.sources[0].result.__getitem__(*statement.action.args, **statement.action.kwargs)
                # print("key",statement.action.kwargs['key'])
                # print(statement.sources[0].result['loan_status'])
                # print("kwargs",statement.action.kwargs['key'])
                # print("sources",statement.sources[0].result.dtypes)
                statement.result = statement.sources[0].result[statement.action.kwargs['key']]

        elif statement.action.optype == LazyOpType.SET_ITEM:
            assert statement.action.kwargs.get('key', False), f'Was expecting a "key" for setitem, got {statement.action.kwargs}'
            if statement.old_node.in_deg > 1:
                statement.result = deepcopy(statement.old_node.result)  # multiple nodes depend on old_node, do deepcopy
            else:
                statement.result = statement.old_node.result  # only this statement depends on old_node, do soft-copy
            statement.result[statement.action.kwargs['key']] = statement.sources[0].result if isinstance(statement.action.kwargs['value'], BaseNode) else statement.action.kwargs['value']

        elif statement.action.optype == LazyOpType.LOC_INDEXER_SETITEM:
            if statement.old_node.in_deg > 1:
                statement.result = deepcopy(statement.old_node.result)  # multiple nodes depend on old_node, do deepcopy
            else:
                statement.result = statement.old_node.result  # only this statement depends on old_node, do soft-copy

            row_key = statement.sources[1].result if isinstance(statement.action.kwargs['row_key'], BaseFrame) else statement.action.kwargs['row_key']
            col_key = statement.action.kwargs['col_key']
            value = statement.sources[-1].result if isinstance(statement.action.kwargs['value'], BaseFrame) else statement.action.kwargs['value']
            value = value[0] if isinstance(value, library.Series) and len(value) == 1 else value

            if col_key:
                statement.result.loc[row_key, col_key] = value
            else:
                statement.result.loc[row_key] = value

        elif isinstance(statement.action.optype, (DtAccProps, StrAccFunc)):
            # print('-------------------------------')
            # print(statement.action.optype.value)

            if isinstance(statement.action.optype, DtAccProps):
                # Its property
                # print("-"*50)
                # print(statement, statement.action, dir(statement.sources[0].result.dt))
                statement.result = getattr(statement.sources[0].result.dt, statement.action.optype.value)
            elif isinstance(statement.action.optype, StrAccFunc):
                # Its a method
                if statement.action.optype == LazyOpType.str.cat:
                    # Chiranmoy 30-1-24: others can be dataframe
                    # bhu modified feb 14 2025 to avoid others error
                    others = statement.action.kwargs.pop('others', None)
                    if isinstance(others, BaseFrame):
                        others = statement.sources[1].result
                    statement.result = getattr(statement.sources[0].result.str, statement.action.optype.value)(others=others, **statement.action.kwargs)
                else:
                    statement.result = getattr(statement.sources[0].result.str, statement.action.optype.value)(*statement.action.args, **statement.action.kwargs)
            else:
                raise NotImplementedError()

        elif statement.action.optype in LazyOpType.PANDAS_LEVEL:
            kwargs = statement.action.kwargs.copy()

            kwargs.pop('downcast', []) # dask doesn't support downcast in to_numeric
            statement.result = library.__getattribute__(statement.action.optype)(statement.sources[0].result, *statement.action.args, **kwargs)

        #TODO: rewtie
        elif statement.action.optype == LazyOpType.RESET_INDEX:
            statement.result = statement.old_node.result.reset_index(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype == LazyOpType.TO_STRING:
            statement.result = statement.old_node.result.to_string(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype == LazyOpType.SET_INDEX:
            statement.result = statement.old_node.result.set_index(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype == LazyOpType.SORT_INDEX:
            if executor == BackendEngines.DASK:
                statement.result = handle_dask_sort_index(statement, library.Series)
            else:
                statement.result = statement.old_node.result.sort_index(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype == LazyOpType.INDEX:
            statement.result = statement.old_node.result.index

        elif statement.action.optype == LazyOpType.CONCAT:
            if executor == BackendEngines.DASK:
                statement.result = library.multi.concat([src.result for src in statement.sources], *statement.action.args, **statement.action.kwargs)
            else:
                statement.result = library.concat([src.result for src in statement.sources], *statement.action.args, **statement.action.kwargs)

        elif statement.action.optype in LazyOpType.CONVERSION:
            # if statement.action.optype != LazyOpType.ASTYPE:
            #     raise NotImplementedError(f"NOT IMPLEMENT {statement.action.optype}")
            statement.result = statement.sources[0].result.__getattribute__(statement.action.optype)(*statement.action.args)

        # TODO optimize it
        elif statement.action.optype == LazyOpType.APPEND:
            if executor == BackendEngines.DASK:
                # todo update here
                statement.result = statement.old_node.result.append(*statement.action.args, **statement.action.kwargs)
            else:
                statement.result = dask_compute(statement.sources[0].result).__getattribute__(statement.action.optype)(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype == LazyOpType.UNSTACK:
            if executor == BackendEngines.DASK:
                raise Exception("Append not supported by LaFP with Dask")
            else:
                statement.result = statement.old_node.result.unstack(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype == LazyOpType.GET_DUMMIES:
            if BackendEngines.DASK == executor:
                # Chiranmoy: get_dummies expects a categorical dtype
                src = statement.sources[0].result.to_frame() if isinstance(statement.sources[0].result, library.Series) else statement.sources[0].result
                statement.result = library.reshape.get_dummies(library.DataFrame.categorize(src), *statement.action.args, **statement.action.kwargs)
                del src
            else:
                statement.result = library.get_dummies(statement.sources[0].result, *statement.action.args, **statement.action.kwargs)

        elif statement.action.optype == LazyOpType.MERGE:
            # source 2 is the right one
            statement.result = statement.old_node.result.merge(statement.sources[1].result, *statement.action.args, **statement.action.kwargs)

        elif statement.action.optype in LazyOpType.INFO:
            # Chiranmoy 6-3-24: check "def info(...)" in pandas for more details
            from io import StringIO
            buffer = statement.action.kwargs['buf'] if 'buf' in statement.action.kwargs else StringIO()
            statement.action.kwargs['buf'] = buffer
            statement.sources[0].result.__getattribute__(statement.action.optype)(*statement.action.args, **statement.action.kwargs)
            statement.result = buffer.getvalue()
            buffer.close()

            # statement.result = statement.action.kwargs['buf'].read()
            # Either results are directly printed on stdout or returns as pandas object (no need to compute())
            # return statement.result
        elif statement.action.optype in LazyOpType.GROUPBY:
            # Chiranmoy 3-2-24: extended support for groupby
            by = statement.action.kwargs.pop('by')
            if isinstance(by, BaseFrame):
                statement.result = statement.sources[0].result.groupby(by=statement.sources[1].result, *statement.action.args, **statement.action.kwargs)
            elif isinstance(by, list) and isinstance(by[0], BaseFrame):
                statement.result = statement.sources[0].result.groupby(by=[key.result for key in statement.sources[1:]], *statement.action.args, **statement.action.kwargs)
            else:
                statement.result = statement.sources[0].result.groupby(by=by, *statement.action.args, **statement.action.kwargs)
            statement.action.kwargs['by'] = by

        elif statement.action.optype == LazyOpType.SORT_VALUES:
            if isinstance(statement.old_node.result, library.Series):
                statement.result = handle_series_sort(statement, executor)
            else:
                statement.result = statement.sources[0].result.__getattribute__(statement.action.optype)(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype in LazyOpType.PREF_DEF:
            if statement.action.optype in (LazyOpType.HEAD, LazyOpType.TAIL) and executor != BackendEngines.DASK:
                statement.action.kwargs.pop('compute', [])

            # Chiranmoy 6-3-24: Dask expects the columns field to be categorical for pivot_table
            if statement.action.optype == LazyOpType.PIVOT_TABLE and executor == BackendEngines.DASK and 'columns' in statement.action.kwargs:
                statement.sources[0].result = library.DataFrame.categorize(statement.sources[0].result, columns=statement.action.kwargs['columns'])

            if statement.action.optype == LazyOpType.ISIN:
                # print(type(statement.sources[0]))
                # print(statement.action.args[0])
                # print(type(statement.action.args[0].query_node.result))
                statement.result = statement.sources[0].result.__getattribute__(statement.action.optype)(*statement.action.args, **statement.action.kwargs)
            # changes for parquet

            if statement.action.optype == LazyOpType.QUANTILE:
                obj = statement.sources[0].result
                method = getattr(obj, statement.action.optype)
                kwargs = dict(statement.action.kwargs)
                if isinstance(obj, library.Series) and statement.action.optype == 'quantile':
                    kwargs.pop('axis', None)
                statement.result = method(*statement.action.args, **kwargs)
            else:
                statement.result = statement.sources[0].result.__getattribute__(statement.action.optype)(*statement.action.args, **statement.action.kwargs)

        elif statement.action.optype in LazyOpType.BINARY_OP:
            assert len(statement.sources) == 2, f'Was expecting two sources, got {len(statement.sources)} for this {statement}'
            # print(statement.action.optype, statement.sources[1].__result)
            # if statement.action.optype == LazyOpType.GT:
            #     print(statement.sources[0].__result.compute() > 0)
            assert isinstance(statement.sources[0], BaseNode),f'Expecting a Node element, got {statement.sources[0]}'
            # print('--------------', statement)
            # print(statement.sources[0].result)
            # print(statement.sources[1].result)

            # statement.sources[0].result.visualize()
            try:
                # print(statement.action.optype, statement.action.args, statement.action.kwargs)
                statement.result = statement.sources[0].result.__getattribute__(statement.action.optype)(statement.sources[1].result)
            except AttributeError:
                # dd.Scalar doesnot have funcs like mul, add, div etc. So we must we magic methods
                statement.result = statement.sources[0].result.__getattribute__(f"__{statement.action.optype}__")(statement.sources[1].result)
        # Modified on 19/03/2024 by Bhu as error in PANDAS --- TypeError: dropna() takes 1 positional argument but 4 were given
        # elif statement.action.optype == LazyOpType.DROPNA:
        #     axis, how, subset = statement.action.kwargs.pop('axis'), statement.action.kwargs.pop('how'), statement.action.kwargs.pop('subset')
        #     if executor == BackendEngines.DASK:
        #         statement.result = statement.sources[0].result.dropna(how, subset)  # Dask doesn't support axis here
        #     else:
        #         statement.result = statement.sources[0].result.dropna(axis, how, subset)  # Dask doesn't support axis here

        elif statement.action.optype == LazyOpType.DROPNA:
            if executor == BackendEngines.DASK:
                # bhu changes feb 14 2025 for default dropna
                axis, how, subset = statement.action.kwargs.pop('axis', 0), statement.action.kwargs.pop('how','any'), statement.action.kwargs.pop('subset',None)
                if statement.old_node.shape[1] == 1:  # For Series
                    statement.result = statement.sources[0].result.dropna()  # Dask doesn't support axis here
                else:  # For DataFrame
                    statement.result = statement.sources[0].result.dropna(how, subset)  # Dask doesn't support axis here
            else:
                if statement.old_node.shape[1] == 1:  # For Series
                    statement.action.kwargs.pop('subset')
                    statement.result = statement.sources[0].result.dropna(*statement.action.args, **statement.action.kwargs)  # Dask doesn't support axis here
                else:  # For DataFrame
                    statement.result = statement.sources[0].result.dropna(*statement.action.args, **statement.action.kwargs)  # Dask doesn't support axis here

        # Chiranmoy 5-2-24: support for loc, iloc, at and iat
        elif statement.action.optype in LazyOpType.INDEX_SLICE:
            row_key = statement.sources[1].result if isinstance(statement.action.kwargs['row_key'], BaseFrame) else statement.action.kwargs['row_key']
            # Chiranmoy 9-4-24: col_key is a column name or List[column name] but never a DataFrame
            # col_key = statement.sources[1].result if isinstance(statement.action.kwargs['col_key'], BaseFrame) else statement.action.kwargs['col_key']
            col_key = statement.action.kwargs['col_key']
            if col_key:
                statement.result = statement.sources[0].result.__getattribute__(statement.action.optype)[row_key, col_key]
            else:
                statement.result = statement.sources[0].result.__getattribute__(statement.action.optype)[row_key]
            del row_key, col_key

        elif statement.action.optype == LazyOpType.SHAPE:
            shape = list(statement.old_node.result.shape)
            shape[0] = dask_compute(shape[0]) if executor == BackendEngines.DASK else shape[0]
            statement.result = tuple(shape)

        elif statement.action.optype == LazyOpType.LEN:
            shape = statement.old_node.result.shape
            statement.result = dask_compute(shape[0]) if executor == BackendEngines.DASK else shape[0]

        elif statement.action.optype == LazyOpType.CELL_COUNT:
            size = statement.old_node.result.size
            statement.result = dask_compute(size) if executor == BackendEngines.DASK else size

        elif statement.action.optype == LazyOpType.COL_RENAME:
            if statement.old_node.in_deg > 1:
                statement.result = deepcopy(statement.old_node.result)  # multiple nodes depend on old_node, do deepcopy
            else:
                statement.result = statement.old_node.result  # only this statement depends on old_node, do soft-copy
            statement.result.columns = statement.columns

        elif statement.action.optype == LazyOpType.INDEX_RENAME:
            if statement.old_node.in_deg > 1:
                statement.result = deepcopy(statement.old_node.result)  # multiple nodes depend on old_node, do deepcopy
            else:
                statement.result = statement.old_node.result  # only this statement depends on old_node, do soft-copy
            statement.result.index = statement.action.kwargs['index']

        elif statement.action.optype == LazyOpType.COMPUTE:
            # if executor != BackendEngines.DASK:
            #     raise TypeError(f'Inappropriate executor. It must be DASK for compute(). Got {executor}')
            # statement.result = dask_compute_multi(library, *[op.result for op in statement.sources])
            # return statement.result

            # Chiranmoy: For non-Dask backend i.e., Modin and Pandas the result is already stored in sources' result field
            # returning them instead of throwing error
            if executor == BackendEngines.DASK:
                # statement.sources[0].result.optimize().visualize(ilename='003.png')
                statement.result = dask_compute_multi(library, *[op.result for op in statement.sources])
                return statement.result
            else:
                return [node.result for node in statement.sources]
        else:
            print(type(statement.action.optype))
            raise NotImplementedError(f"Wasn't expecting {statement.action.optype}. May be you forgot to add this method in LazyOpType")
    # print(statement, hex(id(statement)),hex(id(statement.old_node)), type(statement.result))

    if statement.in_deg > 1 and executor == BackendEngines.DASK and statement.action.optype != LazyOpType.GROUPBY:
        try:
            # SIGMOD REDUNDANT PERSIST temp fix
            if not (statement.in_deg == 2 and statement.redundant_persist):
                statement.result = dask_persist(statement.result)
                print("persisting:",statement)
            else:
                print("\n***** ----- ***** ----- ***** REDUNDANT PERSIST ***** ----- ***** ----- *****\n")
        except Exception as e:
            pass


def execute_lazy_print(statement, library, executor):
    if executor != BackendEngines.DASK:
        __printer(statement.sources, **statement.action.kwargs)
    else:
        sources = list(filter(lambda x: x.action.optype != LazyOpType.PRINT, statement.sources))
        dask_results = [x.result for x in sources]

        for res, src in zip(dask_compute_multi(library, *[src.result for src in sources]), sources):
            src.result = res

        __printer(statement.sources, **statement.action.kwargs)

        for src, res in zip(sources, dask_results):
            src.result = res


def execute_read_csv(library, executor: BackendEngines, statement: BaseNode):
    filepath_or_buffer = statement.action.kwargs.get('filepath_or_buffer', [])
    # print(f'READING {filepath_or_buffer}')
    assert len(filepath_or_buffer) > 0, 'Expecting a source for read_csv'
    usecols = statement.action.kwargs.get('usecols', [])
    if callable(usecols):
        print('Callable usecols not supported. Fallback to list')
        usecols = []

    if len(usecols) == 0:
        usecols = [x for x in statement.outcols if x in statement.allcols]
    # usecols = []

    kwargs = statement.action.kwargs.copy()
    kwargs.pop('filepath_or_buffer')
    if len(usecols) == 0:
        kwargs.pop('usecols', [])
        usecols = list(statement.allcols.keys())
        # print('Reading all columns', filepath_or_buffer)

        # parse_dates should only contain columns that are being read
    parse_dates = [x for x in statement.action.kwargs.get('parse_dates', []) if x in usecols]
    kwargs['usecols'] = usecols
    kwargs['parse_dates'] = parse_dates

    # print('Reading', filepath_or_buffer)
    # print('Usecols', usecols)
    # print('With parse_dates', parse_dates)
    print('read_csv args', statement.action.args, statement.action.kwargs)
    if executor == BackendEngines.DASK:
        index_cols = []
        kwargs['assume_missing'] = True
        # Keywords 'index' and 'index_col' not supported. Use dd.read_csv(...).set_index('my-index') instead
        if kwargs.get('index_col', None) is not None:
            index_cols = kwargs.pop('index_col')
        elif kwargs.get('index', None) is not None:
            index_cols = kwargs.pop('index')

        # print('Reading', usecols)
        statement.result = library.read_csv(filepath_or_buffer, *statement.action.args, **kwargs)
        if index_cols:
            statement.result = statement.result.set_index(index_cols)
    else:
        #     if len(statement.action.kwargs.get('filepath_or_buffer')) > 1:
        #         raise NotImplementedError('Miltiple files not supported in pandas executor')
        #     statement.result = library.read_csv(filepath_or_buffer[0], *statement.action.args, **statement.action.kwargs, **kwargs)
        #     continue
        statement.result = library.read_csv(filepath_or_buffer[0], *statement.action.args, **kwargs)
        # statement.result = library.concat([library.read_csv(f, *statement.action.args, **kwargs) for f in filepath_or_buffer])


def execute_read_parquet(library, executor: BackendEngines, statement: BaseNode):
    '''
    Read a Parquet file into a Dataframe (depending on library)
    '''
    filepath_or_buffer = statement.action.kwargs.get('filepath_or_buffer', [])
    assert len(filepath_or_buffer) > 0, 'Expecting a source for read_parquet'
    columns = statement.action.kwargs.get('columns', [])
    if callable(columns):
        print('Callable usecols not supported. Fallback to list')
        columns = []

    if len(columns) == 0:
        columns = [x for x in statement.outcols if x in statement.allcols]

    kwargs = statement.action.kwargs.copy()
    kwargs.pop('filepath_or_buffer')
    if len(columns) == 0:
        kwargs.pop('columns', [])
        columns = list(statement.allcols.keys())

    # parse_dates should only contain columns that are being read
    # parse_dates = [x for x in statement.action.kwargs.get('parse_dates', []) if x in columns]
    kwargs['columns'] = columns
    # kwargs['parse_dates'] = parse_dates

    # print('Reading', filepath_or_buffer)
    # print('Usecols', columns)
    # print('With parse_dates', parse_dates)

    if executor == BackendEngines.DASK:
        statement.result = library.read_parquet(filepath_or_buffer, *statement.action.args, split_row_groups=True, **kwargs)
    else:
        statement.result = library.concat([library.read_parquet(f, *statement.action.args, **kwargs) for f in filepath_or_buffer])
        # commented below and added above to avoid parquet error
        # statement.result = library.concat([library.read_parquet(f, *statement.action.args, split_row_groups=True, **kwargs) for f in filepath_or_buffer])
    # print('-'*50)
    # statement.result.info()
    # print()
    # print('-'*50)
    # print()


def default_to_pandas_dask(statement: BaseNode, dask):
    # print(f'*** DEFAULTING TO PANDAS for {statement} ***')

    import pandas
    dask_sources_results, dask_old_node_result = [src.result for src in statement.sources], None if statement.old_node is None else statement.old_node.result

    for src in statement.sources:
        if isinstance(src.result, dask.Series) or isinstance(src.result, dask.DataFrame):
            src.result = src.result.compute()
    if statement.old_node is not None and (isinstance(statement.old_node.result, dask.Series) or isinstance(statement.old_node.result, dask.DataFrame)):
        statement.old_node.result = statement.old_node.result.compute()

    execute_statement(statement, pandas, BackendEngines.PANDAS)
    if isinstance(statement.result, pandas.Series) or isinstance(statement.result, pandas.DataFrame):
        statement.result = dask.from_pandas(statement.result, npartitions=DASK_PARTITIONS)

    for src, res in zip(statement.sources, dask_sources_results):
        src.result = res
    if statement.old_node is not None:
        statement.old_node.result = dask_old_node_result


def handle_series_sort(statement: BaseNode, executor):
    """
     Chiranmoy 16-3-24: Steps
         1. Convert Series to DataFrame
         2. Sort the DataFrame
         3. Convert the sorted DataFrame back to Series
    """
    statement.action.kwargs.pop('by', None)

    if executor == BackendEngines.DASK:
        dask_node = statement.old_node.result
        dask_node = dask_node.to_frame("__temp_col")
        dask_node = dask_node.sort_values("__temp_col", *statement.action.args, **statement.action.kwargs)
        dask_node = dask_node.squeeze()
        dask_node.name = None
        return dask_node

    return statement.sources[0].result.__getattribute__(statement.action.optype)(*statement.action.args, **statement.action.kwargs)


def handle_dask_sort_index(statement: BaseNode, type_series):
    """
     Chiranmoy 16-3-24: Steps
         1. If node is a Series, Convert Series to DataFrame
         2. Add the index as a column
         3. Sort the DataFrame using the index column
         4. Drop the index column
         3. If node is a Series, Convert the sorted DataFrame back to Series
    """
    dask_node = statement.old_node.result
    if isinstance(statement.old_node.result, type_series):
        dask_node = dask_node.to_frame("__temp_col")

    dask_node["__temp_index_col"] = dask_node.index
    dask_node = dask_node.sort_values('__temp_index_col', *statement.action.args, **statement.action.kwargs)
    dask_node = dask_node.drop(columns='__temp_index_col')

    if isinstance(statement.old_node.result, type_series):
        dask_node = dask_node.squeeze()
        dask_node.name = None

    # Chiranmoy 6-3-24:
    # dask_node = statement.old_node.result
    # dask_node.index = dask_node.index.rename("__temp_index_col")
    # dask_node = dask_node.reset_index()
    # dask_node = dask_node.sort_values('__temp_index_col', *statement.action.args, **statement.action.kwargs)
    # dask_node = dask_node.set_index('__temp_index_col', sort=False)
    return dask_node


def clear_intermediate_results(statement):
    # Chiranmoy 19-3-24: clear intermediate node results, in_deg > 0 => node is still useful
    for src in statement.sources:
        src.in_deg -= 1
        if src.in_deg == 0:
            src.result = None

    # decrement only if 'old_node' is not present in the 'sources' list
    if statement.old_node and statement.old_node.get_id() not in [src.get_id() for src in statement.sources]:
        statement.old_node.in_deg -= 1
        if statement.old_node.in_deg == 0:
            statement.old_node.result = None

    # collect_garbage()  # force garbage collection


def dask_compatibility_check(statement):
    def is_series():
        return statement.shape[1] == 1

    if statement.action.optype == LazyOpType.LOC_INDEXER_SETITEM:
        raise DaskUnsupported("Assignment via loc, iloc, at and iat unsupported by Dask. Syntax 'df.loc[...] = ...' is unsupported.", LazyOpType.LOC_INDEXER_SETITEM)
    elif statement.action.optype == LazyOpType.SERIES:
        raise DaskUnsupported("pandas.Series is not supported by Dask", LazyOpType.SERIES)
    elif statement.action.optype == LazyOpType.APPLY and not is_series() and statement.action.kwargs.get('axis', 0) == 0:
        raise DaskUnsupported("dask.DataFrame.apply only supports axis=1", LazyOpType.APPLY, axis=0)


def modin_compatibility_check(statement):
    pass


def dask_compute(task_graph):
    # print('Executing single', '-'*50)

    # from dask.distributed import Client
    # client = Client(n_workers=2, threads_per_worker=8)
    # s = time()
    # task_graph.visualize()

    res = task_graph.compute()
    # client.close()
    # print('More internal', time()-s)
    return res


def dask_compute_multi(dask, *task_graphs):
    # dask.optimize(*task_graphs).print()
    # task_graphs[0].dask.visualize(filename='transpose-hlg.svg')
    # print('Executing multi', '-'*50)
    # from dask.distributed import Client, performance_report, LocalCluster
    # cluster = LocalCluster(n_workers=1, threads_per_worker=1, memory_limit='10GB')
    # client = Client(cluster)
    # client = Client(n_workers=1, threads_per_worker=4, memory_limit='12GB')
    # s = time()
    # from distributed.diagnostics import MemorySampler
    # with performance_report():
    # print(*task_graphs)
    # return [0]*8
    # print()
    # print('Not executing', '='*50)

    # from dask.base import collections_to_dsk, unpack_collections
    # collections, repack = unpack_collections(*task_graphs)
    # dsk = collections_to_dsk(collections, True)
    # dsk is optimized graph

    # from dask.base import visualize
    # visualize(*task_graphs, filename='002.png', optimize_graph=True)
    # visualize(*task_graphs,filename="transpose.svg", optimize_graph=True)


    # return [1]*len(task_graphs)
    res = dask.compute(*task_graphs)
    # ms.sample('yellow_taxi6', interval=2)
    # ms.to_pandas.to_csv('mem_profile.csv')
    # client.close()
    # print('More internal', time()-s)
    return res


def dask_persist(task_graph):
    """
    Chiranmoy 30-1-24
    """
    return task_graph.persist()
    # return task_graph

