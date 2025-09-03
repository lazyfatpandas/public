package ir.expr.compare;

import ir.IExpr;
import ir.JPExpr;
import ir.expr.Name;
import ir.internalast.InternalASTType;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class Compare_old extends JPExpr implements IExpr {
    int lineno;
    int col_offset;
    List<IExpr> comparators;
    IExpr left;
    InternalASTType ast_type=InternalASTType.Compare;
    List<InternalASTType> ops;


    public List<IExpr> getComparators() {
        return comparators;
    }

    public void setComparators(List<IExpr> comparators) {
        this.comparators = comparators;
    }

    public IExpr getLeft() {
        return left;
    }

    public void setLeft(IExpr left) {
        this.left = left;
    }

    public InternalASTType getAst_type() {
        return ast_type;
    }

    public void setAst_type(InternalASTType ast_type) {
        this.ast_type = ast_type;
    }

    public List<InternalASTType> getOps() {
        return ops;
    }

    public void setOps(List<InternalASTType> ops) {
        this.ops = ops;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Object val:comparators) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }
        }
        listL.addAll(left.getLocals());
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
