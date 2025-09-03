package ir.Stmt;

import ast.StmtAST;
import ir.expr.Name;
import ir.internalast.*;
import ir.JPAbstractStmt;

import java.util.ArrayList;
import java.util.List;

public class ImportStmt extends JPAbstractStmt {


     Alias names;
     StmtAST ast_type= StmtAST.Import;
     boolean modified=false;

    public ImportStmt(int lineno, int col_offset, Alias names, StmtAST ast_type) {
        //Lineno and col_offset from JPAbstractStmt
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.names = names;
        this.ast_type = ast_type;
    }

    public Alias getNames() {
        return names;
    }

    public void setNames(Alias names) {
        this.names = names;
    }

    @Override
    public String toString() {
        String stmt="import "+names.toString();
        return stmt;

    }

    @Override
    public int getLineno() {
        return lineno;
    }

    @Override
    public boolean isModified() {
        return modified;
    }



    public void setModified(boolean modified) {
        this.modified = modified;
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
        return new ArrayList<>();
    }
}
