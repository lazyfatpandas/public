package ir.expr;

import ast.ExprAST;
import ir.IExpr;
import ir.JPExpr;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class JoinedStr extends JPExpr {
    int lineno;
    int col_offset;
    Expr_Context expr_context;
    ExprAST ast_type=ExprAST.JoinedStr;
    List<IExpr> values;

    public JoinedStr() {
        this.values = new ArrayList<>();
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

    public ExprAST getAst_type() {
        return ast_type;
    }

    public void setAst_type(ExprAST ast_type) {
        this.ast_type = ast_type;
    }

    public List<IExpr> getValues() {
        return values;
    }

    public void setValues(List<IExpr> values) {
        this.values = values;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(IExpr iExpr:values){
            if(iExpr instanceof Name){
                listL.add((Local) iExpr);
            }
        }
        return listL;
    }

    public String toString() {
        String source="f'";
        for(IExpr iExpr:values){
            if(iExpr instanceof Str) {
                source = source + iExpr.toString().replaceAll("^[\"']+|[\"']+$", "");;
            }
            else if(iExpr instanceof Constant) {
                Constant constant=(Constant)iExpr;
                //3.01 for formatted csv
                if(iExpr.toString().contains(".csv")){
                    //source = source+ iExpr.toString();
                    source = source + iExpr.toString().replaceAll("\'", "");;
                }
                else if(constant.consType.equals("String")){
                    source = source + iExpr.toString().replaceAll("\'", "");;
                }
                else {
                    //source = source + "\"" + iExpr.toString() + "\"";
                    //3.01
                    //source = source + iExpr.toString().replaceAll("\'", "");;
                    source = source + iExpr.toString() ;
                }
            }
            else {
                source=source+"{"+iExpr.toString()+"}";
            }
        }
        source=source+"'";
        return source;
    }


    @Override
    public Object clone() {
        return this;
    }


    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        for(IExpr expr : values)
            dataframes.addAll(expr.getDataFrames());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        if(true)
            throw new NotImplementedException("isDataFrameOp not implemented in " + this.getClass().getSimpleName());
        return false;
    }
}
