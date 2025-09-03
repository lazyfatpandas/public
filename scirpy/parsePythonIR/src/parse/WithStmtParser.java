package parse;

import ir.IExpr;
import ir.Stmt.ForStmtPy;
import ir.Stmt.WithStmtPy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WithStmtParser {
    JSONObject block;
    WithStmtPy withStmtPy;
    JSONArray bodyStmtList;



    JSONArray itemsStmtList;


    public WithStmtParser(JSONObject block) {
        this.block = block;
    }
    public WithStmtParser() {

    }

    public WithStmtPy getWithStmtPy(JSONObject block) {
        this.block=block;
        parseWithStatement();
        return withStmtPy;
    }
    public WithStmtPy getWithStmtPy() {
        parseWithStatement();
        return withStmtPy;
    }


    private void parseWithStatement(){
        IRParser irParser=new IRParser();
        withStmtPy=new WithStmtPy();

        bodyStmtList=(JSONArray)block.get("body");
        //orelseStmtList=(JSONArray)block.get("orelse");
        //iterObj=(JSONObject)block.get("iter");
        //targetObj=(JSONObject)block.get("target");
        itemsStmtList=(JSONArray)block.get("items");

        if(bodyStmtList!=null){
            withStmtPy.setBody(irParser.getIR(bodyStmtList));
        }

        if(itemsStmtList!=null) {
            irParser=new IRParser();
            withStmtPy.setItemsStmtBody(irParser.getIR(itemsStmtList));
        }



    }

    public JSONObject getBlock() {
        return block;
    }

    public void setBlock(JSONObject block) {
        this.block = block;
    }




}
