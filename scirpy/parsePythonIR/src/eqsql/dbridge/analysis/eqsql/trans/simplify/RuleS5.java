package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 1/6/2017.
 * Simplification Rule {@link RuleS5}: Remove Dao references.
 * Input:
 *      InvokeMethod (0)
 *          Dao (1)
 *          Any (2)
 *          Any (3)
 * Note: Any (3) should match an empty func params node. This transformation rule
 * assumes that the function Any (2) does not take any parameters.
 * //TODO If Any (2) takes parameters, then the parameters should be substituted
 * before applying this rule.
 * Output:
 *      Any (2)
 */
public class RuleS5 extends Rule {
    public RuleS5() {
        super(makeInputPattern(), makeOutputPattern());
    }

    private static InputTree makeInputPattern() {
        InputTree dao1 = new InputTree(OpType.Dao, 1);
        InputTree any2 = new InputTree(OpType.Any, 2);
        InputTree any3 = new InputTree(OpType.Any, 3);

        InputTree methodInv0 = new InputTree(OpType.InvokeMethod, 0, dao1, any2, any3);

        return methodInv0;
    }

    private static OutputTree makeOutputPattern() {
        return new OutputTree(2);
    }


}
