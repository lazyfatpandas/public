package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.NotEqOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by ek on 17/10/16.
 */
public class NotEqNode extends Node implements HQLTranslatable, SQLTranslatable {
    public NotEqNode(Node lhs, Node rhs) {
        super(new NotEqOp(), lhs, rhs);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        String lhsSQL = ((HQLTranslatable) children[0]).toHibQuery();

        Node rhs = children[1];
        if(rhs instanceof ValueNode && ((ValueNode)rhs).isNull()){
            return lhsSQL + " is not null";
        }
        else{
            String rhsSQL = ((HQLTranslatable) rhs).toHibQuery();
            return lhsSQL + " <> " + rhsSQL;
        }
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        String lhsSQL = ((SQLTranslatable) children[0]).toSQLQuery();

        Node rhs = children[1];
        if(rhs instanceof ValueNode && ((ValueNode)rhs).isNull()){
            return lhsSQL + " is not null";
        }
        else{
            String rhsSQL = ((HQLTranslatable) rhs).toHibQuery();
            return lhsSQL + " <> " + rhsSQL;
        }
    }
}
