package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.VarOp;
import soot.jimple.internal.JimpleLocal;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ek on 18/10/16.
 * Representation of a variable in the DIR.
 */
public class VarNode extends LeafNode implements Comparable<VarNode>, HQLTranslatable, SQLTranslatable {
    protected static final String COND_VAR_NAME = "condition";
    protected static final String UPDATE_VAR_NAME = "update";
    protected static final String RETURN_VAR_NAME = "return";

    private JimpleLocal jimpleVar;
    /** A custom variable for special purposes such as "condition", "return" etc. */
    private String specialVar;

    /* Intentionally package local. This constructor can be accessed indirectly through NodeFactory.v() */
    VarNode(JimpleLocal _var) {
        super(new VarOp());
        jimpleVar = _var;
        specialVar = null;
    }

    /**
     * Constructor to be used for special purpose variables, such as
     * "condition" and "return". Do not use this if you do not know
     * what it means.
     */
    public VarNode(String varStr) {
        super(new VarOp());
        jimpleVar = null;
        specialVar = varStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VarNode varNode = (VarNode) o;

        if (jimpleVar != null ? !jimpleVar.equals(varNode.jimpleVar) : varNode.jimpleVar != null) return false;
        return specialVar != null ? specialVar.equals(varNode.specialVar) : varNode.specialVar == null;
    }

    @Override
    public int hashCode() {
        int result = jimpleVar != null ? jimpleVar.hashCode() : 0;
        result = 31 * result + (specialVar != null ? specialVar.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if(jimpleVar != null){
            return jimpleVar.toString();
        }
        return specialVar;
    }

    /**
     * Note that a new VarNode is created and returned in each invocation.
     * //TODO Can we use a single instance of a condition VarNode?
     */
    public static VarNode getACondVar() {
        return new VarNode(COND_VAR_NAME);
    }

    public static VarNode getUpdateVar(){
        return new VarNode(UPDATE_VAR_NAME);
    }

    public boolean isJimpleVar(){
        return jimpleVar != null;
    }

    private String getVarName(){
        if(this.isJimpleVar()){
            return jimpleVar.getName();
        }
        return specialVar;
    }

    /**
     * Base case for readSet()
     */
    @Override
    public Set<VarNode> readSet() {
        Set<VarNode> rs = new HashSet<>();
        if(this.isJimpleVar()){
            rs.add(this);
        }
        return rs;
    }

    /**
     * This method is only so that the VarNode keys of DIR can be sorted and printed
     * in an order. No other significance.
     */
    @Override
    public int compareTo(VarNode varNode) {
        return this.getVarName().compareTo(varNode.getVarName());
    }

    @Override
    public String toHibQuery() {
        return toString();
    }

    @Override
    public String toSQLQuery() {
        return toString();
    }
}
