package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.MethodHasNextOp;

/**
 * Created by ek on 27/10/16.
 */
public class MethodHasNextNode extends LeafNode implements MethodRef {
    public MethodHasNextNode() {
        super(new MethodHasNextOp());
    }
}
