package passes;

import DataFileAnalysis.DataAnalyzer;
import DataFileAnalysis.model.FileInfo;
import DataFileAnalysis.model.MetaData;
import DataFileAnalysis.model.RandomSampling;
import PythonGateWay.ReadProps;
import analysis.LiveVariable.Alva2;
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

import java.util.*;

public class DropColumnPassInteprocedural {
    JPBody jpBody ;
    boolean hasImport;
    boolean hasPandas;
    String pandasAlias="";
    PatchingChain<Unit> unitChain;
    List<Pd1Elt> pdLists =new ArrayList<>();
    List<InputFileDataTypeMapper> ifdtmList =new ArrayList<>();

    String mainPath="";


    public DropColumnPassInteprocedural(JPBody jpBody) {
        this.jpBody = jpBody;
    }

    CFG cfg ;
    JPMethod jpMethod;
    LiveVariableAnalysis lva;
    Iterator unitIterator;
    AttributeLiveVariableAnalysis alva;
    Alva2 alva2;
    List<Unit> pandasUnits;
    public DropColumnPassInteprocedural(JPMethod jpMethod, String path) {
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
        System.out.println(this.cfg.getUnitGraph().toString());
        this.lva = new LiveVariableAnalysis(cfg.getUnitGraph());
        this.alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        this.unitIterator = cfg.getUnitGraph().iterator();
        this.alva2=new Alva2(cfg.getUnitGraph());
        pandasUnits=new ArrayList<Unit>();
        this.mainPath=path;
        performPass();
        updateUsedColumns();
    }

    public List<Pd1Elt> getPdLists() {
        return pdLists;
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

                                    //PD2PassADDStart
                                    String arg="";
                                    if(call.getArgs().get(0) instanceof Str) {
                                        Str argStr = (Str) call.getArgs().get(0);
                                        arg=argStr.getS();
                                    }
                                    Pd1Elt elt=new Pd1Elt();
                                    String attr=func.getAttr();
                                    //String attrName=att
                                    if(attr.equals("read_csv")) {
                                        String fullPath = getFullPath(mainPath, arg.toString());
                                        //set filename, generate & store metadata if required & get metadata
                                        InputFileDataTypeMapper ifdtm = new InputFileDataTypeMapper();
                                        ifdtm.setLineno(lineno);
                                        ifdtm.setUnit(unit);
                                        ifdtm.setMd(getFileMetaData(fullPath));
                                        elt.setIfdtm(ifdtm);

                                        //PD2PASSADDStop

                                        //String attrName=att
                                        List atList=new ArrayList();
                                        System.out.println(name.id+" is used to create dataframe with name: "+dfName+ " at lineno: "+lineno+" and variables live are:"+ afterSet );
                                        FlowSet killed=new ArraySparseSet();
                                        afterSet.difference(beforeSet,killed);

                                        for (Iterator<Local> itr = killed.toList().iterator(); itr.hasNext(); ){
                                            //for(Local l:outIt){
                                            Local l=itr.next();
                                            String localName=l.getName();
//
                                            //TODO verify this code::::Verified for working...
                                            //Newly added
                                            if(l instanceof  Name){
                                                Name nam=(Name)l;
                                                //HERE checking if it is a pandas dataframe attribute for this dataframe
                                                if(nam.getParent()!=null && nam.getParent().equals(dfName)){
                                                    atList.add(nam.getName());
                                                    //System.exit(1);
                                                }
                                            }
                                        }
                                        //System.out.println("Columns used from file are:"+atList );
                                        elt.setLineno(lineno);
                                        elt.setUnit(unit);
                                        elt.setCols(atList);
                                        assert(dfName!="");
                                        elt.setDfName(dfName);
                                        ifdtmList.add(ifdtm);

                                        //TODO added this to verify that if no columns are used, dont add anythin.
                                        //TODO tis could also mean to remove that dataframe all together:)
                                        //TODO Update:this also used to removes drop columns, so used even if no column used..
                                        pdLists.add(elt);
                                        //code before update
//                                    if(atList.size()!=0) {
//                                        pdLists.add(elt);
//                                    }
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
                FlowSet set = (FlowSet) alva.getFlowBefore(unit);
                //retainUsedDropColumns(set, pdLists);



            }



        }// end unitIterator
    }//end Perform pass

    public void insertLists(){
        HashMap<Unit,Unit> insertMap= new HashMap<>();
        this.unitIterator = cfg.getUnitGraph().iterator();
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            if(unit instanceof AssignmentStmtSoot) {
                for (Pd1Elt pdList : pdLists) {
                    Unit listUnit = pdList.getUnit();
                    //see if list has to be inserted here
                    if (unit.equals(listUnit)) {
                        AssignStmt assignStmt=getListStmt(pdList);
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
            //To verify that it is not an empty list stmt..if it is empty, don't insert
            assert (assignStmt instanceof AssignStmt);
            ListComp rhs=(ListComp) ((AssignStmt)assignStmt).getRHS();
            int size=rhs.getElts().size();
            if(size>0) {
                jpMethod.getBody().getUnits().insertBefore(assignStmt, unit);
                //update dataframe creation statement to include usecols
                updatePDCreationStmt(assignStmt, unit, "usecols");
            }
        }

    }
    public AssignStmt getListStmt(Pd1Elt pdList){
        //TODO change to col_offset
        int lineno=pdList.getLineno();
        int col_offset=lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        assignStmt.getTargets().add(new Name("columns", Expr_Context.Load));
        ListComp listComp=getListCompforDFCreation(pdList);
        /*ListComp listComp=new ListComp();
        listComp.setCol_offset(col_offset);
        listComp.setLineno(lineno);
        for(String colname:pdList.getCols()) {
            listComp.getElts().add(new Str(lineno,col_offset,colname));
        }*/
        assignStmt.setRHS(listComp);
        assignStmt.setModified(true);
        return assignStmt;
    }

    public ListComp getListCompforDFCreation(Pd1Elt pdList){
        //TODO change to col_offset
        //TODO if one drop stmt contains multiple column name, check that and remove only that column that has been dropped
        int lineno=pdList.getLineno();
        int col_offset=lineno;
        ListComp listComp=new ListComp();
        listComp.setCol_offset(col_offset);
        listComp.setLineno(lineno);
        if(pdList.getCols().size()!=0) {
            for (String colname : pdList.getCols()) {
                listComp.getElts().add(new Str(lineno, col_offset, colname));
            }
        }
        else if(pdList.getCols().size()==0 && pdList.getDropCols().size()!=0){
            for (String colname : pdList.getIfdtm().getMd().getClmnList()) {
                if(!pdList.getDropCols().contains(colname)) {
                    listComp.getElts().add(new Str(lineno, col_offset, colname));
                }

            }
        }
        //Drop drop column statements here
        for(String colname: pdList.getDropCols()){
            //as this column is dropped and is not read, therefore delete that statement of drop
            Unit unit=(Unit)(pdList.getDropColUnitMap().get(colname));
            assert(unit instanceof CallExprStmt);
            CallExprStmt dropUnit=(CallExprStmt)unit;
            dropUnit.setLineno(-10);

        }
        return listComp;
    }



    public void updateUsedColumns(){
        //Call check condition
        //if condition fails, remove that column from drop column list
        this.unitIterator = cfg.getUnitGraph().iterator();
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            FlowSet set = (FlowSet) alva.getFlowBefore(unit);
            System.out.println("Unit: " + unit+"\n");

            System.out.println("\t att live before Before: " + set);

            set = (FlowSet) alva.getFlowAfter(unit);
            System.out.println("\t att live after: " + set);
            retainUsedDropColumns(set, pdLists);
//            for(Local local:(FlowSet)set.){
//
//            }
        }
        // System.out.println("Dropped Columns are:"+pdLists.get(0).getDropCols());


    }
    public void  retainUsedDropColumns(FlowSet liveBefore, List<Pd1Elt> pdLists){
        Map<Pd1Elt,List<String>> columnstoBeRetained=new HashMap<>();
        //add live columns that are dropped to a list
        for(Pd1Elt pd1Elt: pdLists){
            columnstoBeRetained.put(pd1Elt, new ArrayList<>());
            for(String dropColumn: pd1Elt.getDropCols()){
                Iterator liveBeforeIterator=liveBefore.iterator();
                while (liveBeforeIterator.hasNext()){
                    IExpr var=(IExpr) liveBeforeIterator.next();
                    String varStr=var.toString();
                    if(varStr.equals(dropColumn)){
                        List<String> clmnsRetainedforthisPD=columnstoBeRetained.get(pd1Elt);
                        clmnsRetainedforthisPD.add(dropColumn);
                    }
                }

            }
        }
        //remove live clmns pd wise
        for(Pd1Elt pd1Elt: pdLists) {
            List<String> clmnsRetainedforthisPD=columnstoBeRetained.get(pd1Elt);
            for(String clmn:clmnsRetainedforthisPD){
                pd1Elt.getDropCols().remove(clmn);
            }

        }


    }

    //PD2Start
    public void insertClmnTypeStmt(){
        HashMap<Unit,Unit> insertMap= new HashMap<>();
        this.unitIterator = cfg.getUnitGraph().iterator();
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            if(unit instanceof AssignmentStmtSoot) {
                for (InputFileDataTypeMapper ifdtm : ifdtmList) {
                    if(ifdtm.getMd()!=null) {
                        Unit listUnit = ifdtm.getUnit();
                        //see if list has to be inserted here
                        if (unit.equals(listUnit)) {
                            AssignStmt assignStmt = getStmtForClmnType(ifdtm.getMd());
                            //TODO::create this Map at the time of insertion in pdLists to avoid this method call altogether
                            insertMap.put(assignStmt, unit);
                            //jpMethod.getBody().getUnits().insertAfter(assignStmt,unit);
                            System.out.println("Created column list for :" + unit.toString() + "\n" + assignStmt.toString());
                        }
                    }//ifdtm md!=null
                }

            }//if assignment statement

        }
        for (Unit assignStmt : insertMap.keySet()) {
            Unit unit=insertMap.get(assignStmt);
            //insert in jpBody
            jpMethod.getBody().getUnits().insertBefore(assignStmt,unit);
            //update dataframe creation statement to include usecols
            updatePDCreationStmt(assignStmt,unit,"dtype");
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

    private void updatePDCreationStmt(Unit assignStmt,Unit unit, String pyArgName){
        AssignStmt assignStmt1=(AssignStmt)assignStmt;

        Keyword keyword=new Keyword();
        keyword.setArg(pyArgName);
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
                //3.01 vldb
                //md = DataAnalyzer.csvMetaDataGenerator(md);
                md = RandomSampling.csvRandomMetaDataGenerator(md);

            }
            else{
                //TODO modify this
                //md = DataAnalyzer.csvMetaDataGenerator(md);
                //3.01 vldb
                //md = DataAnalyzer.csvMetaDataGenerator(md);
                md = RandomSampling.csvRandomMetaDataGenerator(md);
            }
        }
        return md;
    }
    //PD2End

}

