package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 27/10/16.
 * Operator to represent java collection iterator()
 */
public class MethodIteratorOp extends Operator{
    public MethodIteratorOp() {
        super("Iterator()", OpType.MethodIterator, 0);
        /* No children */
    }
}
