package passes.lasp;

import ir.expr.Name;
import rewrite.pd1.Pd1Elt;

import java.util.List;

public class Common {
    public static boolean isExistingDF(Name dfName, List<Pd1Elt> pdLists){
        for(Pd1Elt pd1Elt:pdLists){
//            Name listDfName=(Name) pd1Elt.getDfName();
            if(pd1Elt.getDfName().equals(dfName.id)){
                return true;
                //pd1Elt.getDropCols().add("test");
            }
        }
        //TODO rectify this
        return false;
    }
}
