package parse;

import ir.Operator;
import ir.Unaryop;
import ir.expr.BinOp;
import ir.expr.UnaryOp;
import org.json.simple.JSONObject;

public class UnaryOpParser {
    JSONObject block;
    UnaryOp unaryOp;
    JSONObject operandObj,opObj;


    public UnaryOpParser(JSONObject block) {
        this.block = block;
    }
    public UnaryOpParser() {
    }

    public UnaryOp getUnaryOp() {
        parseUnaryOp();
        return unaryOp;
    }
    public UnaryOp getUnaryOp(JSONObject block) {
        this.block=block;
        parseUnaryOp();
        return unaryOp;
    }

    private void parseUnaryOp(){
        unaryOp=new UnaryOp();
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        operandObj=(JSONObject)block.get("operand");

        opObj=(JSONObject)block.get("op");
        String opStr=(String) opObj.get("ast_type");
        unaryOp.setOp(Unaryop.valueOf(opStr));
        //String objType=leftObj.get("ast_type").toString();

        AttributeParser attributeParser=new AttributeParser();
        //TODO some issue with left, taking subscript as type of name...check and rectify
        unaryOp.setOperand(attributeParser.parseAttribute(operandObj));




    }

}
