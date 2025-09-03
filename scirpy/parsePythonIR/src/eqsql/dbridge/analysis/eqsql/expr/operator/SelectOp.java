package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 23/5/16.
 */
public class SelectOp extends Operator {

    public SelectOp() {
        super("Sel", OpType.Select, 2);
        /* Operands will be a query followed by a where clause condition.*/
    }
}
