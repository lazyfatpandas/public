package passes;

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
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Pd1Pass {

    JPBody jpBody ;
    boolean hasImport;
    boolean hasPandas;
    String pandasAlias="";
    PatchingChain<Unit> unitChain;
    List<Pd1Elt> pdLists =new ArrayList<>();



    public Pd1Pass(JPBody jpBody) {
        this.jpBody = jpBody;
    }

    CFG cfg ;
    JPMethod jpMethod;
    LiveVariableAnalysis lva;
    Iterator unitIterator;
    AttributeLiveVariableAnalysis alva;

    public Pd1Pass(JPMethod jpMethod) {
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
        System.out.println(this.cfg.getUnitGraph().toString());
        this.lva = new LiveVariableAnalysis(cfg.getUnitGraph());
        this.alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        this.unitIterator = cfg.getUnitGraph().iterator();
        performPass();

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
            //Test
            System.out.println("Unit: " +unit.toString() +"Before Set:"+beforeSet.toString());
            //Test End
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

                                        //String attrName=att
                                        List atList=new ArrayList();
                                        System.out.println(name.id+" is used to create dataframe with name: "+dfName+ " at lineno: "+lineno+" and variables live are:"+ afterSet );
                                        FlowSet killed=new ArraySparseSet();
                                        afterSet.difference(beforeSet,killed);
                                        Pd1Elt elt=new Pd1Elt();

                                        for (Iterator<Local> itr=killed.toList().iterator(); itr.hasNext(); ){
                                            //for(Local l:outIt){
                                            Local l=itr.next();
                                            String localName=l.getName();
                                            //TODO delete this code as it is not required anymore
                                            //dead code not working?
                                            String names[]=localName.split("_");
                                            if(names[0].equals(dfName) && names.length==2){
                                                atList.add(names[1]);
                                                //atList.add(names[0]);
                                                elt.getCols().add(names[1]);
                                            }
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
                                        System.out.println("Columns used from file are:"+atList );
                                        elt.setLineno(lineno);
                                        elt.setUnit(unit);
                                        elt.setCols(atList);
                                        //TODO added this to verify that if no columns are used, dont add anythin.
                                        //TODO tis could also mean to remove that dataframe all together:)
                                        if(atList.size()!=0) {
                                            pdLists.add(elt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(unit instanceof CallExprStmt){
                        //System.out.println("CallExprStmt statement:"+ unit);
                        //CallExprStmt callExprStmt=(CallExprStmt)unit;
                    }

                }



        }// end unitIterator









    /*    for(Unit unit:unitChain){
            if(unit instanceof ImportStmt){
                hasImport=true;
                ImportStmt importStmt=(ImportStmt)unit;
                if(importStmt.getNames().getName().equals("pandas")) {
                    hasPandas = true;
                    pandasAlias = importStmt.getNames().getAsname();
                    }
            }
            if(hasPandas){

                if(unit instanceof AssignmentStmtSoot){
                    AssignmentStmtSoot assignmentStmtSoot=(AssignmentStmtSoot)unit;
                    AssignStmt assignStmt=assignmentStmtSoot.getAssignStmt();
                    IExpr rhs=assignStmt.getRHS();
                    List<IExpr> target=assignStmt.getTargets();
                    if(rhs instanceof Name){
                        Name name=(Name)rhs;
                    }
                    else if(rhs instanceof Call){
                        Call call=(Call)rhs;
                    }

                }
                if(unit instanceof CallExprStmt){
                    CallExprStmt callExprStmt=(CallExprStmt)unit;
                }

            }

        }
*/

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
            jpMethod.getBody().getUnits().insertBefore(assignStmt,unit);
            //update dataframe creation statement to include usecols
            updatePDCreationStmt(assignStmt,unit);
        }

    }
    public AssignStmt getListStmt(Pd1Elt pdList){
        //TODO change to col_offset
        int lineno=pdList.getLineno();
        int col_offset=lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        assignStmt.getTargets().add(new Name("columns", Expr_Context.Load));
        ListComp listComp=new ListComp();
        listComp.setCol_offset(col_offset);
        listComp.setLineno(lineno);
        for(String colname:pdList.getCols()) {
            listComp.getElts().add(new Str(lineno,col_offset,colname));
        }
        assignStmt.setRHS(listComp);
        assignStmt.setModified(true);
        return assignStmt;
    }

    private void updatePDCreationStmt(Unit assignStmt,Unit unit){
        AssignStmt assignStmt1=(AssignStmt)assignStmt;

        Keyword keyword=new Keyword();
        keyword.setArg("usecols");
        keyword.setValue(assignStmt1.getTargets().get(0));
        AssignmentStmtSoot unitAssignSoot=(AssignmentStmtSoot)unit;
        Call call=(Call)unitAssignSoot.getAssignStmt().getRHS();
        call.getKeywords().add(keyword);
        unitAssignSoot.setModified(true);

    }

}
