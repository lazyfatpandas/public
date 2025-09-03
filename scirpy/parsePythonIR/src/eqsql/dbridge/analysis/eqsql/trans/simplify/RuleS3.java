package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 2/11/16.
 * Simplification Rule {@link RuleS3}: Remove next()
 * Input
 *      InvokeMethod (0)
 *          Any (1)
 *          Next (2)
 *          Any (3)
 * Note: Any (3) will match an empty FuncParamsNode (because next() does not take arguments)
 * Output
 *      Any (1)
 */
public class RuleS3 extends Rule {

    private static InputTree makeInputPattern() {
        InputTree any1 = new InputTree(OpType.Any, 1);
        InputTree next = new InputTree(OpType.MethodNext, 2);
        InputTree any3 = new InputTree(OpType.Any, 3);
        InputTree methodInv = new InputTree(OpType.InvokeMethod, 0, any1, next, any3);

        return methodInv;
    }

    private static OutputTree makeOutputPattern() {
        return new OutputTree(1);
    }

    public RuleS3() {
        super(makeInputPattern(), makeOutputPattern());
    }
}
