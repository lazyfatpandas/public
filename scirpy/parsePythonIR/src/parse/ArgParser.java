package parse;

import ast.ExprAST;
import ir.DataType.FloatType;
import ir.DataType.IType;
import ir.DataType.IntType;
import ir.IExpr;
import ir.expr.*;
import ir.internalast.Slice;
import org.json.simple.JSONObject;

import static java.lang.System.exit;

public class ArgParser {
    JSONObject argsObj;

    public ArgParser(JSONObject argsObj) {
        this.argsObj = argsObj;
    }
    public ArgParser() {

    }
    public IExpr getArg(JSONObject argsObj) {
        this.argsObj = argsObj;
        return getArg();
    }


    public IExpr getArg() {
        NumParser numParser=new NumParser();
        AttributeParser attparser;
        IExpr arg = null;
        String argsType = argsObj.get("ast_type").toString();
        int col_offset = Integer.parseInt(argsObj.get("col_offset").toString());
        int lineno = Integer.parseInt(argsObj.get("lineno").toString());
        switch (argsType) {

            case "Num":

                arg = numParser.getNumber(argsObj);
                break;
            case "Str":
                String s = argsObj.get("s").toString();
                arg = new Str(lineno, col_offset, s);

                break;
            case "Name":
                String id = argsObj.get("id").toString();
                //TODO verify whether context is always load here ---
                arg = new Name(lineno, col_offset, id, Expr_Context.Load);

                break;
            //TODO this is for function call with subscripted args
            case "Subscript":
                //TODO what if it is a function call as attribute..write code for that
                JSONObject sliceObj = (JSONObject) argsObj.get("slice");
                JSONObject argsAttributeValueObj = (JSONObject) argsObj.get("value");
                Slice slice = getSlice(sliceObj);
                /*
                IExpr attribute=parseAttribute(argsAttributeValueObj);

                 */
                attparser=new AttributeParser();

                arg = attparser.parseAttribute(argsAttributeValueObj);
                if(arg instanceof  Attribute) {
                    Attribute at = (Attribute) arg;
                    at.setSlice(slice);
                    arg=at;
                }
                //TODO tvm 1, verify for correctness
                else{
                    Subscript sc=new Subscript();
                    sc.setValue(arg);
                    sc.setSlice(slice);
                    arg=sc;
                }
                //whether this was required here????

                //call.getArgs().add(attribute)
                break;
            case "Attribute":
                attparser=new AttributeParser();
                arg = attparser.parseAttribute(argsObj);
                //call.getArgs().add(attribute)
                break;
                //TODO check this arg..this is not name, but it should be mapped to the place from where it is being called...
                //Putting as string is a quickfix, but is factually wrong.
            case "arg":
                s = argsObj.get("arg").toString();
                arg = new Str(lineno, col_offset, s);

                break;
            case "BinOp":
                BinOpParser binOpParser=new BinOpParser(argsObj);
                arg=binOpParser.getBinOp();
                break;

            case "NameConstant":
                GenericExpressionParser gep=new GenericExpressionParser();
                arg=gep.getNameConstant(argsObj);
                break;

            case "Constant":
                GenericExpressionParser gepc=new GenericExpressionParser();
                arg=gepc.getConstant(argsObj);
                break;

            case "List":
                ListParser listParser=new ListParser();
                arg=listParser.parseList(argsObj);
                break;

            case "Call":
                //ListComp listComp=new ListComp();
                GetCall getCall=new GetCall();
                Call call=getCall.GetFunctionalCall(argsObj);
                call.setAst_type(ExprAST.Call);
                call.setLineno(lineno);
                call.setCol_offset(col_offset);
                arg=call;
                break;

            case "UnaryOp":
                UnaryOpParser unaryOpParser = new UnaryOpParser(argsObj);
                arg= unaryOpParser.getUnaryOp();
                break;


            case "ListComp":
                //ListComp listComp=new ListComp();
                ListCompCParser listCompCParser=new ListCompCParser();
                arg=listCompCParser.parseList(argsObj);
                break;

            case "Tuple":
                listParser=new ListParser();
                IExpr listComp=listParser.parseList(argsObj);
                ListComp lc=(ListComp)listComp;
                lc.setTypeOfList("Tuple");
                arg= lc;
                break;
            case "Lambda":
                LambdaParser lambdaParser=new LambdaParser();
                IExpr lambdaExpression=lambdaParser.parseLambda(argsObj);
                System.out.println(argsType + " still Not implemented in Arguements for a function at line:"+lineno);
                arg= lambdaExpression;
                break;
            case "JoinedStr":
                JoinedStrParser joinedStrParser=new JoinedStrParser();
                arg=joinedStrParser.parseJoinedStr(argsObj);
                break;
            case "Compare":
                //TODO verify this example 6
                CompareParser compareParser=new CompareParser(argsObj);
                IExpr compare=compareParser.getCompare();
                arg =compare;
                break;
            case "Dict":
                //TODO verify this example 6
                DictParser dictParser=new DictParser(argsObj);
                IExpr dict= dictParser.parseDict();
                arg =dict;
                break;
            default:
                System.out.println(argsType + " still Not implemented in Arguements for a function at line:"+lineno);
                exit(1);

                // Statements
        }

        return arg;

    }





    private Slice getSlice(JSONObject sliceObj) {
        //3.01 change
//        NumParser numParser=new NumParser();
//        Slice slice=new Slice();
//        JSONObject lowerObj=(JSONObject)sliceObj.get("lower");
//        JSONObject stepObj=(JSONObject)sliceObj.get("step");
//        JSONObject upperObj=(JSONObject)sliceObj.get("upper");
//        JSONObject valueObj=(JSONObject)sliceObj.get("value");
//        if(lowerObj!=null){
//            slice.setLower(numParser.getNumber(lowerObj));
//        }
//        if(stepObj!=null){
//            slice.setStep(numParser.getNumber(stepObj));
//        }
//        if(upperObj!=null){
//            slice.setUpper(numParser.getNumber(upperObj));
//        }
//        if(valueObj!=null){
//            ArgParser ap=new ArgParser(valueObj);
//            slice.setValue(ap.getArg());
//        }
//
//        return slice;
        SliceParser sliceParser=new SliceParser();
        Slice slice = sliceParser.getSlice(sliceObj);
        return slice;
    }

    public JSONObject getArgsObj() {
        return argsObj;
    }

    public void setArgsObj(JSONObject argsObj) {
        this.argsObj = argsObj;
    }
}
