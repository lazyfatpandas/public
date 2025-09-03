package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.ExistOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by ek on 17/10/16.
 */
public class ExistNode extends Node implements HQLTranslatable, SQLTranslatable {
    public ExistNode(ProjectNode query) {
        super(new ExistOp(), query);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        String querySQL = ((HQLTranslatable) children[0]).toHibQuery();
        return "EXISTS (" + querySQL + ")";
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        String querySQL = ((SQLTranslatable) children[0]).toSQLQuery();
        return "EXISTS (" + querySQL + ")";
    }
}
