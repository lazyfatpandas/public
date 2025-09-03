package analysis.LiveVariable;

import ir.expr.Name;
import ir.internalast.JPValueBox;
import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import java.util.Iterator;

    public class Alva2 extends LiveVariableAnalysis {

        public Alva2 (DirectedGraph g) {
            super(g);
            doAnalysis();
        }
        @Override
        protected void merge(Object in1Value, Object in2Value, Object outValue) {
            FlowSet in1 = (FlowSet) in1Value, in2 = (FlowSet) in2Value, out=(FlowSet) outValue;
            in1.union(in2, out);
        }
        @Override
        protected void flowThrough(Object inValue, Object nodeValue, Object outValue) {
            FlowSet out = (FlowSet) outValue, in = (FlowSet) inValue;
            Unit node=(Unit)nodeValue;
            FlowSet writes = new ArraySparseSet();
            //TODO::::why getUseAndDefBoxes and not DefBoxes only
            for (Object defObj:  node.getUseAndDefBoxes()) {
                ValueBox def=(ValueBox)defObj;
                if(def instanceof JPValueBox){
                    System.out.println("JPValueBox");
                }
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
                //Change for alva
                if (use.getValue() instanceof JPValueBox) out.add((Local) use.getValue());
            }

            for (Object defObj:  node.getDefBoxes()) {
                ValueBox def=(ValueBox)defObj;
                String outName=((Local) def.getValue()).getName();
                for (Iterator<Local> itr = out.toList().iterator(); itr.hasNext(); ){
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
//                    {
//                    String name = l.getName();
//                    String names[] = name.split("_");
//                    if (names[0].equals(outName)) {
//                        out.remove(l);
//                        }
//                     }//else
                }//for Iterator
            }//for defObj
        }//flowThrough
    }


