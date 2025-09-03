package parse;
/* bhu created on 19/5/20  */


import ir.IExpr;
import ir.expr.Dict;
import ir.expr.ListComp;
import ir.expr.Name;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DictParser {

    JSONObject obj;

    public DictParser(JSONObject obj) {
        this.obj = obj;
    }
    public DictParser() {
    }

    public IExpr parseDict() {
        return parseDict(obj);
    }
    public IExpr parseDict(JSONObject obj) {
        IExpr expr=null;
        Dict dict=new Dict();

        int lineno=Integer.parseInt(obj.get("lineno").toString());
        int col_offset=Integer.parseInt(obj.get("col_offset").toString());
        JSONArray keysArray=(JSONArray)obj.get("keys");
        JSONArray valuesArray=(JSONArray)obj.get("values");
        dict.setCol_offset(col_offset);
        dict.setLineno(lineno);
        AttributeParser ap=new AttributeParser();
        for( Object key:keysArray){
            JSONObject keyJSON=(JSONObject)key;
            dict.getKeys().add(ap.parseAttribute(keyJSON));
        }
        for( Object value:valuesArray){
            JSONObject valueJSON=(JSONObject)value;
            dict.getValue().add(ap.parseAttribute(valueJSON));
        }
        return dict;
        //keysArray.forEach(keywords -> parseElt((JSONObject) keywords,dict));
        //should never reach here
        //return expr;
    }
}
