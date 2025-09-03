package parse;

import ir.DataType.FloatType;
import ir.DataType.IType;
import ir.DataType.IntType;
import ir.IExpr;
import ir.expr.*;
import org.json.simple.JSONObject;

public class GenericExpressionParser {

    public IExpr getName(JSONObject block){
        int col_offset = Integer.parseInt(block.get("col_offset").toString());
        int lineno = Integer.parseInt(block.get("lineno").toString());
        String id = block.get("id").toString();
        //TODO verify whether context is always load here ---
        Name name = new Name(lineno, col_offset, id, Expr_Context.Load);
        return name;
    }

    public IExpr getNumber(JSONObject argsObj) {
        IExpr arg=null;
        int col_offset=Integer.parseInt(argsObj.get("col_offset").toString());
        int lineno=Integer.parseInt(argsObj.get("lineno").toString());
        IType data;
        JSONObject nObj=(JSONObject)argsObj.get("n");
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
        //changed from above i.e. main code block
        arg=new Num(lineno,col_offset,data);
        return arg;

    }
    public IExpr getStr(JSONObject block){
        int col_offset = Integer.parseInt(block.get("col_offset").toString());
        int lineno = Integer.parseInt(block.get("lineno").toString());
        String s = block.get("s").toString();
        Str str = new Str(lineno, col_offset, s);
        return str;
    }

    public IExpr getNameConstant(JSONObject block){
        int col_offset = Integer.parseInt(block.get("col_offset").toString());
        int lineno = Integer.parseInt(block.get("lineno").toString());
        String value = block.get("value").toString();
        NameConstant nc = new NameConstant(lineno, col_offset, value);
        return nc;
    }

    //TODO use this generic version everywhere
    public IExpr getExpr(JSONObject block){
        int col_offset = Integer.parseInt(block.get("col_offset").toString());
        int lineno = Integer.parseInt(block.get("lineno").toString());
        String value = block.get("value").toString();
        NameConstant nc = new NameConstant(lineno, col_offset, value);
        return nc;
    }

    public IExpr getConstant(JSONObject block){
        int col_offset = Integer.parseInt(block.get("col_offset").toString());
        int lineno = Integer.parseInt(block.get("lineno").toString());
        String value=null;
        //4.01 added support for constant as None --read as null by parser
        if(block.get("value")!=null) {
            value = block.get("value").toString();
        }
        else{
            value="None";
        }
        //try begin
        Constant c=null;
        Object valObj=block.get("value");
        if(valObj instanceof java.lang.Number){
            c = new Constant(lineno, col_offset, value,"Num");
            return c;
        }
        else if(valObj instanceof String){
            if(value.equals("\n")){
                value="\\n";
            }
            c = new Constant(lineno, col_offset, value,"String");
            return c;
        }
        else if(valObj instanceof java.lang.Boolean){
            c = new Constant(lineno, col_offset, value,"Boolean");
            return c;
        }

        //try end
        c = new Constant(lineno, col_offset, value,"Other");
        return c;
    }



}
