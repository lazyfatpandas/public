package ir.Stmt;

import ast.StmtAST;
import ir.JPAbstractStmt;
import ir.expr.Name;
import ir.internalast.Alias;
import java.util.ArrayList;
import java.util.List;


public class ImportFrom extends JPAbstractStmt {

    String module;
    Alias names;
    StmtAST ast_type= StmtAST.Import;
    boolean modified=false;
    List<Alias> namesList=null;



   //4.04
    public ImportFrom(int lineno, int col_offset, Alias names, StmtAST ast_type, String module,List<Alias> namesList) {
        //Lineno and col_offset from JPAbstractStmt
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.names = names;
        this.ast_type = ast_type;
        this.module=module;
        this.namesList=namesList;

    }

    public ImportFrom(int lineno, int col_offset, Alias names, StmtAST ast_type, String module) {
        //Lineno and col_offset from JPAbstractStmt
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.names = names;
        this.ast_type = ast_type;
        this.module=module;
    }

    public Alias getNames() {
        return names;
    }

    public void setNames(Alias names) {
        this.names = names;
    }

    @Override
    public String toString() {
        String stmt="";
        stmt = "from " + module + " import " + names.toString();
        if(namesList!=null) {
            for(Alias alias:namesList){
                stmt=stmt+","+alias.toString();
            }
        }
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public StmtAST getAst_type() {
        return ast_type;
    }

    public void setAst_type(StmtAST ast_type) {
        this.ast_type = ast_type;
    }

    public String getSourceCode() {
        return sourceCode;
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
