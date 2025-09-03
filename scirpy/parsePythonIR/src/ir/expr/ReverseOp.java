package ir.expr;
//THIS Should not be used any more..changed implemenatation...keeping the code for future reference
public class ReverseOp {

    /*
    Gt,
    Lt,
    NotEq,
    Eq,
    LtE,
    GtE
     */
    public static OpsType getReverseOp(OpsType opsType) {
        OpsType reverseOpsType=null;
        switch (opsType){
            case Gt:
                reverseOpsType= OpsType.LtE;
                break;
            case Lt:
                reverseOpsType= OpsType.GtE;
                break;
            case NotEq:
                reverseOpsType= OpsType.Eq;
                break;
            case LtE:
                reverseOpsType= OpsType.Gt;
                break;
            case GtE:
                reverseOpsType= OpsType.Lt;
                break;
            case Eq:
                reverseOpsType= OpsType.NotEq;
                break;
        }
        return reverseOpsType;
    }
}
