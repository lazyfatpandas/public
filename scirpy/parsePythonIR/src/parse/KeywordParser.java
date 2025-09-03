package parse;

import ir.IExpr;
import ir.JPExpr;
import ir.internalast.Keyword;
import org.json.simple.JSONObject;

public class KeywordParser {
    JSONObject block;
    Keyword keyword;

    public IExpr parseKeyword(JSONObject block) {
        keyword=new Keyword();
        keyword.setArg(block.get("arg").toString());
        JSONObject value=(JSONObject)block.get("value");
        AttributeParser attributeParser=new AttributeParser();
        keyword.setValue(attributeParser.parseAttribute(value));
        return keyword;
    }

    }
