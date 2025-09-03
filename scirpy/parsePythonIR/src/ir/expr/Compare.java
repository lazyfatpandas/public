package ir.expr;

import ir.IExpr;
import ir.JPExpr;
import ir.internalast.JPValueBox;
import org.jboss.util.NotImplementedException;
import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ConditionExpr;

import java.util.ArrayList;
import java.util.List;
//TODO for soot compatibility, only single comparator, modify it to handle complicated comparisons..

public class Compare extends JPExpr implements IExpr, ConditionExpr {

    int col_offset;
    int lineno;
    List<IExpr> comparators=new ArrayList<>();
    IExpr left;
    List<OpsType> ops=new ArrayList<>();
    boolean isReverse=false;


    public int getCol_offset() {
        return col_offset;
    }

    public void setCol_offset(int col_offset) {
        this.col_offset = col_offset;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

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

    public List<OpsType> getOps() {
        return ops;
    }

    public void setOps(List<OpsType> ops) {
        this.ops = ops;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    //TODO ALL these will need modification to handle complex conditions
    @Override
    public Value getOp1() {
        return left;
    }

    @Override
    public Value getOp2() {
        return comparators.get(0);
    }

    @Override
    public ValueBox getOp1Box() {
        JPValueBox valueBox = new JPValueBox(left);
        return valueBox;

    }

    @Override
    public ValueBox getOp2Box() {
        JPValueBox valueBox = new JPValueBox(comparators.get(0));
        return valueBox;
    }

    @Override
    public void setOp1(Value value) {

    }

    @Override
    public void setOp2(Value value) {

    }

    @Override
    public String getSymbol() {
        return ops.get(0).toString();
    }

    public void copyLineInfo(Compare cmp) {
        this.setCol_offset(cmp.getCol_offset());
        this.setLineno(cmp.getLineno());
    }

    @Override
    public String toString() {
       int i=0;
       String stmt= "(" + left.toString();
       for (OpsType op: ops){
        stmt=stmt+" "+op.getSymbol()+" "+comparators.get(i).toString();
           i++;
       }
       return stmt+")";

    }
    @Override
    public Object clone(){
        Compare compare=new Compare();
        compare.setCol_offset(col_offset);
        compare.setLineno(lineno);
        compare.setLeft((IExpr)left.clone());
        for(IExpr comparator:comparators){
            compare.getComparators().add((IExpr)comparator.clone());
        }
        compare.setOps(ops);
        return compare;
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
        List<Name> dataframes = left.getDataFrames();
        for(IExpr expr : comparators)
            dataframes.addAll(expr.getDataFrames());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        boolean isDF = left.isDataFrame();
        for(IExpr expr : comparators)
            isDF |= expr.isDataFrame();
        return isDF;
    }
}
