package parse;

import ast.ExprAST;
import ir.DataType.FloatType;
import ir.DataType.IType;
import ir.DataType.IntType;
import ir.IExpr;
import ir.Stmt.AssignStmt;
import ir.expr.*;
import ir.internalast.Targets;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import soot.JastAddJ.Expr;

import static java.lang.System.exit;

public class GetAssignmentStatementHelper {
    AssignStmt assignStmt;
    JSONObject block;

    public GetAssignmentStatementHelper(JSONObject block) {
        //this.assignStmt = assignStmt;
        this.block = block;
        buildAssignmentStatement();

        //testAssignmentStatement();

    }

    private void testAssignmentStatement() {
        IExpr rHS=assignStmt.getRHS();
        if(rHS instanceof Num){

            Num num=(Num)rHS;
            IType type=num.getData();
            if(type instanceof IntType){
                IntType intType=(IntType)type;
               // System.out.println("This is no with value:"+ intType.n_str);

            }
        }
        else if(rHS instanceof Str){
            Str str=(Str)rHS;
            System.out.println("This is str:"+ str.getS());
        }
    }

    public void  buildAssignmentStatement(){
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        assignStmt=new AssignStmt(lineno,col_offset);
        //Array containing all targets like a=b=c=3
        JSONArray childarray=(JSONArray)block.get("targets");

        //read each target in json array, captured as list in
        childarray.forEach(target -> parseTarget((JSONObject) target));
        //System.out.println("Size of targets"+assignStmt.getTargets().size());

        //JSONArray valuearray=(JSONArray)block.get("value");
        JSONObject valueObj=(JSONObject)block.get("value");
        //JSONObject valueObj=(JSONObject) valuearray.get(0);
        //now for rhs
        col_offset=Integer.parseInt(valueObj.get("col_offset").toString());
        lineno=Integer.parseInt(valueObj.get("lineno").toString());
        String typeofValue=valueObj.get("ast_type").toString();

        //System.out.println("Value obj is of type:"+typeofValue);

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
                //System.out.println("break");
                break;
            case "Name":
                String id=valueObj.get("id").toString();
                //TODO get it from json

                IExpr name=new Name(lineno, col_offset,id,Expr_Context.Load);

                assignStmt.setRHS(name);
                //System.out.println("break");
                break;

            case "List":
                ListParser listParser=new ListParser();
                IExpr listComp=listParser.parseList(valueObj);
                assignStmt.setRHS(listComp);
                //System.out.println("break for list");
                break;
            case "Tuple":
                listParser=new ListParser();
                listComp=listParser.parseList(valueObj);
                ListComp lc=(ListComp)listComp;
                lc.setTypeOfList("Tuple");
                assignStmt.setRHS(lc);
                //System.out.println("break for list");
                break;

            case "BinOp":
                BinOpParser binOpParser=new BinOpParser(valueObj);
                IExpr binOp=binOpParser.getBinOp();
                assignStmt.setRHS(binOp);
                System.out.println("break for BinOp");
                break;
            case "UnaryOp":
                UnaryOpParser unaryOpParser=new UnaryOpParser(valueObj);
                IExpr unaryOp=unaryOpParser.getUnaryOp();
                assignStmt.setRHS(unaryOp);
                System.out.println("break for UnaryOp");
                break;
            case "Subscript":
                //TODO verify correctness
                SubscriptParser subscriptParser=new SubscriptParser(valueObj);
                assignStmt.setRHS(subscriptParser.parseSubscript());
                break;
            case "ListComp":
                ListCompCParser listCompCParser=new ListCompCParser();
                assignStmt.setRHS(listCompCParser.parseList(valueObj));
                break;

            case "Dict":
                DictParser dictParser=new DictParser();
                assignStmt.setRHS(dictParser.parseDict(valueObj));
                break;
            case "Attribute":
                AttributeParser attributeParser=new AttributeParser();
                IExpr attribute=attributeParser.parseAttribute(valueObj);
                assignStmt.setRHS(attribute);
                System.out.println("break for Attribute");
                break;
            case "Compare":
                //TODO verify this example 6
                CompareParser compareParser=new CompareParser(valueObj);
                IExpr compare=compareParser.getCompare();
                assignStmt.setRHS(compare);
                System.out.println("break for Compare");
                break;
            case "Lambda":
                //TODO verify this example 6
                LambdaParser lambdaParser=new LambdaParser(valueObj);
                IExpr lambdaExpr=lambdaParser.parseLambda();
                assignStmt.setRHS(lambdaExpr);
                System.out.println("break for Lambda");
                break;
            case "Constant":
                //TODO verify this example 6
                GenericExpressionParser gep=new GenericExpressionParser();
                IExpr constExpr=gep.getConstant(valueObj);
                assignStmt.setRHS(constExpr);
                System.out.println("break for Constant");
                break;
            default :
                System.out.println(typeofValue+ " still Not implemented in assignment in lineno="+lineno);
                exit(1);

                // Statements
        }
    }

    //TODO moved to target parser, if any modification, remove from here and modify there...
    //bhu 02/02/2020
    private void parseTarget(JSONObject targetsObj) {
        /*String typeofTarget=valueObj.get("ast_type").toString();
        switch(typeofValue) {
            case "Name":

            default :
                System.out.println(typeofTarget+ " still not implemented in targets");
                exit(1);
        }
*/
//commented for changes
        /*
        //TODO change this to Name if name is only target else modify target class accordingly
        Targets target=new Targets(Integer.parseInt(targetsObj.get("lineno").toString())
                ,Integer.parseInt(targetsObj.get("col_offset").toString()),targetsObj.get("id").toString());//Targets(int lineno, int col_offset, Name name, String id )

        assignStmt.getTargets().add(target);
*/
        String typeofTarget=targetsObj.get("ast_type").toString();
        switch(typeofTarget) {
            case "id":
                Targets target=new Targets(Integer.parseInt(targetsObj.get("lineno").toString())
                        ,Integer.parseInt(targetsObj.get("col_offset").toString()),targetsObj.get("id").toString());//Targets(int lineno, int col_offset, Name name, String id )

                assignStmt.getTargets().add(target);
                break;
            case "Attribute":
                AttributeParser attributeParser=new AttributeParser();
                IExpr attTarget=attributeParser.parseAttribute(targetsObj);
                assignStmt.getTargets().add(attTarget);
                break;
            case "Name":
                String id = targetsObj.get("id").toString();
                int lineno=Integer.parseInt(targetsObj.get("lineno").toString());
                int col_offset=Integer.parseInt(targetsObj.get("col_offset").toString());
                Name name = new Name(lineno, col_offset, id, Expr_Context.Load);
                assignStmt.getTargets().add(name);
                break;
                //TODO verify this
            case "Subscript":
                SubscriptParser subscriptParser=new SubscriptParser(targetsObj);
                assignStmt.getTargets().add(subscriptParser.parseSubscript());
                break;
            case "Tuple":
                ListParser listParser=new ListParser();
                IExpr listComp=listParser.parseList(targetsObj);
                ListComp lc=(ListComp)listComp;
                lc.setTypeOfList("Tuple");
                assignStmt.getTargets().add(lc);
                break;
            default :
                System.out.println(typeofTarget+ " still not implemented in targets");
                exit(1);
        }

    }




    public AssignStmt getAssignStmt() {

        return assignStmt;
    }
}
