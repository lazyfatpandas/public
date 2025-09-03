package parse;

import ir.IExpr;
import ir.Stmt.IfStmtPy;
import ir.Stmt.ReturnStmt;
import ir.expr.Expr_Context;
import ir.expr.Name;
import org.json.simple.JSONObject;

public class ReturnStmtParser {
    JSONObject block,valueObj;
    //IfStmt ifStmt;
    ReturnStmt returnStmt=null;
    IExpr iExpr=null;

    public ReturnStmtParser(JSONObject block) {
        this.block = block;
    }
    public ReturnStmtParser() {
    }

    public ReturnStmt getReturnStmt(JSONObject block) {
        this.block=block;
        return getReturnStmt();
    }

    public ReturnStmt getReturnStmt() {
        valueObj=(JSONObject)block.get("value");
        AttributeParser attributeParser=new AttributeParser();
        iExpr=attributeParser.parseAttribute(valueObj);
        returnStmt=new ReturnStmt(iExpr);

        int col_offset = Integer.parseInt(block.get("col_offset").toString());
        int lineno = Integer.parseInt(block.get("lineno").toString());
        returnStmt.setCol_offset(col_offset);
        returnStmt.setLineno(lineno);
        return returnStmt;
    }


    private IExpr getReturnExpr(){
        return iExpr;
    }
}
