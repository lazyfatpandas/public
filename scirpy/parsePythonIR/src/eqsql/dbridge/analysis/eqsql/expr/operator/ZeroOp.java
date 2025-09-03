package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 1/11/16.
 */
public class ZeroOp extends Operator {
    public ZeroOp() {
        super("0", OpType.Zero, 0);
        /* 0 operands. */
    }
}
