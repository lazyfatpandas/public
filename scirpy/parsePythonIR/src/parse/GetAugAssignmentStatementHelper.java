package parse;

import ast.ExprAST;
import ir.DataType.FloatType;
import ir.DataType.IType;
import ir.DataType.IntType;
import ir.IExpr;
import ir.Operator;
import ir.Stmt.AssignStmt;
import ir.Stmt.AugAssignStmt;
import ir.expr.*;
import ir.internalast.Targets;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import soot.JastAddJ.Expr;
import parse.TargetParser;

import static java.lang.System.exit;

public class GetAugAssignmentStatementHelper  {
    AugAssignStmt assignStmt;
    JSONObject block;
    public GetAugAssignmentStatementHelper(JSONObject block) {

        //this.assignStmt = assignStmt;
        this.block = block;
        buildAugAssignmentStatement();

        //testAssignmentStatement();

    }


    public void  buildAugAssignmentStatement(){
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        assignStmt=new AugAssignStmt(lineno,col_offset);
        //Array containing all targets like a=b=c=3
        JSONObject target=(JSONObject)block.get("target");

        //read each target in json array, captured as list in
        TargetParser targetparser = new TargetParser();
        targetparser.parseTarget(target);

        JSONObject opObj=(JSONObject)block.get("op");
        String opStr = (String) opObj.get("ast_type");
        assignStmt.setOp(Operator.valueOf(opStr));

        //System.out.println("Size of targets"+assignStmt.getTargets().size());

        //JSONArray valuearray=(JSONArray)block.get("value");
        JSONObject valueObj=(JSONObject)block.get("value");
        //JSONObject valueObj=(JSONObject) valuearray.get(0);
        //now for rhs
        col_offset=Integer.parseInt(valueObj.get("col_offset").toString());
        lineno=Integer.parseInt(valueObj.get("lineno").toString());
        String typeofValue=valueObj.get("ast_type").toString();

        System.out.println("Value obj is of type:"+typeofValue);

        //System.out.println(valueObj.toString());
        //TODO same logic used in arguement//all modification should be replicated...or rewrite if required
        switch(typeofValue)
        {
            case "Num":

                IType data;
                JSONObject nObj=(JSONObject)valueObj.get("n");
                String numType=nObj.get("ast_type").toString();
                if (numType.equals("int")) {
                    data = new IntType(Integer.parseInt(nObj.get("n").toString()),
                            nObj.get("n_str").toString());
                }
                else if (numType.equals("float")){
                    data = new FloatType(Double.parseDouble(nObj.get("n").toString()));
                }
                else{
                    //TODO remove this gracefully
                    data =new IntType(0,"0");
                }
                IExpr num=new Num(lineno,col_offset,data);
                assignStmt.setRHS(num);
                //System.out.println("Int type object is:"+intType.n +" "+intType.n_str);
                break;
            case "Str":
                String s=valueObj.get("s").toString();
                IExpr str=new Str(lineno,col_offset,s);
                assignStmt.setRHS(str);
                break;
            case "Call":
                GetCall getCall=new GetCall();
                //Call call=GetFunctionalCall(valueObj);
                Call call=getCall.GetFunctionalCall(valueObj);
                call.setAst_type(ExprAST.Call);
                call.setLineno(lineno);
                call.setCol_offset(col_offset);

                assignStmt.setRHS(call);
                System.out.println("break");
                break;
            case "Name":
                String id=valueObj.get("id").toString();
                //TODO get it from json

                IExpr name=new Name(lineno, col_offset,id,Expr_Context.Load);

                assignStmt.setRHS(name);
                System.out.println("break");
                break;
            case "List":
                ListParser listParser=new ListParser();
                IExpr listComp=listParser.parseList(valueObj);
                assignStmt.setRHS(listComp);
                System.out.println("break for list");
                break;


            default :
                System.out.println(typeofValue+ " still Not implemented in assignment");
                exit(1);

                // Statements
        }
    }


    public AugAssignStmt getAugAssignStmt() {

        return assignStmt;
    }


}