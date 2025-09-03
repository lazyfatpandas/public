package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.IfOp;

/**
 * Created by ek on 17/10/16.
 */
public class IfNode extends Node {
    public IfNode(Node condition, Node trueDag) {
        super(new IfOp(), condition, trueDag);
    }
}
