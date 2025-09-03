package parse;

import ir.IExpr;
import ir.expr.Expr_Context;
import ir.expr.ListComp;
import ir.expr.Name;
import ir.internalast.Targets;
import org.json.simple.JSONObject;

import static java.lang.System.exit;

public class TargetParser {

    public IExpr parseTarget(JSONObject targetsObj) {
        IExpr iExpr=null;
        /*String typeofTarget=valueObj.get("ast_type").toString();
        switch(typeofValue) {
            case "Name":

            default :
                System.out.println(typeofTarget+ " still not implemented in targets");
                exit(1);
        }
*/
//commented for changes
        /*
        //TODO change this to Name if name is only target else modify target class accordingly
        Targets target=new Targets(Integer.parseInt(targetsObj.get("lineno").toString())
                ,Integer.parseInt(targetsObj.get("col_offset").toString()),targetsObj.get("id").toString());//Targets(int lineno, int col_offset, Name name, String id )

        assignStmt.getTargets().add(target);
*/
        String typeofTarget=targetsObj.get("ast_type").toString();
        switch(typeofTarget) {
            case "id":
                Targets target=new Targets(Integer.parseInt(targetsObj.get("lineno").toString())
                        ,Integer.parseInt(targetsObj.get("col_offset").toString()),targetsObj.get("id").toString());//Targets(int lineno, int col_offset, Name name, String id )

                return target;
            case "Attribute":
                AttributeParser attributeParser=new AttributeParser();
                IExpr attTarget=attributeParser.parseAttribute(targetsObj);
                return attTarget;
            case "Name":
                String id = targetsObj.get("id").toString();
                int lineno=Integer.parseInt(targetsObj.get("lineno").toString());
                int col_offset=Integer.parseInt(targetsObj.get("col_offset").toString());
                return new Name(lineno, col_offset, id, Expr_Context.Load);
            //TODO verify this
            case "Subscript":
                SubscriptParser subscriptParser=new SubscriptParser(targetsObj);
                return subscriptParser.parseSubscript();
            case "Tuple":
                ListParser listParser=new ListParser();
                IExpr listComp=listParser.parseList(targetsObj);
                ListComp lc=(ListComp)listComp;
                lc.setTypeOfList("Tuple");
                return lc;
            default :
                System.out.println(typeofTarget+ " still not implemented in target parser");
                exit(1);
        }
        return iExpr;

    }
}
