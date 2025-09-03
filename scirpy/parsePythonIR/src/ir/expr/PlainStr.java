package ir.expr;

import ir.JPExpr;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class PlainStr extends JPExpr{
    String s=null;


    public PlainStr(String s){
        this.s=s;
    }
    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        return listL;
    }

    @Override
    public String toString(){

        return s;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PlainStr){
            PlainStr strC=(PlainStr)obj;
            if(strC.s.equals(this.s)){
                return true;
            }
        }

        return false;

    }

    @Override
    public List<Name> getDataFrames() {
        return new ArrayList<>();
    }

    @Override
    public boolean isDataFrame() {
        return false;
    }
}
