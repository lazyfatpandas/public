package eqsql.dbridge.analysis.eqsql.expr.node;

/**
 * Created by ek on 28/10/16.
 * Node that represents a function being invoked.
 * (To be used for construction of InvokeMethodNode)
 */
public class MethodRefNode extends StringConstNode implements MethodRef {

    public MethodRefNode(String _str) {
        super(_str);
    }

    public String getMethodName(){
        return getStr();
    }
}
