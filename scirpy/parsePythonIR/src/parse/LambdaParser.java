package parse;

import ir.IExpr;
import ir.expr.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static java.lang.System.exit;

public class LambdaParser {

    JSONObject obj;

    //We only handle lambdas with args only..
    public LambdaParser(JSONObject obj) {
        this.obj = obj;
    }
    public LambdaParser() {

    }
    public IExpr parseLambda() {
        return parseLambda(obj);
    }

    public IExpr parseLambda(JSONObject obj) {
        IExpr expr=null;
        Lambda lambdaExpr=new Lambda();

        int lineno=Integer.parseInt(obj.get("lineno").toString());
        int col_offset=Integer.parseInt(obj.get("col_offset").toString());
        JSONObject mainArgsObj=(JSONObject)obj.get("args");
        //JSONObject mainArgs=(JSONObject)mainArgsObj.get("value");


        JSONArray argsArray=(JSONArray)mainArgsObj.get("args");
        if(mainArgsObj.get("kwarg")!=null || ((JSONArray)mainArgsObj.get("kwonlyargs")).size()!=0 || ((JSONArray)mainArgsObj.get("kw_defaults")).size()!=0 || ((JSONArray)mainArgsObj.get("defaults")).size()!=0 ){
            System.out.println("Kwargs not implemented for lambda expression at line no:"+lineno);
            System.exit(0);
        }

        lambdaExpr.setCol_offset(col_offset);
        lambdaExpr.setLineno(lineno);
        argsArray.forEach(keywords -> parseArg((JSONObject) keywords,lambdaExpr));

        //parse lambda body
        JSONObject bodyObj=(JSONObject)obj.get("body");
        AttributeParser attributeParser=new AttributeParser();
        lambdaExpr.setBody(attributeParser.parseAttribute(bodyObj));


        return lambdaExpr;
    }

    private void parseArg(JSONObject listObject, Lambda lambdaExpr){
        String elementType = listObject.get("ast_type").toString();
        switch (elementType){
            case "Str":
                StringParser stringParser=new StringParser();
                lambdaExpr.getArgs().add(stringParser.parse(listObject));
                break;
            case "Num":
                NumParser numParser=new NumParser();
                lambdaExpr.getArgs().add(numParser.parse(listObject));
                break;
            case "Name":
                GenericExpressionParser gep=new GenericExpressionParser();
                lambdaExpr.getArgs().add(gep.getName(listObject));
                break;
            case "UnaryOp":
                UnaryOpParser unaryOpParser=new UnaryOpParser(listObject);
                lambdaExpr.getArgs().add(unaryOpParser.getUnaryOp());
                break;
            case "Subscript":
                SubscriptParser subscriptParser=new SubscriptParser(listObject);
                lambdaExpr.getArgs().add(subscriptParser.parseSubscript());
                break;
            case "Attribute":
                AttributeParser aP=new AttributeParser();
                lambdaExpr.getArgs().add(aP.parseAttribute(listObject));
                break;
            case "Call":
                GetCall getCall=new GetCall();
                lambdaExpr.getArgs().add(getCall.GetFunctionalCall(listObject));
                break;

            case "Constant":
                GenericExpressionParser gepc=new GenericExpressionParser();
                lambdaExpr.getArgs().add(gepc.getConstant(listObject));
                break;
            case "arg":
                String s=  listObject.get("arg").toString();
                int lineno=Integer.parseInt(listObject.get("lineno").toString());
                int col_offset=Integer.parseInt(listObject.get("col_offset").toString());
                Arg arg = new Arg(lineno, col_offset, s);
                lambdaExpr.getArgs().add(arg);
                break;

            default:
                System.out.println(elementType + " Not implemented in List parsing in Lambda parser");
                exit(0);
        }



    }



}


