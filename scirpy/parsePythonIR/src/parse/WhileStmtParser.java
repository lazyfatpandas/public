package parse;

import ir.IExpr;
import ir.JPBody;
import ir.Stmt.IfStmt;
import ir.Stmt.IfStmtPy;
import ir.Stmt.WhileStmtPy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import parse.ops.OpParser;
import soot.Unit;
import soot.jimple.Jimple;
import ir.Stmt.NopStmt;

public class WhileStmtParser {
    JSONObject block;
    //IfStmt ifStmt;
    WhileStmtPy whileStmtPy;
    JSONArray bodyStmtList;
    IExpr condition;//Compare in Python AST, condition in soot
    JSONArray orelseStmtList;

    public WhileStmtParser(JSONObject block) {
        this.block = block;
    }

    //    public IfStmt getIfStmt(){
//        parseIfStmt();
//        return ifStmt;
//    }


    public WhileStmtPy getWhileStmtPy(JSONObject block) {
        this.block=block;
        parseWhileStmt();
        return whileStmtPy;
    }
    public WhileStmtPy getWhileStmtPy() {
        parseWhileStmt();
        return whileStmtPy;
    }

    private void parseWhileStmt(){
        IRParser irParser=new IRParser();
        CompareParser compareParser=new CompareParser();

        //setting body of if
        bodyStmtList=(JSONArray)block.get("body");

        //setting body of else
        orelseStmtList=(JSONArray)block.get("orelse");

        //setting test condition here
        JSONObject testObj=(JSONObject)block.get("test");

        condition=testParser(testObj);
        //TODO see why unit is only 1
        Unit unit=null;
        //TODO see if this is what it should be,,
        NopStmt nop= new NopStmt();
        //TODO change this when extending soot
        //TODO getting ifstmtpy here, if need soot compatible if, alter the lines
        //ifStmt=new IfStmt(condition, nop);
        whileStmtPy=new WhileStmtPy();
        if(bodyStmtList!=null){
            whileStmtPy.setWhileBody(irParser.getIR(bodyStmtList));
        }

        if(orelseStmtList!=null) {
            irParser=new IRParser();
            whileStmtPy.setOrelseBody(irParser.getIR(orelseStmtList));
        }

        whileStmtPy.setTest(condition);
        whileStmtPy.setLineno(Integer.parseInt(block.get("lineno").toString()));



    }

    private IExpr testParser(JSONObject testObj){
        OpParser opParser=new OpParser();
        return opParser.parseOp(testObj);
    }


}
