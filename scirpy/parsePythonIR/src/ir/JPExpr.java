package ir;

import ir.expr.Compare;
import soot.Type;
import soot.UnitPrinter;
import soot.Value;
import soot.jimple.NumericConstant;
import soot.util.Switch;

import java.util.ArrayList;
import java.util.List;

// TODO verify this code::::::::Just inserted on instinct
public abstract class JPExpr  extends NumericConstant implements IExpr{
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

//    @Override
//    public List getUseBoxes() {
//        return new ArrayList();
//    }

    @Override
    public NumericConstant add(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant subtract(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant multiply(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant divide(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant remainder(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant equalEqual(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant notEqual(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant lessThan(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant lessThanOrEqual(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant greaterThan(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant greaterThanOrEqual(NumericConstant numericConstant) {
        return null;
    }

    @Override
    public NumericConstant negate() {
        return null;
    }
}
