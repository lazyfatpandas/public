package parse;

import ir.DataType.IntType;
import ir.Stmt.*;
import ir.expr.Num;
import soot.PatchingChain;
import soot.Unit;
import soot.util.Chain;

public class WhileStmtHelper {
    public void addDetailedStmtsWhile(PatchingChain units, WhileStmtPy whileStmtPy, Chain locals){
        //Build initial statement
        AssignStmt assignStmtInit=new AssignStmt();
        //set LHS as target in iterator as it is getting killed
        assignStmtInit.getTargets().add(whileStmtPy.getTest());
        //TODO check this--added LHS=0
        assignStmtInit.setRHS(new Num(-1,-1, new IntType(0,"0")));
        assignStmtInit.setLineno(-10); //this implies don't print
        //Build iterator expression
        //commented on Apr 29 04 2024
//        AssignStmt assignStmtEndLoop=new AssignStmt();
//        assignStmtEndLoop.setRHS(whileStmtPy.getIterator());
//        assignStmtEndLoop.getTargets().add(new Num(-1,-1, new IntType(0,"0")));
//        assignStmtEndLoop.setLineno(-10); //this implies don't print

        //TODO write code here to add each statement from if body and orelsebody to patchingchain units
        //TODO complete this
        //TODO modify this and make this pakka
        NopStmt nopLoopHeader = new NopStmt();//Jimple.v().newNopStmt();
        NopStmt nopLoopBodyBegin = new NopStmt();//Jimple.v().newNopStmt();
        NopStmt nopLoopBodyEnd = new NopStmt();//Jimple.v().newNopStmt();


        IfStmt ifStmt=new IfStmt(whileStmtPy.getTest(),nopLoopBodyBegin);
        ifStmt.setPyStmt(whileStmtPy);
        ifStmt.setTypePyStmt("while");
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
        if(whileStmtPy.getWhileBody()!=null) {
            for(Unit unit:whileStmtPy.getWhileBody().getUnits()){
                units.add(unit);
            }
        }

        //TODO check this,,,improve this..this is terrible hack
//        units.add(assignStmtEndLoop);
        units.add(gotoAfterBody);//After loop goto the beginning of nop before while loop so that condition can be checked again
        units.add(nopLoopBodyEnd);

    }
}
