package passes;
/* bhu created on 19/5/20  */


import DataFileAnalysis.DataAnalyzer;
import DataFileAnalysis.model.FileInfo;
import DataFileAnalysis.model.MetaData;
import PythonGateWay.ReadProps;
import analysis.LiveVariable.AttributeLiveVariableAnalysis;
import analysis.LiveVariable.LiveVariableAnalysis;
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
import rewrite.pd1.Pd1Elt;
import rewrite.pd2.InputFileDataTypeMapper;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Pd2Pass {
    JPBody jpBody ;
    boolean hasImport;
    boolean hasPandas;
    String pandasAlias="";
    PatchingChain<Unit> unitChain;
    String mainPath="";
    List<InputFileDataTypeMapper> ifdtmList =new ArrayList<>();

    MetaData md=null;

    public Pd2Pass(JPBody jpBody) {
        this.jpBody = jpBody;
    }
    CFG cfg ;
    JPMethod jpMethod;
    LiveVariableAnalysis lva;
    Iterator unitIterator;
    AttributeLiveVariableAnalysis alva;
    public Pd2Pass(JPMethod jpMethod, String path) {
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
        this.lva = new LiveVariableAnalysis(cfg.getUnitGraph());
        this.alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        this.unitIterator = cfg.getUnitGraph().iterator();
        this.mainPath=path;
        performPass();


    }


    public MetaData getMd() {
        return md;
    }

    public void setMd(MetaData md) {
        this.md = md;
    }

    public String getMainPath() {
        return mainPath;
    }

    public void setMainPath(String mainPath) {
        this.mainPath = mainPath;
    }

    public void performPass(){
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
                                    //CHANGE FROM PD1 start here
                                    String arg="";
                                    if(call.getArgs().get(0) instanceof Constant) {
                                        Constant argStr = (Constant) call.getArgs().get(0);
                                        arg=argStr.getValue();
                                    }
                                    if(call.getArgs().get(0) instanceof Str) {
                                        Str argStr = (Str) call.getArgs().get(0);
                                        arg=argStr.getS();
                                    }

                                    String attr=func.getAttr();
                                    //String attrName=att
                                    if(attr.equals("read_csv")){
                                    String fullPath=getFullPath(mainPath,arg.toString());
                                    //set filename, generate & store metadata if required & get metadata

                                    List atList=new ArrayList();
                                    System.out.println(name.id+" is used to create dataframe with name: "+dfName+ " at lineno: "+lineno+" and variables live are:"+ afterSet );
                                    FlowSet killed=new ArraySparseSet();
                                    afterSet.difference(beforeSet,killed);
                                    InputFileDataTypeMapper ifdtm=new InputFileDataTypeMapper();
                                        ifdtm.setLineno(lineno);
                                        ifdtm.setUnit(unit);
                                        ifdtm.setMd(getFileMetaData(fullPath));

                                    //TODO added this to verify that if no columns are used, dont add anythin.
                                    //TODO tis could also mean to remove that dataframe all together:)
                                        ifdtmList.add(ifdtm);
                                    }
                                    //modified on 25/09/20 for merge
                                    else if(attr.equals("merge")){
                                        //TODO implement merge here
                                    }
                                }
                            }
                        }
                    }
                }//instanceof AssignmentStmtSoot

            }//haspandas



        }// end unitIterator
    }//end Perform pass

    public void insertClmnTypeStmt(){
        HashMap<Unit,Unit> insertMap= new HashMap<>();
        this.unitIterator = cfg.getUnitGraph().iterator();
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            if(unit instanceof AssignmentStmtSoot) {
                for (InputFileDataTypeMapper ifdtm : ifdtmList) {
                    Unit listUnit = ifdtm.getUnit();
                    //see if list has to be inserted here
                    if (unit.equals(listUnit)) {
                        AssignStmt assignStmt=getStmtForClmnType(ifdtm.getMd());
                        //TODO::create this Map at the time of insertion in pdLists to avoid this method call altogether
                        insertMap.put(assignStmt,unit);
                        //jpMethod.getBody().getUnits().insertAfter(assignStmt,unit);
                        System.out.println("Created column list for :"+unit.toString()+"\n"+assignStmt.toString());
                    }
                }

            }//if assignment statement

        }
        for (Unit assignStmt : insertMap.keySet()) {
            Unit unit=insertMap.get(assignStmt);
            //insert in jpBody
            jpMethod.getBody().getUnits().insertBefore(assignStmt,unit);
            //update dataframe creation statement to include usecols
            updatePDCreationStmt(assignStmt,unit);
        }

    }
    public AssignStmt getStmtForClmnType(MetaData md){
        //TODO change to col_offset
        int lineno=0;//pdList.getLineno();
        int col_offset=0;//lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        assignStmt.getTargets().add(new Name("c_d_t", Expr_Context.Load));
        Dict dict=new Dict();
        dict.setCol_offset(col_offset);
        dict.setLineno(lineno);
        for(String clmn:md.getClmnList()){
            dict.getKeys().add(new Str(0,0,clmn));
        }
        int i=0;
        for(String clmn:md.getClmnList()){
           // String type=md.getClmnTypeMap().get(clmn);
            if(md.getIsCategory()[i]==true){
                dict.getValue().add(new Str(0,0,"category"));
            }
            else{
                dict.getValue().add(new Str(0,0,md.getClmnTypeMap().get(clmn)));
            }

            i++;

        }

        assignStmt.setRHS(dict);
        assignStmt.setModified(true);
        return assignStmt;
    }

    private void updatePDCreationStmt(Unit assignStmt,Unit unit){
        AssignStmt assignStmt1=(AssignStmt)assignStmt;

        Keyword keyword=new Keyword();
        keyword.setArg("dtype");
        keyword.setValue(assignStmt1.getTargets().get(0));
        AssignmentStmtSoot unitAssignSoot=(AssignmentStmtSoot)unit;
        Call call=(Call)unitAssignSoot.getAssignStmt().getRHS();
        call.getKeywords().add(keyword);
        unitAssignSoot.setModified(true);

    }
private String getFullPath(String mainPath, String filePath){
        String fullPath="";
        if(filePath.substring(0,1).equals("/") || filePath.substring(0,4).equals("http")){
            return filePath;
        }
        else{
            fullPath=mainPath+"/"+filePath;
        }
        return fullPath;
}

public MetaData getFileMetaData(String filePath){
    FileInfo fileInfo=new FileInfo(filePath);
    MetaData md=new MetaData(fileInfo);
    if(fileInfo.getExtensionType().equalsIgnoreCase("csv")) {
        if (!DataAnalyzer.isExistingFileMD(md)) {
            md = DataAnalyzer.csvMetaDataGenerator(md);
        }
        else{
            //TODO modify this
            md = DataAnalyzer.csvMetaDataGenerator(md);
        }
    }
    return md;
}
// method added on 7th Sept 2020


    public List<InputFileDataTypeMapper> getIfdtmList() {
        return ifdtmList;
    }
}
