package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by K. Venkatesh Emani on 3/9/2017.
 */
public class GroupByOp extends Operator{
    public GroupByOp() {
        super("group by", OpType.GroupBy, 2);
        /* Two children will be: query without group by columns (ProjectNode), group by columns*/
    }
}
