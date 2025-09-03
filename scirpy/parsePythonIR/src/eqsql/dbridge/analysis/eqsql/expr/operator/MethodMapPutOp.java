package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by K. Venkatesh Emani on 3/9/2017.
 */
public class MethodMapPutOp extends Operator {
    public MethodMapPutOp() {
        super("MapPut()", OpType.MethodMapPut, 0);
        /* No children because this op is used only to represent the method name */
    }
}
