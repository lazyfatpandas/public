package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 2/11/16.
 * Simplification rule {@link RuleS4}: Remove self from field refs
 * Input:
 *      InvokeMethod (0)
 *          Any (1)
 *          FieldRef (2)
 *              Self (3)
 *              Any (4)
 *              Any (5)
 *          Any (6)
 * Note: The second argument of MethodInv should be a MethodRef node. However,In the above pattern, you may observe
 * that it is a FieldRef node, which is obtained as a result of resolving MethodRef with return dag of
 * corresponding method.
 * Any (3) will match an empty FuncParamsNode
 * Output:
 *      FieldRef
 *          Any (1)
 *          Any (4)
 *          Any (5)
 *
 */
public class RuleS4 extends Rule {

    public RuleS4() {
        super(makeInputPattern(), makeOutputPattern());
    }

    private static InputTree makeInputPattern() {
        InputTree any1 = new InputTree(OpType.Any, 1);
        InputTree self = new InputTree(OpType.SelfRef, 3);
        InputTree any4 = new InputTree(OpType.Any, 4);
        InputTree any5 = new InputTree(OpType.Any, 5);
        InputTree fieldRef = new InputTree(OpType.FieldRef, 2, self, any4, any5);
        InputTree any6 = new InputTree(OpType.Any, 6);
        InputTree methodInv = new InputTree(OpType.InvokeMethod, 0, any1, fieldRef, any6);

        return methodInv;
    }

    private static OutputTree makeOutputPattern() {
        return new OutputTree(OpType.FieldRef, new OutputTree(1), new OutputTree(4), new OutputTree(5));
    }


}
