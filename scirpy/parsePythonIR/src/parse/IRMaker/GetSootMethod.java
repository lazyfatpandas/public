package parse.IRMaker;

import ir.JPBody;
import org.json.simple.JSONArray;
import parse.IRParser;
import soot.SootMethod;

public class GetSootMethod {
    JSONArray stmtList;
    String methodName;

    public GetSootMethod(JSONArray stmtList, String methodName) {
        this.stmtList = stmtList;
        this.methodName = methodName;
    }
    public SootMethod getSootMethod(){
        SootMethod sootMethod=null;
        IRParser irParser=new IRParser();

        JPBody jpBody=irParser.getIR(stmtList,methodName);
        return  jpBody.getMethod();
    }
}
