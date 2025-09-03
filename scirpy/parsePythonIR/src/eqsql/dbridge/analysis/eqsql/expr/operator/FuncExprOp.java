package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 4/11/16.
 */
public class FuncExprOp extends Operator {
    public FuncExprOp() {
        super("FuncExpr", OpType.FuncExpr, 1);
        /* 1 operand will be a node representing a functional expression */
    }
}
