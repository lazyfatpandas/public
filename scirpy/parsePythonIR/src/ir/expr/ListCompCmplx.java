package ir.expr;
/* bhu created on 8/5/20  */


import ir.IExpr;
import ir.JPExpr;
import ir.internalast.Comprehension;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;
//TODO not complete, check it and complete it :D, when not feeling SLEEPY....seriously
public class ListCompCmplx extends JPExpr {

    public ListCompCmplx() {
    }

    int lineno;
    int col_offset;
    Expr_Context expr_context;
    IExpr elt;
    List<Comprehension> comprehensions=new ArrayList<>();

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

    public Expr_Context getExpr_context() {
        return expr_context;
    }

    public void setExpr_context(Expr_Context expr_context) {
        this.expr_context = expr_context;
    }

    public IExpr getElt() {
        return elt;
    }

    public void setElt(IExpr elt) {
        this.elt = elt;
    }

    public List<Comprehension> getComprehensions() {
        return comprehensions;
    }

    public void setComprehensions(List<Comprehension> comprehensions) {
        this.comprehensions = comprehensions;
    }


    public String toString() {
        String str="";
        if(this!=null){
            //[ f(point) for point in points ]
            str="[ ";
            str=str+elt.toString();
            str=str + " for ";
            for(Comprehension comprehension:comprehensions){
                str=str+comprehension.toString();
            }
            str=str + " ]";

        }
        return str;

    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Comprehension val:comprehensions) {
                listL.addAll(val.getLocals());
        }
        listL.addAll(elt.getLocals());
        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
//        if(true)
//            throw new NotImplementedException("getDataFrame not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        return false;
    }
}
