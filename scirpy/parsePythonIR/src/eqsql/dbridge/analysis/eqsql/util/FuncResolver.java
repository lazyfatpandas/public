package eqsql.dbridge.analysis.eqsql.util;

import eqsql.dbridge.analysis.eqsql.expr.DIR;
import eqsql.dbridge.analysis.eqsql.expr.node.MethodRefNode;
import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.RetVarNode;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;
import eqsql.dbridge.visitor.NodeVisitor;

import java.util.HashMap;

/**
 * Created by ek on 28/10/16.
 * Resolve reference to function with dag of its return value
 */
public class FuncResolver implements NodeVisitor {

    private HashMap<String, DIR> funcDirMap;

    public FuncResolver(HashMap<String, DIR> funcDirMap) {
        this.funcDirMap = funcDirMap;
    }

    /** Resolve reference to function with dag of its return value */
    @Override
    public Node visit(Node node) {
        if(node instanceof MethodRefNode){
            String methodName = ((MethodRefNode) node).getMethodName();
            assert (funcDirMap.containsKey(methodName)) : "Method " + methodName + " not present in funcDirMap";
            DIR methodDir = funcDirMap.get(methodName);

            VarNode retVar = RetVarNode.getARetVar();
            assert methodDir.contains(retVar);
            Node methodRetDag = methodDir.find(retVar);

            /* Note that methodRetDag may have references to other methods, hence
            * FuncResolver needs to be */
            return methodRetDag.accept(this);
        }
        return node;
    }
}
