package parse;

import ir.IExpr;
import ir.Stmt.FunctionDefStmt;
import ir.expr.Expr_Context;
import ir.expr.Name;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FuncDefParser {
    JSONObject funDefObject;
    FunctionDefStmt funcDef=new FunctionDefStmt();

    public FuncDefParser(JSONObject funDefObject) {
        this.funDefObject = funDefObject;
    }

    public FunctionDefStmt getFuncDef() {
        parseArgs();
        parseVariousOtherArgs();
        parseBody();
        parseOtherInfo();

        return funcDef;
    }

    private void parseVariousOtherArgs() {
        //TODO yet to be implemented, get json object/ array for each and then for each arg, use ArgParser
        /*
                 "defaults": [],
                "kw_defaults": [],
                "kwarg": null,
                "kwonlyargs": [],
                "vararg": null
         */
    }

    private void parseBody(){
        JSONArray stmtList=(JSONArray) funDefObject.get("body");
        String name=  funDefObject.get("name").toString();

        IRParser irParser=new IRParser();
        if(stmtList!=null){
            funcDef.setBody(irParser.getIR(stmtList,name));
        }
    }

    private void parseArgs(){
        JSONObject argsObj=(JSONObject) funDefObject.get("args");
        JSONArray argsArray=(JSONArray) argsObj.get("args");
        int i=0,nosArgs=argsArray.size();
        ArgParser argParser=new ArgParser();

        for(i=0; i<nosArgs; i++){
            JSONObject argObj=(JSONObject)argsArray.get(i);
            argParser.setArgsObj(argObj);
            IExpr arg=argParser.getArg();
            funcDef.getArgs().add(arg);
        }
    }

    private void parseOtherInfo(){
      int col_offset = Integer.parseInt(funDefObject.get("col_offset").toString());
      int lineno = Integer.parseInt(funDefObject.get("lineno").toString());
      String name=funDefObject.get("name").toString();
      funcDef.setName(new Name(lineno, col_offset, name, Expr_Context.Load));
      funcDef.setCol_offset(col_offset);
      funcDef.setLineno(lineno);
    }
}
