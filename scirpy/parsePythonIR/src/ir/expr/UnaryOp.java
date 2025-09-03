package ir.expr;

import ir.IExpr;
import ir.JPExpr;
import ir.Unaryop;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class UnaryOp extends JPExpr implements IExpr {
    int lineno;
    int col_offset;
    Unaryop op;
    IExpr left;
    IExpr right;

    public UnaryOp(int lineno, int col_offset, Unaryop op, IExpr left, IExpr right) {
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public UnaryOp() {
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

    public Unaryop getOp() {
        return op;
    }

    public void setOp(Unaryop op) {
        this.op = op;
    }

    public IExpr getOperand() {
        return left;
    }

    public void setOperand(IExpr left) {
        this.left = left;
    }

    public String toString() {
        return op.getSymbol() + "("+ left.toString() +")";
    }
    @Override
    public Object clone() {
        IExpr rightC=null;
        if(this.right!=null){
            rightC=(IExpr) this.right.clone();
        }
        return new UnaryOp(this.lineno, this.col_offset, this.op, (IExpr) this.left.clone(),rightC );

    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        listL.addAll(left.getLocals());
       // listL.addAll(right.getLocals());
        return listL;
    }


    // Chiranmoy 8-2-24, get dataframes in this expr/stmt
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        if(left != null)  dataframes.addAll(left.getDataFrames());
        if(right != null) dataframes.addAll(right.getDataFrames());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        boolean l = left != null && left.isDataFrame();
        boolean r = right != null && right.isDataFrame();
        return l || r;
    }
}
