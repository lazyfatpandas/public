package analysis.LiveVariable;

import ir.JPMethod;
import ir.expr.Name;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
//import sun.awt.image.ImageWatched;

import java.util.*;

public class AttributeLiveVariableAnalysisVariant extends BackwardFlowAnalysis {

    public AttributeLiveVariableAnalysisVariant (DirectedGraph g) {

        super(g);
        //this.firstUsed=firstUsed;
        //this.ug = new BriefUnitGraph(jpMethod.getBody());
        doAnalysis();
    }
    @Override
    protected void merge(Object in1Value, Object in2Value, Object outValue) {
       /* FlowSet in1 = (FlowSet) in1Value, in2 = (FlowSet) in2Value, out=(FlowSet) outValue;
        in1.union(in2, out);
        System.out.println("into merge");*/
        LinkedHashSet<Local> in1= (LinkedHashSet<Local>) in1Value;
        LinkedHashSet<Local> in2= (LinkedHashSet<Local>) in2Value;
        LinkedHashSet<Local> out = (LinkedHashSet<Local>) outValue;
        out.clear();
        out.addAll(in1);
        out.addAll(in2);
    }

 /*   @Override
    protected void mergeInto(Object succNode, Object inout, Object in) {

        FlowSet tmp = this.newInitialFlow();
        this.merge(succNode, inout, in, tmp);
        this.copy(tmp, inout);
        if(flowAfter != null){
            LinkedHashSet<Local> after = new LinkedHashSet<Local>();
            after.addAll(tmp.toList());
            flowAfter.put((Unit)succNode,after);}




    }*/

    @Override
    protected void flowThrough(Object inValue, Object nodeValue, Object outValue) {
        //System.out.println("node "+nodeValue);
        //System.out.println("dict "+firstUsed);
        //System.out.println("in "+inValue);


        /*
         To block the passing of column liveness if it was first used in the next node.
         */

        /*FlowSet out = (FlowSet) outValue, in = (FlowSet) inValue;
        Unit node=(Unit)nodeValue;
        FlowSet writes = new ArraySparseSet();

        for(Iterator<Local> itr=in.toList().iterator(); itr.hasNext();){

            Local l = itr.next();
            String localName = l.getName();

            if (firstUsed!=null && firstUsed.get(localName)==nodeValue){
                writes.add(l);
            }
        }


        //TODO::::why getUseAndDefBoxes and not DefBoxes only
        for (Object defObj:  node.getUseAndDefBoxes()) {
            ValueBox def=(ValueBox)defObj;
            if (def.getValue() instanceof Local){
                writes.add((Local) def.getValue());
                //modification from live variable started
                //Get defined value, see if it is dataframe and if it is remove all attributes of that frame from liveness
                //String outName=((Local) def.getValue()).getName();
                //System.out.println("Written:" +outName);


            }
        }
        in.difference(writes, out);



        for (ValueBox use: node.getUseBoxes()) {
            if (use.getValue() instanceof Local) out.add((Local) use.getValue());
        }

        for (Object defObj:  node.getDefBoxes()) {
            ValueBox def=(ValueBox)defObj;
            String outName=((Local) def.getValue()).getName();
            for (Iterator<Local> itr=out.toList().iterator(); itr.hasNext(); ){
                //for(Local l:outIt){
                Local l=itr.next();
                Name nameOb=null;
                if(l instanceof Name){
                    nameOb=(Name)l;
                    //Kill l if parent of l is killed
                    if(nameOb.getParent()!=null && nameOb.getParent().equals(outName)){
                        out.remove(l);
                    }
                }
                //else This is dead code now..kept just in case have to revert to this for test
                {
                    String name = l.getName();
                    String names[] = name.split("_");
                    if (names[0].equals(outName)) {
                        out.remove(l);
                    }
                }//else
            }//for Iterator
        }//for defObj

         */

        Unit node=(Unit)nodeValue;
        LinkedHashSet<Local> in=(LinkedHashSet<Local>) inValue;
        LinkedHashSet<Local> out=(LinkedHashSet<Local>) outValue;
        LinkedHashSet<Local> writes = new LinkedHashSet<Local>() ;
        LinkedHashSet<Local> temp = new LinkedHashSet<Local>() ;

        //TODO::::why getUseAndDefBoxes and not DefBoxes only
        for (Object defObj:  node.getUseAndDefBoxes()) {
            ValueBox def=(ValueBox)defObj;
            if (def.getValue() instanceof Local){
                writes.add((Local) def.getValue());
                //modification from live variable started
                //Get defined value, see if it is dataframe and if it is remove all attributes of that frame from liveness
                //String outName=((Local) def.getValue()).getName();
                //System.out.println("Written:" +outName);


            }
        }
        //in.difference(writes, out);
        out.addAll(in);
        out.removeAll(writes);


        for (ValueBox use: node.getUseBoxes()) {
            if (use.getValue() instanceof Local) out.add((Local) use.getValue());
        }

        for (Object defObj:  node.getDefBoxes()) {
            ValueBox def=(ValueBox)defObj;
            String outName=((Local) def.getValue()).getName();
            for (Iterator<Local> itr=out.iterator(); itr.hasNext(); ){
                //for(Local l:outIt){
                Local l=itr.next();
                Name nameOb=null;
                if(l instanceof Name){
                    nameOb=(Name)l;
                    //Kill l if parent of l is killed
                    if(nameOb.getParent()!=null && nameOb.getParent().equals(outName)){
                        //out.remove(l);
                        temp.add(l);
                    }
                }
                //else This is dead code now..kept just in case have to revert to this for test
//                {
//                    String name = l.getName();
//                    String names[] = name.split("_");
//                    if (names[0].equals(outName)) {
//                        //out.remove(l);
//                        temp.add(l);
//                    }
//                }//else
            }//for Iterator
        }//for defObj
        out.removeAll(temp);



    }

    @Override
    protected LinkedHashSet<Local> newInitialFlow() {
        LinkedHashSet<Local> emptySet = new LinkedHashSet<Local>();
        return emptySet;
    }
    @Override
    protected  LinkedHashSet<Local>  entryInitialFlow() {
        LinkedHashSet<Local> emptySet = new LinkedHashSet<Local>();
        return emptySet;
    }
    @Override
    protected void copy(Object srcSetValue, Object destSetValue) {
        LinkedHashSet<Local> srcSet = (LinkedHashSet<Local>) srcSetValue, destSet = (LinkedHashSet<Local>) destSetValue;
        destSet.clear();
        destSet.addAll(srcSet);
    }

}
