package parse;

import ir.IExpr;
import ir.Stmt.ClassDefStmt;
import ir.Stmt.FunctionDefStmt;
import ir.expr.Expr_Context;
import ir.expr.Name;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ClassDefParser {
    JSONObject classDefObject;
    ClassDefStmt classDefStmt=new ClassDefStmt();

    public ClassDefParser(JSONObject classDefObject) {
        this.classDefObject = classDefObject;
    }

    public ClassDefStmt getClassDefStmt() {
        parseVariousOtherArgs();
        parseBody();
        parseOtherInfo();

        return classDefStmt;
    }

    private void parseVariousOtherArgs() {
        //TODO yet to be implemented, get json object/ array for each and then for each arg, use ArgParser
        /*
        "bases": [],
        "decorator_list": [],
       "keywords": [],

        */
    }

    private void parseBody(){
        JSONArray stmtList=(JSONArray) classDefObject.get("body");
        IRParser irParser=new IRParser();
        //interprocedural
        String name=  (String)classDefObject.get("name");
        if(stmtList!=null){
            classDefStmt.setBody(irParser.getIR(stmtList,name));
        }
    }



    private void parseOtherInfo(){
        /*
            "body": [
            "col_offset": 0,
            "lineno": 1,
            "name": "Person"
         */
        int col_offset = Integer.parseInt(classDefObject.get("col_offset").toString());
        int lineno = Integer.parseInt(classDefObject.get("lineno").toString());
        String name=classDefObject.get("name").toString();
        classDefStmt.setName(new Name(lineno, col_offset, name, Expr_Context.Load));
        classDefStmt.setLineno(lineno);
        classDefStmt.setCol_offset(col_offset);
    }
}


