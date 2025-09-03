package eqsql.dbridge.analysis.eqsql.analysis;

import dbridge.analysis.region.exceptions.RegionAnalysisException;
import dbridge.analysis.region.regions.ARegion;
import eqsql.dbridge.analysis.eqsql.expr.DIR;
import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.TernaryNode;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;

import java.util.Map;

/**
 * Created by ek on 4/5/16.
 */
public class DIRBranchRegionAnalyzer extends AbstractDIRRegionAnalyzer {

    /* Singleton */
    private DIRBranchRegionAnalyzer(){};
    public static DIRBranchRegionAnalyzer INSTANCE = new DIRBranchRegionAnalyzer();

    @Override
    public DIR constructDIR(ARegion region) throws RegionAnalysisException {
        ARegion headRegion = region.getSubRegions().get(0);
        ARegion trueRegion = region.getSubRegions().get(2); // small hack
        ARegion falseRegion = null;
        if (region.getSubRegions().size() > 2) {
            falseRegion = region.getSubRegions().get(1);  // small hack
        }
        assert falseRegion != null;

        DIR headDIR = (DIR) headRegion.analyze();
        DIR trueDIR = (DIR) trueRegion.analyze();
        DIR falseDIR = (DIR) falseRegion.analyze();
        Node condition = Utils.extractCondition(headDIR);

        DIR condRegDIR = new DIR();
        insertFromTrueDag(condRegDIR, condition, trueDIR, falseDIR);
        insertFromFalseDag(condRegDIR, condition, trueDIR, falseDIR);

        DIR retDIR = Utils.mergeSeqDirs(headDIR, condRegDIR);
        return retDIR;
    }

    /** This method constructs and inserts TernaryNodes for variables which are present
     * in false region only */
    private void insertFromFalseDag(DIR condRegDIR, Node condition, DIR trueDIR, DIR falseDIR) {
        for (Map.Entry<VarNode, Node> entry : falseDIR.getVeMap().entrySet()) {
            VarNode var = entry.getKey();
            Node falseDag = entry.getValue();

            TernaryNode ternaryNode;
            if(trueDIR.contains(var)){
                continue;
                /* We would have processed this already in insertFromTrueDag */
            }
            else{
                ternaryNode = new TernaryNode(condition, var, falseDag);
            }
            condRegDIR.insert(var, ternaryNode);
        }
    }

    /** This method constructs and inserts TernaryNodes for variables which are present
    * in both true region and false region, as well as variables which are present in
    * true region only. */
    private void insertFromTrueDag(DIR condRegDIR, Node condition, DIR trueDIR, DIR falseDIR) {
        for (Map.Entry<VarNode, Node> entry : trueDIR.getVeMap().entrySet()) {
            VarNode var = entry.getKey();
            Node trueDag = entry.getValue();

            TernaryNode ternaryNode;
            if(falseDIR.contains(var)){
                Node falseDag = falseDIR.find(var);
                ternaryNode = new TernaryNode(condition, trueDag, falseDag);
            }
            else{
                ternaryNode = new TernaryNode(condition, trueDag, var);
            }
            condRegDIR.insert(var, ternaryNode);
        }
    }
}
