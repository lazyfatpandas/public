package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 6/11/16.
 * Transformation rule T1B: Fold removal with lazy attribute join
 * This rule is similar to {@link RuleT1A} in structure. Semantically,
 * it identifies a lazy fetch and replaces it with a join.
 *
 * Input:
 *      Fold (0)
 *          FuncExpr (1)
 *            MethodInv (2)
 *              PlaceholderVar (3)
 *              MethodInsert (4)
 *              FuncParams (5)
 *                  LazyFetch (6)
 *                      FieldRef (7)
*           Bottom (8)
 *          Any (9)
 * Output:
 *      Cartesian
 *          Any (9)
 *          FieldRef (7)
 *
 *
 */
public class RuleT1B extends Rule {
    private static InputTree makeInPattern() {
        InputTree phVar = new InputTree(OpType.PlaceholderVar, 3);
        InputTree insert = new InputTree(OpType.MethodInsert, 4);

        InputTree fr7 = new InputTree(OpType.FieldRef, 7);
        InputTree lazy6 = new InputTree(OpType.LazyFetch, 6, fr7);
        InputTree funcParams = new InputTree(OpType.FuncParams, 5, lazy6);

        InputTree methodInv = new InputTree(OpType.InvokeMethod, 2,
                phVar, insert, funcParams);


        InputTree funcExpr = new InputTree(OpType.FuncExpr, 1, methodInv);
        InputTree bottom = new InputTree(OpType.Bottom, 8);
        InputTree any9 = new InputTree(OpType.Any, 9);

        InputTree fold = new InputTree(OpType.Fold, 0, funcExpr, bottom, any9);

        return fold;
    }

    private static OutputTree makeOutPattern() {
        OutputTree any9 = new OutputTree(9);
        OutputTree fr7 = new OutputTree(7);
        OutputTree cp = new OutputTree(OpType.CartesianProd, any9, fr7);

        return cp;
    }

    public RuleT1B() {
        super(makeInPattern(), makeOutPattern());
    }
}
