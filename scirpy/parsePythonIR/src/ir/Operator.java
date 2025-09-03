package ir;


public enum Operator {
    // operator = Add | Sub | Mult | MatMult | Div | Mod | Pow | LShift
    //                 | RShift | BitOr | BitXor | BitAnd | FloorDiv
    Add ("+"),
    Sub ("-"),
    Mult ("*"),
    MatMult ("*"),
    Div ("/"),
    Mod("%"),
    Pow("**"),
    LShift("<<"),
    RShift(">>"),
    BitOr("|"),
    BitXor("^"),
    BitAnd("&"),
    FloorDiv("//");

    private String custom;
    Operator(String custom) {
        this.custom = custom;
    }
    public String getSymbol(){
    return custom;
    }
}
