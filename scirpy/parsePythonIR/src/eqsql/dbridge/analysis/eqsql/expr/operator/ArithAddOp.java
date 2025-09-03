package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by K. Venkatesh Emani on 1/10/2017.
 */
public class ArithAddOp extends Operator{
    public ArithAddOp() {
        super("+", OpType.ArithAdd, 2);
        /* 2 operands for arithmetic addition */
    }
}
