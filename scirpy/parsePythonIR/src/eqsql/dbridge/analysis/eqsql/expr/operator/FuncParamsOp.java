package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 15/10/16.
 * Operator that represents a list of actual parameters to be passed to a function
 */
public class FuncParamsOp extends Operator {
    public FuncParamsOp(int arity) {
        super("FuncParams", OpType.FuncParams, arity);
        /* Can represent arity number of parameters */
    }
}
