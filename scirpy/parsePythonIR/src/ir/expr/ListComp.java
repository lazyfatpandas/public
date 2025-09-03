package ir.expr;

import ir.IExpr;
import ir.JPExpr;
import soot.Local;

import java.util.ArrayList;
import java.util.List;


public class ListComp extends JPExpr {
    int lineno;
    int col_offset;
    Expr_Context expr_context;
    List<IExpr> elts;
    String typeOfList="List";
    //TODO add comprehension* generators

    public ListComp(List<IExpr> elts) {
        this.elts = elts;
    }

    public ListComp() {
        this.elts = new ArrayList<>();
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

    public Expr_Context getExpr_context() {
        return expr_context;
    }

    public void setExpr_context(Expr_Context expr_context) {
        this.expr_context = expr_context;
    }

    public List<IExpr> getElts() {
        return elts;
    }

    public void setElts(List<IExpr> elts) {
        this.elts = elts;
    }

    public String getTypeOfList() {
        return typeOfList;
    }

    public void setTypeOfList(String typeOfList) {
        this.typeOfList = typeOfList;
    }
    public String toString() {
        String symOpen="[",symClose="]";
        if(typeOfList.equals("Tuple")){
            symOpen="(";
            symClose=")";
        }
        String source=symOpen;
        for(IExpr iExpr:elts){
//            if(iExpr instanceof Constant) {
//                source = source + "'"+ iExpr.toString() + "',";
//            }
//            else{
//                source = source + iExpr.toString() + ",";
//            }
            //3.01
            source = source + iExpr.toString() + ",";

        }
        if(source.length()>1) {
            source = source.substring(0, source.length() - 1) + symClose;
        }
        else{
            source=source+symClose;
        }
        return source;
    }

    @Override
    public Object clone() {
        ListComp listComp=new ListComp();
        listComp.setTypeOfList(typeOfList);
        listComp.setCol_offset(col_offset);
        listComp.setLineno(lineno);
        listComp.setExpr_context(expr_context);
        for(IExpr elt:elts){
            listComp.getElts().add((IExpr)elt.clone());
        }
        return listComp;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Object val:elts) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }

        }
        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        for(IExpr expr : elts)
            dataframes.addAll(expr.getDataFrames());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        return false;
    }
}
