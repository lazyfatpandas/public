package eqsql.dbridge.analysis.eqsql.expr.operator;

import soot.Value;

/**
 * Created by ek on 26/10/16.
 * Operator which holds a soot value
 */
public class ValueOp extends Operator {
    private Value value;

    public ValueOp(Value value) {
        super("SootValue", OpType.Value, 0);
        /* 0 operands. The soot value will be contained as an attribute */
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
