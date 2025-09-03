package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 16/10/16.
 */
public class FieldRefOp extends Operator {

    private String baseClass;
    private String fieldName;
    private String typeClass;

    public FieldRefOp(String baseClass, String fieldName, String typeClass) {
        super("FieldRef", OpType.FieldRef, 0);
        /* Leaf node. baseClass and fieldName will be stored as attributes */
        this.baseClass = baseClass;
        this.fieldName = fieldName;
        this.typeClass = typeClass;
    }

    public String getBaseClass() {
        return baseClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getTypeClass() {
        return typeClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FieldRefOp that = (FieldRefOp) o;

        if (baseClass != null ? !baseClass.equals(that.baseClass) : that.baseClass != null) return false;
        return fieldName != null ? fieldName.equals(that.fieldName) : that.fieldName == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (baseClass != null ? baseClass.hashCode() : 0);
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getName() + "(" + baseClass + "." + fieldName + ")";
    }
}
