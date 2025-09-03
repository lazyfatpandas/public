package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.ArithAddOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by K. Venkatesh Emani on 1/10/2017.
 * Node representing arithmetic addition operation.
 */
public class ArithAddNode extends Node implements HQLTranslatable, SQLTranslatable {
    public ArithAddNode(Node op1, Node op2) {
        super(new ArithAddOp(), op1, op2);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        String op1Query = ((HQLTranslatable)children[0]).toHibQuery();
        String op2Query = ((HQLTranslatable)children[1]).toHibQuery();
        return "(" + op1Query + " + " + op2Query + ")";
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        String op1Query = ((SQLTranslatable)children[0]).toSQLQuery();
        String op2Query = ((SQLTranslatable)children[1]).toSQLQuery();
        return "(" + op1Query + " + " + op2Query + ")";
    }
}
