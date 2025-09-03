package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 23/5/16.
 */
public class TernaryOp extends Operator {
    public TernaryOp() {
        super("?", OpType.Ternary, 3);
        /* 3 operands: condition, trueDag, falseDag */
    }
}
