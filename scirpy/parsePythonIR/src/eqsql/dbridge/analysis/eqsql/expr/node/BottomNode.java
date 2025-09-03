package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.BottomOp;

/**
 * Created by ek on 26/10/16.
 */
public class BottomNode extends LeafNode {

    private static BottomNode v;
    /**
     * Constructor does not use super() because it is a special kind of Node,
     * which has no children.
     */
    private BottomNode() {
        super(new BottomOp());
    }

    public static boolean isBottom(Object o){
        return (o != null)
            && (o instanceof BottomNode);
    }

    public static BottomNode v(){
        if(v == null){
            v = new BottomNode();
        }
        return v;
    }

    @Override
    public String toString() {
        return "BottomNode";
    }
}
