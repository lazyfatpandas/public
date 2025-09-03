package parse;

import ir.DataType.FloatType;
import ir.DataType.IType;
import ir.DataType.IntType;
import ir.IExpr;
import ir.expr.*;
import ir.internalast.Keyword;
import ir.internalast.Slice;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static java.lang.System.exit;

public class GetCall {

    public GetCall() {
    }

    public Call GetFunctionalCall(JSONObject valueObj) {
        //IExpr icall=null;
        Call call=new Call();
        //get args array
        JSONArray argsArray=(JSONArray)valueObj.get("args");
        //read each args in json array, captured as list in call
        argsArray.forEach(args -> parseArgs((JSONObject) args,call));

        //keywords
        JSONArray keywordsArray=(JSONArray)valueObj.get("keywords");
        keywordsArray.forEach(keywords -> parseKeywords((JSONObject) keywords,call));

        //nOW TRYING FUNCTION HERE: can be single name or reference like np.array, marked as attribute
        JSONObject funcObj=(JSONObject)valueObj.get("func");

        String funcType=funcObj.get("ast_type").toString();
        //System.out.println(funcType+ " is the type of func");
        AttributeParser attparser=new AttributeParser();
        IExpr attribute=attparser.parseAttribute(funcObj);
        call.setFunc(attribute);
        call.setLineno(Integer.parseInt(valueObj.get("lineno").toString()));
        call.setBaseName(attparser.baseName);
        return  call;
    }




    private void parseKeywords(JSONObject keywordObj, Call call) {
        KeywordParser keywordParser=new KeywordParser();

        call.getKeywords().add((Keyword)keywordParser.parseKeyword(keywordObj));
        //System.out.println("Keywords not implemented for method at line no:"+call.getLineno());
    }



    private void parseArgs(JSONObject argsObj, Call call) {
        //get type of args
        IExpr arg=null;
        ArgParser argParser=new ArgParser(argsObj);
        arg=argParser.getArg();
        call.getArgs().add(arg);
    }




}
