package parse;

import ir.internalast.Slice;
import org.json.simple.JSONObject;

public class SliceParser {

    public Slice getSlice(JSONObject sliceObj) {
        NumParser numParser=new NumParser();
        Slice slice=new Slice();
        JSONObject lowerObj = null,stepObj=null,upperObj=null,indexObj=null,listObj=null;

        try {
             lowerObj = (JSONObject) sliceObj.get("lower");
             stepObj = (JSONObject) sliceObj.get("step");
             upperObj = (JSONObject) sliceObj.get("upper");
             if(sliceObj.get("ast_type").toString().equals("List")) {
                 listObj = sliceObj;
             }
               //3.2
//             if(!(sliceObj.get("value") instanceof String)) {
               if(!(sliceObj.get("value") instanceof String) && !(sliceObj.get("value") instanceof Long)) {
                     indexObj = (JSONObject) sliceObj.get("value");
                 }
             }

        catch (Exception e){
            System.out.println("JSON error, probably python 3.9 or later AST");
        }
        AttributeParser ap=new AttributeParser();
        if(lowerObj!=null){
            //slice.setLower(numParser.getNumber(lowerObj));
            slice.setLower(ap.parseAttribute(lowerObj));
        }
        if(stepObj!=null){
            //slice.setStep(numParser.getNumber(stepObj));
            slice.setStep(ap.parseAttribute(stepObj));
        }
        if(upperObj!=null){
            //slice.setUpper(numParser.getNumber(upperObj));
            slice.setUpper(ap.parseAttribute(upperObj));
        }
        if(indexObj!=null){

            slice.setIndex(ap.parseAttribute(indexObj));
        }
        //3.01 not needed
//        if(listObj!=null){
//            slice.setListComp(ap.parseAttribute(sliceObj));
//        }
        //2.01 lasp for python 3.9
        //if(indexObj==null && ((String)sliceObj.get("ast_type")).equals("Constant")){
        if(indexObj==null && lowerObj==null && upperObj==null && stepObj==null){
            slice.setIndex(ap.parseAttribute(sliceObj));
        }

        return slice;
    }
}
