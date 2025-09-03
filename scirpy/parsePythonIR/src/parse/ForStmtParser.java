package parse;

import ir.IExpr;
import ir.Stmt.ForStmtPy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ForStmtParser {
    JSONObject block;
    ForStmtPy forStmtPy;
    JSONArray bodyStmtList;
    JSONArray orelseStmtList;
    JSONObject iterObj;
    JSONObject targetObj;



    public ForStmtParser(JSONObject block) {
        this.block = block;
    }
    public ForStmtParser() {

    }

    public ForStmtPy getForStmtPy(JSONObject block) {
        this.block=block;
        parseForStatement();
        return forStmtPy;
    }
    public ForStmtPy getForStmtPy() {
        parseForStatement();
        return forStmtPy;
    }


    private void parseForStatement(){
        IRParser irParser=new IRParser();
        forStmtPy=new ForStmtPy();

        bodyStmtList=(JSONArray)block.get("body");
        orelseStmtList=(JSONArray)block.get("orelse");
        iterObj=(JSONObject)block.get("iter");
        targetObj=(JSONObject)block.get("target");

        if(bodyStmtList!=null){
            forStmtPy.setBody(irParser.getIR(bodyStmtList));
        }
        if(orelseStmtList!=null) {
            irParser=new IRParser();
            forStmtPy.setOrelseBody(irParser.getIR(orelseStmtList));
        }
        if(iterObj!=null) {
            AttributeParser attributeParser=new AttributeParser();
            IExpr attTarget=attributeParser.parseAttribute(iterObj);
            forStmtPy.setIterator(attTarget);
        }
        if(targetObj!=null) {
            TargetParser targetParser=new TargetParser();
            forStmtPy.setTarget(targetParser.parseTarget(targetObj));
        }


    }

    public JSONObject getBlock() {
        return block;
    }

    public void setBlock(JSONObject block) {
        this.block = block;
    }



    public void setForStmtPy(ForStmtPy forStmtPy) {
        this.forStmtPy = forStmtPy;
    }

    public JSONArray getBodyStmtList() {
        return bodyStmtList;
    }

    public void setBodyStmtList(JSONArray bodyStmtList) {
        this.bodyStmtList = bodyStmtList;
    }

    public JSONArray getOrelseStmtList() {
        return orelseStmtList;
    }

    public void setOrelseStmtList(JSONArray orelseStmtList) {
        this.orelseStmtList = orelseStmtList;
    }

}
