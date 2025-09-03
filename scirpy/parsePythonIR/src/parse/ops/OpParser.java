package parse.ops;

import ast.ExprAST;
import ir.IExpr;
import ir.expr.Call;
import ir.expr.ListComp;
import ir.expr.Name;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import parse.*;

public class OpParser {
    JSONObject opObj;


    public OpParser(JSONObject opObj) {
        this.opObj = opObj;
    }
    public OpParser() {
    }

    public IExpr parseOp() {
        return parseOp(opObj);
    }

    public IExpr parseOp(JSONObject opObj) {
        IExpr expr = null;
        String opType = opObj.get("ast_type").toString();

        switch (opType) {
            case "BoolOp":
                BoolOpParser boolOpParser=new BoolOpParser();
                expr=boolOpParser.parseBoolOp(opObj);
                break;
            case "Compare":
                CompareParser compareParser=new CompareParser(opObj);
                expr=compareParser.getCompare();
                break;
            case "BinOp":
                BinOpParser binOpParser=new BinOpParser(opObj);
                expr=binOpParser.getBinOp();
                break;
            case "Constant":
                GenericExpressionParser gepc=new GenericExpressionParser();
                expr=gepc.getConstant(opObj);
                break;
            //2024 sigmod bhu
//            case "Call":
            case "Call":
                GetCall getCall=new GetCall();
                Call call=getCall.GetFunctionalCall(opObj);
                call.setAst_type(ExprAST.Call);
//                call.setLineno(");
//                call.setCol_offset(col_offset);
                expr=call;
                break;
            default:
                System.out.println(opType +" not implemented in OpParser");
                System.exit(0);
        }
        return expr;
    }
}


