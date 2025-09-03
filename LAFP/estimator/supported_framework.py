from enum import Enum


class SupportedFrameworks(Enum):
    PANDAS = 1
    DASK = 2
    MODIN = 3
