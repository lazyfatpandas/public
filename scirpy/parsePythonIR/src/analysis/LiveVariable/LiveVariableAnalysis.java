package analysis.LiveVariable;

import ir.JPStmt;
import ir.Stmt.AssignmentStmtSoot;
import soot.options.*;

import soot.*;
import java.util.*;
import soot.util.ArraySet;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;

public class LiveVariableAnalysis extends BackwardFlowAnalysis{

    public LiveVariableAnalysis (DirectedGraph g) {
        super(g);
        doAnalysis();
    }
    protected FlowSet newInitialFlow() {
        FlowSet emptyFlowSet =new ArraySparseSet();
        return emptyFlowSet;
    }
    protected FlowSet entryInitialFlow() {
        FlowSet emptyFlowSet = new ArraySparseSet();
        return emptyFlowSet;
    }



    /*  @Override
      protected void flowThrough(FlowSet in, Unit node, FlowSet out) {
          FlowSet<Local> writes = new ArraySet<>();

          for (ValueBox def: node.getUseAndDefBoxes()) {
              if (def.getValue() instanceof Local) writes.add((Local) def.getValue());
          }
          in.difference(writes, out);

          for (ValueBox use: node.getUseBoxes()) {
              if (use.getValue() instanceof Local) out.add((Local) use.getValue());
          }
      }

  */
    @Override
    protected void flowThrough(Object inValue, Object nodeValue, Object outValue) {
        FlowSet out = (FlowSet) outValue, in = (FlowSet) inValue;
        Unit node=(Unit)nodeValue;
        FlowSet writes = new ArraySparseSet();

        for (Object defObj:  node.getUseAndDefBoxes()) {
            ValueBox def=(ValueBox)defObj;
            if (def.getValue() instanceof Local) writes.add((Local) def.getValue());
        }
        in.difference(writes, out);

        for (ValueBox use: node.getUseBoxes()) {
            if (use.getValue() instanceof Local) out.add((Local) use.getValue());
        }

    }
    /*
      @Override
        protected void merge(FlowSet in1, FlowSet in2, FlowSet out) {
            in1.union(in2, out);
        }
        @Override
        protected void copy(FlowSet srcSet, FlowSet destSet) {
            srcSet.copy(destSet);
        }
     */
    @Override
    protected void merge(Object in1Value, Object in2Value, Object outValue) {
        FlowSet in1 = (FlowSet) in1Value, in2 = (FlowSet) in2Value, out=(FlowSet) outValue;
        in1.union(in2, out);
    }

    @Override
    protected void copy(Object srcSetValue, Object destSetValue) {
        FlowSet srcSet = (FlowSet) srcSetValue, destSet = (FlowSet) destSetValue;
        srcSet.copy(destSet);

    }
}