package ir.DataType;

public class IntType extends Common implements IType {


    DataType ast_type=DataType.Int;
    public int n;
    public String n_str;

    public IntType(int n, String n_str) {
        this.n = n;
        this.n_str = n_str;
    }

    @Override
    public String toString() {
        return n_str;
    }

    @Override
    public Object clone(){
        return this;
    }
}
