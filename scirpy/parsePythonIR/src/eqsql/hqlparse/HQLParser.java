package eqsql.hqlparse;

import eqsql.dbridge.analysis.eqsql.expr.node.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by venkatesh on 2/15/2018.
 * The query string is expected to be in the format:
 * "from<space><className><space><alias><space>[where<space><eqCondLHS><space>=<space><eqCondRHS>]"
 */
public class HQLParser {
    private HQLQueryType queryType;
    /**
     * HQL query is on which class
     */
    private String className;
    /**
     * LHS and RHS of where clause, if any
     */
    private String eqCondLHS;
    private String eqCondRHS;

    private static final String KW_FROM = "from";
    private static final String KW_WHERE = "where";
    private static final String KW_EQ = "=";


    public HQLParser(String queryStr) {

        String[] cmpntsArr = queryStr.split(" ");
        List<String> cmpnts = new ArrayList<>(Arrays.asList(cmpntsArr));

        /* obtain className */
        assert cmpnts.contains(KW_FROM);
        int fromIndex = cmpnts.indexOf(KW_FROM);
        this.className = cmpnts.get(fromIndex+1);
        this.queryType = HQLQueryType.LOAD_ALL;

        /* obtain where LHS RHS, if they exist*/
        if(cmpnts.contains(KW_WHERE)){
            int whereIndex = cmpnts.indexOf(KW_WHERE);
            this.eqCondLHS = cmpnts.get(whereIndex + 1);
            assert cmpnts.get(whereIndex+2).equals(KW_EQ);
            this.eqCondRHS = cmpnts.get(whereIndex + 3);
            this.queryType = HQLQueryType.FILTER;
        }
    }

    public Node getQueryNode(){
        switch (queryType){
            case LOAD_ALL:
                return new CartesianProdNode(new ClassRefNode(className));

            case FILTER:
                return new SelectNode(new ClassRefNode(className),
                        new EqNode(new StringConstNode(eqCondLHS), new StringConstNode(eqCondRHS)));
                //we have treated eqCondRHS and eqCondLHS as strings. //TODO they may be
                //complex expressions, so we need to parse them properly.
        }
        return null;
    }

}
