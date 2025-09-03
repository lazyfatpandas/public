package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.NotExistOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by ek on 17/10/16.
 */
public class NotExistNode extends Node implements HQLTranslatable, SQLTranslatable {
    public NotExistNode(ProjectNode query) {
        super(new NotExistOp(), query);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        String querySQL = ((HQLTranslatable) children[0]).toHibQuery();
        return "NOT EXISTS (" + querySQL + ")";
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        String querySQL = ((SQLTranslatable) children[0]).toSQLQuery();
        return "NOT EXISTS (" + querySQL + ")";
    }
}
