package ir.expr;
/* bhu created on 19/5/20  */


import ir.IExpr;
import ir.JPExpr;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class Dict extends JPExpr {

    int lineno;
    int col_offset;
    Expr_Context expr_context=Expr_Context.Load;
    List<IExpr> keys=new ArrayList<>();
    List<IExpr> value=new ArrayList<>();


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

    public List<IExpr> getKeys() {
        return keys;
    }

    public void setKeys(List<IExpr> keys) {
        this.keys = keys;
    }

    public List<IExpr> getValue() {
        return value;
    }

    public void setValue(List<IExpr> value) {
        this.value = value;
    }

    public String toString(){
        String str="{";
        for(int i=0;i<keys.size();i++){
            //3.01
//        if(keys.get(i) instanceof Constant){
//            str=str+ "'"+keys.get(i).toString() + "':";
//        }
//        else{
//            str=str+keys.get(i).toString() + ":";
//        }
//        if(value.get(i) instanceof Constant){
//            str=str+ "'"+value.get(i).toString()+"'";
//        }
//        else {
//            str=str+value.get(i).toString();
//        }
        str=str+keys.get(i).toString() + ":"+ value.get(i).toString();
        if(i!=keys.size()-1){
            str=str+",";
        }

        }
        str=str+"}";
        return str;
    }
    @Override
    public Object clone() {
        Dict dict=new Dict();
        dict.setCol_offset(col_offset);
        dict.setLineno(lineno);
        dict.setExpr_context(expr_context);
        for(IExpr key:keys){
            dict.getKeys().add((IExpr)key.clone());
        }
        for(IExpr val:value){
            dict.getValue().add((IExpr)val.clone());
        }
        return dict;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Object val:keys) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }
        }
        for(Object val:value) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }
        }

        return listL;
    }

    public void removeKey(String clmn) {

       // IntStream.of(10000).map(a->a*a).boxed().forEach(System.out::println);

        //this.keys.stream().filter(key->((Str)key).s.equals(clmn)).forEach(key -> keys.remove((Str)key));
        this.keys.removeIf(key->((Str)key).s.equals(clmn));

    }


    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        for(IExpr expr : value)
            dataframes.addAll(expr.getDataFrames());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        return false;
    }
}
