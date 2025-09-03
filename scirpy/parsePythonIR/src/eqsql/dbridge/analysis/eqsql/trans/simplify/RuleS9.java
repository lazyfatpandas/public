package eqsql.dbridge.analysis.eqsql.trans.simplify;

import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.trans.InputTree;
import eqsql.dbridge.analysis.eqsql.trans.OutputTree;
import eqsql.dbridge.analysis.eqsql.trans.Rule;

/**
 * Created by venkatesh on 2/16/2018.
 * Simplification Rule {@link RuleS9}: Replace FieldRef.Dao with Dao
 * Input
 *      InvokeMethod (0)
 *          FieldRef (1)
 *          Dao (2)
 *          Any (3)
 * Note: Any (3) will match an empty FuncParamsNode
 *
 * Output
 *      Dao (2)
 */
public class RuleS9 extends Rule {

    public RuleS9() {
        super(makeInputPattern(), makeOutputPattern());
    }

    private static InputTree makeInputPattern() {
        InputTree fieldRef1 = new InputTree(OpType.FieldRef, 1);
        InputTree dao2 = new InputTree(OpType.Dao, 2);
        InputTree any3 = new InputTree(OpType.Any, 3);

        InputTree invMethod0 = new InputTree(OpType.InvokeMethod, 0, fieldRef1, dao2, any3);
        return invMethod0;
    }

    private static OutputTree makeOutputPattern(){
        return new OutputTree(2);
    }

}
