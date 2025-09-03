package dbridge.analysis.region.regions;

import ir.Stmt.AssignStmt;
import ir.Stmt.AssignmentStmtSoot;
import ir.Stmt.GotoStmt;
import ir.Stmt.NopStmt;
import soot.Unit;
import soot.toolkits.graph.Block;

public class FunctionRegion extends Region{
    public FunctionRegion(Block b) {
       super(b);
    }
    public String toString2() {
        String toStr = "";//super.toString2();// + "\n";
        for (Unit unit : getUnits()) {
            //quick fix for for loop, REPAIR IT
            //TODO Repair quick fix for avoiding extra statements in for loop
            boolean negCond=false;
            negCond=getCondPrint(unit);
            if(!(negCond || unit instanceof GotoStmt || unit instanceof NopStmt || (unit instanceof AssignStmt && ((AssignStmt) unit).getLineno()==-10) || (unit instanceof AssignmentStmtSoot && ((AssignmentStmtSoot) unit).getAssignStmt().getLineno()==-10))){
                toStr += unit.toString() + "\n";
            }


        }
        return toStr;
    }
}
