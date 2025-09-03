// Chiranmoy 9-2-24: Intra-procedural Live DataFrame Analysis
package analysis.LiveVariable;

import analysis.PythonScene;
import cfg.CFG;
import cfg.JPUnitGraph;
import ir.IExpr;
import ir.IStmt;
import ir.JPMethod;
import ir.Stmt.AssignmentStmtSoot;
import ir.Stmt.CallExprStmt;
import ir.expr.Call;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

public class LiveDataFrame extends BackwardFlowAnalysis<Unit, FlowSet<String>> {
    JPMethod jpMethod;
    JPUnitGraph unitGraph;
    public LiveDataFrame(JPMethod jpMethod) {
        super(new CFG(jpMethod).getUnitGraph());
        unitGraph = new CFG(jpMethod).getUnitGraph();
        this.jpMethod = jpMethod;
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<String> inSet, Unit unit, FlowSet<String> outSet) {
        // inSet is the input, compute outSet based on genSet and KillSet
        FlowSet<String> killSet = getKillSet(unit), genSet = getGenSet(unit);

//        FlowSet<String> killSet = newInitialFlow(), genSet = newInitialFlow();
//        handleUDFCalls(inSet, unit, outSet, killSet, genSet);

        outSet.union(inSet);

        // livenessGenerate: liveness is generated if a dataframe is printed or passed to an external module
        // livenessPropagate: for the genSet to be live, at least one LHS should be live in out (here inSet)
        boolean livenessGenerate  = unit instanceof CallExprStmt && (((CallExprStmt) unit).getBaseName().equals("print") || PythonScene.imported.contains(((CallExprStmt) unit).getBaseName()));
        boolean livenessPropagate = unit instanceof AssignmentStmtSoot && inSet.contains(killSet.iterator().next());

//        boolean livenessPropagate = !killSet.isEmpty() && inSet.contains(killSet.iterator().next());

        if(livenessGenerate || livenessPropagate) {
            outSet.difference(killSet);
            outSet.union(genSet);
        }
    }

    @Override
    protected FlowSet<String> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected void merge(FlowSet<String> inSet1, FlowSet<String> inSet2, FlowSet<String> outSet) {
        inSet1.union(inSet2, outSet);
    }

    @Override
    protected void copy(FlowSet<String> src, FlowSet<String> dest) {
        src.copy(dest);
    }

    private FlowSet<String> getGenSet(Unit unit) {
        IStmt stmt = (IStmt) unit;
        FlowSet<String> genSet = newInitialFlow();
        stmt.getDataFramesUsed().forEach(name -> genSet.add(name.getName()));
        return genSet;
    }

    private FlowSet<String> getKillSet(Unit unit) {
        IStmt stmt = (IStmt) unit;
        FlowSet<String> killSet = newInitialFlow();
        stmt.getDataFramesDefined().forEach(name -> killSet.add(name.getName()));
        return killSet;
    }

    public void print() {
        System.out.println("\n***** LIVE DATAFRAME ANALYSIS *****\n");
        Unit unit = unitGraph.getHeads().get(0);

        do {
            System.out.println("Before: " + getFlowBefore(unit));
            System.out.println(unit);
            System.out.println("After: " + getFlowAfter(unit) + "\n");
            unit = unitGraph.getSuccsOf(unit).isEmpty() ? null : unitGraph.getSuccsOf(unit).get(0);
        } while(unit != null);
    }
}
