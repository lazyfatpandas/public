package eqsql.dbridge.analysis.eqsql.hibernate.construct;

import eqsql.dbridge.analysis.eqsql.expr.node.*;
import eqsql.exceptions.UnknownConstructException;
import eqsql.hqlparse.HQLParser;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JimpleLocal;

import java.util.List;

/**
 * Created by ek on 24/10/16.
 */
class Utils {
    static VarNode fetchBase(Value source)  {
        Value base = null;
        if(source instanceof VirtualInvokeExpr)
            base = ((VirtualInvokeExpr)(source)).getBase();
        else if(source instanceof InterfaceInvokeExpr)
            base = ((InterfaceInvokeExpr)(source)).getBase();

        assert base instanceof JimpleLocal;
        Node var = NodeFactory.constructFromValue(base);
        assert var instanceof VarNode;
        return (VarNode) var;
    }

    static VarNode getVarNode(ValueBox valueBox) throws UnknownConstructException {
        Value value = valueBox.getValue();
        if(!(value instanceof JimpleLocal)){
            throw new UnknownConstructException(value + " is not JimpleLocal");
        }
        Node var = NodeFactory.constructFromValue(value);
        assert var instanceof VarNode;
        return (VarNode) var;
    }

    private static  Node[] makeNodeArray(List<Value> valueList){
        Node[] valueNodes = new Node[valueList.size()];
        int i = 0;
        for (Value value : valueList) {
            Node v = NodeFactory.constructFromValue(value);
            valueNodes[i] = v;
            i++;
        }
        return valueNodes;
    }

    /**
     * Create and return a Node by parsing InvokeExpr
     */
    static Node parseInvokeExpr(InvokeExpr invokeExpr){
        String methodName = invokeExpr.getMethod().getName();
        String methodSignature = trim(invokeExpr.getMethod().toString());

        if(invokeExpr instanceof JStaticInvokeExpr){
            return parseStaticInvoke(invokeExpr, methodName);
        }
        else{
            return parseObjectInvoke(invokeExpr, methodName, methodSignature);
        }
    }

    private static Node parseStaticInvoke(InvokeExpr invokeExpr, String methodName)
    {
        Node retNode;
        assert methodName.equals("valueOf");
        List<Value> args = invokeExpr.getArgs();
        assert args.size() == 1;

        retNode = NodeFactory.constructFromValue(args.get(0));
        return retNode;
    }

    private static Node parseObjectInvoke(InvokeExpr invokeExpr, String methodName, String methodSignature) {
        Node[] args;
        MethodRef methodNode;
        FuncParamsNode funcParamsNode;
        VarNode baseObj;

        baseObj = fetchBase(invokeExpr);
        switch (methodName) {
            case "equals":
                args = makeNodeArray(invokeExpr.getArgs());
                assert args.length == 1;
                return new EqNode(baseObj, args[0]); //Note the return here

            case "list":
                return baseObj;

            case "createQuery":
                assert invokeExpr.getArgCount() == 1 : "#arguments for createQuery > 1";
                String hqlStr = ((StringConstant) (invokeExpr.getArg(0))).value;
                HQLParser hqlParser = new HQLParser(hqlStr);
                return hqlParser.getQueryNode();

            case "iterator":
                methodNode = new MethodIteratorNode();
                funcParamsNode = FuncParamsNode.getEmptyParams();
                break;

            case "next":
                methodNode = new MethodNextNode();
                funcParamsNode = FuncParamsNode.getEmptyParams();
                break;

            case "hasNext":
                methodNode = new MethodHasNextNode();
                funcParamsNode = FuncParamsNode.getEmptyParams();
                break;

            case "booleanValue":
                methodNode = new MethodBooleanValueNode();
                funcParamsNode = FuncParamsNode.getEmptyParams();
                break;

            case "add":
                args = makeNodeArray(invokeExpr.getArgs());
                funcParamsNode = new FuncParamsNode(args);
                methodNode = new MethodInsertNode();
                break;

            case "put":
                args = makeNodeArray(invokeExpr.getArgs());
                funcParamsNode = new FuncParamsNode(args);
                methodNode = new MethodMapPutNode();
                break;

            case "loadAll":
                args = makeNodeArray(invokeExpr.getArgs());
                assert args.length == 1;
                assert args[0] instanceof ClassRefNode;
                return new CartesianProdNode((ClassRefNode)args[0]); //note the return here

            default:
                args = makeNodeArray(invokeExpr.getArgs());
                funcParamsNode = new FuncParamsNode(args);
                methodNode = new MethodRefNode(methodSignature);
                break;
        }
        return new InvokeMethodNode(baseObj, methodNode, funcParamsNode);
    }

    /** Remove the angular brackets appended by SootMethod.toString() to the method signature at the beginning and
     * the end
     */
    private static String trim(String methodSign){
        return methodSign.substring(1, methodSign.length() - 1);
    }
}
