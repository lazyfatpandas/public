package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.GtOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by ek on 17/10/16.
 */
public class GtNode extends Node implements HQLTranslatable, SQLTranslatable {
    public GtNode(Node lhs, Node rhs)  {
        super(new GtOp(), lhs, rhs);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        String lhsSQL = ((HQLTranslatable) children[0]).toHibQuery();
        String rhsSQL = ((HQLTranslatable) children[1]).toHibQuery();
        return lhsSQL + " > " + rhsSQL;
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        String lhsSQL = ((SQLTranslatable) children[0]).toSQLQuery();
        String rhsSQL = ((SQLTranslatable) children[1]).toSQLQuery();
        return lhsSQL + " > " + rhsSQL;
    }
}
