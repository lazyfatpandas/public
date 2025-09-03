package eqsql.dbridge.analysis.eqsql.analysis;

import dbridge.analysis.region.regions.ARegion;
import eqsql.dbridge.analysis.eqsql.expr.DIR;
import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;
import eqsql.dbridge.analysis.eqsql.hibernate.construct.StmtDIRConstructionHandler;
import eqsql.dbridge.analysis.eqsql.hibernate.construct.StmtInfo;
import eqsql.dbridge.analysis.eqsql.util.VarResolver;
import eqsql.exceptions.UnknownConstructException;
import soot.Unit;
import soot.toolkits.graph.Block;

import java.util.Iterator;


/**
 * Created by ek on 4/5/16.
 */
public class DIRRegionAnalyzer extends AbstractDIRRegionAnalyzer {

    /* Singleton */
    private DIRRegionAnalyzer(){};
    public static DIRRegionAnalyzer INSTANCE = new DIRRegionAnalyzer();

    @Override
    public DIR constructDIR(ARegion region) {
        Block basicBlock = region.getHead();
        Iterator<Unit> iterator = basicBlock.iterator();

        DIR dir = new DIR(); //dir for this region

        StmtInfo stmtInfo = StmtInfo.nullInfo;

        while (iterator.hasNext()) {
            Unit curUnit = iterator.next();
            try {
                stmtInfo = StmtDIRConstructionHandler.constructDagSS(curUnit);
                if(stmtInfo == StmtInfo.nullInfo){
                    continue;
                }
                VarNode dest = stmtInfo.getDest();
                Node source = stmtInfo.getSource();

                VarResolver varResolver = new VarResolver(dir);
                Node resolvedSource = source.accept(varResolver);

                dir.insert(dest, resolvedSource, curUnit);
            }
            catch (UnknownConstructException e) {
                //Temporary fix. Todo: pass exception to higher level.
                e.printStackTrace();
                return null;
            }
        }

        return dir;
    }
}
