package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 1/9/2017.
 * Simplification rule {@link RuleS7A}: Remove redundant CartesianProd operators
 * Input:
 *      CartesianProd (0)
 *          CartesianProd (1)
 *              Any (2)
 *          Any (3)
 * Output:
 *      CartesianProd
 *          Any (2)
 *          Any (3)
 * Note: There is a variations of this rule provided by {@link RuleS7B}.
 */
public class RuleS7A extends Rule{
    public RuleS7A() {
        super(makeInputPattern(), makeOutputPattern());
    }

    private static InputTree makeInputPattern() {
        InputTree any2 = new InputTree(OpType.Any, 2);
        InputTree cp1 = new InputTree(OpType.CartesianProd, 1, any2);

        InputTree any3 = new InputTree(OpType.Any, 3);

        InputTree cp0 = new InputTree(OpType.CartesianProd, 0, cp1, any3);
        return cp0;
    }

    private static OutputTree makeOutputPattern() {
        OutputTree any2 = new OutputTree(2);
        OutputTree any3 = new OutputTree(3);
        OutputTree cp = new OutputTree(OpType.CartesianProd, any2, any3);
        return cp;
    }
}
