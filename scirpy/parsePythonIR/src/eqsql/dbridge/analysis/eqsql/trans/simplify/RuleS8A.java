package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by venkatesh on 8/7/17.
 * Simplification rule {@link RuleS8A}: Simplify InvokeMethod(CartesianProd,FieldRef) to FieldRef
 * Why this pattern occurs: When we have getter function invocation, such as baseObj.getX(),
 * it is represented as: InvokeMethod(baseObj, MethodRef(getX)). Later on, MethodRef(getX) is
 * resolved to FieldRef(x), making it InvokeMethod(baseObj, FieldRef(x)).
 *
 * Input:
 *      InvokeMethod (0)
 *          CartesianProd (1)
 *          FieldRef (2)
 *          Any (3)
 * Any(3) matches an empty FuncParams node.
 * Output:
 *      FieldRef (2)
 *
 * Note: A more general version of this rule is InvokeMethod(Any, FieldRef) => FieldRef
 * But we are not sure if we may lose information by discarding something as generic as Any.
 * So for now, we stick to the above version of the rule.
 */
public class RuleS8A extends Rule{
    public RuleS8A() {
        super(makeInputPattern(), makeOutputPattern());
    }

    private static InputTree makeInputPattern() {
        InputTree cp1 = new InputTree(OpType.CartesianProd, 1);
        InputTree fr2 = new InputTree(OpType.FieldRef, 2);
        InputTree any3 = new InputTree(OpType.Any, 3);

        InputTree invMethod0 = new InputTree(OpType.InvokeMethod, 0, cp1, fr2, any3);
        return invMethod0;
    }

    private static OutputTree makeOutputPattern() {
        return new OutputTree(2);
    }
}
