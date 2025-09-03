package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.MethodMapPutOp;

/**
 * Created by K. Venkatesh Emani on 3/9/2017.
 */
public class MethodMapPutNode extends LeafNode implements MethodRef {
    public MethodMapPutNode() {
        super(new MethodMapPutOp());
    }
}
