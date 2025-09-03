package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.node.CountStarNode;
import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.LeafConstructor;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 2/16/2017.
 * Transformation rule T5: Count(*) identification
 * Input:
 *   Fold (0)
 *     FuncExpr (1)
 *       + (2)
 *         Any (3)
 *         1 (4)
 *     0 (5)
 *     Any (6)
 * Note: Any (3) matches a PlaceholderVarNode
 * Output:
 *   Project
 *     Any (6)
 *     CountStar
 */
public class RuleT5 extends Rule {
    private static InputTree makeInputPattern() {
        InputTree any3 = new InputTree(OpType.Any, 3);
        InputTree one4 = new InputTree(OpType.One, 4);

        InputTree add2 = new InputTree(OpType.ArithAdd, 2, any3, one4);

        InputTree funcExpr1 = new InputTree(OpType.FuncExpr, 1, add2);

        InputTree zero5 = new InputTree(OpType.Zero, 5);

        InputTree any6 = new InputTree(OpType.Any, 6);

        InputTree fold = new InputTree(OpType.Fold, 0, funcExpr1, zero5, any6);
        return fold;

    }

    private static LeafConstructor countStarCons = op -> new CountStarNode();

    private static OutputTree makeOutputPattern() {
        OutputTree any6 = new OutputTree(6);
        OutputTree countStar = new OutputTree(countStarCons);
        OutputTree project = new OutputTree(OpType.Project, any6, countStar);
        return project;
    }

    public RuleT5() {
        super(makeInputPattern(), makeOutputPattern());
    }
}
