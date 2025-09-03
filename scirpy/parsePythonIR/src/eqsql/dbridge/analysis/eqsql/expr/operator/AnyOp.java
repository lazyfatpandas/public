package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 1/11/16.
 * Special operator that can represent any operator. For use only within transformation rules.
 */
public class AnyOp extends Operator {
    public AnyOp() {
        super("Any", OpType.Any, 0);
    }

    public static boolean isAnyOp(Operator op){
        return op.equals(new AnyOp());
    }
}
