package ir.DataType;

public class FloatType extends Common implements IType {


    //TODO used double for precision
    public double n;
    DataType ast_type=DataType.Float;
    public FloatType(double n) {
        this.n = n;
    }

    @Override
    public String toString() {
        return String.valueOf(n);
    }
    @Override
    public Object clone(){
        return this;
    }
}
