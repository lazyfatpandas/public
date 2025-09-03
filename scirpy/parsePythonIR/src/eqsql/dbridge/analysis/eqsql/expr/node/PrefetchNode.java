package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.PrefetchOp;

public class PrefetchNode extends LeafNode {
    public PrefetchNode(String relName) {
        super(new PrefetchOp(relName));
    }
}
