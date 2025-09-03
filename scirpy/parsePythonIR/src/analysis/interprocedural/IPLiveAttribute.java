package analysis.interprocedural;

import analysis.LiveVariable.LiveVariableAnalysis;
import analysis.Pandas.PandasAPIs;
import analysis.PythonScene;
import ir.IExpr;
import ir.Stmt.AssignStmt;
import ir.Stmt.AssignmentStmtSoot;
import ir.Stmt.CallExprStmt;
import ir.expr.*;
import ir.util.UtilityFuncs;
import soot.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.Iterator;
import java.util.List;

public class IPLiveAttribute extends LiveVariableAnalysis {
    List<SootClass> sootClasses=null;
    public IPLiveAttribute (DirectedGraph g, List<SootClass> sootClasses) {
            super(g);
            this.sootClasses=sootClasses;
            doAnalysis();

        }
    public IPLiveAttribute (DirectedGraph g) {
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
        Unit node = (Unit) nodeValue;
        FlowSet writes = new ArraySparseSet();
        //TODO::::why getUseAndDefBoxes and not DefBoxes only
        for (Object defObj : node.getUseAndDefBoxes()) {
            ValueBox def = (ValueBox) defObj;
            if (def.getValue() instanceof Local) {
                writes.add((Local) def.getValue());

                /* Commented on 10/06/21 as these cases are handled in can kill
                // modified on June 4, 2021, only kill all attributes if it is a dataframe define operation from csv file or other dataframes.
                //if other dataframe, killing liveness is required..if same dataframe, not as it is a filter operation.
                //This is a temp fix by identifying that if it same df and type subscript, it is a filter operation
                if(! (((AssignmentStmtSoot)nodeValue).getAssignStmt().getRHS() instanceof Subscript) ) {

                    if(!getRHSDFName(node).equals(getDFName(def))) {
                        writes.add((Local) def.getValue());
                    }
                    //modification from live variable started
                    //Get defined value, see if it is dataframe and if it is remove all attributes of that frame from liveness
                    //String outName=((Local) def.getValue()).getName();
                    //System.out.println("Written:" +outName);
                }
                */
            }
        }
        in.difference(writes, out);

        for (ValueBox use : node.getUseBoxes()) {
            //Modified to avoid Pandas APIS
            //if (use.getValue() instanceof Local) out.add((Local) use.getValue());
            if (use.getValue() instanceof Local && ! PandasAPIs.getAPIs().contains(((Local) use.getValue()).getName()) ){
                out.add((Local) use.getValue());
            }
            //TODO quick fix..
            //23 added if condition to avoid typecast error for b1 prog
            if(use.getValue() instanceof Local ) {
                if (PandasAPIs.getAPIs().contains(((Local) use.getValue()).getName()) && ((Local) use.getValue()).getName().equals("columns")) {
                    // Temp comment for DIAS bhu 07-10-2024
//                    System.out.println("Can't optimize as identified column reference by index.. in IPLiveAttribute for"+ use.getValue() );
//                    System.exit(0);
                }
            }
        }
        /*Added for checking all conditions for killing live attributes
            The conditions to not kill live attributes are:
            1. It is a groupby clause
            2. It is a compare clause on a df such that it will retain df
         */
        //3.0 transitiv dependencies
        updateTransitiveDFs(node,out);
        //4.0 lambda Start
        updateLambdaDFs(node,out);
        //4.0 Lambda End
        boolean canKill = true;
        canKill = canKillAttributes(node);
        if (canKill){
            for (Object defObj : node.getDefBoxes()) {
                ValueBox def = (ValueBox) defObj;
                String outName = getDFName(def);

                for (Iterator<Local> itr = out.toList().iterator(); itr.hasNext(); ) {
                    //for(Local l:outIt){
                    Local l = itr.next();
                    Name nameOb = null;
                    if (l instanceof Name) {
                        nameOb = (Name) l;
                        //Kill l if parent of l is killed
                        //TODO for groupby, temp fix, work it out later
//                        if (PythonScene.isGroupBy && PythonScene.groupbyStmts.contains(node)) {
//                        } else
                        if( ((Local) def.getValue()) instanceof Name ){
                        if (nameOb.getParent() != null && nameOb.getParent().equals(outName)) {
                            out.remove(l);
                            //updateDFCopy(l,out,node);
                        }
                        }//if
                        else if(((Local) def.getValue()) instanceof Subscript ){
                            if (nameOb.getParent() != null && nameOb.getParent().equals(outName)) {
                                if(nameOb.id.equals(getSubscriptDFColumnName((Subscript)((Local) def.getValue())))) {
                                    out.remove(l);
                                    //updateDFCopy(l,out,node);
                                }
                            }

                        }
                    }

                }//for Iterator

            }//for defObj
    }//cankill if
        if(sootClasses!=null){
            IPAttributeAnalysis.insertIPLiveAttributes(node,out,sootClasses);
        }

    }//flowThrough


    private boolean canKillAttributes(Unit node){
        //TODO improve this to kill other attributes
        if(PythonScene.isGroupBy && PythonScene.groupbyStmts.contains(node)){
            return false;
        }
        if(PythonScene.compareStmtsDFFiltering.contains(node)){
            return false;
        }
        if(PythonScene.DFFilterOps.contains(node)){
            return false;
        }
        if(PythonScene.unitGetDummiesMap.get(node)!=null){
            return false;
        }
        return true;


    }

    private String getDFName(ValueBox def ){
        if( ((Local) def.getValue()) instanceof Subscript){
            IExpr value=((Subscript)(Local) def.getValue()).getValue();
            if(value instanceof Name){
                return ((Name)value).id;
            }
        }
        return ((Local) def.getValue()).getName();


    }


    private String getSubscriptDFColumnName(Subscript subscript){
        IExpr index=subscript.getSlice().getIndex();
        if(index instanceof Str){
            return ((Str) index).getS();
        }
        //3.01 vldb
        else if(index instanceof Constant){
            return ((Constant) index).getValue();
        }
        return null;

    }

    //3.0 This code is to get columns used transitively
    //i.e. if a dataframe is created from other dataframe, the the columns it uses should be made available in the parent dataframe while parent dataframe is initialized
    //TODO complete this
    private void updateTransitiveDFs(Unit node,FlowSet out){
       if(node instanceof AssignmentStmtSoot){
           //May 2024
//          String isDummy=PythonScene.unitGetDummiesMap.get(node);

           for (Object defObj : node.getDefBoxes()) {
               ValueBox def = (ValueBox) defObj;
               String outName = getDFName(def);
               //May 2024 get dummmies changes
//               if(isDummy!=null && isDummy.equals("yes")){
//                   outName="";
//               }
               for (Iterator<Local> itr = out.toList().iterator(); itr.hasNext(); ) {
                   //for(Local l:outIt){
                   Local l = itr.next();
                   Name nameOb = null;
                   if (l instanceof Name) {
                       nameOb = (Name) l;
                       //Kill l if parent of l is killed
                       //TODO for groupby, temp fix, work it out later
//                        if (PythonScene.isGroupBy && PythonScene.groupbyStmts.contains(node)) {
//                        } else
                       if( ((Local) def.getValue()) instanceof Name ){
                           //this means lhs has some df..
                           if (nameOb.getParent() != null && nameOb.getParent().equals(outName)) {
                               //here either remove if it is a df creation from file or change df if this df created from other df, update its name
                               //out.remove(l);
                               boolean readFileStmt=false;
                               readFileStmt=isReadFileStmt(node);
                               //if this is not a df creation statement, assuming that this df is created from some other df
                               //therefore update live attributes from lhs df to rhs df
                               if(!readFileStmt) {
                                   String rhsParentDFName=getRHSDFName(node);
                                   //update lhs df element name to rhs df element name
                                   ((Name)l).setParent(rhsParentDFName);
                                   updateDFCopy(l, out, node);
                               }
                           }
                       }//if
//                       else if(((Local) def.getValue()) instanceof Subscript ){
//                           if (nameOb.getParent() != null && nameOb.getParent().equals(outName)) {
//                               if(nameOb.id.equals(getSubscriptDFColumnName((Subscript)((Local) def.getValue())))) {
//                                   //out.remove(l);
//                                   boolean readFileStmt=false;
//                                   readFileStmt=isReadFileStmt(node);
//                                   //if this is not a df creation statement, assuming that this df is created from some other df
//                                   //therefore update live attributes from lhs df to rhs df
//                                   if(!readFileStmt) {
//                                       updateDFCopy(l, out, node);
//                                   }
//                               }
//                           }
//
//                       }
                   }

               }//for Iterator

           }//for defObj
       }

    }



    private void updateDFCopy(Local l,FlowSet out, Unit unit){
        System.out.println("Df from other df:"+unit);

    }

    public boolean isReadFileStmt(Unit node){
     if(PythonScene.unitToDfNameMap.containsKey(node)){
         return true;
     }
     return false;
    }

    private String getRHSDFName(Unit unit){
        String rhsDFName="";
        if(unit instanceof AssignmentStmtSoot){
            AssignmentStmtSoot assignmentStmtSoot =(AssignmentStmtSoot)unit;
            AssignStmt assignStmt =assignmentStmtSoot.getAssignStmt();
            IExpr dfName=assignStmt.getTargets().get(0);
            IExpr rhs=assignStmt.getRHS();
            if(rhs instanceof Call){

                return UtilityFuncs.getParentnameAttribute(rhs).getName();
            }
            else if(rhs instanceof Subscript){
              Subscript rhsSubscript=(Subscript)rhs;
              IExpr subValue=rhsSubscript.getValue();
              if(subValue instanceof Name){
                  rhsDFName=((Name)subValue).getName();
              }
            }


        }
        return rhsDFName;
    }



//THis is to update DF cols used in case a lambda statement is used in the program
    private void updateLambdaDFs(Unit node,FlowSet out) {
        if (node instanceof AssignmentStmtSoot) {
            boolean isLambda = false;
            Name actuallDfName = null;
            String dfNameinLambda = null;


            IExpr rhs = ((AssignmentStmtSoot) node).getAssignStmt().getRHS();
            if (rhs instanceof Call) {
                if (((Call) rhs).getArgs().size() != 0) {
                    if (((Call) rhs).getArgs().get(0) instanceof Lambda) {
                        Name lambdaDfName = null;
                        //This is lambda statement, update maybe need
                        IExpr func = ((Call) rhs).getFunc();
                        if (func instanceof Attribute) {
                            if (((Attribute) func).getValue() instanceof Name) {
                                actuallDfName = (Name) ((Attribute) func).getValue();
                                Lambda lambdaExpr = (Lambda) ((Call) rhs).getArgs().get(0);
                                if (lambdaExpr.getArgs().get(0) instanceof Arg) {
                                    Arg dfArg = (Arg) lambdaExpr.getArgs().get(0);
                                    dfNameinLambda = dfArg.getS();
                                    isLambda = true;
                                }
                            }

                        }


                    }
                }
            }

            //TODO this is incomplete, code after this is from previous method..modify this code
            if (isLambda) {
                for (Iterator<Local> itr = out.toList().iterator(); itr.hasNext(); ) {
                    Local l = itr.next();
                    Name nameOb = null;
                    if (l instanceof Name) {
                        nameOb = (Name) l;
                        if (nameOb.getParent() != null && nameOb.getParent().equals(dfNameinLambda)) {
                            nameOb.setParent(actuallDfName.id);
                        }

                    }

                }

            }

        }
    }
    //OLD CODE
    //    CallGraph cg;
//    public IPLiveAttribute(CallGraph cg) {
//        this.cg=cg;
//        analyze();
//    }

//    private void analyze(){
//        Iterator<Edge> edgeIterator= cg.iterator();
//        while (edgeIterator.hasNext()){
//            Edge edge=edgeIterator.next();
//            SootMethod sootMethod=edge.getSrc().method();
//            JPMethod jpMethod=(JPMethod)sootMethod;
//            CFG cfg ;
//            cfg = new CFG(jpMethod);
//            AttributeLiveVariableAnalysis alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
//
//        }
//        for( )
//        CFG cfg ;
////        cfg = new CFG(method);
//    }
//}
}