package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.node.ConstTableNode;
import eqsql.dbridge.analysis.eqsql.expr.node.CountStarNode;
import eqsql.dbridge.analysis.eqsql.expr.node.ZeroNode;
import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.LeafConstructor;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by K. Venkatesh Emani on 3/5/2017.
 * Rule T4: Exists
 * Input:
 *      Fold (0)
 *          FuncExpr (1)
 *              1 (2)
 *          0 (3)
 *          Any (4)
 * Output:
 *      Project (outer)
*           CartesianProd
 *           ClassRef (ConstTable)
 *          >
 *              Project (inner)
 *                  Any (4)
 *                  CountStar
 *              0
 *
 */
public class RuleT4 extends Rule {
    private static InputTree makeInPattern() {
        InputTree one2 = new InputTree(OpType.One, 2);
        InputTree funcExpr1 = new InputTree(OpType.FuncExpr, 1, one2);

        InputTree zero3 = new InputTree(OpType.Zero, 3);
        InputTree any4 = new InputTree(OpType.Any, 4);

        InputTree fold0 = new InputTree(OpType.Fold, 0, funcExpr1, zero3, any4);
        return fold0;
    }

    private static LeafConstructor constTableLc = op -> new ConstTableNode();
    private static LeafConstructor countStarLc = op -> new CountStarNode();
    private static LeafConstructor zeroLc = op -> new ZeroNode();


    private static OutputTree makeOutPattern() {
        OutputTree classRefConst = new OutputTree(constTableLc);
        OutputTree cp = new OutputTree(OpType.CartesianProd, classRefConst);

        OutputTree any4 = new OutputTree(4);
        OutputTree countStar = new OutputTree(countStarLc);
        OutputTree projectInner = new OutputTree(OpType.Project, any4, countStar);

        OutputTree zero = new OutputTree(zeroLc);
        OutputTree gt = new OutputTree(OpType.Gt, projectInner, zero);

        OutputTree projectOuter = new OutputTree(OpType.Project, cp, gt);
        return projectOuter;
    }

    public RuleT4() {
        super(makeInPattern(), makeOutPattern());
    }
}
