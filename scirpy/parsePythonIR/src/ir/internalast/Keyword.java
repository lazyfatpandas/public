package ir.internalast;

import ir.IExpr;
import ir.JPExpr;
import ir.expr.Constant;
import ir.expr.Expr_Context;
import ir.expr.ListComp;
import ir.expr.Name;
import soot.Local;

import java.util.List;

public class Keyword extends JPExpr {
    String arg;
    IExpr value;

    public Keyword(String arg, IExpr value) {
        this.arg = arg;
        this.value = value;
    }

    public Keyword() {
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public IExpr getValue() {
        return value;
    }

    public void setValue(IExpr value) {
        this.value = value;
        //3.9 False is read as false by ast
        if(this.value instanceof Constant && this.value.toString().equals("false")){
            Constant val=(Constant)value;
            val.setValue("False");
        }
//        //2.01
//        else if(value instanceof Constant){
//                Constant val=(Constant)value;
//                val.setValue("'"+val.getValue()+"'");
//        }
    }
    @Override
    public List<Local> getLocals() {
        return value.getLocals();
    }

    @Override
    public Object clone() {
        Keyword keyword = new Keyword();
        keyword.arg = this.arg;
        keyword.value = (IExpr)this.value.clone();
        return keyword;
    }

    @Override
    public List<Name> getDataFrames() {
        return value.getDataFrames();
    }

    @Override
    public boolean isDataFrame() {
        return false;
    }
}
