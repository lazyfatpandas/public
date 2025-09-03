package passes.JoinOptimization;
import DataFileAnalysis.DataAnalyzer;
import DataFileAnalysis.model.FileInfo;
import DataFileAnalysis.model.MetaData;
import PythonGateWay.ReadProps;
import analysis.LiveVariable.Alva2;
import analysis.LiveVariable.AttributeLiveVariableAnalysis;
import analysis.LiveVariable.LiveVariableAnalysis;
import analysis.interprocedural.IPLiveAttribute;
import cfg.CFG;
import ir.IExpr;
import ir.JPBody;
import ir.JPMethod;
import ir.Stmt.AssignStmt;
import ir.Stmt.AssignmentStmtSoot;
import ir.Stmt.CallExprStmt;
import ir.Stmt.ImportStmt;
import ir.expr.*;
import ir.internalast.Keyword;
import ir.internalast.Targets;
import passes.PrePass;
import rewrite.pd1.Pd1Elt;
import rewrite.pd2.InputFileDataTypeMapper;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.*;

public class JoinOptmizations {

    JPBody jpBody ;
    boolean hasImport;
    boolean hasPandas;
    String pandasAlias="";
    PatchingChain<Unit> unitChain;
    List<Pd1Elt> pdLists =new ArrayList<>();
    List<InputFileDataTypeMapper> ifdtmList =new ArrayList<>();
    List<Unit> mergeUnits=new ArrayList<>();
    List<JoinAttributes> allJoinStmts=new ArrayList<>();

    String mainPath="";
    public JoinOptmizations(JPBody jpBody) {
        this.jpBody = jpBody;
    }
    List <SootClass> sootClasses=null;
    CFG cfg ;
    JPMethod jpMethod;
    LiveVariableAnalysis lva;
    Iterator unitIterator;
    //AttributeLiveVariableAnalysis alva;
    IPLiveAttribute alva;
    Alva2 alva2;
    List<Unit> pandasUnits;
    PrePass prePass;

    public JoinOptmizations(JPMethod jpMethod, String path, List <SootClass> sootClasses) {
        this.prePass=new PrePass(jpMethod);
        this.sootClasses=sootClasses;
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
        System.out.println(this.cfg.getUnitGraph().toString());
        this.lva = new LiveVariableAnalysis(cfg.getUnitGraph());
        //this.alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        this.alva=new IPLiveAttribute(cfg.getUnitGraph(),sootClasses);
        this.unitIterator = cfg.getUnitGraph().iterator();
        this.alva2=new Alva2(cfg.getUnitGraph());
        pandasUnits=new ArrayList<Unit>();
        this.mainPath=path;
        performPassJoin();
        System.out.println("Merged Units are:" +mergeUnits);
//        updateUsedColumns();

    }

    public void performPassJoin(){
        FlowSet beforeSet,afterSet;



        //generate flowset for each unit
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            beforeSet = (FlowSet) alva.getFlowBefore(unit);
            afterSet = (FlowSet) alva.getFlowAfter(unit);
            if(unit instanceof ImportStmt) {
                hasImport = true;
                ImportStmt importStmt = (ImportStmt) unit;
                if (importStmt.getNames().getName().equals("pandas") || importStmt.getNames().getName().equals(ReadProps.read("ScapaName"))) {
                    hasPandas = true;
                    pandasAlias = importStmt.getNames().getAsname();
                    //System.out.println("Pandas alias:"+ pandasAlias);
                }

            }
            if(hasPandas){
                if(unit instanceof AssignmentStmtSoot){
                    //System.out.println("Assignment statement:"+ unit);
                    AssignmentStmtSoot assignmentStmtSoot=(AssignmentStmtSoot)unit;
                    AssignStmt assignStmt=assignmentStmtSoot.getAssignStmt();
                    IExpr rhs=assignStmt.getRHS();
                    List<IExpr> target=assignStmt.getTargets();
                    if(rhs instanceof Name){
                        Name name=(Name)rhs;
                    }
                    else if(rhs instanceof Call){
                        Call call=(Call)rhs;
                        if(call.getFunc() instanceof Attribute){
                            Attribute func=(Attribute)call.getFunc();
                            //System.out.println(func.getAttr());
                            while(func.getValue() instanceof Attribute){
                                //System.out.println(func.getAttr());
                                func=(Attribute)func.getValue();

                            }
                            if(func.getValue() instanceof Name){

                                Name name=(Name)func.getValue();

                                if(name.id.equals(pandasAlias)){
                                    String dfName="";
                                    int lineno=-1;
                                    if(target.get(0) instanceof Targets) {
                                        dfName = ((Targets) target.get(0)).getName();
                                        lineno = ((Targets) target.get(0)).getLineno();
                                    }
                                    if(target.get(0) instanceof Name) {
                                        dfName = ((Name) target.get(0)).getName();
                                        lineno = ((Name) target.get(0)).getLineno();
                                    }

                                    //PD2PassADDStart
                                    String arg="";
                                    if(call.getArgs().size()!=0 && call.getArgs().get(0) instanceof Str) {
                                        Str argStr = (Str) call.getArgs().get(0);
                                        arg=argStr.getS();
                                    }
                                    Pd1Elt elt=new Pd1Elt();
                                    String attr=func.getAttr();
                                    //String attrName=att
                                    if(attr.equals("merge")) {
                                        mergeUnits.add(unit);
                                        JoinAttributes joinAttributes=new JoinAttributes(unit);
                                        allJoinStmts.add(joinAttributes);


                                        //set filename, generate & store metadata if required & get metadata



                                    }//attr.equals("read_csv")
                                }
                            }
                        }
                    }
                }
                if(unit instanceof CallExprStmt){
                    IExpr calIExpr=((CallExprStmt) unit).getCallExpr();
                    if(calIExpr instanceof Call){
                        //dfNmae=name of dataframe if dataframe. attr=action, we are looking for "drop" here
                        String dfName="", attr="";
                        Call call=(Call)calIExpr;
                        if(call.getFunc()!=null && call.getFunc() instanceof Attribute){
                            Attribute func=(Attribute)call.getFunc();
                            attr=func.getAttr();
                            if(func.getValue()!=null && func.getValue() instanceof Name){
                                dfName=((Name)func.getValue()).getName();
                            }
                        }
                        //If something is dropped::check if that is used from here to the point of definition in dataframe....
                        if(attr.equals("drop")){
                            //if a column is dropped, checking dataframe from which it was dropped, and adding the drop info to its list

                            for(Pd1Elt pd1Elt:pdLists){
                                if(pd1Elt.getDfName().equals(dfName)){
                                    List<IExpr> args= call.getArgs();
                                    for(IExpr arg:args){
                                        if(arg instanceof ListComp){
                                            ListComp argLC=(ListComp)arg;
                                            for(IExpr elt: argLC.getElts()){
                                                if(elt instanceof Str){
                                                    pd1Elt.getDropCols().add((((Str) elt)).getS());
                                                    pd1Elt.getDropsColsUnit().add(unit);
                                                    pd1Elt.getDropColUnitMap().put( ((Str) elt).getS(),unit);
                                                }
                                            }
                                        }
                                    }
                                    //pd1Elt.getDropCols().add("test");
                                }
                            }

                        }


                    }


                    //to delete line if it is a drop column
                    // ((CallExprStmt) unit).setLineno(-10);

                    //System.out.println("CallExprStmt statement:"+ unit);
                    //CallExprStmt callExprStmt=(CallExprStmt)unit;
                }



            }



        }// end unitIterator
    }//end Perform pass
}
