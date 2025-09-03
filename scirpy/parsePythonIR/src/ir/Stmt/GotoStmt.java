package ir.Stmt;

import ir.IStmt;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;
import soot.Unit;
import soot.jimple.internal.JGotoStmt;

import java.util.ArrayList;
import java.util.List;

public class GotoStmt extends JGotoStmt implements IStmt {

    public GotoStmt(Unit target) {
        super(target);
    }

    @Override
    public String toString(){
        return null;
    }

    @Override
    public int getLineno() {
        return -1;
    }

    @Override
    public boolean isModified() {
        return false;
    }
    @Override
    public String getOriginalSource() {
        return null;
    }

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        return new ArrayList<>();
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return new ArrayList<>();
    }
}
