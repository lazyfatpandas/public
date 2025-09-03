package ir.internalast;

import ir.IExpr;
import ir.expr.Call;
import ir.expr.Num;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class Slice {
    IExpr lower;
    IExpr upper;
    IExpr step;
    IExpr index;
    //added while handling row['latitude'] type
    IExpr value;
    IExpr listComp;

    public Slice(IExpr lower, IExpr upper, IExpr step) {
        this.lower = lower;
        this.upper = upper;
        this.step = step;
    }
    public Slice() {
        this.lower = null;
        this.upper = null;
        this.step = null;
        this.index = null;
        this.value = null;

    }



    public IExpr getLower() {
        return lower;
    }

    public void setLower(IExpr lower) {
        this.lower = lower;
    }

    public IExpr getUpper() {
        return upper;
    }

    public void setUpper(IExpr upper) {
        this.upper = upper;
    }

    public IExpr getStep() {
        return step;
    }

    public void setStep(IExpr step) {
        this.step = step;
    }

    public IExpr getIndex() {
        return index;
    }

    public void setIndex(IExpr index) {
        this.index = index;
    }

    public IExpr getValue() {
        return value;
    }

    public void setValue(IExpr value) {
        this.value = value;
    }

    public IExpr getListComp() {
        return listComp;
    }

    public void setListComp(IExpr listComp) {
        this.listComp = listComp;
    }

    @Override
    public Object clone() {
        Slice slice=new Slice();
        if(this.getLower()!=null)
            slice.setLower((IExpr) this.getLower().clone());
        if(this.getUpper()!=null)
            slice.setUpper((IExpr) this.getUpper().clone());
        if(this.getStep()!=null)
            slice.setStep((IExpr) this.getStep().clone());
        if(this.getIndex()!=null)
            slice.setIndex((IExpr) this.getIndex().clone());
        if(this.getValue()!=null)
            slice.setValue((IExpr) this.getValue().clone());
        if(this.getListComp()!=null)
            slice.setListComp((IExpr) this.getListComp().clone());
        return slice;
    }

    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        if(lower!=null)
        listL.addAll(lower.getLocals());
        if(upper!=null)
        listL.addAll(upper.getLocals());
        if(step!=null)
        listL.addAll(step.getLocals());
        if(index!=null)
        listL.addAll(index.getLocals());
        if(value!=null)
        listL.addAll(value.getLocals());
        if(listComp!=null)
        listL.addAll(listComp.getLocals());
        return listL;
    }
}
