package eqsql.dbridge.analysis.eqsql.trans.fold;

import eqsql.dbridge.analysis.eqsql.expr.node.ClassRefNode;
import eqsql.dbridge.analysis.eqsql.expr.operator.FieldRefOp;
import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.LeafConstructor;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by ek on 6/11/16.
 * Transformation rule T1C: Fold removal with lazy attribute using seq
 * This rule is similar to {@link RuleT1B} in structure. Semantically,
 * it identifies a lazy fetch and replaces it with a full fetch followed by a FieldRef.
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
 *      Seq
 *          Cartesian
 *              ClassRef(FieldRef(7).typeClass)
 *          Cartesian
 *              Any(9)
 *
 */
public class RuleT1C extends Rule {
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

    private static LeafConstructor frTypeCons = op -> {
        assert op instanceof FieldRefOp;
        return new ClassRefNode(((FieldRefOp) op).getTypeClass());
    };

    private static OutputTree makeOutPattern() {
        OutputTree fr7TypeClass = new OutputTree(7, frTypeCons);
        OutputTree cartesian = new OutputTree(OpType.CartesianProd, fr7TypeClass);

        OutputTree any9 = new OutputTree(9);
        OutputTree seq = new OutputTree(OpType.Seq, cartesian, any9);

        return seq;
    }

    public RuleT1C() {
        super(makeInPattern(), makeOutPattern());
    }
}
