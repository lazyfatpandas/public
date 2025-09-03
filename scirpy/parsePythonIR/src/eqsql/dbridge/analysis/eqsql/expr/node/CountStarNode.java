package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.CountStarOp;

/**
 * Created by K. Venkatesh Emani on 2/16/2017.
 */
public class  CountStarNode extends LeafNode implements HQLTranslatable, SQLTranslatable {
    public CountStarNode(){
        super(new CountStarOp());
    }

    @Override
    public String toHibQuery() {
        return "count(*)";
    }

    @Override
    public String toSQLQuery() {
        return "count(*)";
    }
}
