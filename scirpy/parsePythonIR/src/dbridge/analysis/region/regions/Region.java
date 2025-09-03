package dbridge.analysis.region.regions;

import ir.Stmt.*;
import soot.Unit;
import soot.toolkits.graph.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by K. Venkatesh Emani on 12/19/2016.
 */
public class Region extends ARegion {
    public Region(Block b) {
        this.head = b;
        this.regionType = RegionType.BasicBlockRegion;
    }

    @Override
    public Unit firstStmt() {
        return getHead().getHead();
    }

    @Override
    public Unit lastStmt() {
        return getHead().getTail();
    }

    @Override
    public List<Unit> getUnits() {
        List<Unit> units = new ArrayList<>();
        //modified by bhu

//        for (Unit aHead : head) {
//            units.add(aHead);
//        }
        Iterator unitIt=head.iterator();
        while(unitIt.hasNext()){
            units.add((Unit) unitIt.next());
        }
        return units;
    }

    @Override
    public String toString() {
        String toStr = super.toString() + "\n";
        for (Unit unit : getUnits()) {
            toStr += unit.toString() + "\n";
        }
        return toStr;
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
    public String toString2(String indent) {
        String toStr = "";//super.toString2();// + "\n";
        for (Unit unit : getUnits()) {
            //quick fix for for loop, REPAIR IT
            //TODO Repair quick fix for avoiding extra statements in for loop
            boolean negCond=false;
            negCond=getCondPrint(unit);
            if(!(negCond || unit instanceof GotoStmt || unit instanceof NopStmt || (unit instanceof AssignStmt && ((AssignStmt) unit).getLineno()==-10) || (unit instanceof AssignmentStmtSoot && ((AssignmentStmtSoot) unit).getAssignStmt().getLineno()==-10))){
                if(unit instanceof  FunctionDefStmt){
                    toStr += unit.toString() + "\n";
                }
                else{
                    toStr += indent+unit.toString() + "\n";
                }

            }


        }
        return toStr;
    }
    public boolean getCondPrint(Unit unit){
        boolean negCond=false;
        if(unit instanceof AssignStmt && ((AssignStmt) unit).getLineno()==-10){
            negCond=true;
        }
        if(unit instanceof CallExprStmt && ((CallExprStmt) unit).getLineno()==-10){
            negCond=true;
        }
        return negCond;
    }

}
