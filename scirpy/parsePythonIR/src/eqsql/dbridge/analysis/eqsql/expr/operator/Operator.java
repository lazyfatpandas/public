package eqsql.dbridge.analysis.eqsql.expr.operator;

import eqsql.EqSQLConfig;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ek on 23/5/16.
 */
public class Operator {
    protected String name;
    protected OpType type;
    protected int arity;
    /** The statement used to create this expression. Can be null for nodes that are
     * created as a result of transformations.
     */
    private List<Stmt> stmts;

    /**
     * Package local constructor. For use within OpType enum
     */
    Operator(){
    }

    public Operator(String name, OpType type, int arity) {
        this.name = name;
        this.type = type;
        this.arity = arity;
        this.stmts = new ArrayList<>();
    }

    public List<Stmt> getStmts() {
        return stmts;
    }

    public void addStmts(Stmt... stmts) {
        this.stmts.addAll(Arrays.asList(stmts));
    }

    public void setStmts(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    public String getName() {
        return name;
    }

    public OpType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operator operator = (Operator) o;

        return arity == operator.arity && type == operator.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + arity;
        return result;
    }

    @Override
    public String toString() {
        String str = name;
        if(!stmts.isEmpty()){
            str += " (" + stmts.toString() + ")";
        }
        return str;
    }

    public int getArity() {
        return arity;
    }

    public void print(){
        EqSQLConfig.getLogger().info(toString());
    }

    public boolean isLeafOp(){
        return arity == 0;
    }
}
