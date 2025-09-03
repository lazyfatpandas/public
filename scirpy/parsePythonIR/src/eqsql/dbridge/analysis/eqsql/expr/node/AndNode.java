package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.AndOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by ek on 17/10/16.
 */
public class AndNode extends Node implements HQLTranslatable, SQLTranslatable {
    public AndNode(Node cond1, Node cond2)  {
        super(new AndOp(), cond1, cond2);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        String cond1SQL = ((HQLTranslatable) children[0]).toHibQuery();
        String cond2SQL = ((HQLTranslatable) children[1]).toHibQuery();
        return cond1SQL + " AND " + cond2SQL;
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        String cond1SQL = ((SQLTranslatable) children[0]).toSQLQuery();
        String cond2SQL = ((SQLTranslatable) children[1]).toSQLQuery();
        return cond1SQL + " AND " + cond2SQL;
    }
}
