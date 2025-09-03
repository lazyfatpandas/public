package parse;

import ir.IExpr;
import ir.expr.Compare;
import ir.expr.OpsType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CompareParser {
    JSONObject compareBlock;
    Compare compare;

    public CompareParser(JSONObject compareBlock) {
        this.compareBlock = compareBlock;
        compare=new Compare();
    }
    public CompareParser() {
        compare=new Compare();
    }

    public Compare getCompare() {
        parse();
        return compare;
    }

    public void parse(JSONObject compareBlock){
        this.compareBlock=compareBlock;
        parse();

    }
    public void parse(){
        JSONArray comparatorsList=(JSONArray) compareBlock.get("comparators");
        JSONArray opsList=(JSONArray) compareBlock.get("ops");
        JSONObject leftObj=(JSONObject)compareBlock.get("left");
        compare.setLineno(Integer.parseInt(compareBlock.get("lineno").toString()));
        compare.setCol_offset(Integer.parseInt(compareBlock.get("col_offset").toString()));

        int i=0,nos=comparatorsList.size();
        //TODO used argparser here...Move argparser to some generic parser that can be used anywhere...
        ArgParser argParser=new ArgParser();
        argParser.setArgsObj(leftObj);
        compare.setLeft(argParser.getArg());


        for(i=0; i<nos; i++){
            JSONObject comparator=(JSONObject)comparatorsList.get(i);
            argParser.setArgsObj(comparator);
            IExpr arg=argParser.getArg();
            compare.getComparators().add(arg);
        }

        for(i=0; i<nos; i++){
            JSONObject op=(JSONObject)opsList.get(i);
            String opStr=(String) op.get("ast_type");
            compare.getOps().add(OpsType.valueOf(opStr));
        }

    }

}
