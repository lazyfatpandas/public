package ir.expr;

import ir.IExpr;
import ir.JPExpr;
import soot.Local;

import java.util.List;
import java.util.ArrayList;

public class BoolOp extends JPExpr implements IExpr {

    int lineno;
    int col_offset;
    BoolOpType op;
    List<IExpr> values=new ArrayList<IExpr>();

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public int getCol_offset() {
        return col_offset;
    }

    public void setCol_offset(int col_offset) {
        this.col_offset = col_offset;
    }

    public BoolOpType getOp() {
        return op;
    }

    public void setOp(BoolOpType op) {
        this.op = op;
    }

    public List getValues() {
        return values;
    }

    public void setValues(List values) {
        this.values = values;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Object val:values) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }

        }
        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        for(IExpr expr : values)
            dataframes.addAll(expr.getDataFrames());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        boolean isDF = false;
        for(IExpr expr : values)
            isDF |= expr.isDataFrame();
        return isDF;
    }
}
