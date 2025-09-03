package parse;

import ir.DataType.FloatType;
import ir.DataType.IType;
import ir.DataType.IntType;
import ir.IExpr;
import ir.expr.Num;
import org.json.simple.JSONObject;

public class NumParser {
JSONObject obj;

public NumParser(JSONObject obj) {
        this.obj = obj;
        }
public NumParser() {
        }

    public IExpr parse(){
        return parse(obj);
        }

       public IExpr parse(JSONObject object){
       return getNumber(object);
        }

    //TODO moved to GenericExpressionParser...use from there and modify there
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



        }