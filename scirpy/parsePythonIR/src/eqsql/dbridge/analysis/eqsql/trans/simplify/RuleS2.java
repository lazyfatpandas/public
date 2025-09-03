package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 1/11/16.
 * Simplification rule {@link RuleS2}: Remove iterator()
 * Input:
 *      InvokeMethod (0)
 *          Any (1)
 *          Iterator (2)
 *          Any (3)
 * Note: In the input expression, Any (3) will match an empty func params node (because iterator() takes no arguments)
 * Output:
 *      Any (1)
 */
public class RuleS2 extends Rule {

    private static InputTree makeInputPattern(){
        InputTree any1 = new InputTree(OpType.Any, 1);
        InputTree iterator = new InputTree(OpType.MethodIterator, 2);
        InputTree any3 = new InputTree(OpType.Any, 3);
        InputTree methodInv = new InputTree(OpType.InvokeMethod, 0, any1, iterator, any3);

        return methodInv;
    }

    private static OutputTree makeOutputPattern(){
        return new OutputTree(1);
    }

    public RuleS2() {
        super(makeInputPattern(), makeOutputPattern());
    }
}
