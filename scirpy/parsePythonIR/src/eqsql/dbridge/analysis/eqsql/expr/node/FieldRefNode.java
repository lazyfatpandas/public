package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.FieldRefOp;

/**
 * Created by ek on 17/10/16.
 */
public class FieldRefNode extends LeafNode implements HQLTranslatable, SQLTranslatable {

    public FieldRefNode(String baseClass, String fieldName, String typeClass){
        super(new FieldRefOp(baseClass, fieldName, typeClass));
    }

    @Override
    public String toHibQuery() {
        String baseClass = ((FieldRefOp)operator).getBaseClass();
        String fieldName = ((FieldRefOp)operator).getFieldName();
        String classAlias = ClassToAliasMapper.getAlias(baseClass);

        return classAlias + "." + fieldName;
    }



    public ClassRefNode getTypeClassRef() {
        return new ClassRefNode(((FieldRefOp) operator).getTypeClass());
    }

    @Override
    public String toSQLQuery() {
        String baseClass = ((FieldRefOp)operator).getBaseClass();
        String fieldName = ((FieldRefOp)operator).getFieldName();
        String classAlias = ClassToAliasMapper.getAlias(baseClass);

        //Currently, we assume that the column name is the same as the
        //field name, but with all small case letters
        return classAlias.toLowerCase() + "." + fieldName.toLowerCase();
    }
}
