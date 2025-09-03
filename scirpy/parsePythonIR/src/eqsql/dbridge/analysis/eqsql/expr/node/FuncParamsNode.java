package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.FuncParamsOp;

/**
 * Created by ek on 17/10/16.
 * Node representing a list of actual parameters
 */
public class FuncParamsNode extends Node {
    public FuncParamsNode(Node[] params) {
        super(new FuncParamsOp(params.length), params);
    }

    public static FuncParamsNode getEmptyParams(){
        return new FuncParamsNode(new Node[0]);
    }

    public boolean isEmpty(){
        return children.length == 0;
    }
}
