package parse;

import ir.IExpr;
import ir.expr.JoinedStr;
import ir.expr.ListComp;
import ir.expr.Name;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static java.lang.System.exit;

public class JoinedStrParser {

    JSONObject obj;


    public JoinedStrParser(JSONObject obj) {
        this.obj = obj;
    }
    public JoinedStrParser() {

    }
    public IExpr parseJoinedStr() {
        return parseJoinedStr(obj);
    }

    public IExpr parseJoinedStr(JSONObject obj) {
        IExpr expr=null;
        JoinedStr joinedStr=new JoinedStr();

        int lineno=Integer.parseInt(obj.get("lineno").toString());
        int col_offset=Integer.parseInt(obj.get("col_offset").toString());
        JSONArray valuesArray=(JSONArray)obj.get("values");
        joinedStr.setCol_offset(col_offset);
        joinedStr.setLineno(lineno);
        valuesArray.forEach(keywords -> parseValue((JSONObject) keywords,joinedStr));
        //should never reach here
        return joinedStr;
    }

    private void parseValue(JSONObject listObject, JoinedStr joinedStr){
        String elementType = listObject.get("ast_type").toString();
        switch (elementType){
            case "Str":
                StringParser stringParser=new StringParser();
                joinedStr.getValues().add(stringParser.parse(listObject));
                break;
            case "Num":
                NumParser numParser=new NumParser();
                joinedStr.getValues().add(numParser.parse(listObject));
                break;
            case "Name":
                GenericExpressionParser gep=new GenericExpressionParser();
                joinedStr.getValues().add(gep.getName(listObject));
                break;
            case "UnaryOp":
                UnaryOpParser unaryOpParser=new UnaryOpParser(listObject);
                joinedStr.getValues().add(unaryOpParser.getUnaryOp());
                break;
            case "Subscript":
                SubscriptParser subscriptParser=new SubscriptParser(listObject);
                joinedStr.getValues().add(subscriptParser.parseSubscript());
                break;
            case "Attribute":
                AttributeParser aP=new AttributeParser();
                joinedStr.getValues().add(aP.parseAttribute(listObject));
                break;
            case "Call":
                GetCall getCall=new GetCall();
                joinedStr.getValues().add(getCall.GetFunctionalCall(listObject));
                break;
            case "Constant":
                GenericExpressionParser gepc=new GenericExpressionParser();
                joinedStr.getValues().add(gepc.getConstant(listObject));
                break;

            case "FormattedValue":
                JSONObject formattedValueObj=(JSONObject) listObject.get("value");
                parseValue(formattedValueObj,joinedStr);
                break;
            case "BinOp":
                BinOpParser binOpParser=new BinOpParser();
                joinedStr.getValues().add(binOpParser.getBinOp(listObject));
                break;

            default:
                System.out.println(elementType + " Not implemented in in JoinedStrParser.java at:"+listObject);
                exit(1);
        }



    }



}

