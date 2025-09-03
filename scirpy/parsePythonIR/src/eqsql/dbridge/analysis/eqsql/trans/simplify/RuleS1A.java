package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 31/10/16.
 * Simplification rule {@link RuleS1A}:
 * Input:
 *      Equals (0)
 *          Equals (1)
 *          0 (2)
 * Output:
 *      Equals (1)
 */
public class RuleS1A extends Rule {

    private static InputTree makeInputPattern(){
        InputTree equals1 = new InputTree(OpType.Eq, 1);
        InputTree zero = new InputTree(OpType.Zero, 2);
        InputTree equals0 = new InputTree(OpType.Eq, 0, equals1, zero);

        return equals0;
    }

    private static OutputTree makeOutputPattern(){
        return new OutputTree(1);
    }

    public RuleS1A() {
        super(makeInputPattern(), makeOutputPattern());
    }
}
