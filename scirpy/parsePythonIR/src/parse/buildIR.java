package parse;

import ast.StmtAST;
import ir.*;
import ir.expr.Call;
import ir.internalast.Alias;
import ir.internalast.InternalASTType;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.util.ArrayList;

import ir.Stmt.*;
import java.util.List;

import soot.*;
import soot.util.Chain;
import soot.util.HashChain;

import static java.lang.System.exit;
//TODO this is deprecated...use new class IRParser
public class buildIR {
    //codeBodies
    static int i=0;
    //TODO remove this and add Chain for soot after reading them
    public static List<StmtAST> allStmtASTS =new ArrayList<>();
    public static PatchingChain<Unit> units = new PatchingChain<Unit>(
            new HashChain<Unit>());
    public static Chain<Local> locals = new HashChain<Local>();
    public static void parseCodeBlock(JSONObject block)
    {
        String statementType=block.get("ast_type").toString();
        switch(statementType)
        {
            case "Import":
                //System.out.println("Import statement keys:" + block.keySet());
                //getImportStatemnt(block);
                //allStmtASTS.add(getImportStatemnt(block));
                units.add(getImportStatemnt(block));
                break;
            case "ImportFrom":
                break;
            case "Assign":
                //System.out.println("Assign statement keys:" + block.keySet());
               // System.out.println(block.toString());
                //TODO assignment currently can't handle a,b,c=1,2,3...add it..
                AssignStmt assignStmt=getAssignStatemnt(block);
                //This case is for type a=b=c=10 and normal assignments only, will fail for TODO above
                for(IExpr target:assignStmt.getTargets()){
                    Value lvalue= target;
                    Value rvalue=assignStmt.getRHS();
                    AssignmentStmtSoot assignmentStmtSoot=new AssignmentStmtSoot(lvalue,rvalue,assignStmt);
                    units.add(assignmentStmtSoot);
                }

                //units.add(getAssignStatemnt(block));
                break;
            case "FunctionDef":
                units.add(getFuncDef(block));
                //System.out.println(block.keySet());
                //System.out.println(block.get("body"));
                //System.out.println(statementType + " statement Not implemented");
               // exit(1);
                break;
            case "Expr":
                units.add(getExpr(block));
                //System.out.println(statementType + " statement Not implemented");
                //exit(1);
                break;

            default :
                System.out.println(statementType + " statement Not implemented");
                //exit(1);

                // Statements
        }

       /* for(Iterator iterator = block.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            //System.out.println(key+",");
            //System.out.println(body.get(key));
        }*/
        //System.out.println(i+":"+block.get("ast_type"));
        i++;
        //System.out.println("END of body");

        //TODO put analysis code here
    }



    public static JPBody buildBody(){
        //TODO this is dummy, modify it with actual list later on
        List<Local> parameterTypes=new ArrayList();
        //String name, List parameterTypes, JPBody body)
        JPMethod jpMethod=new JPMethod("Module", parameterTypes, null);
        //JPMethod method, PatchingChain<Unit> unitChain,Chain<Local> localChain
        JPBody jpBody=new JPBody(jpMethod, units, locals);
        jpBody.getMethod().setActiveBody(jpBody);
        return jpBody;
    }



    private static ImportStmt getImportStatemnt(JSONObject block) {
        //TODO add checks for proper Import Statement
        //assert (xyz);

        ImportStmt importStmt = null;
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        String names=block.get("names").toString();
        JSONArray childarray=(JSONArray)block.get("names");
        JSONObject aliasObj=(JSONObject) childarray.get(0);
        Alias alias=new Alias(aliasObj.get("name").toString(), aliasObj.get("asname").toString(), true, InternalASTType.alias);
        importStmt=new ImportStmt(lineno,col_offset,alias, StmtAST.Import);
        //System.out.println(importStmt);

        return importStmt;
    }

    private static AssignStmt getAssignStatemnt(JSONObject block) {
        //TODO add checks for proper Import Statement
        //assert (xyz);

        AssignStmt assignStmt = null;
        GetAssignmentStatementHelper getAssignmentStatementHelper=new GetAssignmentStatementHelper(block);
        //assignStmt=
        assignStmt=getAssignmentStatementHelper.getAssignStmt();
        return assignStmt;
    }

    private static CallExprStmt getExpr(JSONObject block) {
        //TODO move this logic to GetExpr class if getting complicated here
        //System.out.println("Printing expression");
        //System.out.println(block.toString());
        JSONObject exprValueObj=(JSONObject) block.get("value");
        String exprType=exprValueObj.get("ast_type").toString();
        switch(exprType) {
            case "Call":
                GetCall getCall=new GetCall();
                Call call=getCall.GetFunctionalCall(exprValueObj);
                CallExprStmt callExprStmt =new CallExprStmt();
                callExprStmt.setCallExpr(call);
                return callExprStmt;
        }

        CallExprStmt callExprStmt =new CallExprStmt();
        //Should never reach here
        return callExprStmt;

    }

   private static FunctionDefStmt getFuncDef(JSONObject block) {
       FuncDefParser funcDefParser=new FuncDefParser(block);
       return funcDefParser.getFuncDef();
   }


    public static void TestChains() {
        Chain<Unit> unitChain = units;


        for(Unit unit : unitChain) {

            List valueBoxes = unit.getUseAndDefBoxes();
            System.out.println(valueBoxes.size());
            for(Object obj : valueBoxes) {
                assert obj instanceof ValueBox;
                ValueBox valueBox = (ValueBox) obj;
                Value value = valueBox.getValue();
                System.out.println(value.toString());
            }

        }
    }


}
