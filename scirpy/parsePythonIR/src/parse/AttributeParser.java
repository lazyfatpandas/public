package parse;

import ast.ExprAST;
import ir.IExpr;
import ir.expr.*;
import org.json.simple.JSONObject;

import static java.lang.System.exit;

public class AttributeParser {
    public String baseName = null;

    public IExpr parseAttribute(JSONObject attributeObj) {
        int lineno=0,col_offset=0;
        IExpr expr=null;
        Attribute value=null;
        Name name=null;
        if(attributeObj.get("lineno")!=null) {
            lineno = Integer.parseInt(attributeObj.get("lineno").toString());
            col_offset = Integer.parseInt(attributeObj.get("col_offset").toString());
        }
        String attributeType=attributeObj.get("ast_type").toString();
        if(attributeType.equals("Attribute")) {
                /*
                ExprAST ast_type=ExprAST.Attribute;
                String attr;
                int col_offset;
                int lineno;
                Expr_Context ctx;
                IExpr value;
                             */
            //TODO assumed copntext to be always load here
            value = new Attribute();
            value.setAst_type(ExprAST.Attribute);
            value.setAttr(attributeObj.get("attr").toString());
            value.setCol_offset(col_offset);
            value.setLineno(lineno);
            //recursively call to make all the nested attributes
            value.setValue(parseAttribute((JSONObject) attributeObj.get("value")));
            value.setBaseName(baseName);
            return value;
        }//value.setCtx();
        else if(attributeType.equals("Name")) {
            // Chiranmoy 8-2-24: for statements like pd.read_csv(...), pd.merge(...)
            // Since the base (pd in pd.read_csv) is a Name this is bound to be the endpoint
            String id = attributeObj.get("id").toString();
            name = new Name(lineno, col_offset, id, Expr_Context.Load);
            baseName = name.getName();
            return name;
        }
        else if(attributeType.equals("Subscript")) {
            SubscriptParser subscriptParser=new SubscriptParser(attributeObj);
            IExpr subscript = subscriptParser.parseSubscript();
            baseName = subscriptParser.baseName;
            return subscript;
        }
        else if(attributeType.equals("BinOp")) {
            BinOpParser binOpParser=new BinOpParser(attributeObj);
            return binOpParser.getBinOp();
        }
        else if(attributeType.equals("UnaryOp")) {
            UnaryOpParser unaryOpParser = new UnaryOpParser(attributeObj);
            return unaryOpParser.getUnaryOp();
        }
        else if(attributeType.equals("Call")){
            //TODO FINISH THIS
            //Changes on 07/05/20 for lc in for loop
            GetCall getCall=new GetCall();
            //Call call=GetFunctionalCall(valueObj);
            Call call=getCall.GetFunctionalCall(attributeObj);
            call.setAst_type(ExprAST.Call);
            call.setLineno(lineno);
            call.setCol_offset(col_offset);
            baseName = call.getBaseName();
            return call;
        }
        else if(attributeType.equals("Num")){
            //Changes on 07/05/20 for lc in for loop
            //Call call=GetFunctionalCall(valueObj);
            GenericExpressionParser gep=new GenericExpressionParser();
            return gep.getNumber(attributeObj);
        }
        else if(attributeType.equals("Str")){
            //Changes on 07/05/20 for lc in for loop
            //Call call=GetFunctionalCall(valueObj);
            GenericExpressionParser gep=new GenericExpressionParser();
            return gep.getStr(attributeObj);
        }
        else if(attributeType.equals("Constant")){
            //Changes on 07/05/20 for lc in for loop
            //Call call=GetFunctionalCall(valueObj);
            GenericExpressionParser gep=new GenericExpressionParser();
            return gep.getConstant(attributeObj);
        }
        else if(attributeType.equals("IfExp")){
            //Changes on 07/05/20 for lc in for loop
            //Call call=GetFunctionalCall(valueObj);
            GenericExpressionParser gep=new GenericExpressionParser();
            System.out.println(attributeType+ " still Not implemented in func for functional call on line no="+lineno);
            exit(1);
        }
        else if(attributeType.equals("List")){
            ListParser listParser=new ListParser();
             return listParser.parseList(attributeObj);
        }
        else if(attributeType.equals("Tuple")){
            ListParser listParser=new ListParser();
            //listComp=listParser.parseList(valueObj);
            ListComp lc=(ListComp)listParser.parseList(attributeObj);
            lc.setTypeOfList("Tuple");
            return lc;
        }
        else if(attributeType.equals("NameConstant")){
            GenericExpressionParser gep=new GenericExpressionParser();
            return gep.getNameConstant(attributeObj);
        }
        else if(attributeType.equals("Constant")) {
            GenericExpressionParser gepc = new GenericExpressionParser();
            return gepc.getConstant(attributeObj);
        }
        else if(attributeType.equals("Compare")){
            CompareParser compareParser=new CompareParser(attributeObj);
            return compareParser.getCompare();
            //return gep.getNameConstant();
        }
        else if(attributeType.equals("Dict")){
            DictParser dictParser=new DictParser(attributeObj);
            return dictParser.parseDict();
            //return gep.getNameConstant();
        }
        else{
            System.out.println(attributeType+ " still Not implemented in func for functional call on line no="+lineno);
            exit(1);
        }
        //should never reach here
        return expr;
    }
}
