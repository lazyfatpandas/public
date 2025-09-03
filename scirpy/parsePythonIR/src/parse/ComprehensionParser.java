package parse;
/* bhu created on 10/5/20  */


import ir.IExpr;
import ir.expr.ListCompCmplx;
import ir.internalast.Comprehension;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ComprehensionParser {
    JSONObject obj;

    public ComprehensionParser(JSONObject obj) {
        this.obj = obj;
    }

    public ComprehensionParser() {
    }

    public Comprehension parseComprehension(JSONObject obj) {
        //IExpr expr=null;
        Comprehension cmpr=new Comprehension();
        int is_async=Integer.parseInt(obj.get("is_async").toString());
        cmpr.setIs_async(is_async);

        AttributeParser attparser=new AttributeParser();

        //set Target of comprehension
        JSONObject objC=(JSONObject)obj.get("target");
        cmpr.setTarget(attparser.parseAttribute(objC));

        //set iter
        objC=(JSONObject)obj.get("iter");
        cmpr.setIter(attparser.parseAttribute(objC));

        //set ifs(all the conditions)
        JSONArray objCArr=(JSONArray)obj.get("ifs");

        if(objCArr!=null){
            for(Object obj2:objCArr){
                cmpr.getIfs().add(attparser.parseAttribute((JSONObject) obj2));
            }
        }
        return cmpr;
    }

    public JSONObject getObj() {
        return obj;
    }

    public void setObj(JSONObject obj) {
        this.obj = obj;
    }
}
