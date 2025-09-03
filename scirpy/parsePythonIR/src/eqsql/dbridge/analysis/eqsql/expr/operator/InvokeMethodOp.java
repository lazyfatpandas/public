package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 14/10/16.
 */
public class InvokeMethodOp extends Operator {
    public InvokeMethodOp() {
        super("InvokeMethod", OpType.InvokeMethod, 3);
        /* 3 operands will be: base object, method, list of params (ParamsNode)*/
    }
}
