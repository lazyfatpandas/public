package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.PlaceholderVarOp;

/**
 * Created by ek on 5/11/16.
 * A placeholder variable
 */
public class PlaceholderVarNode extends LeafNode {

    public PlaceholderVarNode() {
        super(new PlaceholderVarOp());
    }

    @Override
    public String toString() {
        return "<v" + ((PlaceholderVarOp)operator).getId() + ">";
    }
}
