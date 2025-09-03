package parse.IRMaker;

import analysis.PythonScene;
import ir.JPMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetSootClass {
    JSONArray stmtList;
    String className;
    public GetSootClass(JSONArray stmtList, String className) {
        this.stmtList = stmtList;
        this.className=className;
    }

    public SootClass getSootClass(){
        //TODO Implement this correctly
        SootClass sClass = new SootClass(className, Modifier.PUBLIC);
        List<JSONObject> methodDefStmtList=new ArrayList<>();
        List<SootMethod> sootMethods=new ArrayList<>();
        for (Iterator it = stmtList.iterator(); it.hasNext(); ) {
            JSONObject stmt = (JSONObject) it.next();
            String typeOfStmt=stmt.get("ast_type").toString();
            if(typeOfStmt.equals("FunctionDef")){
                String methodName=stmt.get("name").toString();
                JSONArray stmtList=new JSONArray();
                stmtList.add(stmt);
                stmtList.addAll((JSONArray) (stmt.get("body")));
                //GetSootMethod getSootMethod=new GetSootMethod((JSONArray) (stmt.get("body")),methodName);
                GetSootMethod getSootMethod=new GetSootMethod(stmtList,methodName);
                //sootMethods.add(getSootMethod.getSootMethod());
                sClass.addMethod(getSootMethod.getSootMethod());
                methodDefStmtList.add(stmt);
            }

        }
        for(JSONObject stmt:methodDefStmtList){
            stmtList.remove(stmt);
        }
        //if stmtList still contains statement, then this is whole program and dummy main method should be created else this is error
        if(stmtList.size()!=0 && className.equals("MainClass")){
            GetSootMethod getSootMethod=new GetSootMethod(stmtList,"main");
            //sootMethods.add(getSootMethod.getSootMethod());
            sClass.addMethod(getSootMethod.getSootMethod());
        }


        return sClass;
    }

    //SootClass sClass = new SootClass(className, Modifier.PUBLIC);
    //TODO call IRParser with soot class so that each method can be called as a separate

}
