package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 6/11/16.
 * Transformation rule T1D: Fold removal
 * Input:
 *      Fold (0)
 *          FuncExpr (1)
 *            MethodInv (2)
 *              PlaceholderVar (3)
 *              MethodInsert (4)
 *              FuncParams (5)
 *                  FieldRef (6)
*           Bottom (7)
 *          Any (8)
 * Output:
 *      Project
 *          Any (8)
 *          FieldRef (6)
 * Precondition: The column added, FieldRef(6), should come from the query, Any(8).
 * Since we use the query itself to represent the tuple, the precondition will
 * be that FieldRef(6)'s ClassRef is same as Any(8)'s ClassRef
 *
 * To account for the case of SELECT *, whereby the entire row (object)
 *  is added, we need to add a simplification rule as follows:
 *  Input:
 *      Project
 *          Select (1)
 *          Select (2)
 *  Output
 *      Select (1)
 */
public class RuleT1D extends Rule {
    private static InputTree makeInPattern() {
        InputTree phVar = new InputTree(OpType.PlaceholderVar, 3);
        InputTree insert = new InputTree(OpType.MethodInsert, 4);

        InputTree fieldRef6 = new InputTree(OpType.FieldRef, 6);
        InputTree funcParams = new InputTree(OpType.FuncParams, 5, fieldRef6);

        InputTree methodInv = new InputTree(OpType.InvokeMethod, 2,
                phVar, insert, funcParams);


        InputTree funcExpr = new InputTree(OpType.FuncExpr, 1, methodInv);
        InputTree bottom = new InputTree(OpType.Bottom, 7);
        InputTree any8 = new InputTree(OpType.Any, 8);

        InputTree fold = new InputTree(OpType.Fold, 0, funcExpr, bottom, any8);

        return fold;
    }

    private static OutputTree makeOutPattern() {
        OutputTree rel = new OutputTree(8);
        OutputTree projEl = new OutputTree(6);
        return new OutputTree(OpType.Project, rel, projEl);
    }

    public RuleT1D() {
        super(makeInPattern(), makeOutPattern());
    }
}
