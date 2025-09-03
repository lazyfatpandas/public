package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.TernaryOp;

/**
 * Created by ek on 17/10/16.
 */
public class TernaryNode extends Node {
    public TernaryNode(Node condition, Node trueDag, Node falseDag) {
        super(new TernaryOp(), condition, trueDag, falseDag);
    }
}
