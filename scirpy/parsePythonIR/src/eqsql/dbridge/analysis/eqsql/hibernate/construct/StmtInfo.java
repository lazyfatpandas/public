package eqsql.dbridge.analysis.eqsql.hibernate.construct;

import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;

/**
 * Created by ek on 13/10/16.
 * Information obtained from each individual statement.
 * We assume that each statement is (essentially) an assignment of the form dest = source, where dest refers to a variable, and source is an expression. For example, a list append statement "list.add(e)" is treated as "list = add(list, e)". In case of a conditional statement, dest will be assigned the string "condition", and source will be the expression for the boolean condition. In case of a
 * return statement, dest will be assigned the string "return", source will be the return expression.
 */
public class StmtInfo {
    private VarNode dest;
    private Node source;

    public static StmtInfo nullInfo = null;

    public StmtInfo(VarNode _dest, Node _source) {
        this.dest = _dest;
        this.source = _source;
    }

    public VarNode getDest() {
        return dest;
    }

    public Node getSource() {
        return source;
    }

    @Override
    public String toString() {
        return dest.toString() + " : " + source.toString();
    }

}
