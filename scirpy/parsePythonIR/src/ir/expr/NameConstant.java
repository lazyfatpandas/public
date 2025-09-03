package ir.expr;
/* bhu created on 9/5/20  */


import analysis.PythonScene;
import ir.JPExpr;
import ir.util.CRegion;
import org.jboss.util.NotImplementedException;
import soot.*;
import soot.jimple.ConditionExpr;
import soot.util.Switch;

import java.util.ArrayList;
import java.util.List;

public class NameConstant extends JPExpr implements CRegion, ConditionExpr {
    int lineno;
    int col_offset;
    String value;

    public NameConstant(int lineno, int col_offset, String value) {
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.value = value;
    }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Value getOp1() {
        return null;
    }

    @Override
    public Value getOp2() {
        return null;
    }

    @Override
    public ValueBox getOp1Box() {
        return null;
    }

    @Override
    public ValueBox getOp2Box() {
        return null;
    }

    @Override
    public void setOp1(Value value) {

    }

    @Override
    public void setOp2(Value value) {

    }

    @Override
    public String getSymbol() {
        return null;
    }

//    @Override
//    public List getUseBoxes() {
//        return null;
//    }

    @Override
    public Type getType() {
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
    public Object clone() {
        return null;
    }

    public String toString() {
        return value;
    }

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
