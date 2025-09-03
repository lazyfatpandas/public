package ir.expr;

import ast.ExprAST;
import ir.DataType.IType;
import ir.JPExpr;
import ir.util.CRegion;
import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ConditionExpr;

import java.util.ArrayList;
import java.util.List;

public class Num extends JPExpr implements CRegion, ConditionExpr {
    ExprAST ast_type=ExprAST.Num;
    int lineno;
    int col_offset;
    IType data;//value

    public Num(int lineno, int col_offset, IType data) {
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.data = data;
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


    public IType getData() {
        return data;
    }
    public String toString() {

        return (data.toString());
    }
    @Override
    public Object clone() {
        return new Num(lineno, col_offset,data);
    }

    @Override
    public Value getOp1() {
        return this;
    }

    @Override
    public Value getOp2() {
        return null;
    }

    @Override
    public ValueBox getOp1Box() {
        return null;
    }

    @Override
    public ValueBox getOp2Box() {
        return null;
    }

    @Override
    public void setOp1(Value value) {

    }

    @Override
    public void setOp2(Value value) {

    }

    @Override
    public String getSymbol() {
        return null;
    }

    @Override
    //TODO verify if this is local somewhere
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        return listL;
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
