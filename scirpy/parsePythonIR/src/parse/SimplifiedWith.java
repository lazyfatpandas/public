package parse;

import ast.ExprAST;
import ir.DataType.FloatType;
import ir.DataType.IType;
import ir.DataType.IntType;
import ir.IExpr;
import ir.Stmt.*;
import ir.expr.*;
import ir.internalast.Targets;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import soot.PatchingChain;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.util.HashChain;

import static java.lang.System.exit;

public class SimplifiedWith {

    public PatchingChain<Unit> getSimplifiedWith(WithStmtPy withStmtPy,JSONObject block){
        PatchingChain<Unit> extendedIfList=new PatchingChain<Unit>(new HashChain<Unit>());
        //IExpr test= ifStmtPy.getTest();
        //TODO createSimpleConditions and Add statements for them in extendedIfList
        /*IExpr condition=createSimpleConditions(test, extendedIfList);
        NopStmt nop= Jimple.v().newNopStmt();
        IfStmt ifStmt=new IfStmt(condition,nop);
        ifStmt.copyIf(ifStmtPy);*/
        //extendedIfList.add(ifStmt);
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        AssignStmt  assignStmt=new AssignStmt(lineno,col_offset);
        buildAssignmentStatement(assignStmt,block);
        for(IExpr target:assignStmt.getTargets()){
            Value lvalue= target;
            Value rvalue=assignStmt.getRHS();
            AssignmentStmtSoot assignmentStmtSoot=new AssignmentStmtSoot(lvalue,rvalue,assignStmt);
            extendedIfList.add(assignmentStmtSoot);
        }
        if(withStmtPy.getBody()!=null) {
            for(Unit unit:withStmtPy.getBody().getUnits()){
                extendedIfList.add(unit);
            }
        }
        CallExprStmt callExprStmt = getFileCloseStmt(block);
        extendedIfList.add(callExprStmt);

        return extendedIfList;

    }

    public void  buildAssignmentStatement(AssignStmt assignStmt,JSONObject block){
        
        JSONArray itemsStmtList=(JSONArray)block.get("items");
        JSONObject item=(JSONObject) itemsStmtList.get(0);
        JSONObject valueObj= (JSONObject)item.get("context_expr");

        int col_offset=Integer.parseInt(valueObj.get("col_offset").toString());
        int lineno=Integer.parseInt(valueObj.get("lineno").toString());
        String typeofValue=valueObj.get("ast_type").toString();
        GetCall getCall=new GetCall();

        Call call=getCall.GetFunctionalCall(valueObj);
        call.setAst_type(ExprAST.Call);
        call.setLineno(lineno);
        call.setCol_offset(col_offset);
        assignStmt.setRHS(call);

        JSONObject targetsObj= (JSONObject)item.get("optional_vars");
        String id = targetsObj.get("id").toString();
        lineno=Integer.parseInt(targetsObj.get("lineno").toString());
        col_offset=Integer.parseInt(targetsObj.get("col_offset").toString());
        Name name = new Name(lineno, col_offset, id, Expr_Context.Load);
        assignStmt.getTargets().add(name);



    }
    private static CallExprStmt getFileCloseStmt(JSONObject block) {

        Call call= new Call();
        CallExprStmt callExprStmt =new CallExprStmt();

        Attribute value=new Attribute();
        value.setAst_type(ExprAST.Attribute);
        value.setAttr("close");
        JSONArray itemsStmtList=(JSONArray)block.get("items");
        JSONObject item=(JSONObject) itemsStmtList.get(0);
        JSONObject targetsObj= (JSONObject)item.get("optional_vars");
        String id =  targetsObj.get("id").toString();;
        // values of line number and column offset are dummy as 'file.close()' doesnt exist in actual source code
        Name name = new Name(3, 0, id, Expr_Context.Load);
        value.setValue(name);

        call.setFunc(value);
        callExprStmt.setCallExpr(call);

        return callExprStmt;

    }


}
