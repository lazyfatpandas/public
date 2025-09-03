package ir.expr;

import ast.ExprAST;
import ir.JPExpr;
import soot.Local;
import soot.Type;

import java.util.ArrayList;
import java.util.List;
/*
Add  "implements Local" for building local chain in case Str is treated as a variable.
If "implements Local" gives error, remove it and add "implements CRegion"
 */

public class Str extends JPExpr implements Local {
    ExprAST ast_type=ExprAST.Str;
    int lineno;
    int col_offset;
    String s ;

    public Str(int lineno, int col_offset, String s) {
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.s = s;
    }

    public ExprAST getAst_type() {
        return ast_type;
    }

    public int getLineno() {
        return lineno;
    }

    public int getCol_offset() {
        return col_offset;
    }

    public String getS() {
        return s;
    }

    @Override
    public String toString(){
        return "\""+s+"\"";
    }

    @Override
    public Object clone() {
        Str strClone=new Str(this.lineno,this.col_offset,this.s);
        return strClone;

    }
    @Override
    //TODO verify this if str is local some place
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        return listL;
    }

    @Override
    public String getName() {
        return this.s;
    }

    @Override
    public void setName(String s) {
    this.s=s;
    }

    @Override
    public void setType(Type type) {

    }

    @Override
    public void setNumber(int i) {

    }

    @Override
    public int getNumber() {
        return 0;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Str){
            Str strC=(Str)obj;
            if(strC.s.equals(this.s)){
                return true;
            }
        }

       return false;

    }

    // Chiranmoy 8-2-24, get dataframes in this expr/stmt
    @Override
    public List<Name> getDataFrames() {
        return new ArrayList<>();
    }

    @Override
    public boolean isDataFrame() {
        return false;
    }
}