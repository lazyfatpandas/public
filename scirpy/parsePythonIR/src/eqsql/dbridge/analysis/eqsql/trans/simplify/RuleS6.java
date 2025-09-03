package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 12/20/2016.
 * Simplification Rule {@link RuleS6}: Remove function call for translating a value into boolean. After translating to SQL (HQL), this happens automatically, so we don't care about it here.
 * Input
 *      InvokeMethod (0)
 *          Any (1)
 *          BooleanValue (2)
 *          Any (3)
 * Note: Any (3) will match an empty FuncParamsNode (because next() does not take arguments)
 * Output
 *      Any (1)
 */
public class RuleS6 extends Rule{
    public RuleS6() {
        super(makeInputPattern(), makeOutputPattern());
    }

    private static InputTree makeInputPattern() {
        InputTree any1 = new InputTree(OpType.Any, 1);
        InputTree booleanValue = new InputTree(OpType.MethodBooleanValue, 2);
        InputTree any3 = new InputTree(OpType.Any, 3);
        InputTree methodInv = new InputTree(OpType.InvokeMethod, 0, any1, booleanValue, any3);

        return methodInv;
    }

    private static OutputTree makeOutputPattern() {
        return new OutputTree(1);
    }


}
