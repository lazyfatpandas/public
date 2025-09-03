package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.MethodInsertOp;

/**
 * Created by ek on 27/10/16.
 */
public class MethodInsertNode extends LeafNode implements MethodRef {
    public MethodInsertNode() {
        super(new MethodInsertOp());
    }
}
