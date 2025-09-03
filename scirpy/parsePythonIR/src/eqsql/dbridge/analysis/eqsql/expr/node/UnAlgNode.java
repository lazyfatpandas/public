package eqsql.dbridge.analysis.eqsql.expr.node;

/**
 * Created by ek on 24/10/16.
 * Node to represent unalgebrizable expressions.
 */
public class UnAlgNode extends LeafNode {
    private UnAlgNode() {
        super(null);
    }

    public static UnAlgNode v() {
        return new UnAlgNode();
    }

    public static boolean isUnAlgNode(Object other){
        return other instanceof UnAlgNode;
    }

    @Override
    public String toString() {
        return "UnAlg";
    }
}
