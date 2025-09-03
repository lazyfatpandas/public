package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.GroupByOp;
import eqsql.exceptions.QueryTranslationException;

/**
 * Created by K. Venkatesh Emani on 3/9/2017.
 */
public class GroupByNode extends Node implements HQLTranslatable, SQLTranslatable {

    protected GroupByNode(ProjectNode piNode, FieldRefNode groupByCol){
        super(new GroupByOp(), piNode, groupByCol);
    }

    @Override
    public String toHibQuery() throws QueryTranslationException {
        String piNodeSQL = ((HQLTranslatable)children[0]).toHibQuery();
        String groupBySQL = ((HQLTranslatable)children[1]).toHibQuery();
        piNodeSQL = addGroupByAttrToProj(piNodeSQL, groupBySQL);

        //remove trailing ")" added in ProjectNode
        piNodeSQL = piNodeSQL.substring(0, piNodeSQL.length()-1);
        String querySQL = piNodeSQL + " group by " + groupBySQL + ")";
        return querySQL;
    }

    /**
     * Rewrite querySQL so that its group by attributes are also added
     * to its projection list.
     * @return the rewritten query
     */
    private String addGroupByAttrToProj(String piNodeSQL, String groupBySQL) {
        /*
        TODO: Ideally, the attributes should have been added in the project list
        already. But right now, we make the assumption that in any query, only
        one element is projected. This method provides a hack to work around that
        assumption, until it is fixed.
        This hack works only for the simple case of non-nested queries.
         */
        int projectionStartPos = 8; //pointing to the end of "select " keyword
        String prefix = piNodeSQL.substring(0, projectionStartPos);
        String suffix = piNodeSQL.substring(projectionStartPos);
        String modSQL = prefix + groupBySQL + ", " + suffix;
        return modSQL;
    }

    @Override
    public String toSQLQuery() throws QueryTranslationException {
        String piNodeSQL = ((SQLTranslatable)children[0]).toSQLQuery();
        String groupBySQL = ((SQLTranslatable)children[1]).toSQLQuery();
        piNodeSQL = addGroupByAttrToProj(piNodeSQL, groupBySQL);

        //remove trailing ")" added in ProjectNode
        piNodeSQL = piNodeSQL.substring(0, piNodeSQL.length()-1);
        String querySQL = piNodeSQL + " group by " + groupBySQL + ")";
        return querySQL;
    }
}
