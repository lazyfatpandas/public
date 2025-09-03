package parse;

import ast.ExprAST;
import ir.IExpr;
import ir.expr.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static java.lang.System.exit;

public class ListParser {
    JSONObject listObj;


    public ListParser(JSONObject listObj) {
        this.listObj = listObj;
    }
    public ListParser() {

    }
    public IExpr parseList() {
        return parseList(listObj);
    }

    public IExpr parseList(JSONObject listObj) {
        IExpr expr=null;
        ListComp listComp=new ListComp();
        Name name=null;

        int lineno=Integer.parseInt(listObj.get("lineno").toString());
        int col_offset=Integer.parseInt(listObj.get("col_offset").toString());
        JSONArray eltsArray=(JSONArray)listObj.get("elts");
        listComp.setCol_offset(col_offset);
        listComp.setLineno(lineno);
        eltsArray.forEach(keywords -> parseElt((JSONObject) keywords,listComp));
        //should never reach here
        return listComp;
    }

    private void parseElt(JSONObject listObject, ListComp listComp){
        String elementType = listObject.get("ast_type").toString();
        switch (elementType){
            case "Str":
                StringParser stringParser=new StringParser();
                listComp.getElts().add(stringParser.parse(listObject));
                break;
            case "Num":
                NumParser numParser=new NumParser();
                listComp.getElts().add(numParser.parse(listObject));
                break;
            case "Name":
                GenericExpressionParser gep=new GenericExpressionParser();
                listComp.getElts().add(gep.getName(listObject));
                break;
            case "UnaryOp":
                UnaryOpParser unaryOpParser=new UnaryOpParser(listObject);
                listComp.getElts().add(unaryOpParser.getUnaryOp());
                break;
            case "Subscript":
                SubscriptParser subscriptParser=new SubscriptParser(listObject);
                listComp.getElts().add(subscriptParser.parseSubscript());
                break;
            case "Attribute":
                AttributeParser aP=new AttributeParser();
                listComp.getElts().add(aP.parseAttribute(listObject));
                break;
            case "Call":
                GetCall getCall=new GetCall();
                listComp.getElts().add(getCall.GetFunctionalCall(listObject));
                break;
            case "BinOp":
                BinOpParser binOpParser=new BinOpParser(listObject);
                listComp.getElts().add(binOpParser.getBinOp());
                break;

            case "Constant":
                GenericExpressionParser gepc=new GenericExpressionParser();
                listComp.getElts().add(gepc.getConstant(listObject));
                break;
                //bhu changes ADDED ON 31/08/2021
            case "Tuple":
                ListParser listParser=new ListParser();
                ListComp lc=(ListComp)listParser.parseList(listObject);
                lc.setTypeOfList("Tuple");
                listComp.getElts().add(lc);
                break;
            case "Compare":
                CompareParser compareParser=new CompareParser(listObject);
                IExpr compare=compareParser.getCompare();
                listComp.getElts().add(compare);
                break;
            case "Dict":
                DictParser dictParser=new DictParser(listObject);
                listComp.getElts().add(dictParser.parseDict());
                break;
            default:
                System.out.println(elementType + " Not implemented in List parsing in ListParser.java");
                exit(1);
        }



    }



}
