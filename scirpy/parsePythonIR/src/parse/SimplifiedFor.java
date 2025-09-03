package parse;

import ir.IExpr;
import ir.Stmt.ForStmtPy;
import ir.Stmt.IfStmt;
import ir.Stmt.IfStmtPy;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.util.Chain;
import soot.util.HashChain;

public class SimplifiedFor {
    public PatchingChain<Unit> getSimplifiedFor(ForStmtPy forStmtPy, Chain locals){
        PatchingChain<Unit> extendedForList=new PatchingChain<Unit>(new HashChain<Unit>());
        //extendedIfList.add(ifStmt);
        ForStmtHelper forStmtHelper=new ForStmtHelper();
        forStmtHelper.addDetailedStmtsFor(extendedForList, forStmtPy,locals);
        return extendedForList;
    }

    }
