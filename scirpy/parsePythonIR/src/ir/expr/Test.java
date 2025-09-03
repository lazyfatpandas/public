package ir.expr;

import ir.IExpr;
import org.jboss.util.NotImplementedException;
import soot.Local;
import soot.Type;
import soot.UnitPrinter;
import soot.util.Switch;

import java.util.ArrayList;
import java.util.List;

public class Test  implements IExpr {

    int col_offset;
    int lineno;
    //List<IExpr> values;
    String ast_type;
    IExpr testExpr;





    @Override
    public List getUseBoxes() {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void toString(UnitPrinter unitPrinter) {

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
    public void apply(Switch aSwitch) {

    }

    @Override
    public List<Local> getLocals() {
        return testExpr.getLocals();
    }


    // Chiranmoy 8-2-24, get dataframes in this expr/stmt
    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        if(dataframes.isEmpty())
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
