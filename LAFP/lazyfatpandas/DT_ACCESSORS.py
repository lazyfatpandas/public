# DATE TIME dt
from .base import BaseFrame
from .LazyOp import (LazyOp, LazyOpType)


class DtAccessors(BaseFrame):

    def __init__(self):
        pass

    def date(self):
        return BaseFrame([self], LazyOp(LazyOpType.DATE))

    def time(self):
        return BaseFrame([self], LazyOp(LazyOpType.TIME))

    def year(self):
        return BaseFrame([self], LazyOp(LazyOpType.YEAR))

    def month(self):
        return BaseFrame([self], LazyOp(LazyOpType.MONTH))

    def day(self):
        return BaseFrame([self], LazyOp(LazyOpType.DAY))

    def hour(self):
        return BaseFrame([self], LazyOp(LazyOpType.HOUR))

    def minute(self):
        return BaseFrame([self], LazyOp(LazyOpType.MINUTE))

    def second(self):
        return BaseFrame([self], LazyOp(LazyOpType.SECOND))
