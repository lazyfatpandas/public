package parse.IRMaker;

import soot.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ScirpySceneTransformer extends SceneTransformer {
    @Override
    protected void internalTransform(String phaseName, Map options) {
        Map<String, String> cgMap = new HashMap<String, String>();
        cgMap.put("enabled", "true");
        cgMap.put("apponly", "true");
        List<SootMethod> entryMthds = new ArrayList<SootMethod>();
        SootClass mainClass=Scene.v().getMainClass();
        entryMthds.add(mainClass.getMethodByName("main"));
        Scene.v().setEntryPoints(entryMthds);
        CHATransformer.v().transform("wjtp", cgMap);

        CallGraph cg = Scene.v().getCallGraph();
        Iterator<Edge> ite = cg.iterator();
//        riskMthds = getRiskMthds();
//        while (ite.hasNext()) {
//            Edge edge = ite.next();
//            if (isRiskCall(edge)) {
//                riskRlts.add(new MethodCall(edge.src().getSignature(), edge.tgt().getSignature()));
//            }
//        }
        //CHATransformer.v().transform();
//        SootClass a = Scene.v().getSootClass("testers.A");
//
//        SootMethod src = Scene.v().getMainClass().getMethodByName("doStuff");
//
//        Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
//        while (targets.hasNext()) {
//            SootMethod tgt = (SootMethod)targets.next();
//            System.out.println(src + " may call " + tgt);
//        }
    }
}
