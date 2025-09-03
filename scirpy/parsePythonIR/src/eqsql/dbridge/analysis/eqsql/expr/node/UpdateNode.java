package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.UpdateOp;

/**
 * Created by ek on 27/10/16.
 */
public class UpdateNode extends LeafNode {
    public UpdateNode() {
        super(new UpdateOp());
    }
}
