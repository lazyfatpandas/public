package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 23/5/16.
 */
public class ExistOp extends Operator {
    public ExistOp() {
        super("Exists", OpType.Exist, 1);
        /* 1 operand will be a query */
    }
}
