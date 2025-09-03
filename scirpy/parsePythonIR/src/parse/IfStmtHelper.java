package parse;

import ir.Stmt.IfStmt;
import ir.Stmt.WhileStmtPy;
import soot.JastAddJ.WhileStmt;
import soot.PatchingChain;
import soot.Unit;
import ir.Stmt.NopStmt;
import ir.Stmt.GotoStmt;
import soot.util.Chain;

/*OUR UNDERSTANDING IS:
          If (reverse condition) goto ElseLabel
                  stmts in if loop
                  goto AfterElselabel
ElseLabel: nop
              stmts in else
AfterElselabel: nop
            Remaining program
         */
public class IfStmtHelper {
    public void addDetailedStmtsIf(PatchingChain units, IfStmt ifStmt,Chain locals){
        //TODO write code here to add each statement from if body and orelsebody to patchingchain units
        //TODO complete this
        //TODO modify this and make this pakka

//        NopStmt nopElseBegin = Jimple.v().newNopStmt();
//        GotoStmt gotoElseBegin = Jimple.v().newGotoStmt(nopElseBegin);
//        NopStmt nopAfterElse = Jimple.v().newNopStmt();
//        GotoStmt gotoNopAfterElse = Jimple.v().newGotoStmt(nopAfterElse);

        NopStmt nopElseBegin = new NopStmt();
        GotoStmt gotoElseBegin = new GotoStmt(nopElseBegin);
        NopStmt nopAfterElse = new NopStmt();
        GotoStmt gotoNopAfterElse = new GotoStmt(nopAfterElse);
        units.add(ifStmt);
        //TEMP uncomment to see if there is issue with our implementation
        //units.add(gotoElseBegin);// Control will come here in case if condiiton fails, we will go to else in this goto
        //units.add(ifStmt.getTarget());// This is nop to begin if loop if the condition of if holds to be true

        if(ifStmt.getIfBody()!=null) {
            for(Unit unit:ifStmt.getIfBody().getUnits()){
                units.add(unit);
            }
            //IF condition is executed, should not execute else condition
            if(ifStmt.getOrelseBody()!=null && ifStmt.getOrelseBody().getUnits().size()!=0)
                units.add(gotoNopAfterElse);
        }
        //units.add(nopElseBegin);
        units.add(ifStmt.getTarget());


        if(ifStmt.getOrelseBody()!=null && ifStmt.getOrelseBody().getUnits().size()!=0) {
            for (Unit unit : ifStmt.getOrelseBody().getUnits()) {
                units.add(unit);
            }
            units.add(nopAfterElse);
        }


    }

    public void addDetailedStmtsWhile(PatchingChain units, IfStmt ifStmt,Chain locals){
        //TODO write code here to add each statement from if body and orelsebody to patchingchain units
        //TODO complete this
        //TODO modify this and make this pakka
//        NopStmt nopBeforeWhile = Jimple.v().newNopStmt();
//        NopStmt nopAfterWhile = Jimple.v().newNopStmt();

        NopStmt nopBeforeWhile = new NopStmt();
        NopStmt nopAfterWhile = new NopStmt();

        //NopStmt nopWhileBegin = Jimple.v().newNopStmt();

//
//        GotoStmt gotoBeforeWhile = Jimple.v().newGotoStmt(nopBeforeWhile);
//        GotoStmt gotoAfterWhile= Jimple.v().newGotoStmt(nopAfterWhile);

        GotoStmt gotoBeforeWhile = new GotoStmt(nopBeforeWhile);
        GotoStmt gotoAfterWhile= new GotoStmt(nopAfterWhile);


        units.add(nopBeforeWhile);// Control will come here after loop is executed
        units.add(ifStmt);//Here while is inserted as if stmt and will goto Target
        units.add(gotoAfterWhile);//if control reaches here, means condition has failed and hence exit loop by going to nop after while
        units.add(ifStmt.getTarget());// This is nop to begin if loop if the condition of if holds to be true

        //Add all the statements in while body here
        if(ifStmt.getIfBody()!=null) {
            for(Unit unit:ifStmt.getIfBody().getUnits()){
                units.add(unit);
            }
        }
        units.add(gotoBeforeWhile);//After loop goto the beginning of nop before while loop so that condition can be checked again
        units.add(nopAfterWhile);

    }


    public void addDetailedStmtsWhileN(PatchingChain units, WhileStmtPy whileStmtPy, Chain locals){
        //TODO write code here to add each statement from if body and orelsebody to patchingchain units
        //TODO complete this
        //TODO modify this and make this pakka
//        NopStmt nopBeforeWhile = Jimple.v().newNopStmt();
//        NopStmt nopAfterWhile = Jimple.v().newNopStmt();

        NopStmt nopBeforeWhile = new NopStmt();
        NopStmt nopAfterWhile = new NopStmt();

        //NopStmt nopWhileBegin = Jimple.v().newNopStmt();

//
//        GotoStmt gotoBeforeWhile = Jimple.v().newGotoStmt(nopBeforeWhile);
//        GotoStmt gotoAfterWhile= Jimple.v().newGotoStmt(nopAfterWhile);

        GotoStmt gotoBeforeWhile = new GotoStmt(nopBeforeWhile);
        GotoStmt gotoAfterWhile= new GotoStmt(nopAfterWhile);


        units.add(nopBeforeWhile);// Control will come here after loop is executed
        units.add(whileStmtPy);//Here while is inserted as if stmt and will goto Target
        units.add(gotoAfterWhile);//if control reaches here, means condition has failed and hence exit loop by going to nop after while
        NopStmt nopLoopBodyBegin = new NopStmt();//Jimple.v().newNopStmt();
        units.add(nopLoopBodyBegin);// This is nop to begin if loop if the condition of if holds to be true

        //Add all the statements in while body here
        if(whileStmtPy.getWhileBody()!=null) {
            for(Unit unit:whileStmtPy.getWhileBody().getUnits()){
                units.add(unit);
            }
        }
        units.add(gotoBeforeWhile);//After loop goto the beginning of nop before while loop so that condition can be checked again
        units.add(nopAfterWhile);

    }




}
