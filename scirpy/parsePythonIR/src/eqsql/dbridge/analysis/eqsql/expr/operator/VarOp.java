package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 18/10/16.
 * Operator that represents a variable
 */
public class VarOp extends Operator{
    public VarOp() {
        super("Var", OpType.Var, 0);
        /* 0 operands. The variable will be an attribute of VarNode */
    }
}
