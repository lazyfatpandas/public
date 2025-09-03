package eqsql.dbridge.analysis.eqsql.expr.node;

/**
 * Created by K. Venkatesh Emani on 3/5/2017.
 * Represents a table with a single cell. For evaluating constants.
 */
public class ConstTableNode extends ClassRefNode {
    public ConstTableNode() {
        super("ConstTable");
    }
}
