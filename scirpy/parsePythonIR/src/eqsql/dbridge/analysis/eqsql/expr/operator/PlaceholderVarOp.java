package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 5/11/16.
 */
public class PlaceholderVarOp extends Operator {

    private static int curId = 1;
    /** a unique id assigned to the place holder to distinguish it from placeholders
     * of other loops. */
    private int id;

    public PlaceholderVarOp() {
        super("PhVar", OpType.PlaceholderVar, 0);
        id = curId++;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "<v" + id + ">";
    }
}
