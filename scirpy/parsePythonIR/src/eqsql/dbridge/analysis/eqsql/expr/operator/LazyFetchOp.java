package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by venkatesh on 9/7/17.
 * Operator to represent lazy fetch of an attribute.
 */
public class LazyFetchOp extends Operator {

    public LazyFetchOp() {
        super("LazyFetch", OpType.LazyFetch, 1);
        /* 1 child will be the FieldRef to be lazily fetched. */
    }
}
