// Chiranmoy 10-2-24: Add .compute(live_df=[...]) when dataframe passed to imported methods
package ForceCompute;

import analysis.LiveVariable.LiveDataFrame;
import analysis.PythonScene;
import cfg.CFG;
import ir.IExpr;
import ir.IStmt;
import ir.JPMethod;
import ir.Stmt.*;
import ir.expr.*;
import ir.internalast.Keyword;
import org.jboss.util.NotImplementedException;
import soot.Unit;
import java.util.ArrayList;
import java.util.List;

public class ForceCompute {
    JPMethod jpMethod;
    CFG cfg;
    LiveDataFrame ldf;

    public ForceCompute(JPMethod jpMethod, LiveDataFrame ldf) {
        this.jpMethod = jpMethod;
        this.ldf = ldf;
        this.cfg = new CFG(jpMethod);
    }

    public void force() {
        for (Unit unit : cfg.getUnitGraph()) {
            List<String> liveList = new ArrayList<>();
            ldf.getFlowAfter(unit).forEach(liveList::add);
            forceStmt(unit, liveList);
        }
    }

    private void forceStmt(Unit unit, List<String> liveList) {
        if (unit instanceof AssignmentStmtSoot) {
            AssignStmt assStmt = ((AssignmentStmtSoot) unit).getAssignStmt();
            IExpr rhs = assStmt.getRHS();

            if (rhs instanceof Call) {
                forceComputeCall((Call) rhs, liveList, true);
            }
        } else if (unit instanceof CallExprStmt) {
            Call call = (Call) ((CallExprStmt) unit).getCallExpr();
            forceComputeCall(call, liveList, true);
        } else if (unit instanceof IfStmt) {
            IStmt pyStmt = (IStmt) ((IfStmt) unit).getPyStmt();

            if (pyStmt instanceof ForStmtPy)
                forceComputeForStmt((ForStmtPy) pyStmt, liveList);
            else
                forceComputeIfStmt((IfStmtPy) pyStmt, liveList);
        } else if (!(unit instanceof ImportFrom || unit instanceof ImportStmt || unit instanceof AssignStmt ||
                unit instanceof GotoStmt || unit instanceof NopStmt)) {
//            throw new NotImplementedException("Stmt " + unit + " not implemented in force compute");
        }
        else{
            //
        }
    }

    private void forceComputeCall(Call call, List<String> liveList, boolean forceBasedOnImport) {
        String baseName = call.getBaseName();
        if(forceBasedOnImport && !PythonScene.imported.contains(baseName))
            return;

        List<Name> used = call.getDataFrames();

        for(int i=used.size()-1; i>0; i--) {
            liveList.add(used.get(i).getName());
        }

        for(int i=0; i<call.getArgs().size(); i++) {
            IExpr arg = call.getArgs().get(i);

            if(!arg.isDataFrame())
                continue;

            call.getArgs().set(i, makeComputeCall(arg, liveList));

            if(used.size() > 1 && !liveList.isEmpty())
                liveList.remove(liveList.size()-1);
        }

        for(int i=0; i<call.getKeywords().size(); i++) {
            Keyword keyword = call.getKeywords().get(i);

            if(!keyword.getValue().isDataFrame())
                continue;

            keyword.setValue(makeComputeCall(keyword.getValue(), liveList));

            if(used.size() > 1 && !liveList.isEmpty())
                liveList.remove(liveList.size()-1);
        }
    }

    private void forceComputeCompare(Compare compare, List<String> liveList) {
        IExpr left = compare.getLeft();

        if(left instanceof Call)
            forceComputeCall((Call) left, liveList, false);
        else if(left.isDataFrame())
            compare.setLeft(makeComputeCall(left, liveList));

        for(int i=0; i<compare.getComparators().size(); i++) {
            IExpr comp = compare.getComparators().get(i);

            if(comp.isDataFrame())
                compare.getComparators().set(i, makeComputeCall(comp, liveList));
            else if(comp instanceof Call)
                forceComputeCall((Call) comp, liveList, false);
        }
    }

    private void forceComputeForStmt(ForStmtPy forStmt, List<String> liveList) {
        IExpr iter = forStmt.getIterator();
        if(iter.isDataFrame())
            forStmt.setIterator(makeComputeCall(iter, liveList));
        else if(iter instanceof Call)
            forceComputeCall((Call) iter, liveList, false);
        // Handle other cases as you see them
    }


    private void forceComputeIfStmt(IfStmtPy ifStmt, List<String> liveList) {
        IExpr test = ifStmt.getTest();

        if(test.isDataFrame())
            ifStmt.setTest(makeComputeCall(test, liveList));
        else if(test instanceof Call)
            forceComputeCall((Call) test, liveList, false);
        else if(test instanceof Compare) {
            forceComputeCompare((Compare) test, liveList);
        }
        // Handle other cases as you see them
    }

    private Call makeComputeCall(IExpr expr, List<String> liveList) {
        List<IExpr> liveListNames = new ArrayList<>();
        liveList.forEach(elem -> liveListNames.add(new Name(elem, Expr_Context.Param)));

        Call computeCall = new Call();
        computeCall.getKeywords().add(new Keyword("live_df", new ListComp(liveListNames)));
        computeCall.setFunc(new Attribute("compute", expr));
        return computeCall;
    }
}
