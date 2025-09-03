package parse;

import ir.IExpr;
import ir.JPBody;
import ir.Stmt.IfStmt;
import ir.Stmt.IfStmtPy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import parse.ops.OpParser;
import soot.Unit;
import soot.jimple.Jimple;
import ir.Stmt.NopStmt;

public class IfStmtParser {
    JSONObject block;
    //IfStmt ifStmt;
    IfStmtPy ifStmt;
    JSONArray ifStmtList;
    JSONArray orelseStmtList;
    IExpr condition;//Compare in Python AST, condition in soot

    public IfStmtParser(JSONObject block) {
        this.block = block;
    }

//    public IfStmt getIfStmt(){
//        parseIfStmt();
//        return ifStmt;
//    }
public IfStmtPy getIfStmt(){
        parseIfStmt();
        return ifStmt;
    }

    private void parseIfStmt(){
        IRParser irParser=new IRParser();
        CompareParser compareParser=new CompareParser();

        //setting body of if
        ifStmtList=(JSONArray)block.get("body");

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
        ifStmt=new IfStmtPy();
        if(ifStmtList!=null){
            ifStmt.setIfBody(irParser.getIR(ifStmtList));
        }
        if(orelseStmtList!=null) {
            irParser=new IRParser();
            ifStmt.setOrelseBody(irParser.getIR(orelseStmtList));
        }
        ifStmt.setTest(condition);
        ifStmt.setLineno(Integer.parseInt(block.get("lineno").toString()));



    }

    private IExpr testParser(JSONObject testObj){
        OpParser opParser=new OpParser();
        return opParser.parseOp(testObj);
    }


}
