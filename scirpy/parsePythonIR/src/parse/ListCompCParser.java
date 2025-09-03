package parse;
/* created on 10/5/20  */


import ir.IExpr;
import ir.expr.ListComp;
import ir.expr.ListCompCmplx;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ListCompCParser {
    JSONObject obj;

    public ListCompCParser(JSONObject obj) {
        this.obj = obj;
    }
    public ListCompCParser() {
    }


    public IExpr parseList(JSONObject listObj) {
        ListCompCmplx lcce=null;
        lcce=new ListCompCmplx();

        int lineno=Integer.parseInt(listObj.get("lineno").toString());
        int col_offset=Integer.parseInt(listObj.get("col_offset").toString());
        lcce.setLineno(lineno);
        lcce.setCol_offset(col_offset);

        AttributeParser attparser=new AttributeParser();

        //set elt
        JSONObject obj=(JSONObject)listObj.get("elt");
        lcce.setElt(attparser.parseAttribute(obj));

        //set comprehensions
        JSONArray objCArr=(JSONArray)listObj.get("generators");
        ComprehensionParser cmpParser=new ComprehensionParser();

        if(objCArr!=null){
            for(Object obj2:objCArr){
                lcce.getComprehensions().add(cmpParser.parseComprehension((JSONObject)obj2));
            }
        }


        return lcce;
    }


    public JSONObject getObj() {
        return obj;
    }

    public void setObj(JSONObject obj) {
        this.obj = obj;
    }
}
