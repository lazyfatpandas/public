package passes.lasp;

import DataFileAnalysis.model.MetaData;
import PythonGateWay.ReadProps;
import analysis.LiveVariable.Alva2;
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
import ir.internalast.Targets;
import passes.JoinOptimization.JoinAttributes;
import passes.PrePass;
import rewrite.pd1.Pd1Elt;
import rewrite.pd2.InputFileDataTypeMapper;
import soot.PatchingChain;
import soot.SootClass;
import soot.Unit;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class LaspRewrites {
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
    public LaspRewrites(JPBody jpBody) {
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

    public LaspRewrites(JPMethod jpMethod, String path, List <SootClass> sootClasses, List<Pd1Elt> pdLists) {
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
        this.pdLists=pdLists;
        //23 change for multi functional
        this.hasImport=true;
        this.hasPandas=true;
        this.pandasAlias="pd";
        performPassLaspRewrite();

        //System.out.println("Merged Units are:" +mergeUnits);
//        updateUsedColumns();

    }

    public void performPassLaspRewrite(){
        FlowSet beforeSet,afterSet;
        Unit pdAnalyzeUnit=null;



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
                    importStmt.getNames().setName(ReadProps.read("ScapaName"));
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
                    else if(rhs instanceof Attribute){
                        //3.01
//                        Attribute attribute=(Attribute)rhs;
//                        Call lambdaCall=processAttributeApplyTransformations(attribute);
//                        if(lambdaCall!=null && lambdaCall.getFunc()!=null) {
//                            assignStmt.setRHS(lambdaCall);
//                        }


                    }
                }
                if(unit instanceof CallExprStmt){
                    IExpr calIExpr=((CallExprStmt) unit).getCallExpr();
                    if(calIExpr instanceof Attribute){
                        //dfNmae=name of dataframe if dataframe. attr=action, we are looking for "drop" here
                        String dfName="", attr="";
                        Attribute attribute=(Attribute) calIExpr;
                        //3.01
//                        Call lambdaCall=processAttributeApplyTransformations(attribute);
//                        if(lambdaCall!=null) {
//                            ((CallExprStmt) unit).setCallExpr(lambdaCall);
//                        }
                    }
                    //Remove pd.analyze
                    if(calIExpr instanceof Call){
                        Call call=(Call)calIExpr;
                        if(call.getFunc() instanceof Attribute){
                            Attribute funcAtt=(Attribute)call.getFunc();
                            if(funcAtt.getValue().toString().equals(pandasAlias) && (call.getArgs().size()==0) && funcAtt.getAttr().equals(ReadProps.read("analyzemethodName"))){
                                pdAnalyzeUnit=unit;

                            }
                        }

                    }
                    //10 04 2024 commented update print statement
//                   UpdatePrintStmts.update(unit,pdLists);

                }



            }



        }// end unitIterator
       // System.out.print("");
        try {
            //2024 bhu lazyprint
            AssignStmt assignStmt=getStmtPrintOverRide();
            jpMethod.getBody().getUnits().insertBefore(assignStmt,pdAnalyzeUnit);
            jpMethod.getBody().getUnits().add(getStmtPrintFlush());
            //2024 bhu lazyprint end 1
            jpMethod.getBody().getUnits().remove(pdAnalyzeUnit);
        }catch (Exception e){

        }
    }//end Perform pass
     private Call processAttributeApplyTransformations(Attribute attribute){
       /*
       df.pickup_datetime.dt.dayofweek
       to
       df.pickup_datetime.apply(lambda x: dt.dayofweek)
        */
       Name dfName=getDFNameA(attribute);
       Call lamdaCall=new Call();
       if(Common.isExistingDF(dfName,pdLists)) {
           Stack<String> callStack=getCallStack(attribute,lamdaCall);
           if(callStack.size()>0) {
               IExpr value = attribute.getValue();
               String attr = attribute.getAttr();
               IExpr lambdaAttribute = new Attribute();
               Lambda lambda = new Lambda();

               lambda.getArgs().add(new Arg(-1, -1, "x"));
               IExpr argAttribute = new Attribute();
               argAttribute = buildCallStackAttribute(callStack);
               if(argAttribute==null){
                   lamdaCall=null;
               }
               else {
                   lambda.setBody(argAttribute);
                   lamdaCall.getArgs().add(lambda);
               }
           }
           else{
               lamdaCall=null;
           }
       }
       return lamdaCall;
    }

    private Stack<String> getCallStack(Attribute attribute, Call lamdaCall) {
        Stack<String> callStack=new Stack<>();
        while(attribute.getValue() instanceof Attribute){
            if(!isSpecialCaseofAttr(attribute.getAttr())) {
                callStack.add(attribute.getAttr());
            }
                attribute = (Attribute) attribute.getValue();

        }
        Attribute lambdaAttribute=new Attribute();
        //get the df.COL portion and set as attribute
        lambdaAttribute.setValue(attribute);
        //set apply as final attribute
        lambdaAttribute.setAttr("apply");
        lamdaCall.setFunc(lambdaAttribute);
        return  callStack;
    }

    private boolean isSpecialCaseofAttr(String attr) {
        if(attr.equals("dt")){
            return true;
        }
        return false;
    }

    private IExpr buildCallStackAttribute(Stack<String> callStack) {
        Attribute lambdaAttribute=new Attribute();
        Name name=new Name("x",Expr_Context.Load);

//        Name name=new Name(callStack.pop(),Expr_Context.Load);
        if(callStack.size()==0){
            return name;

        }
        else {
            if (callStack.size() != 0) {
                lambdaAttribute.setValue(name);
                lambdaAttribute.setAttr(callStack.pop());
            }
            while (callStack.size() != 0) {

                Attribute attr = new Attribute();
                attr.setValue(lambdaAttribute);
                attr.setAttr(callStack.pop());
                lambdaAttribute = attr;
            }
        }
        return lambdaAttribute;

    }

    private Name getDFNameA(Attribute attribute){
            Name dfName=null;

            if(attribute.getValue()!=null && attribute.getValue() instanceof Name){
                dfName=(Name)attribute.getValue();
            }
            else if(attribute.getValue() instanceof Attribute){
                dfName= getDFNameA((Attribute) attribute.getValue());
        }
            else if(attribute.getValue() instanceof Subscript){
                Subscript dfSub=(Subscript)attribute.getValue();
                if(dfSub.getValue() instanceof Name) {
                    dfName = (Name) dfSub.getValue();
                }
            }
            return dfName;
    }

    //bhu sep 2024
    public AssignStmt getStmtPrintOverRide(){

        //TODO change to col_offset
        int lineno=0;//pdList.getLineno();
        int col_offset=0;//lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        assignStmt.getTargets().add(new Name("print", Expr_Context.Load));
        assignStmt.setRHS(new Name("pd.lazyPrint", Expr_Context.Load));
        assignStmt.setModified(true);
        return assignStmt;
    }
    public CallExprStmt getStmtPrintFlush(){

        //TODO change to col_offset
        int lineno=0;//pdList.getLineno();
        int col_offset=0;//lineno;

        CallExprStmt callExprStmt=new CallExprStmt();
        callExprStmt.setLineno(0);
        callExprStmt.setModified(true);
        callExprStmt.setCallExpr(new Name("pd.flush()", Expr_Context.Load));

        return callExprStmt;
    }


}


