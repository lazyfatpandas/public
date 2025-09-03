package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.SeqOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by venkatesh on 13/7/17.
 */
public class SeqNode extends Node implements HQLTranslatable, SQLTranslatable{
    public SeqNode(Node predecessor, Node follower) {
        super(new SeqOp(), predecessor, follower);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        /* As of now, we are translating it into a union query. But it
         * should be a sequential region. */
        return ((HQLTranslatable)children[0]).toHibQuery()
                + " union " +
                ((HQLTranslatable)children[1]).toHibQuery();
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        /* As of now, we are translating it into a union query. But it
         * should be a sequential region. */
        return ((SQLTranslatable)children[0]).toSQLQuery()
                + " union " +
                ((SQLTranslatable)children[1]).toSQLQuery();
    }
}
