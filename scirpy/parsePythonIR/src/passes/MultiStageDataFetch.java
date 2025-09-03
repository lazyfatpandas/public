package passes;

import PythonGateWay.ReadProps;
import analysis.LiveVariable.AttributeLiveVariableAnalysis;
import analysis.LiveVariable.AttributeLiveVariableAnalysisVariant;
import analysis.LiveVariable.LiveVariableAnalysis;
import cfg.CFG;
import ir.IExpr;
import ir.JPBody;
import ir.JPMethod;
import ir.Stmt.*;
import ir.expr.*;
import ir.internalast.Keyword;
import ir.internalast.Targets;
import rewrite.pd1.Pd1Elt;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class MultiStageDataFetch{

    JPBody jpBody ;
    /*HashMap<String,Unit> firstUsed = new HashMap();
    HashMap<String,Unit> fetch = new HashMap();
    HashMap<String,Unit>  kill = new HashMap();*/
    HashMap<Unit,FlowSet> firstUsed1 = new HashMap();

    HashMap<Unit,FlowSet> removeColumns = new HashMap();
    HashMap<Unit,FlowSet> usedColumns = new HashMap();
    HashMap<Unit,LinkedHashSet<Name>> neededColumns = new HashMap();
    List<String> allUsedColumns= new ArrayList<String>();


    FlowSet dataframe = new ArraySparseSet();
    boolean hasImport;
    boolean hasPandas;
    String pandasAlias="";
    CFG cfg ;
    JPMethod jpMethod;
    Iterator unitIterator;
    //Iterator unitIterator1;
    AttributeLiveVariableAnalysis alva;
    AttributeLiveVariableAnalysisVariant alvav;

    /*public MultiStageDataFetch(JPBody jpBody) {
        this.jpBody = jpBody;
    }*/


    public MultiStageDataFetch(JPMethod jpMethod) {
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
        this.unitIterator = cfg.getUnitGraph().iterator();
        //this.unitIterator1 = cfg.getUnitGraph().iterator();
        this.alva=new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        //buildDict();
        this.alvav = new AttributeLiveVariableAnalysisVariant(cfg.getUnitGraph());

        boolean check = checkApplicability();
        if(check == true)
            Analysis();


    }



    public boolean checkApplicability(){

        //Unit prevUnit=null,temp=null;
        //FlowSet beforeSet,afterSet;
        //generate flowset for each unit
        while (unitIterator.hasNext()) {

            Unit unit = (Unit) unitIterator.next();
            //b0eforeSet = (FlowSet) alva.getFlowBefore(unit);
            //afterSet = (FlowSet) alva.getFlowAfter(unit);
            FlowSet gen = new ArraySparseSet();
            //System.out.println('1');
            for (ValueBox use: unit.getUseBoxes()) {
                if (use.getValue() instanceof Local) {
                    gen.add((Local) use.getValue());
                    if (dataframe.contains(use.getValue().toString())) {
                        System.out.println("Multistage Data Fetching can not be performed :" + (Local) use.getValue() + " is used in program");
                        return false;
                    }
                }
            }

           /* if (gen.isEmpty()==false)
            {
                firstUsed1.put(prevUnit,gen);
            }
            //System.out.println(gen);
            for (Iterator<Local> itr=gen.toList().iterator(); itr.hasNext(); ) {
                //for(Local l:outIt){
                Local l = itr.next();
                String localName = l.getName();

                if (firstUsed.get(localName)== null){
                    // we are storing the statement after which column is first used in program i.e. previous statement to the statement in which column is first being used
                    firstUsed.put(localName,prevUnit);
                }
                //lastUsed.put(localName,unit);

            }
            temp=(Unit) unitIterator1.next();
            // to avoid null entries in firstUsed map
            if (temp.toString()!=null)
                prevUnit = temp;

            //System.out.println(unit);

        */

            if(unit instanceof ImportStmt) {
                hasImport = true;
                ImportStmt importStmt = (ImportStmt) unit;
                if (importStmt.getNames().getName().equals("pandas") || importStmt.getNames().getName().equals(ReadProps.read("ScapaName"))) {
                    hasPandas = true;
                    pandasAlias = importStmt.getNames().getAsname();
                    //System.out.println("Pandas alias:"+ pandasAlias);
                }

            }
            if(hasPandas) {

                if (unit instanceof AssignmentStmtSoot) {
                    //System.out.println("Assignment statement:"+ unit);

                    AssignmentStmtSoot assignmentStmtSoot = (AssignmentStmtSoot) unit;
                    AssignStmt assignStmt = assignmentStmtSoot.getAssignStmt();
                    IExpr rhs = assignStmt.getRHS();
                    List<IExpr> target = assignStmt.getTargets();
                    if (rhs instanceof Name) {
                        Name name = (Name) rhs;
                    } else if (rhs instanceof Call) {
                        Call call = (Call) rhs;
                        if (call.getFunc() instanceof Attribute) {
                            Attribute func = (Attribute) call.getFunc();
                            //System.out.println(func.getAttr());
                            while (func.getValue() instanceof Attribute) {
                                //System.out.println(func.getAttr());
                                func = (Attribute) func.getValue();

                            }
                            if (func.getValue() instanceof Name) {

                                Name name = (Name) func.getValue();

                                if (name.id.equals(pandasAlias)) {
                                    String dfName = "";

                                    if (target.get(0) instanceof Targets) {
                                        dfName = ((Targets) target.get(0)).getName();
                                        dataframe.add((dfName));

                                    }
                                    if (target.get(0) instanceof Name) {
                                        dfName = ((Name) target.get(0)).getName();
                                        dataframe.add((dfName));
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }// end unitIterator
        //System.out.println(firstUsed);
        return true;
    }//end buildDict


/*
    public void multiStageDataFetchSummarize() {
        if (hasPandas) {


            FlowSet beforeSet, afterSet, fetchInfo, killInfo;
            unitIterator = cfg.getUnitGraph().iterator();
            unitIterator1 = cfg.getUnitGraph().iterator();
            //generate flowset for each unit
            Unit unit, nextUnit;
            if (unitIterator1.hasNext()) {
                nextUnit = (Unit) unitIterator1.next();
            }
            while (unitIterator1.hasNext()) {
                unit = (Unit) unitIterator.next();
                nextUnit = (Unit) unitIterator1.next();
                fetchInfo = (FlowSet) alvav.getFlowAfter(unit);
                killInfo = (FlowSet) alva.getFlowAfter(unit);
                for (Iterator<Local> itr = fetchInfo.toList().iterator(); itr.hasNext(); ) {
                    Local l = itr.next();
                    String localName = l.getName();

                    if (fetch.get(localName) == null) {
                        fetch.put(localName, unit);
                    }

                }
                for (Iterator<Local> itr = killInfo.toList().iterator(); itr.hasNext(); ) {
                    Local l = itr.next();
                    String localName = l.getName();
                    kill.put(localName, nextUnit);

                }

            }

            System.out.println("Statement after which column should be fetched : " + fetch);
            System.out.println("Statement after which column should be removed : " + kill);
            System.out.println(firstUsed1);
        }
    }*/
    public void Analysis(){
        //KillInfo();
        unitIterator = cfg.getUnitGraph().iterator();

        while(unitIterator.hasNext()){
            Unit unit = (Unit) unitIterator.next();
            FlowSet used = new ArraySparseSet();
            FlowSet out = (FlowSet) alva.getFlowAfter(unit);
            FlowSet remove = new ArraySparseSet();
            for (ValueBox use: unit.getUseBoxes()) {
                if (use.getValue() instanceof Local) {
                   used.add(use.getValue());
                }
            }
            usedColumns.put(unit,used);
            //TODO : change needed column and all used columns here
            neededColumns.put(unit,(LinkedHashSet<Name>) alvav.getFlowAfter(unit));
            allUsedColumns.addAll(used.toList());
            used.difference(out,remove);
            removeColumns.put(unit,remove);
        }
        System.out.println("usedColumns"+usedColumns);
        System.out.println("neededColumns"+neededColumns);
        System.out.println("allUsedColumns"+allUsedColumns);
        System.out.println("removeColumns"+removeColumns);

    }



    /*public void KillInfo(){
        FlowSet killInfo;
        unitIterator = cfg.getUnitGraph().iterator();
        unitIterator1 = cfg.getUnitGraph().iterator();
        //generate flowset for each unit
        Unit unit, nextUnit;
        if (unitIterator1.hasNext()) {
            nextUnit = (Unit) unitIterator1.next();
        }
        while (unitIterator1.hasNext()) {
            unit = (Unit) unitIterator.next();
            nextUnit = (Unit) unitIterator1.next();
            killInfo = (FlowSet) alva.getFlowAfter(unit);

            for (Iterator<Local> itr = killInfo.toList().iterator(); itr.hasNext(); ) {
                Local l = itr.next();
                String localName = l.getName();
                kill.put(localName, nextUnit);

            }

        }

    }*/


}// end

