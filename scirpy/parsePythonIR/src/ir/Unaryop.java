package ir;

public enum Unaryop {
    //Invert | Not | UAdd | USub
    Invert("~"),
    Not("-"),
    UAdd("+"),
    USub("-");

    private String custom;
    Unaryop(String custom) {
        this.custom = custom;
    }
    public String getSymbol(){
        return custom;
    }
}
