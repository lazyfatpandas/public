package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 27/10/16.
 * Operator to represent a database update. Currently,
 * we do not distinguish between different update queries.
 */
public class UpdateOp extends Operator{
    public UpdateOp() {
        super("Update", OpType.Update, 0);
        /* No children */
    }
}
