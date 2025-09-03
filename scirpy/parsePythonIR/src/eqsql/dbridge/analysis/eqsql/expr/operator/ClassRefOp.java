package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 17/10/16.
 */
public class ClassRefOp extends Operator {

    /** Name of mapped entity */
    private String className;

    public ClassRefOp(String className) {
        super("ClassRef", OpType.ClassRef, 0);
        /* name of class will be stored as attribute of operator, so no operands */
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String toString(){
        return getName() + "(" + className + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClassRefOp that = (ClassRefOp) o;

        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + className.hashCode();
        return result;
    }
}
