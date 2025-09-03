package passes;

import PythonGateWay.ReadProps;
import analysis.LiveVariable.AttributeLiveVariableAnalysis;
import analysis.LiveVariable.LiveVariableAnalysis;
import analysis.PythonScene;
import cfg.CFG;
import ir.IExpr;
import ir.JPMethod;
import ir.Stmt.AssignStmt;
import ir.Stmt.AssignmentStmtSoot;
import ir.Stmt.CallExprStmt;
import ir.Stmt.ImportStmt;
import ir.expr.Attribute;
import ir.expr.Call;
import ir.expr.Name;
import ir.internalast.Targets;
import rewrite.pd1.Pd1Elt;
import soot.Local;
import soot.Unit;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PrePass {
    JPMethod jpMethod = null;
    CFG cfg;
    Iterator unitIterator;


    public PrePass(JPMethod jpMethod) {
        this.jpMethod = jpMethod;
        this.cfg = new CFG(jpMethod);
        this.unitIterator = cfg.getUnitGraph().iterator();
        performPrePass();

    }

    public void performPrePass() {
        FlowSet beforeSet, afterSet;
        List<String> allDfNames=new ArrayList<String>();


        //generate flowset for each unit
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            if (unit instanceof ImportStmt) {
                ImportStmt importStmt = (ImportStmt) unit;
                if (importStmt.getNames().getName().equals("pandas") || importStmt.getNames().getName().equals(ReadProps.read("ScapaName"))) {
                    PythonScene.hasPandas = true;
                    PythonScene.pandasAliasName = importStmt.getNames().getAsname();
                    //System.out.println("Pandas alias:"+ pandasAlias);
                }

            }

            if(PythonScene.hasPandas){
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
                        if(call.getFunc() instanceof Attribute){
                            Attribute func=(Attribute)call.getFunc();
                            while(func.getValue() instanceof Attribute){
                                func=(Attribute)func.getValue();
                            }
                            if(func.getValue() instanceof Name){
                                Name name=(Name)func.getValue();
                                if(name.id.equals(PythonScene.pandasAliasName)){
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
                                    allDfNames.add(dfName);
                                    PythonScene.dfNametoUnitMap.put(dfName,unit);
                                    PythonScene.unitToDfNameMap.put(unit,dfName);
                                }//id
                            }//if func
                        }
                    }//rhs
                }//unit is assignmentSoot



            }
          }// end unitIterator
        PythonScene.dfsFromFile.addAll(allDfNames);

    }

}