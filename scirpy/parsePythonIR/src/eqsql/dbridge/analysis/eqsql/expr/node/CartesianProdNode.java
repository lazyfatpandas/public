package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.CartesianProdOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by venkatesh on 7/7/17.
 * Node to represent a single relation or a cartesian product of relations.
 */
public class CartesianProdNode extends Node implements HQLTranslatable, SQLTranslatable {

    public CartesianProdNode(Node... relations){
        super(new CartesianProdOp(relations.length), relations);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        StringBuilder hibQuery = new StringBuilder("from ");
        for (int i = 0; i < children.length; i++) {
            Node child = children[i];
            String childHQL;
            if(child instanceof ClassRefNode){
                ClassRefNode crn = (ClassRefNode)child;
                childHQL = crn.toHibQuery() + " as " + crn.getAlias();
                hibQuery.append(i == 0 ? childHQL : ", " + childHQL);
            }
            else if(child instanceof FieldRefNode){
                FieldRefNode frn = (FieldRefNode) child;
                if(i ==0){
                    ClassRefNode crn = frn.getTypeClassRef();
                    childHQL = crn.toHibQuery() + " as " + crn.getAlias();
                }
                else {
                    childHQL = "join fetch " + frn.toHibQuery();
                }
                hibQuery.append(" ").append(childHQL);
            }
            else{
                throw new QueryTranslationException(child + " is not a ClassRef or FieldRef");
            }
        }

        return hibQuery.toString();
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        StringBuilder sqlQuery = new StringBuilder("from ");
        for (int i = 0; i < children.length; i++) {
            Node child = children[i];
            String childSQL;

            if(child instanceof ClassRefNode || child instanceof FieldRefNode){
                ClassRefNode crn = child instanceof ClassRefNode ?
                        (ClassRefNode)child : ((FieldRefNode)child).getTypeClassRef();
                childSQL = crn.toSQLQuery() + " as " + crn.getAlias();
                sqlQuery.append(i == 0 ? childSQL : ", " + childSQL);
            }
            else{
                throw new QueryTranslationException(child + " is not a ClassRef or FieldRef");
            }
        }

        return sqlQuery.toString();
    }
}
