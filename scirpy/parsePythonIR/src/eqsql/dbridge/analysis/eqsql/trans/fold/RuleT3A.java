package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 3/5/2017.
 * Transformation {@link RuleT3A}: Join identification
 * Input:
 *      Fold (0)
 *        FuncExpr (1)
 *          Fold (2)
 *            FuncExpr (3)
 *              InvokeMethod (4)
 *                PlaceholderVar (5)
 *                MethodInsert (6)
 *                Any (7)
 *            Any (8)
 *            Sel (9)
 *              Any (10)
 *              Any (11)
 *         Bottom (12)
 *         Any (13)
 *
 * Output:
 *      Sel
 *          CartesianProd
 *              Any (10)
 *              Any (13)
 *          Any (11)
 * Assumption: Any(7) matches a FuncParams->CartesianProd->ClassRef node. //TODO Make this a precondition
 */
public class RuleT3A extends Rule {
    private static InputTree makeInPattern() {
        InputTree ph5 = new InputTree(OpType.PlaceholderVar, 5);
        InputTree insert6 = new InputTree(OpType.MethodInsert, 6);
        InputTree any7 = new InputTree(OpType.Any, 7);
        InputTree methodInv4 = new InputTree(OpType.InvokeMethod, 4, ph5, insert6, any7);
        InputTree funcExpr3 = new InputTree(OpType.FuncExpr, 3, methodInv4);

        InputTree any8 = new InputTree(OpType.Any, 8);

        InputTree any10 = new InputTree(OpType.Any, 10);
        InputTree any11 = new InputTree(OpType.Any, 11);
        InputTree sel9 = new InputTree(OpType.Select, 9, any10, any11);

        InputTree fold2 = new InputTree(OpType.Fold, 2, funcExpr3, any8, sel9);
        InputTree funcExpr1 = new InputTree(OpType.FuncExpr, 1, fold2);

        InputTree bottom12 = new InputTree(OpType.Bottom, 12);
        InputTree any13 = new InputTree(OpType.Any, 13);

        InputTree fold0 = new InputTree(OpType.Fold, 0, funcExpr1, bottom12, any13);
        return fold0;
    }

    private static OutputTree makeOutPattern() {
        OutputTree any10 = new OutputTree(10);
        OutputTree any13 = new OutputTree(13);
        OutputTree cp = new OutputTree(OpType.CartesianProd, any10, any13);

        OutputTree any11 = new OutputTree(11);
        OutputTree sel = new OutputTree(OpType.Select, cp, any11);

        return sel;
    }

    public RuleT3A() {
        super(makeInPattern(), makeOutPattern());
    }
}
