package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.ZeroOp;

/**
 * Created by ek on 26/10/16.
 */
public class ZeroNode extends LeafNode implements HQLTranslatable, SQLTranslatable {
    public ZeroNode() {
        super(new ZeroOp());
    }

    @Override
    public String toHibQuery() {
        return "0";
    }

    @Override
    public String toSQLQuery() {
        return "0";
    }
}
