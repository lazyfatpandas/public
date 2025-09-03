
package passes;

        import DataFileAnalysis.DataAnalyzer;
        import DataFileAnalysis.AsyncDataAnalyzer;
        import DataFileAnalysis.glob.MDforGlob;
        import DataFileAnalysis.jsonemitter.JSONEmitter;
        import DataFileAnalysis.model.FileInfo;
        import DataFileAnalysis.model.MetaData;
        import DataFileAnalysis.model.RandomSampling;
        import PythonGateWay.ReadProps;
        import analysis.LiveVariable.Alva2;
        import analysis.LiveVariable.AttributeLiveVariableAnalysis;
        import analysis.LiveVariable.LiveVariableAnalysis;
        import analysis.PythonScene;
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
        import rewrite.pd1.Pd1Elt;
        import rewrite.pd2.InputFileDataTypeMapper;
        import soot.Local;
        import soot.PatchingChain;
        import soot.SootClass;
        import soot.Unit;
        import soot.toolkits.scalar.ArraySparseSet;
        import soot.toolkits.scalar.FlowSet;

        import java.util.*;

public class DropColumnPass {

    JPBody jpBody ;
    boolean hasImport;
    boolean hasPandas;
    String pandasAlias="";
    PatchingChain<Unit> unitChain;
    List<Pd1Elt> pdLists =new ArrayList<>();
    List<InputFileDataTypeMapper> ifdtmList =new ArrayList<>();

    String mainPath="";


    /*
    Algorithm
    1. Get all dataframe created at different program points
    2. Get all statements of drop for each data frame
    3. For each dataframe, avoid loading those columns in the memory at the time of creation of those statements
    4. delete those statements
     */

    public DropColumnPass(JPBody jpBody) {
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
    public DropColumnPass(JPMethod jpMethod, String path) {
        this.prePass=new PrePass(jpMethod);
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
        System.out.println(this.cfg.getUnitGraph().toString());
        this.lva = new LiveVariableAnalysis(cfg.getUnitGraph());
        //this.alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        this.alva=new IPLiveAttribute(cfg.getUnitGraph());
        this.unitIterator = cfg.getUnitGraph().iterator();
        this.alva2=new Alva2(cfg.getUnitGraph());
        pandasUnits=new ArrayList<Unit>();
        this.mainPath=path;
        //2023 update
        this.hasPandas=true;
        this.pandasAlias="pd";
        performPass();
        updateUsedColumns();
    }
    //THIS FOR IPC LIVE ATTRIBUTE OF PYTHON PROGRAM WITH SINGLE CLASS
    public DropColumnPass(JPMethod jpMethod, String path, List <SootClass> sootClasses) {
        this.prePass=new PrePass(jpMethod);
        this.sootClasses=sootClasses;
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
//      Chiranmoy: temporarily disabling disabling print
//      System.out.println(this.cfg.getUnitGraph().toString());
        //17 mar 24 commented
        //this.lva = new LiveVariableAnalysis(cfg.getUnitGraph());
        //this.alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        this.alva=new IPLiveAttribute(cfg.getUnitGraph(),sootClasses);
        this.unitIterator = cfg.getUnitGraph().iterator();
        //17 mar 24 commented
        //this.alva2=new Alva2(cfg.getUnitGraph());
        pandasUnits=new ArrayList<Unit>();
        this.mainPath=path;
        //2023 update
        this.hasPandas=true;
        this.pandasAlias="pd";
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
                                    if(call.getArgs().size()!=0 && call.getArgs().get(0) instanceof Str) {
                                        Str argStr = (Str) call.getArgs().get(0);
                                        arg=argStr.getS();
                                    }
                                    //python3.9
                                    if(call.getArgs().size()!=0 && call.getArgs().get(0) instanceof Constant) {
                                        Constant argConstant = (Constant) call.getArgs().get(0);
                                        arg=argConstant.getValue();
                                    }
                                    //glob
                                    if(call.getArgs().size()!=0 && call.getArgs().get(0) instanceof ListCompCmplx) {
                                        ListCompCmplx argListCompCmplx = (ListCompCmplx) call.getArgs().get(0);
//                                        arg=argListCompCmplx.getValue();
                                    }

                                    Pd1Elt elt=new Pd1Elt();
                                    String attr=func.getAttr();
                                    String fullPath="";
                                    //String attrName=att
                                    if(attr.equals("read_csv")) {
                                        InputFileDataTypeMapper ifdtm = new InputFileDataTypeMapper();
                                        if(call.getArgs().get(0) instanceof Name) {
                                            Name pathVariable = (Name) call.getArgs().get(0);
                                            // for (Local local : this.jpMethod.getBody().getLocals()) {
                                            //if (local.equals(pathVariable)) {
//                                                    fullPath = local.toString();
//                                                    List useb = local.getUseBoxes();
//                                                    System.out.println(useb);
                                            //TODO may change if mroe than 1 variable
                                            Unit unitWithLocal = PythonScene.varUnitMap.get(pathVariable.toString()).get(0);
                                            AssignmentStmtSoot assignmentStmtSoot1 = (AssignmentStmtSoot) unitWithLocal;
                                            //TODO other possibilities not checked
                                            IExpr iexpr = assignmentStmtSoot1.getAssignStmt().getRHS();
                                            if (iexpr instanceof Call) {
                                                Call callExpr = (Call) iexpr;
                                                String funcGlob = callExpr.getFunc().toString();
                                                if (funcGlob.equals("glob")) {
                                                    IExpr patternExpr = callExpr.getArgs().get(0);
                                                    if (patternExpr instanceof Name) {
                                                        Unit unitForGlobWithPattern = PythonScene.varUnitMap.get(patternExpr.toString()).get(0);
                                                        AssignmentStmtSoot assignmentStmtSoot2 = (AssignmentStmtSoot) unitForGlobWithPattern;
                                                        IExpr iexprglobRhs = assignmentStmtSoot2.getAssignStmt().getRHS();
                                                        if(iexprglobRhs instanceof Call && ((Call) iexprglobRhs).getArgs().size()==2) {
                                                            Call globEnvironCall=(Call)iexprglobRhs;
                                                            if(globEnvironCall.getArgs().get(1) instanceof Constant){
                                                                fullPath=((Constant)(globEnvironCall.getArgs().get(1))).getValue();
                                                            }else {
                                                                fullPath = globEnvironCall.getArgs().get(1).toString();
                                                            }
                                                            fullPath=MDforGlob.getFilePathGlob(unit,ifdtm,fullPath);

                                                        }


                                                    }

                                                }

                                            } else if (iexpr instanceof Str) {
                                                fullPath=MDforGlob.getFilePathGlob(unit,ifdtm,iexpr.toString());
                                            }


                                            // }//if local

                                            //the path is not directly available
                                            boolean isGlobData = MDforGlob.buildMDforGlob(unit, ifdtm, fullPath);

                                            // } for local
                                        }else {
                                            fullPath = getFullPath(mainPath, arg);
                                            //set filename, generate & store metadata if required & get metadata

                                        }
                                        ifdtm.setMd(getFileMetaData(fullPath));
                                        ifdtm.setLineno(lineno);
                                        ifdtm.setUnit(unit);
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

                                     //concat --requirement not clear, therefore, changed to concat12
                                    if(attr.equals("concat12")) {
                                        InputFileDataTypeMapper ifdtm = new InputFileDataTypeMapper();
                                        if(call.getArgs().get(0) instanceof Name) {
                                            Name pathVariable = (Name) call.getArgs().get(0);
                                            // for (Local local : this.jpMethod.getBody().getLocals()) {
                                            //if (local.equals(pathVariable)) {
//                                                    fullPath = local.toString();
//                                                    List useb = local.getUseBoxes();
//                                                    System.out.println(useb);
                                            //TODO may change if mroe than 1 variable
                                            Unit unitWithLocal = PythonScene.varUnitMap.get(pathVariable.toString()).get(0);
                                            AssignmentStmtSoot assignmentStmtSoot1 = (AssignmentStmtSoot) unitWithLocal;
                                            //TODO other possibilities not checked
                                            IExpr iexpr = assignmentStmtSoot1.getAssignStmt().getRHS();
                                            if (iexpr instanceof Call) {
                                                Call callExpr = (Call) iexpr;
                                                String funcGlob = callExpr.getFunc().toString();
                                                if (funcGlob.equals("glob")) {
                                                    IExpr patternExpr = callExpr.getArgs().get(0);
                                                    if (patternExpr instanceof Name) {
                                                        Unit unitForGlobWithPattern = PythonScene.varUnitMap.get(patternExpr.toString()).get(0);
                                                        AssignmentStmtSoot assignmentStmtSoot2 = (AssignmentStmtSoot) unitForGlobWithPattern;
                                                        IExpr iexprglobRhs = assignmentStmtSoot2.getAssignStmt().getRHS();
                                                        if(iexprglobRhs instanceof Call && ((Call) iexprglobRhs).getArgs().size()==2) {
                                                            Call globEnvironCall=(Call)iexprglobRhs;
                                                            fullPath=globEnvironCall.getArgs().get(1).toString();
                                                            fullPath=MDforGlob.getFilePathGlob(unit,ifdtm,fullPath);

                                                        }


                                                    }

                                                }

                                            } else if (iexpr instanceof Str) {
                                                fullPath=MDforGlob.getFilePathGlob(unit,ifdtm,iexpr.toString());
                                            }


                                            // }//if local

                                            //the path is not directly available
                                            boolean isGlobData = MDforGlob.buildMDforGlob(unit, ifdtm, fullPath);

                                            // } for local
                                        }else {
                                            fullPath = getFullPath(mainPath, arg);
                                            //set filename, generate & store metadata if required & get metadata

                                        }
                                        ifdtm.setMd(getFileMetaData(fullPath));
                                        ifdtm.setLineno(lineno);
                                        ifdtm.setUnit(unit);
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
                                    }//attr.equals("concat")








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
                        pdList.setUsedColListStmt(assignStmt);
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
        assignStmt.getTargets().add(new Name("SO_columns", Expr_Context.Load));
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
//            Chiranmoy: temporarily disabling disabling print
//            System.out.println("Unit: " + unit+"\n");

//            System.out.println("\t att live before Before: " + set);

            set = (FlowSet) alva.getFlowAfter(unit);
//            System.out.println("\t att live after: " + set);
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
                            //New for dateTypes
                            //order important as date is changed to str
                            //TODO date column should be removed completely
                            AssignStmt listCompUsedColumnStmt=getListCOmpUsedColStmt(unit);
                            AssignStmt dateAssignStmt = getStmtForDateType(ifdtm.getMd(),listCompUsedColumnStmt);
                            AssignStmt assignStmt = getStmtForClmnType(ifdtm.getMd(), listCompUsedColumnStmt);

                            //TODO::create this Map at the time of insertion in pdLists to avoid this method call altogether
                            //2024 changes for metadata category

//                            insertMap.put(assignStmt, unit);
                            if(assignStmt!=null) {
                                insertMap.put(assignStmt, unit);
                            }

                            if(dateAssignStmt!=null) {
                                insertMap.put(dateAssignStmt, unit);
                            }
                            //jpMethod.getBody().getUnits().insertAfter(assignStmt,unit);
                           System.out.println("Created column list for :" + unit.toString() + "\n" + assignStmt.toString());
                        }
                    }//ifdtm md!=null
                }

            }//if assignment statement

        }
        for (Unit assignStmt : insertMap.keySet()) {
            Unit unit=insertMap.get(assignStmt);
            //insert in jpBody the statement containg column types for each column
            jpMethod.getBody().getUnits().insertBefore(assignStmt,unit);
            //update dataframe creation statement to include dtypes
            assert (assignStmt instanceof AssignStmt);
            if(((AssignStmt)assignStmt).getRHS() instanceof Dict ){
                updatePDCreationStmt(assignStmt,unit,"dtype");
            }
            else if(((AssignStmt)assignStmt).getRHS() instanceof ListComp ){
                updatePDCreationStmt(assignStmt,unit,"parse_dates");
            }

        }

    }
    public AssignStmt getStmtForClmnType(MetaData md, AssignStmt listCompUsedColStmt){
        List<String> usedCols=getUsedColumns(listCompUsedColStmt);

        //TODO change to col_offset
        int lineno=0;//pdList.getLineno();
        int col_offset=0;//lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        assignStmt.getTargets().add(new Name("SO_c_d_t", Expr_Context.Load));
        Dict dict=new Dict();
        dict.setCol_offset(col_offset);
        dict.setLineno(lineno);
        for(String clmn:md.getClmnList()){
            if(usedCols.contains(clmn)) {
                dict.getKeys().add(new Str(0, 0, clmn));
            }
            //Special case, all cols are used
            else if(usedCols.size()==0){
                dict.getKeys().add(new Str(0, 0, clmn));
            }
        }
        int i=0;
        //2024 only for category, enforce metadata
        boolean catFlag=false;
//        for(boolean isCat:md.getIsCategory()){
//            if(isCat){
//                catFlag=true;
//            }
//        }

        for(String clmn:md.getClmnList()) {
            // String type=md.getClmnTypeMap().get(clmn);
            //if(md.getIsCategory()[i]==true){
            //Updated this so that if is is not string type, then it shdn't be taken as category
            if(usedCols.contains(clmn) || usedCols.size()==0) {
            if (md.getIsCategory()[i] == true && md.getClmnTypeMap().get(clmn).equals("str")) {
                dict.getValue().add(new Str(0, 0, "category"));
            }
//            else if (md.getIsCategory()[i] == true && md.getClmnTypeMap().get(clmn).equals("int64")) {
//                dict.getValue().add(new Str(0, 0, "category"));
//              }
            //This is added to remove date type from columns types.. if it doent work, remove this else if part only
            //Added  or portion to remove col type defs for those columns not used in the program
            else if (md.getClmnTypeMap().get(clmn).equals("date")) {
                dict.removeKey(clmn);

            } else {
                dict.getValue().add(new Str(0, 0, md.getClmnTypeMap().get(clmn)));
            }
        }

            i++;

        }
        //2024 only for category, enforce metadata, now assigment is conditional for Dict


        for(IExpr value:dict.getValue()){
            if (value instanceof Str) {
                Str str = (Str) value;
                if(str.getS().equals("category")){
                    catFlag=true;
                }
            }
        }
        assignStmt.setRHS(dict);
//        if(catFlag) {
//            assignStmt.setRHS(new Dict());
//
//        }
//        else{
//            return null;
//        }
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
        //remove already existing args for read_csv
        call.getKeywords().removeIf(kw -> (kw.getArg().equals(pyArgName)));

//        for(IExpr keywordExpr: call.getKeywords()){
//            if(keywordExpr instanceof Keyword){
//                Keyword kw=(Keyword) keywordExpr;
//                if(kw.getArg().equals(pyArgName)){
//                    call.getKeywords().remove(kw);
//                }
//            }

//        }
        call.getKeywords().add(keyword);

        unitAssignSoot.setModified(true);

    }
    private String getFullPath(String mainPath, String filePath){
        //assert(filePath!=null && filePath!="");
        String fullPath="";
        if(filePath.equals("")){
            return null;
        }
        if(filePath.substring(0,1).equals("/") || filePath.substring(0,4).equals("http")){
            return filePath;
        }
        else{
            fullPath=mainPath+"/"+filePath;
        }
        return fullPath;
    }

    public MetaData getFileMetaData(String filePath){
        MetaData md = null;
        if(filePath!=null) {
            FileInfo fileInfo = new FileInfo(filePath);
            md = new MetaData(fileInfo);
            if (fileInfo.getExtensionType().equalsIgnoreCase("csv")) {
                if (!DataAnalyzer.isExistingFileMD(md)) {
                    //3.01 vldb
                    System.out.println("New metadata so finding, Filename is:"+md.getFileInfo().getName());
                    AsyncDataAnalyzer asyncDataAnalyzer=new AsyncDataAnalyzer(md);
                    md = DataAnalyzer.csvMetaDataGenerator(md);
                    asyncDataAnalyzer.generateMetaDataAsync();

                }
                //Commented on 13/06/2021 to use existing metadata
            /*
            else{
                //TODO modify this
                md = DataAnalyzer.csvMetaDataGenerator(md);
            }

             */
            }
        }
        return md;
    }
    //PD2End

    //31/01/21 for dateType
    public AssignStmt getStmtForDateType(MetaData md, AssignStmt listCompUsedColStmt){
        List<String> usedCols=getUsedColumns(listCompUsedColStmt);

        //TODO change to col_offset
        int lineno=0;//pdList.getLineno();
        int col_offset=0;//lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        assignStmt.getTargets().add(new Name("SO_d_d_t", Expr_Context.Load));


        ListComp listComp=new ListComp();
        listComp.setCol_offset(col_offset);
        listComp.setLineno(lineno);
        for(String clmn:md.getClmnList()){
            // String type=md.getClmnTypeMap().get(clmn);
            //if(md.getIsCategory()[i]==true){
            //Updated this so that if is is not string type, then it shdn't be taken as category
            if( md.getClmnTypeMap().get(clmn).equals("date") && usedCols.contains(clmn)){
                listComp.getElts().add(new Str(lineno, col_offset, clmn));
                md.getClmnTypeMap().put(clmn,"str");
                // 2024 date columns
//                md.getParseDates().add(clmn);
            }

        }
        assignStmt.setRHS(listComp);
        assignStmt.setModified(true);
        if (listComp.getElts().size()==0){
            return null;
        }
        return assignStmt;
    }
    public AssignStmt getListCOmpUsedColStmt(Unit unit){
     for (Pd1Elt pdList : pdLists) {
        Unit listUnit = pdList.getUnit();
        //see if list has to be inserted here
        if (unit.equals(listUnit)) {
            return pdList.getUsedColListStmt();
        }
    }
     return null;
    }

    public List<String> getUsedColumns(AssignStmt listCompUsedColStmt){
        ListComp listCompUsedCol;
        List<String> usedCols=new ArrayList<>();
        listCompUsedCol = (ListComp)(listCompUsedColStmt.getRHS());
        for(IExpr elt:listCompUsedCol.getElts()){
            if(elt instanceof Str){
                usedCols.add(((Str)elt).getS());
            }
        }
        return usedCols;

    }


    public void metaJSONGenerator(){
        JSONEmitter jsonEmitter=new JSONEmitter();
        for (Pd1Elt pdList : pdLists) {
            MetaData md=pdList.getIfdtm().getMd();
            for(String usedCol:pdList.getCols()){
                md.getUsedCols().add(usedCol);
            }
            for(String clmn:md.getClmnList()){
                // String type=md.getClmnTypeMap().get(clmn);
                //if(md.getIsCategory()[i]==true){
                if( md.getClmnTypeMap().get(clmn).equals("date") && pdList.getCols().contains(clmn)){
                    // 2024 date columns
                    md.getParseDates().add(clmn);
                }

            }
            jsonEmitter.createJSONFromMD(pdList.getIfdtm().getMd());
        }
    }


}
