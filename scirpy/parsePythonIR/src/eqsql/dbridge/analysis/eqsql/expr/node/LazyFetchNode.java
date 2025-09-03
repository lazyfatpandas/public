package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.LazyFetchOp;

/**
 * Created by venkatesh on 9/7/17.
 * LazyFetchNode is pretty similar to FieldRefNode. It may make
 * sense for LazyFetchNode to extend FieldRefNode. Keeping it
 * separate for now.
 */
public class LazyFetchNode extends Node {

    public LazyFetchNode(FieldRefNode frn) {
        super(new LazyFetchOp(), frn);
    }

}
