package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 6/11/16.
 * Transformation rule T1A: Fold removal
 * Input:
 *      Fold (0)
 *          FuncExpr (1)
 *            MethodInv (2)
 *              PlaceholderVar (3)
 *              MethodInsert (4)
 *              FuncParams (5)
 *                  Cartesian (6)
*           Bottom (7)
 *          Any (8)
 * Output:
 *      Any (8)
 * Precondition: The tuple added, Cartesian(6), should come from the query, Any(8).
 * Since we use the query itself to represent the tuple, the precondition will
 * be that Cartesian(6) = Any(8) (i.e, Any(8) should match a Cartesaian that is the
 * same as Cartesian(6)). This precondition is currently assumed to be true always.
 *
 * Also see {@link RuleT1D} (where the output is a projection)
 */
public class RuleT1A extends Rule {
    private static InputTree makeInPattern() {
        InputTree phVar = new InputTree(OpType.PlaceholderVar, 3);
        InputTree insert = new InputTree(OpType.MethodInsert, 4);

        InputTree cart6 = new InputTree(OpType.CartesianProd, 6);
        InputTree funcParams = new InputTree(OpType.FuncParams, 5, cart6);

        InputTree methodInv = new InputTree(OpType.InvokeMethod, 2,
                phVar, insert, funcParams);


        InputTree funcExpr = new InputTree(OpType.FuncExpr, 1, methodInv);
        InputTree bottom = new InputTree(OpType.Bottom, 7);
        InputTree any8 = new InputTree(OpType.Any, 8);

        InputTree fold = new InputTree(OpType.Fold, 0, funcExpr, bottom, any8);

        return fold;
    }

    private static OutputTree makeOutPattern() {
        return new OutputTree(8);
    }

    public RuleT1A() {
        super(makeInPattern(), makeOutPattern());
    }
}
