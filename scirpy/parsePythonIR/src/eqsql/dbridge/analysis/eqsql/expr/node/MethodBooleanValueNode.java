package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.MethodBooleanValueOp;

/**
 * Created by K. Venkatesh Emani on 12/20/2016.
 */
public class MethodBooleanValueNode extends LeafNode implements MethodRef {
    public MethodBooleanValueNode() {
        super(new MethodBooleanValueOp());
    }
}
