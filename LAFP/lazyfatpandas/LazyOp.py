from enum import Enum
from sys import getsizeof


class Meta(object):
    def __init__(self, source, column, dtype):
        self.source = source
        self.column = column
        self.dtype = dtype
        self.valid = True
        self.index = False
        self.bytes = getsizeof(dtype)

    def invalidate(self):
        self.valid = False

    def __repr__(self) -> str:
        return f'Column: {self.column}, DType: {self.dtype}, Source: {self.source}, size: {self.bytes}'


class LazyOp(object):
    optype = 'Empty'
    args = ()
    kwargs = ()

    def __init__(self, optype, *args, **kwargs) -> None:
        self.optype = optype
        self.args = args
        self.kwargs = kwargs

    def __repr__(self) -> str:
        if self.optype == LazyOpType.LITERAL:
            return repr(self.args[0]) if len(self.args) > 0 else LazyOpType.LITERAL
        return f'{self.optype}'
        # return f'{self.optype}, {self.args}, {self.kwargs}'

    def affects_order(self):
        return self.optype in LazyOpType.CAN_CHANGE_ORDER# or self.optype in LazyOpType.AGGREGATE


class StrAccFunc(Enum):
    split = 'split'
    slice = 'slice'
    len = 'len'
    lower = 'lower'
    contains = 'contains'
    replace = 'replace'
    cat = 'cat'
    startswith = 'startswith'


class DtAccProps(Enum):
    # Datetime properties
    date = 'date'
    time = 'time'
    timez = 'timez'
    year = 'year'
    month = 'month'
    month_name = 'month_name'
    day = 'day'
    hour = 'hour'
    minute = 'minute'
    second = 'second'
    seconds = 'seconds'
    microsecond = 'microsecond'
    nanosecond = 'nanosecond'
    week = 'week'
    dayofweek = 'dayofweek'
    day_of_week = 'day_of_week'
    weekday = 'weekday'
    dayofyear = 'dayofyear'
    day_of_year = 'day_of_year'
    quarter = 'quarter'
    is_month_start = 'is_month_start'
    is_month_end = 'is_month_end'
    is_quarter_start = 'is_quarter_start'
    is_quarter_end = 'is_quarter_end'
    is_year_start = 'is_year_start'
    is_year_end = 'is_year_end'
    is_leap_year = 'is_leap_year'
    daysinomnth = 'daysinmonth'
    # days_in_month = 'days_in_month'

    # BUG: These two gives error "Can't handle meta of type 'NoneType"
    # tz = 'tz'
    # freq = 'freq'

    # TODO: Add Datetime methods
    # TODO: Add Datetime periods
    # TODO: Add Datetime timedelta
    # TODO: Add Datetime methods

# TODO: Add String accessors
# TODO: Add Categorical accessors
# TODO: Add Sparse accessors
# TODO: Add support for custom accessors


class LazyOpType(object):

    dt = DtAccProps
    str = StrAccFunc

    # Conversion
    ASTYPE = 'astype'
    CONVERT_DTYPES = 'convert_dtypes'
    INFER_OBJECTS = 'infer_objects'
    COPY = 'copy'
    TO_DATETIME = 'to_datetime'
    TO_FRAME='to_frame'

    CONVERSION = [ASTYPE, CONVERT_DTYPES, INFER_OBJECTS, COPY, TO_FRAME]

    # Indexing
    AT = 'at'
    IAT = 'iat'
    LOC = 'loc'
    ILOC = 'iloc'
    ISIN = 'isin'
    WHERE = 'where'
    GET_ITEM = '__getitem__'
    SET_ITEM = '__setitem__'

    INDEX_SLICE = [AT, IAT, LOC, ILOC]
    INDEXING = [ISIN, WHERE, GET_ITEM, SET_ITEM]

    REPLACE = 'replace'
    DROP = 'drop'
    INSERT = 'insert'
    READ_CSV = 'read_csv'
    READ_PARQUET = 'read_parquet'

    READER = [READ_CSV, READ_PARQUET]

    DROPNA = 'dropna'
    DROP_DUPLICATE = 'drop_duplicates'

    JOIN = 'join'
    MERGE = 'merge'
    FILTER = 'filter'
    RESET_INDEX = 'reset_index'
    SET_INDEX = 'set_index'
    SORT_INDEX = 'sort_index'
    RENAME = 'rename'
    SORT_VALUES = 'sort_values'
    NLARGEST='nlargest'
    NSMALLEST='nsmallest'
    ROUND='round'
    GENERAL_FUNC = [JOIN, MERGE]
    RESHAPING_SORTING = [DROP, DROP_DUPLICATE, SORT_VALUES, INSERT]
    COMMON_OPS=[NLARGEST]
    EXPLODE = 'explode'
    APPLY = 'apply'
    AGGREGATE = 'agg'
    GROUPBY = 'groupby'
    CONCAT='concat'
    MAP='map'
    MISC = [AGGREGATE, APPLY, EXPLODE,CONCAT,MAP]

    # Computational
    ABS = 'abs'
    ALL = 'all'
    ANY = 'any'
    COUNT = 'count'
    MAX = 'max'
    MIN = 'min'
    SUM = 'sum'
    MEAN = 'mean'
    MODE = 'mode'

    # Missing data handling
    ISNA='isna'
    FILLNA='fillna'
    REPLACE='replace'
    ISNULL='isnull'
    MISSING_DATA_HANDLING = [ISNA, FILLNA, RENAME, REPLACE,ISNULL]

    # Comparators
    AND = '__and__'
    OR = '__or__'
    LE = '__le__'
    GE = '__ge__'
    GT = '__gt__'
    LT = '__lt__'
    NE = '__ne__'
    EQ = '__eq__'

    ADD = 'add'
    SUB = 'sub'
    MUL = 'mul'
    # DIV = 'div'  # Chiranmoy: __div__ is only for Python 2.x
    DIV = 'truediv'
    FLOORDIV = 'floordiv'
    MOD = 'mod'
    POW = 'pow'
    DOT = 'dot'
    QUANTILE = 'quantile'

    COMPUTATIONAL = [ABS, ALL, ANY, COUNT, MAX, MIN, SUM, MEAN, MODE, QUANTILE]
    COMPARATORS = [AND, OR, LE, LT, GE, GT, NE, EQ]
    RELATIONAL_OP = [ADD, SUB, MUL, DIV, FLOORDIV, MOD, POW, DOT]
    BIT_AND = '__and__'
    BIT_OR = '__or__'

    CONDITION_CONNECTORS = [BIT_AND, BIT_OR]

    BIT_OP = [BIT_OR, BIT_AND]
    BINARY_OP = [*COMPARATORS, *RELATIONAL_OP, *BIT_OP]

    ASSIGN = 'assign'
    LITERAL = 'literal'

    TO_DATAFRAME='to_dataframe'
    TO_SERIES='to_series'
    TO_TIMEDELTA='to_timedelta'

    DATE = 'date'
    TIME = 'time'
    YEAR = 'year'
    MONTH = 'month'
    DAY = 'day'
    HOUR = 'hour'
    MINUTE = 'minute'
    SECOND = 'second'

    DT = [DATE, TIME, YEAR, MONTH, DAY, HOUR, MINUTE, SECOND]

    DESCRIBE = 'describe'
    HEAD = 'head'
    TAIL = 'tail'
    INFO = 'info'
    MEMORY_USAGE = 'memory_usage'

    INFORMATION = [DESCRIBE, HEAD, TAIL, MEMORY_USAGE]
    TO_NUMERIC = "to_numeric"
    TO_STRING = "to_string"

    PANDAS_LEVEL = [TO_NUMERIC]

    DROPLEVEL = 'droplevel'
    QUERY = 'query'
    #bhu
    COPY='copy'
    APPEND='append'
    DUPLICATED='duplicated'
    UNSTACK='unstack'
    STACK='stack'
    PLOT='plot'
    IDXMAX='idxmax'
    IDXMIN='idxmin'
    VALUE_COUNTS='value_counts'
    CORR='corr'

    # Chiranmoy 31-1-24: APIs
    FROM_CACHE = 'from_cache'
    BETWEEN = 'between'
    PIVOT_TABLE = 'pivot_table'
    UNIQUE = 'unique'
    NUNIQUE = 'nunique'
    TO_CSV = 'to_csv'
    TO_DICT = 'to_dict'
    TO_LIST = 'to_list'
    SAMPLE = 'sample'
    INDEX = 'index'
    SIZE = 'size'
    GET_DUMMIES = 'get_dummies'
    PRINT = 'print'
    EWM = 'ewm'
    DATAFRAME = 'dataframe'
    LOC_INDEXER_SETITEM = 'loc_indexer_setitem'
    COL_RENAME = 'col_rename'  # columns rename using "df.columns = [...]" syntax
    LEN = 'len'
    SHAPE = 'shape'
    CELL_COUNT = 'CELL_COUNT'  # for df.shape
    INDEX_RENAME = 'index_rename'
    POP = 'pop'

    CONVERSION.extend([TO_CSV, TO_DICT, TO_LIST])
    UNIQUENESS = [NUNIQUE, UNIQUE]

    PREF_DEF = [*UNIQUENESS, *MISSING_DATA_HANDLING, *COMPUTATIONAL, *MISC, *RESHAPING_SORTING, QUANTILE, *GENERAL_FUNC,
                *DT, *INFORMATION, ASTYPE, DROPLEVEL, QUERY, VALUE_COUNTS, PIVOT_TABLE, BETWEEN, ISIN, SAMPLE, SIZE,
                EWM, NLARGEST, NSMALLEST, *CONVERSION, POP]
    CAN_CHANGE_ORDER = [DROP_DUPLICATE, DROPNA, EXPLODE, GROUPBY, *GENERAL_FUNC]

    DEFAULT_COMPUTE = [HEAD, TAIL]
    NO_RETURN = [INFO, PRINT]

    COMPUTE = 'compute'

    # series specific lazyops
    SERIES = 'Series'

    PUSH_DOWN_WHITELIST_OP = [SORT_VALUES, SET_ITEM, GET_ITEM]

# ParamMap = {
#     BackendEngines.DASK: {
#         LazyOpType.HEAD: ['compute']
#     }
# }
