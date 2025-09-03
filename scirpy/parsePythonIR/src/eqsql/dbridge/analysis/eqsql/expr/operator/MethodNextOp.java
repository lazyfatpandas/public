package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 27/10/16.
 * Operator to represent collection iterator next()
 */
public class MethodNextOp extends Operator{
    public MethodNextOp() {
        super("Next()", OpType.MethodNext, 0);
        /* No children */
    }
}
