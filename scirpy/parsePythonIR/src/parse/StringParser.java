package parse;

import ir.expr.Str;
import org.json.simple.JSONObject;

public class StringParser {
    JSONObject obj;

    public StringParser(JSONObject obj) {
        this.obj = obj;
    }
    public StringParser() {
    }

    public Str parse(){
        return parse(obj);
    }
    public Str parse(JSONObject object){
        Str str;
        int lineno=Integer.parseInt(object.get("lineno").toString());
        int col_offset=Integer.parseInt(object.get("col_offset").toString());
        String s = object.get("s").toString();
        str = new Str(lineno, col_offset, s);
        return str;
    }
}
