package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 3/5/2017.
 * Transformation {@link RuleT3B}: Join identification
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
 *            Cartesian (9)
 *              Any (10)
 *         Bottom (11)
 *         Any (12)
 *
 * Output:
 *      CartesianProd
 *              Any (10)
 *              Any (12)
 * Assumption: Any(7) matches a FuncParams->CartesianProd->ClassRef node. //TODO Make this a precondition
 */
public class RuleT3B extends Rule {
    private static InputTree makeInPattern() {
        InputTree ph5 = new InputTree(OpType.PlaceholderVar, 5);
        InputTree insert6 = new InputTree(OpType.MethodInsert, 6);
        InputTree any7 = new InputTree(OpType.Any, 7);
        InputTree methodInv4 = new InputTree(OpType.InvokeMethod, 4, ph5, insert6, any7);
        InputTree funcExpr3 = new InputTree(OpType.FuncExpr, 3, methodInv4);

        InputTree any8 = new InputTree(OpType.Any, 8);

        InputTree any10 = new InputTree(OpType.Any, 10);
        InputTree cart9 = new InputTree(OpType.CartesianProd, 9, any10);

        InputTree fold2 = new InputTree(OpType.Fold, 2, funcExpr3, any8, cart9);
        InputTree funcExpr1 = new InputTree(OpType.FuncExpr, 1, fold2);

        InputTree bottom11 = new InputTree(OpType.Bottom, 11);
        InputTree any12 = new InputTree(OpType.Any, 12);

        InputTree fold0 = new InputTree(OpType.Fold, 0, funcExpr1, bottom11, any12);
        return fold0;
    }

    private static OutputTree makeOutPattern() {
        OutputTree any10 = new OutputTree(10);
        OutputTree any12 = new OutputTree(12);
        OutputTree cp = new OutputTree(OpType.CartesianProd, any10, any12);

        return cp;
    }

    public RuleT3B() {
        super(makeInPattern(), makeOutPattern());
    }
}
