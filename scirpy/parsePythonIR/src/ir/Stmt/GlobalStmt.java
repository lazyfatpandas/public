package ir.Stmt;

import ast.StmtAST;
import ir.JPAbstractStmt;
import ir.expr.Name;
import ir.internalast.Alias;
import org.jboss.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class GlobalStmt extends JPAbstractStmt {

    StmtAST ast_type= StmtAST.Global;
    String[] names;

    public GlobalStmt(int lineno, int col_offset, String[] names) {
        //Lineno and col_offset from JPAbstractStmt
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.names = names;
    }

    public StmtAST getAst_type() {
        return ast_type;
    }

    public void setAst_type(StmtAST ast_type) {
        this.ast_type = ast_type;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    @Override
    public String toString() {
        String stmt="";
            stmt="global ";
            for (String name : names) {
            stmt=stmt+name+", ";
            }
            stmt=stmt.substring(0, stmt.length()-2);
        return stmt;

    }
    public String toString2() {
        String stmt="";
        stmt="global ";
        for (String name : names) {
            stmt=stmt+name+", ";
        }
        stmt=stmt.substring(0, stmt.length()-2);
        return stmt;

    }

    String sourceCode="";
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    @Override
    public int getLineno() {
        return 0;
    }

    @Override
    public boolean isModified() {
        return false;
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
        List<Name> dataframes = new ArrayList<>();
//        if(true)
//            throw new NotImplementedException("getDataFramesDefined not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public List<Name> getDataFramesUsed() {
        List<Name> dataframes = new ArrayList<>();
//        if(true)
//            throw new NotImplementedException("getDataFramesUsed not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }
}
