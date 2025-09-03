package ir.expr;

import ir.IExpr;
import ir.JPExpr;
import ir.Operator;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class BinOp extends JPExpr implements IExpr {

    int lineno;
    int col_offset;
    Operator op;
    IExpr left;
    IExpr right;

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

    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
    }

    public IExpr getLeft() {
        return left;
    }

    public void setLeft(IExpr left) {
        this.left = left;
    }

    public IExpr getRight() {
        return right;
    }

    public void setRight(IExpr right) {
        this.right = right;
    }

    public String toString() {
        //benchmark change
        //3.01
//        if(left instanceof Constant){
//            return "\""+left.toString() + "\" " + op.getSymbol() + " (" + right.toString() +")";
//        }


        return "("+left.toString() + " " + op.getSymbol() + " " + right.toString() +")";
    }

    @Override
    public Object clone() {
        BinOp binOpClone = new BinOp();
        binOpClone.setLineno(getLineno());
        binOpClone.setCol_offset(getCol_offset());
        binOpClone.setOp(getOp());
        binOpClone.setRight((IExpr) this.right.clone());
        binOpClone.setLeft((IExpr)this.left.clone());

        return binOpClone;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        listL.addAll(left.getLocals());
        listL.addAll(right.getLocals());
        return listL;
    }


    // Chiranmoy 8-2-24, get dataframes in this expr/stmt
    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        dataframes.addAll(left.getDataFrames());
        dataframes.addAll(right.getDataFrames());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        return left.isDataFrame() || right.isDataFrame();
    }
}