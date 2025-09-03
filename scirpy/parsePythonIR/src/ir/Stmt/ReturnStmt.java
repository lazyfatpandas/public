package ir.Stmt;

import ast.StmtAST;
import ir.IExpr;
import ir.IStmt;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JReturnStmt;

import java.util.ArrayList;
import java.util.List;

public class ReturnStmt  extends JReturnStmt  implements IStmt{
    StmtAST ast_type=StmtAST.Return;
    int lineno;
    int col_offset;
    IExpr value;
    boolean modified;


    public ReturnStmt(Value returnValue) {
        super(returnValue);
        this.value=(IExpr) returnValue;
    }

    protected ReturnStmt(ValueBox returnValueBox) {
        super(returnValueBox);
    }
    @Override
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

    public IExpr getValue() {
        return value;
    }

    public void setValue(IExpr value) {
        this.value = value;
    }


    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public String toString(){
        String str="return ";
        str=str+value.toString();
        return str;
    }

    String sourceCode="";
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    @Override
    public String getOriginalSource() {
        return sourceCode;
    }
    @Override
    public Object clone() {
        return this;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        return new ArrayList<>();
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return value.getDataFrames();
    }
}
