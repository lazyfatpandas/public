package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.MethodNextOp;

/**
 * Created by ek on 27/10/16.
 */
public class MethodNextNode extends LeafNode implements MethodRef {
    public MethodNextNode() {
        super(new MethodNextOp());
    }
}
