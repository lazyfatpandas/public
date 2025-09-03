package ir;

import ir.expr.Name;
import org.jboss.util.NotImplementedException;
import soot.UnitPrinter;

import java.util.ArrayList;
import java.util.List;

public class JPStmt extends JPAbstractStmt{

    boolean modified=false;
    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void toString(UnitPrinter unitPrinter) {

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
    public List<Name> getDataFramesDefined() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFramesDefined not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public List<Name> getDataFramesUsed() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFramesUsed not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }
}
