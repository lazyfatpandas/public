package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 23/5/16.
 */
public class IfOp extends Operator {
    public IfOp() {
        super("if", OpType.If, 2);
        /* 2 operands will be: condition, true dag */
    }
}
