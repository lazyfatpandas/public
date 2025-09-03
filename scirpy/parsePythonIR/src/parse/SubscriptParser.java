package parse;

import ir.IExpr;
import ir.expr.Attribute;
import ir.expr.Name;
import ir.expr.Subscript;
import ir.internalast.Slice;
import org.json.simple.JSONObject;

public class SubscriptParser {

    JSONObject block;
    public String baseName = null; // Chiranmoy 8-2-24: df in df[df.fare_amount > 0]

    public SubscriptParser(JSONObject block) {
        this.block = block;
    }
    public SubscriptParser() {
    }

    public IExpr parseSubscript(JSONObject block) {
        this.block = block;
        return parseSubscript();
    }
        public IExpr parseSubscript(){
        JSONObject sliceObj = (JSONObject) block.get("slice");
        JSONObject argsAttributeValueObj = (JSONObject) block.get("value");

        SliceParser sliceParser=new SliceParser();
        Slice slice = sliceParser.getSlice(sliceObj);
                /*
                IExpr attribute=parseAttribute(argsAttributeValueObj);

                 */
        AttributeParser attparser=new AttributeParser();

        IExpr atIexpr = attparser.parseAttribute(argsAttributeValueObj);
        if(atIexpr instanceof Attribute) {
            Attribute at = (Attribute) atIexpr;
            at.setSlice(slice);
            at.setBaseName(baseName = attparser.baseName);
            return at;
        }
        //TODO verify this
//        else if(atIexpr instanceof Name) {
//                return atIexpr;
//            }
        //TODO verify this
        else{
            System.out.println("Special unverified case in Subscript Parser observed " +  atIexpr);
            Subscript sc=new Subscript();
            sc.setValue(atIexpr);
            sc.setSlice(slice);
            sc.setBaseName(baseName = attparser.baseName);
            atIexpr=sc;
            //at.setCol_offset(atIexpr.);
        }
            return atIexpr;

    }

    public JSONObject getBlock() {
        return block;
    }

    public void setBlock(JSONObject block) {
        this.block = block;
    }
}
