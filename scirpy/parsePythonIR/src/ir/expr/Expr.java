package ir.expr;

import ir.JPExpr;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class Expr extends JPExpr {
    //This can be a common implementation of expression if required...

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFrames not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        if(true)
            throw new NotImplementedException("isDataFrameOp not implemented in " + this.getClass().getSimpleName());
        return false;
    }
}
