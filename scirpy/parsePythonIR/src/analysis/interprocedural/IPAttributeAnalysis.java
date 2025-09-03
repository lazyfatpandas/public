package analysis.interprocedural;

import analysis.LiveVariable.AttributeLiveVariableAnalysis;
import cfg.CFG;
import ir.IExpr;
import ir.JPBody;
import ir.JPMethod;
import ir.Stmt.CallExprStmt;
import ir.expr.BinOp;
import ir.expr.Call;
import ir.expr.Name;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;

import java.util.Iterator;
import java.util.List;

public class IPAttributeAnalysis {
    //THIS FOR MAIN CLASS ONLY
    public static void insertIPLiveAttributes(Unit node, FlowSet out, List<SootClass> sootClasses) {
        SootClass mainClass = null;

        //TRANSFORMATIONS
        mainClass = getMainClass(sootClasses);
        if (node instanceof CallExprStmt) {
            CallExprStmt callExprStmt = (CallExprStmt) node;
            IExpr callExpr=callExprStmt.getCallExpr();
            if(callExpr instanceof Call) {
                Call call = (Call) (callExprStmt.getCallExpr());
                if (call.getFunc() instanceof Name) {
                    Name funcName = (Name) (call.getFunc());
                    if (!isGenericFunc(funcName.id))
                        out.union(getMethodLiveAttributes(mainClass, funcName.id, null), out);
                }
            }//call
        else if(callExpr instanceof BinOp){
            //BinOp binOp=(BinOp)callExpr;
                //TODO to be implemeted
            }
        }


    }


    private static SootClass getMainClass(List<SootClass> sootClasses) {
        for (SootClass sootClass : sootClasses) {
            if (sootClass.getName().equalsIgnoreCase("MainClass")) {
                return sootClass;
            }
        }
        return null;
    }

    //This is an -unoptimized method now..
    //TODO update it in such a way that based on the args passed to the method, the attributes that are live are fetched.
    private static FlowSet getMethodLiveAttributes(SootClass sootClass, String methodName, List<IExpr> args) {
        FlowSet outSet = null;
        SootMethod otherMethod = sootClass.getMethodByName(methodName);
        JPBody otherBody = (JPBody) (otherMethod.getActiveBody());
        CFG cfg = new CFG((JPMethod) otherBody.getMethod());
        AttributeLiveVariableAnalysis alva = new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        Iterator unitIterator = cfg.getUnitGraph().iterator();
        //generate flowset for each unit
        int i = 0;
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            i++;
            System.out.println(i + " " + unit);
            outSet = (FlowSet) alva.getFlowBefore(unit);
            System.out.println("\tatt Before: " + outSet);
            break;
        }
        return outSet;
    }
    private static boolean isGenericFunc(String name){
        //TODO move this list of generic methods to a list or such methods should have their own Soot class
        if (name.equals("print") || name.equals("display") || name.equals("type"))
            return true;
        //TODO implement generic function for all libraries:
        if (name.equals("plot"))
            return true;
        if (name.equals("sleep"))
            return true;
        return false;
    }
}