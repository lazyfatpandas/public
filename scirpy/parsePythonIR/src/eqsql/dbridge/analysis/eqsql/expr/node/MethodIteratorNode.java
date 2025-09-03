package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.MethodIteratorOp;

/**
 * Created by ek on 27/10/16.
 */
public class MethodIteratorNode extends LeafNode implements MethodRef {
    public MethodIteratorNode() {
        super(new MethodIteratorOp());
    }
}
