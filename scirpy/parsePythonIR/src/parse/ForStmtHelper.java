package parse;

import ir.DataType.IntType;
import ir.IExpr;
import ir.Stmt.AssignStmt;
import ir.Stmt.ForStmtPy;
import ir.Stmt.IfStmt;
import ir.expr.Num;
import soot.PatchingChain;
import soot.Type;
import soot.Unit;
import soot.UnitPrinter;
import ir.Stmt.GotoStmt;
import soot.jimple.Jimple;
import ir.Stmt.NopStmt;
import soot.util.Chain;
import soot.util.Switch;

import java.util.List;

public class ForStmtHelper {
    public void addDetailedStmtsFor(PatchingChain units, ForStmtPy forStmtPy,Chain locals){
        //Build initial statement
        AssignStmt assignStmtInit=new AssignStmt();
        //set LHS as target in iterator as it is getting killed
        assignStmtInit.getTargets().add(forStmtPy.getTarget());
        //TODO check this--added LHS=0
        assignStmtInit.setRHS(new Num(-1,-1, new IntType(0,"0")));
        assignStmtInit.setLineno(-10); //this implies don't print
        //Build iterator expression
        AssignStmt assignStmtEndLoop=new AssignStmt();
        assignStmtEndLoop.setRHS(forStmtPy.getIterator());
        assignStmtEndLoop.getTargets().add(new Num(-1,-1, new IntType(0,"0")));
        assignStmtEndLoop.setLineno(-10); //this implies don't print

        //TODO write code here to add each statement from if body and orelsebody to patchingchain units
        //TODO complete this
        //TODO modify this and make this pakka
        NopStmt nopLoopHeader = new NopStmt();//Jimple.v().newNopStmt();
        NopStmt nopLoopBodyBegin = new NopStmt();//Jimple.v().newNopStmt();
        NopStmt nopLoopBodyEnd = new NopStmt();//Jimple.v().newNopStmt();


        IfStmt ifStmt=new IfStmt(forStmtPy.getIterator(),nopLoopBodyBegin);
        ifStmt.setPyStmt(forStmtPy);
        ifStmt.setTypePyStmt("for");
        GotoStmt gotoAfterHeader = new GotoStmt(nopLoopBodyEnd);//Jimple.v().newGotoStmt(nopLoopBodyEnd);
        GotoStmt gotoAfterBody= new GotoStmt(nopLoopHeader);//Jimple.v().newGotoStmt(nopLoopHeader);

        units.add(assignStmtInit);

        units.add(nopLoopHeader);
        units.add(ifStmt);//Here for condition is inserted

        units.add(gotoAfterHeader);
        units.add(nopLoopBodyBegin);

        //Updating locals
        locals.addAll(assignStmtInit.getLocals());
        locals.addAll(ifStmt.getLocals());


        //Add all the statements in for body here
        if(forStmtPy.getBody()!=null) {
            for(Unit unit:forStmtPy.getBody().getUnits()){
                units.add(unit);
            }
        }

        //TODO check this,,,improve this..this is terrible hack
        units.add(assignStmtEndLoop);
        units.add(gotoAfterBody);//After loop goto the beginning of nop before while loop so that condition can be checked again
        units.add(nopLoopBodyEnd);

    }
}
