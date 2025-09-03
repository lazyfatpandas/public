package eqsql.dbridge.analysis.eqsql.analysis;

import dbridge.analysis.region.exceptions.RegionAnalysisException;
import dbridge.analysis.region.regions.ARegion;
import dbridge.analysis.region.regions.LoopRegion;
import eqsql.dbridge.analysis.eqsql.expr.DIR;
import eqsql.dbridge.analysis.eqsql.expr.node.*;
import soot.Unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ek on 4/5/16.
 */
public class DIRLoopRegionAnalyzer extends AbstractDIRRegionAnalyzer {

    /* Singleton */
    private DIRLoopRegionAnalyzer(){};
    public static DIRLoopRegionAnalyzer INSTANCE = new DIRLoopRegionAnalyzer();

    @Override
    public DIR constructDIR(ARegion region) throws RegionAnalysisException {
        assert region instanceof LoopRegion;

        ARegion head = region.getSubRegions().get(0);
        ARegion loopBody = region.getSubRegions().get(1);
        DIR headDIR = (DIR) head.analyze();
        DIR bodyDIR = (DIR) loopBody.analyze();

        VarNode loopingVar = getLoopingCol(headDIR);
        Map<VarNode, Set<VarNode>> varRsMap = fetchReadSets(bodyDIR);
        Set<VarNode> aggVars = findAggregatedVars(varRsMap);

        DIR loopDIR = new DIR();
        for (VarNode aggVar : aggVars) {
            /* Compute intersection of the var's readSet and the set of aggregated vars */
            Set<VarNode> intersection = new HashSet<>(varRsMap.get(aggVar));
            intersection.retainAll(aggVars);

            if(intersection.size() == 1){
                /* Precondition satisfied (aggVar has cyclic dependency only with itself) */
                FoldNode foldNode = new FoldNode(bodyDIR.find(aggVar), aggVar, loopingVar);
                foldNode.addLoopSwallowed((LoopRegion) region);
                loopDIR.insert(aggVar, foldNode,
                        region.getUnits().toArray(new Unit[0]));
            }
            else {
                /* Precondition not satisfied. So add a not algebrizable expression for this var */
                loopDIR.insert(aggVar, UnAlgNode.v());
            }
        }

        return loopDIR;
    }

    /**
     * Find all the variables that are aggregated in the loop.
     * A variable "var" is determined to be aggregated in the loop if "var" is present in its
     * own read set.
     */
    private Set<VarNode> findAggregatedVars(Map<VarNode, Set<VarNode>> varRsMap) {
        Set<VarNode> aggVars = new HashSet<>();
        for (VarNode var : varRsMap.keySet()) {
            Set<VarNode> varReadset = varRsMap.get(var);
            if(varReadset.contains(var)){
                aggVars.add(var);
            }
        }
        return aggVars;
    }

    /**
     * Return a map containing variable and its read set, for each variable updated inside the loop body.
     * Since readSet() is computed by a traversal of the dag expression, populating readSets and
     * reusing them (instead of calling varNode.readSet() whenever required) does less work.
     */
    private Map<VarNode, Set<VarNode>> fetchReadSets(DIR bodyDIR)  {
        Map<VarNode, Set<VarNode>> varReadsetMap = new HashMap<>();
        for (VarNode var : bodyDIR.getVars()) {
            if(!var.isJimpleVar()){
                continue;
            }
            Set<VarNode> readSet = bodyDIR.find(var).readSet();
            varReadsetMap.put(var, readSet);
        }
        return varReadsetMap;
    }

    /**
     * @return The query or collection variable over which the loop iterates
     */
    private VarNode getLoopingCol(DIR headDIR)  {
        VarNode condVar = VarNode.getACondVar();
        assert headDIR.contains(condVar);
        Node loopCond = headDIR.find(condVar);

        /* loopCond is expected to be of the form:
            ==
              MethodInv
                l5
                HasNext()
              0
         */
        assert (loopCond instanceof EqNode);
        EqNode eqNode = (EqNode)loopCond;
        assert (eqNode.getChild(0) instanceof InvokeMethodNode);
        InvokeMethodNode miNode = (InvokeMethodNode) eqNode.getChild(0);
        assert (miNode.getChild(1) instanceof MethodHasNextNode);

        assert miNode.getChild(0) instanceof VarNode;
        return (VarNode) miNode.getChild(0);
    }
}
