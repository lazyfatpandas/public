package ir.internalast;

import ir.IExpr;
import ir.JPExpr;
import ir.expr.Expr_Context;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

//TODO not used----chjeck and delete
public class Func extends JPExpr {
    IExpr attribute;
    Expr_Context ast_type;
    boolean isName;


    public IExpr getAttribute() {
        return attribute;
    }

    public void setAttribute(IExpr attribute) {
        this.attribute = attribute;
    }

    public Expr_Context getAst_type() {
        return ast_type;
    }

    public void setAst_type(Expr_Context ast_type) {
        this.ast_type = ast_type;
    }

    public boolean isName() {
        return isName;
    }

    public void setName(boolean name) {
        isName = name;
    }
    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        listL.addAll(attribute.getLocals());
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