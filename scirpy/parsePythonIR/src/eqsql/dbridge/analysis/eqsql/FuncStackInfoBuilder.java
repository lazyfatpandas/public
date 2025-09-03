package eqsql.dbridge.analysis.eqsql;

import dbridge.analysis.region.regions.ARegion;
import dbridge.analysis.region.regions.RegionGraph;
import eqsql.dbridge.analysis.eqsql.util.SootClassHelper;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by ek on 26/4/16.
 */
public class FuncStackInfoBuilder extends SceneTransformer {
    private FuncStackAnalyzer funcStackAnalyzer;

    public FuncStackInfoBuilder(FuncStackAnalyzer funcStackAnalyzer) {
        super();
        this.funcStackAnalyzer = funcStackAnalyzer;
    }

    protected void internalTransform(String phaseName, Map options){
        try{
            internalTransformHelper(funcStackAnalyzer);
            funcStackAnalyzer.setSuccess(true);
        }
        catch (Exception | AssertionError e){
            funcStackAnalyzer.setSuccess(false);
        }
    }

    private void internalTransformHelper(FuncStackAnalyzer fsa) {
        Queue funcCall = new LinkedList<>();
        Body body;
        RegionGraph regionGraph;
        ARegion topRegion;

        /* Get info about the top level function */
        String methodFullSign = fsa.topLevelFunc;
        String className = SootClassHelper.getClassName(fsa.topLevelFunc);
        String methodSubsign = SootClassHelper.getMethodSubsignature(fsa.topLevelFunc);

        fsa.funcCallStack.add(methodFullSign);
        SootClass c = Scene.v().forceResolve(className, SootClass.BODIES);
        SootMethod method = c.getMethod(methodSubsign);
        funcCall.add(method);

        body = method.retrieveActiveBody();
        fsa.funcBodyMap.put(trim(method.toString()), body);

        regionGraph = new RegionGraph(body);
        topRegion = regionGraph.getHeads().get(0);
        fsa.funcRegionMap.put(trim(method.toString()), topRegion);

        /* Get info about other callee functions */
        CallGraph cg = Scene.v().getCallGraph();
        while (!funcCall.isEmpty()){
            MethodOrMethodContext caller = (MethodOrMethodContext) funcCall.poll();
            Iterator callees = cg.edgesOutOf(caller);
            while (callees.hasNext()){
                MethodOrMethodContext callee = ((Edge) callees.next()).getTgt();
                String calleeStrNotrim = callee.toString();
                String calleeStr = trim(calleeStrNotrim);

                if(fsa.funcCallStack.search(calleeStr) == -1
                        && isInteresting(calleeStrNotrim)){
                    funcCall.add(callee);
                    fsa.funcCallStack.add(calleeStr);

                    body = ((SootMethod)callee).retrieveActiveBody();
                    fsa.funcBodyMap.put(calleeStr, body);

                    regionGraph = new RegionGraph(body);
                    topRegion = regionGraph.getHeads().get(0);
                    fsa.funcRegionMap.put(calleeStr, topRegion);
                }
            }
        }
    }

    private boolean isInteresting(String methodSign){
        boolean ignore = false;
        ignore = methodSign.startsWith("<javax.")
                || methodSign.startsWith("<soot.")
                || methodSign.startsWith("<sun.")
                || methodSign.startsWith("<java.")
                || methodSign.startsWith("<org.")
                || methodSign.endsWith("<init>()>");
        return !ignore;
    }

    /** Remove the angular brackets appended by m.toString() to the method signature at beginning and end
     */
    private String trim(String methodSign){
        return methodSign.substring(1, methodSign.length() - 1);
    }
}
