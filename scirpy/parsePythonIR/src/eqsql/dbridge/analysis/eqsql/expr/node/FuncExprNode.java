package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.FuncExprOp;
import eqsql.dbridge.visitor.NodeVisitor;

/**
 * Created by ek on 4/11/16.
 * Node representing a parameterized expression, which can be used to represent a function.
 * Currently only represents a binary function which operates on a scalar variable and a tuple variable. Intended to
 * be used to represent the folding function in the fold construct.
 */
public class FuncExprNode extends Node implements ParameterizedNode {

    /**
     * A placeholder to represent the aggregated variable (first argument) in the folding function.
     */
    private PlaceholderVarNode aggVarPh;

    public FuncExprNode(Node funcExpr, VarNode aggVar, VarNode loopVar) {
        super(new FuncExprOp(), funcExpr);
        children[0] = parameterize(funcExpr, aggVar, loopVar);
        /* No instance methods can be called before call to super(), hence children[0] is modified after call to
        super constructor. (i.e., if instance method calls were allowed, the above two lines could alternatively be
        written as:
        super(new FuncExpr(), parameterize(funcExpr, aggVar, loopVar)) */
    }

    /**
     * Constructor to be used when the node is already a parameterized expression. Intended for use during
     * transformations.
     */
    public FuncExprNode(Node paramdExpr){
        super(new FuncExprOp(), paramdExpr);
        findPlaceholders(paramdExpr);
    }

    @Override
    public void findPlaceholders(Node paramdExpr) {
        paramdExpr.accept(new NodeVisitor() {
            /* Function does not modify the node. Only used to find the placeholders from paramdExpr and update state */
            @Override
            public Node visit(Node node) {
                if(node instanceof PlaceholderVarNode){
                    aggVarPh = (PlaceholderVarNode) node;
                }
                return node;
            }
        });
    }

    /** Insert placeholder in place of aggVar
     */
    private Node parameterize(Node funcExpr, final VarNode aggVar, final VarNode loopVar) {
        /*Although the folding function has two arguments: aggVar and loopVar, we do not
        * replace loopVar (query/collection) with a placeholder, because we need it to
        * identify the mapped class of a particular column/field (for the purpose of
        * alias referencing during translation to SQL)*/
        aggVarPh = new PlaceholderVarNode();

        Node modFuncExpr = funcExpr.accept(new NodeVisitor() {
            @Override
            public Node visit(Node node) {
                if (node.equals(aggVar)) {
                    return aggVarPh;
                }
                return node;
            }
        });

        return modFuncExpr;
    }

}
