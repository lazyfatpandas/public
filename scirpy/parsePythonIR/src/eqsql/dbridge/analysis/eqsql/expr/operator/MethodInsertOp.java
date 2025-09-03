package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 23/5/16.
 */
public class MethodInsertOp extends Operator {
    public MethodInsertOp() {
        super("SetInsert()", OpType.MethodInsert, 0);
        /* No children because this op is used only to represent the method name */
    }
}
