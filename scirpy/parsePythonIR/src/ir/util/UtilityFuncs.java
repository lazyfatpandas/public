package ir.util;

import ir.IExpr;
import ir.expr.Attribute;
import ir.expr.Call;
import ir.expr.Name;
import ir.expr.Subscript;

public class UtilityFuncs {
    public static Name getParentnameAttribute(IExpr attriOrCall){
        //Add info for groupby here
        Name dfName=null;
        if(attriOrCall instanceof Call){
            Call call=(Call)attriOrCall;
            IExpr func=call.getFunc();
            return getParentnameAttribute(func);
        }
        if(attriOrCall instanceof Attribute){
            Attribute attribute=(Attribute) attriOrCall;
            if(attribute.getValue() instanceof Name){
                dfName=(Name) (attribute.getValue());
            }
            //3.1 added this condition block
            else if(attribute.getValue() instanceof Call || attribute.getValue() instanceof Attribute){
                return getParentnameAttribute(attribute.getValue());
            }


            else if(attribute.getValue() instanceof Subscript){
                Subscript subscript=(Subscript) (attribute.getValue());
                IExpr subValue=subscript.getValue();
                if(subValue instanceof Name){
                    dfName=(Name)subValue;
                }
                else if(subValue instanceof Call || subValue instanceof Attribute){
                    return getParentnameAttribute(subValue);
                }
            }
        }
        return dfName;
    }
}
