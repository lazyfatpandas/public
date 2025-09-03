package parse.IRMaker;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import soot.Scene;
import soot.SootClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainParser {

    JSONArray stmtList;
    public MainParser(JSONArray stmtList) {
        this.stmtList = stmtList;
    }
    public List<SootClass> getClasses() {
        return getClasses(stmtList);
    }
    /*At the end of this loop, all class definition statements will be removed from stmtlist and
     IR for all those classes will be available in sootClasses, then a main class for remaining statements can be created.
    */
    public List<SootClass> getClasses(JSONArray stmtList){
        List<SootClass> sootClasses=new ArrayList<>();
        List<JSONObject> classDefStmtList=new ArrayList<>();
        for (Iterator it = stmtList.iterator(); it.hasNext(); ) {
            JSONObject stmt = (JSONObject) it.next();
            String typeOfStmt=stmt.get("ast_type").toString();
            if(typeOfStmt.equals("ClassDef")){
                String className=stmt.get("name").toString();

                GetSootClass getSootClass=new GetSootClass((JSONArray) (stmt.get("body")),className);
                SootClass sootClass=getSootClass.getSootClass();
                //TEST===Remove later if not required
                Scene.v().addClass(sootClass);
                //TEST end
                sootClasses.add(sootClass);
                classDefStmtList.add(stmt);
            }

        }
        for(JSONObject stmt:classDefStmtList){
            stmtList.remove(stmt);
        }
        //Now creating IR for remaining statements...
        GetSootClass getSootClass=new GetSootClass(stmtList,"MainClass");
        sootClasses.add(getSootClass.getSootClass());
        return sootClasses;
    }


}
