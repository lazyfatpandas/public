package parse.ops;

import ast.ExprAST;
import ir.IExpr;
import ir.expr.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import parse.CompareParser;

import static java.lang.System.exit;

public class BoolOpParser{

        public IExpr parseBoolOp(JSONObject boolOpObj) {
            IExpr expr=null;
            BoolOp boolOp=new BoolOp();
            int lineno=Integer.parseInt(boolOpObj.get("lineno").toString());
            int col_offset=Integer.parseInt(boolOpObj.get("col_offset").toString());

            String op=(((JSONObject)boolOpObj.get("op")).get("ast_type")).toString();
            boolOp.setCol_offset(col_offset);
            boolOp.setLineno(lineno);
            boolOp.setOp(BoolOpType.valueOf(op));
            JSONArray values=(JSONArray)boolOpObj.get("values");
            int i=0, nos=values.size();

            for(i=0; i<nos; i++){
                JSONObject obj=(JSONObject)values.get(i);
                String objType=(String) obj.get("ast_type");
                switch (objType) {
                    case "Compare":
                        CompareParser compareParser=new CompareParser(obj);
                        boolOp.getValues().add(compareParser.getCompare());
                        break;
                    case "BoolOp":
                        BoolOpParser boolOpParser=new BoolOpParser();
                        boolOp.getValues().add(boolOpParser.parseBoolOp(obj));
                        break;

                }


            }
            return boolOp;
            //should never reach here
           // return boolOp;
        }
}
