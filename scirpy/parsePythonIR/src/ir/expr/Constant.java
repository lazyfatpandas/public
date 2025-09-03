package ir.expr;

import ir.JPExpr;
import ir.util.CRegion;
import soot.*;
import soot.jimple.ConditionExpr;
import soot.util.Switch;

import java.util.ArrayList;
import java.util.List;

public class Constant extends JPExpr implements CRegion, ConditionExpr  {
    int lineno;
    int col_offset;
    String value;
    String consType="";

    public Constant(int lineno, int col_offset, String value) {
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.value = value;
    }
    public Constant(int lineno, int col_offset, String value, String consType) {
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.value = value;
        this.consType=consType;
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
    public List<Local> getLocals() {
    List<Local> listL=new ArrayList<>();

    return listL;
    }

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
        return new Constant(this.lineno,this.col_offset,this.value,this.consType);
    }

    public String toString() {
        if(consType.equals("Num")) {
            return value;
        }
        else  if(consType.equals("String")){
            return "'"+value+"'";
        }
        else  if(consType.equals("Boolean")){
            if(value.equals("true")){
                return "True";
            }
            else if(value.equals("false")){
                return "False";
            }
        }
            return value;

    }

    // Chiranmoy 8-2-24, get dataframes in this expr/stmt
    public List<Name> getDataFrames() {
        return new ArrayList<>();
    }

    @Override
    public boolean isDataFrame() {
        return false;
    }
}
