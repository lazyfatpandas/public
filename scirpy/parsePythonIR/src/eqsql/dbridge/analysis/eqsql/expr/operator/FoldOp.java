package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 24/10/16.
 */
public class FoldOp extends Operator {
    public FoldOp() {
        super("Fold", OpType.Fold, 3);
        /* 3 operands will be: expression representing the folding function, initial value of variable,
         * and the collection on which the corresponding loop iterates */
    }
}
