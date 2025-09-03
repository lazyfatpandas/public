package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.ClassRefOp;
import soot.jimple.ClassConstant;

/**
 * Created by ek on 17/10/16.
 */
public class ClassRefNode extends LeafNode implements HQLTranslatable, SQLTranslatable {
    /**
     * Constructor
     */
    public ClassRefNode(String className) {
        super(new ClassRefOp(className));
    }

    /**
     * Constructor to create a ClassRefNode from soot.ClassConstant
     */
    public ClassRefNode(ClassConstant classConstant){
        this(getClassName(classConstant));
    }

    /**
     * The class name is contained within ClassConstant as a String of the form
     * x/y/z/MyClass for the class MyClass in package x.y.z
     */
    private static String getClassName(ClassConstant classConstant) {
        String[] split = classConstant.getValue().split("/");
        String className = split[split.length - 1];
        return className;
    }

    @Override
    public String toHibQuery() {
        return ((ClassRefOp)operator).getClassName();
    }

    public String getAlias(){
        String className =  ((ClassRefOp)operator).getClassName();
        return ClassToAliasMapper.getAlias(className);
    }

    @Override
    public String toSQLQuery() {
        //Currently, we assume that the name of the associated
        //table is the same as the class name, but with all small
        //case letters.
        return ((ClassRefOp)operator).getClassName().toLowerCase();
    }
}
