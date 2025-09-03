package parse;

import ir.IExpr;
import ir.Stmt.IfStmt;
import ir.Stmt.IfStmtPy;
import ir.Stmt.NopStmt;
import ir.expr.*;
import parse.ops.BoolOpParser;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Jimple;
import soot.util.Chain;
import soot.util.HashChain;

import java.util.ArrayList;
import java.util.List;

public class SimplifiedIf {

    public PatchingChain<Unit> getSimplifiedIf(IfStmtPy ifStmtPy, boolean whileBool, Chain locals){
        PatchingChain<Unit> extendedIfList=new PatchingChain<Unit>(new HashChain<Unit>());
        IExpr test= ifStmtPy.getTest();
        //TODO createSimpleConditions and Add statements for them in extendedIfList
        IExpr condition=createSimpleConditions(test, extendedIfList);
        NopStmt nop= new ir.Stmt.NopStmt();//Jimple.v().newNopStmt();
        IfStmt ifStmt=new IfStmt(condition,nop);
        ifStmt.copyIf(ifStmtPy);
        ifStmt.setPyStmt(ifStmtPy);
        ifStmt.setLineno(ifStmtPy.getLineno());
        //extendedIfList.add(ifStmt);
        IfStmtHelper ifStmtHelper=new IfStmtHelper();
        if(!whileBool)
            ifStmtHelper.addDetailedStmtsIf(extendedIfList,ifStmt,locals);
        if(whileBool) {
            ifStmtPy.setWhile(true);
            //Modified on Apr 29, 2024
//            ifStmtHelper.addDetailedStmtsWhile(extendedIfList, ifStmt, locals);
            ifStmtHelper.addDetailedStmtsIf(extendedIfList,ifStmt,locals);

        }
        return extendedIfList;

    }

    private IExpr createSimpleConditions(IExpr test, PatchingChain<Unit> extendedIfList) {
        //TODO createSimpleConditions and Add statements for them in extendedIfList
        IExpr iExpr=null;
        if(test instanceof BoolOp) {
            BoolOp test1 = (BoolOp) test;
            //TODO simplification
            System.out.println("Condition boolop not implemented for simplfying");
            System.exit(1);
        }
        else if (test instanceof Compare) {
            //TODO implement simplification
            Compare originalTest=(Compare)test;
            return originalTest;

        }
        else if (test instanceof BinOp) {
            //TODO implement simplification
            BinOp originalTest=(BinOp)test;
            return originalTest;

        }
        else {
            System.out.println("Condition encountered not implemented for simplfying");
            System.exit(1);
        }
        //should never reach here
        return iExpr;
    }

    //Should not be used anymore..changed implementation
/*
    private List<OpsType> reverseOps(Compare originalTest) {
        List<OpsType> opsTypes=new ArrayList<>();
        for(OpsType opsType:originalTest.getOps()){
        opsTypes.add(ReverseOp.getReverseOp(opsType));
    }
    return opsTypes;
}
*/

}
