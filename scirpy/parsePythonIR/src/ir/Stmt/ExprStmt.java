package ir.Stmt;

import ir.IExpr;
import ir.JPAbstractStmt;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;
import soot.Local;
import soot.Type;

import java.util.ArrayList;
import java.util.List;

public abstract class ExprStmt extends JPAbstractStmt implements IExpr{

    boolean modified=false;
    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean equivTo(Object o) {
        return false;
    }

    @Override
    public int equivHashCode() {
        return 0;
    }

    @Override
    public int getLineno() {
        return lineno;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    String sourceCode="";
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    @Override
    public String getOriginalSource() {
        return sourceCode;
    }
    @Override
    public Object clone() {
    return this;
    }

    @Override
    public List<Local> getLocals() {
        return new ArrayList<>();
    }
}
