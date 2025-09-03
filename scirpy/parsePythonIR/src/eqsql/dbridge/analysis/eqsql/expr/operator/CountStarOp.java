package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by K. Venkatesh Emani on 2/16/2017.
 */
public class CountStarOp extends Operator {
    public CountStarOp() {
        super("count(*)", OpType.CountStar, 0);
        /* leaf, no children */
    }
}
