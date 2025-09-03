package parse;

import ir.Op;
import ir.Operator;
import ir.expr.BinOp;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BinOpParser {

    JSONObject block;
    BinOp binOp;
    JSONObject leftObj,rightObj,opObj;


    public BinOpParser(JSONObject block) {
        this.block = block;
    }
    public BinOpParser() {
    }

    public BinOp getBinOp() {
        parseBinOp();
        return binOp;
    }
    public BinOp getBinOp(JSONObject block) {
        this.block=block;
        parseBinOp();
        return binOp;
    }

   private void parseBinOp(){
       binOp=new BinOp();
       int lineno=Integer.parseInt(block.get("lineno").toString());
       int col_offset=Integer.parseInt(block.get("col_offset").toString());
       leftObj=(JSONObject)block.get("left");
       rightObj=(JSONObject)block.get("right");
       opObj=(JSONObject)block.get("op");
       String opStr=(String) opObj.get("ast_type");
       binOp.setOp(Operator.valueOf(opStr));
       //String objType=leftObj.get("ast_type").toString();

       AttributeParser attributeParser=new AttributeParser();
       //TODO some issue with left, taking subscript as type of name...check and rectify
       binOp.setLeft(attributeParser.parseAttribute(leftObj));

       binOp.setRight(attributeParser.parseAttribute(rightObj));



   }

}
