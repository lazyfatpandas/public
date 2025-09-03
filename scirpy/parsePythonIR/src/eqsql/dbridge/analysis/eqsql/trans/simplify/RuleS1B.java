package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 31/10/16.
 * Simplification rule {@link RuleS1B}:
 * Input:
 *      NotEq (0)
 *          Equals (1)
 *              Any (2)
 *              Any (3)
 *          0 (4)
 * Output:
 *      NotEq
 *          Any (2)
 *          Any (3)
 */
public class RuleS1B extends Rule {

    private static InputTree makeInputPattern(){
        InputTree any2 = new InputTree(OpType.Any, 2);
        InputTree any3 = new InputTree(OpType.Any, 3);
        InputTree equals1 = new InputTree(OpType.Eq, 1, any2, any3);

        InputTree zero4 = new InputTree(OpType.Zero, 4);
        InputTree notEq0 = new InputTree(OpType.NotEq, 0, equals1, zero4);

        return notEq0;
    }

    private static OutputTree makeOutputPattern(){
        OutputTree any2 = new OutputTree(2);
        OutputTree any3 = new OutputTree(3);
        OutputTree notEq = new OutputTree(OpType.NotEq, any2, any3);

        return notEq;
    }

    public RuleS1B() {
        super(makeInputPattern(), makeOutputPattern());
    }
}
