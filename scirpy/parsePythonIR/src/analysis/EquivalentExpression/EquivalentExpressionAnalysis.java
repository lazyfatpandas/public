package analysis.EquivalentExpression;

import cfg.JPUnitGraph;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.NullConstant;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
//TODO this is not working....
public class EquivalentExpressionAnalysis extends ForwardFlowAnalysis<Unit, ArraySparseSet> {

    public EquivalentExpressionAnalysis(JPUnitGraph graph) {
        super(graph);
        doAnalysis();
    }

    private FlowSet kill(FlowSet out, Unit u) {
        FlowSet toKill = newInitialFlow();
       /* if (u instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) u;
            //TODO check if getLeftOp is implemented
            Value leftOp = assignStmt.getLeftOp();
            for (Object e : out.toList()) {
                ExpressionTree cur = (ExpressionTree) e;
                if (cur.value.equals(leftOp))
                    toKill.add(cur);
            }
        }
*/
        return toKill;
    }


    @Override
    protected ArraySparseSet newInitialFlow() {
        return new ArraySparseSet();
    }

    @Override
    protected void flowThrough(ArraySparseSet arraySparseSet, Unit unit, ArraySparseSet a1) {

    }

    @Override
    protected ArraySparseSet entryInitialFlow() {
        return null;
    }

    @Override
    protected void merge(ArraySparseSet arraySparseSet, ArraySparseSet a1, ArraySparseSet a2) {

    }

    @Override
    protected void copy(ArraySparseSet arraySparseSet, ArraySparseSet a1) {

    }
}
