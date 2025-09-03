package eqsql.dbridge.analysis.eqsql.util;

import eqsql.dbridge.analysis.eqsql.expr.DIR;
import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;
import eqsql.dbridge.visitor.NodeVisitor;

/* Package local class*/

/**
 * Visitor to resolve variable references with preceding region's DIR
 */
public class VarResolver implements NodeVisitor {

    private DIR dir;

    public VarResolver(DIR dir) {
        this.dir = dir;
    }

    /** Resolve variable references in node using dir. */
    @Override
    public Node visit(Node node) {
        if(node instanceof VarNode && dir.contains((VarNode)node)){
            return dir.find((VarNode)node);
        }
        return node;
    }

}
