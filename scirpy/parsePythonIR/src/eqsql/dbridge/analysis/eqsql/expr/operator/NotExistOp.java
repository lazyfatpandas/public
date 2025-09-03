package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 23/5/16.
 */
public class NotExistOp extends Operator {
    public NotExistOp() {
        super("Not Exists", OpType.NotExist, 1);
        /* 1 operand will be: query */
    }
}
