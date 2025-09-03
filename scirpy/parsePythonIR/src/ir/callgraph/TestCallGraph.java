package ir.callgraph;

import analysis.interprocedural.IPLiveAttribute;
import soot.jimple.toolkits.callgraph.CallGraph;

public class TestCallGraph {
    public static void main(String args[]){
        CallGraphBuilder callGraphBuilder=new CallGraphBuilder();
        CallGraph cg= callGraphBuilder.callGraphMaker();
        System.out.println(cg);
       // IPLiveAttribute ipLiveAttribute=new IPLiveAttribute(cg);
    }
}
