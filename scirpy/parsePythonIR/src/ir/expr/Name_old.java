package ir.expr;

import analysis.PythonScene;
import ast.ExprAST;
import ir.IExpr;
import org.jboss.util.NotImplementedException;
import soot.Local;
import soot.jimple.StringConstant;

import java.util.ArrayList;
import java.util.List;
//public class Name extends JPExpr {
public class Name_old extends StringConstant implements IExpr {

    int lineno;
    int col_offset;
    public String id;
    Expr_Context expr_context;
    ExprAST ast_type=ExprAST.Name;

    public Name_old(String id, Expr_Context expr_context) {
        super(id);
        this.id = id;
        this.expr_context = expr_context;

    }

    public Name_old(int lineno, int col_offset, String id, Expr_Context expr_context) {
        super(id);
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.id = id;
        this.expr_context = expr_context;
    }
    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        return listL;
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
