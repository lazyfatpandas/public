package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.FoldOp;

/**
 * Created by ek on 24/10/16.
 */
public class FoldNode extends Node {

    /**
     * @param funcExpr A parameterized expression representing the folding function
     * @param initVal Initial value of the variable which is being aggregated
     * @param loopCol Collection over which the loop is iterating (could be a query or a collection variable)
     */
    public FoldNode(FuncExprNode funcExpr, Node initVal, Node loopCol){
        super(new FoldOp(), funcExpr, initVal, loopCol);
    }

    private static FuncExprNode makeFuncExprNode(Node funcExpr, VarNode aggVar, VarNode loopCol) {
        FuncExprNode funcExprNode = new FuncExprNode(funcExpr, aggVar, loopCol);
        return funcExprNode;
    }

    /**
     * Convenience constructor
     * @param funcExpr Expression (DIR.dag) representing the folding function
     * @param aggVar The variable which is being aggregated
     * @param loopCol Collection variable over which the loop is iterating
     */
    public FoldNode(Node funcExpr, VarNode aggVar, VarNode loopCol) {
        this(makeFuncExprNode(funcExpr, aggVar, loopCol), (Node)aggVar, (Node)loopCol);
    }
}
