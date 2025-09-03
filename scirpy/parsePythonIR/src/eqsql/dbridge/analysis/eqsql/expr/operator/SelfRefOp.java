package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 27/10/16.
 */
public class SelfRefOp extends Operator {
    public SelfRefOp() {
        super("SelfRef", OpType.SelfRef, 0);
        /* 0 operands as there are no children. */
    }
}
