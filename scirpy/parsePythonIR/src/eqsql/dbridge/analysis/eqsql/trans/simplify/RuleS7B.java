package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 1/9/2017.
 * Simplification rule {@link RuleS7B}: Remove redundant CartesianProd operators
 * Input:
 *      CartesianProd (0)
 *          Any (1)
 *          CartesianProd (2)
 *              Any (3)
 * Output:
 *      CartesianProd
 *          Any (1)
 *          Any (3)
 * Note: There is a variations of this rule provided by {@link RuleS7A}
 */
public class RuleS7B extends Rule{
    public RuleS7B() {
        super(makeInputPattern(), makeOutputPattern());
    }

    private static InputTree makeInputPattern() {
        InputTree any1 = new InputTree(OpType.Any, 1);

        InputTree any3 = new InputTree(OpType.Any, 3);
        InputTree cp2 = new InputTree(OpType.CartesianProd, 2, any3);
        
        InputTree cp0 = new InputTree(OpType.CartesianProd, 0, any1, cp2);
        return cp0;
    }

    private static OutputTree makeOutputPattern() {
        OutputTree any1 = new OutputTree(1);
        OutputTree any3 = new OutputTree(3);
        OutputTree cp = new OutputTree(OpType.CartesianProd, any1, any3);
        return cp;
    }
}
