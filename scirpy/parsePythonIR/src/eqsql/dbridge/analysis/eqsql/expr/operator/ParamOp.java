package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 27/10/16.
 * Operator that represents a formal parameter used inside a function.
 */
public class ParamOp extends Operator {

    int index;

    public ParamOp(int index) {
        super("Param", OpType.Param, 0);
        /* 0 operands. Is a leaf node. The parameter number is stored
        as an attribute*/
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
