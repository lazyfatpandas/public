package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 5/11/16.
 * Transformation rule T2: Predicate push into query.
 * Input:
 *      Fold (0)
 *        FuncExpr (1)
 *          ? (2)
 *            Any (3)
 *            Any (4)
 *            PlaceholderVar (5)
 *        Any (6)
 *        Any (7)
 * Output:
 *      Fold
 *        FuncExpr
 *          Any (4)
*         Any (6)
 *        Sel
 *          Any (7)
 *          Any (3)
 * Precondition:
 *      Any (3), which is the condition, should be only in terms of loop query placeholder variable
 *      (currently we assume this to be always true).
 */
public class RuleT2 extends Rule {

    private static InputTree makeInPattern() {
        InputTree any3 = new InputTree(OpType.Any, 3);
        InputTree any4 = new InputTree(OpType.Any, 4);
        InputTree phVar5 = new InputTree(OpType.PlaceholderVar, 5);

        InputTree ternary2 = new InputTree(OpType.Ternary, 2, any3, any4, phVar5);

        InputTree funcExpr1 = new InputTree(OpType.FuncExpr, 1, ternary2);

        InputTree any6 = new InputTree(OpType.Any, 6);
        InputTree any7 = new InputTree(OpType.Any, 7);

        InputTree fold0 = new InputTree(OpType.Fold, 0, funcExpr1, any6, any7);

        return fold0;
    }

    private static OutputTree makeOutPattern() {
        OutputTree any4 = new OutputTree(4);
        OutputTree funcExpr = new OutputTree(OpType.FuncExpr, any4);

        OutputTree any6 = new OutputTree(6);

        OutputTree any7 = new OutputTree(7);//node representing collection
        OutputTree any3 = new OutputTree(3);//selection condition
        OutputTree selNode = new OutputTree(OpType.Select, any7, any3);

        return new OutputTree(OpType.Fold, funcExpr, any6, selNode);
    }

    public RuleT2() {
        super(makeInPattern(), makeOutPattern());
    }
}
