package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.exceptions.QueryTranslationException;

/**
 * Created by K. Venkatesh Emani on 12/16/2016.
 * All nodes which can be translated to Hibernate Query Language should implement this interface.
 */
public interface HQLTranslatable {
    /* Translation to Hibernate Query Language */
    String toHibQuery() throws QueryTranslationException;
}
