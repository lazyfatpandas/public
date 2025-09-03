package ir.expr;

public enum OpsType {
    Gt(">"),
    Lt("<"),
    NotEq("!="),
    Eq("=="),
    LtE("<="),
    In("in"),
    GtE(">=");

    private String custom;
    OpsType(String custom) {
        this.custom = custom;
    }
    public String getSymbol(){
        return custom;
    }

}
