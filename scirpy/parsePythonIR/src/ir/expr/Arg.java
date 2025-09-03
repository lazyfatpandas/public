package ir.expr;

import ast.ExprAST;
import ir.JPExpr;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class Arg extends JPExpr {
    ExprAST ast_type=ExprAST.Arg;
    int lineno;
    int col_offset;
    String s ;

    public Arg(int lineno, int col_offset, String s) {
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
        return s;
    }

    @Override
    public Object clone() {
        Arg argClone=new Arg(this.lineno,this.col_offset,this.s);
        return argClone;

    }
    @Override
    //TODO verify this if str is local some place
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        return listL;
    }

//    @Override
//    public String getName() {
//        return this.s;
//    }
//
//    @Override
//    public void setName(String s) {
//        this.s=s;
//    }
//
//    @Override
//    public void setType(Type type) {
//
//    }

//    @Override
//    public void setNumber(int i) {
//
//    }

//    @Override
//    public int getNumber() {
//        return 0;
//    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Str){
            Arg argC=(Arg)obj;
            if(argC.s.equals(this.s)){
                return true;
            }
        }

        return false;

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
