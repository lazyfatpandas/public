package ir.Stmt;

import ir.IExpr;
import ir.JPAbstractStmt;
import ir.JPBody;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class ClassDefStmt  extends JPAbstractStmt {
    /*
            "ast_type": "ClassDef",
            "bases": [],
            "body": [
            "col_offset": 0,
            "decorator_list": [],
            "keywords": [],
            "lineno": 1,
            "name": "Person"
     */
    Name name;
    JPBody body;
    List<IExpr> keywords;
    List<IExpr> decorator_list,bases;
    int col_offset;
    int lineno;
    boolean modified=false;


    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public JPBody getBody() {
        return body;
    }

    public void setBody(JPBody body) {
        this.body = body;
    }

    public List<IExpr> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<IExpr> keywords) {
        this.keywords = keywords;
    }

    public List<IExpr> getDecorator_list() {
        return decorator_list;
    }

    public void setDecorator_list(List<IExpr> decorator_list) {
        this.decorator_list = decorator_list;
    }

    public List<IExpr> getBases() {
        return bases;
    }

    public void setBases(List<IExpr> bases) {
        this.bases = bases;
    }

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

    @Override
    public boolean isModified() {
        return modified;
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
    public List<Name> getDataFramesDefined() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFramesDefined not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public List<Name> getDataFramesUsed() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFramesUsed not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }
}
