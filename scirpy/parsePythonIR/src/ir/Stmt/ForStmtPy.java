package ir.Stmt;

import ir.IExpr;
import ir.JPAbstractStmt;
import ir.JPBody;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class ForStmtPy extends JPAbstractStmt {
    IExpr target;
    IExpr iterator;
    JPBody body;
    JPBody orelseBody;
    boolean modified=false;
    int lineno=0;


    public IExpr getTarget() {
        return target;
    }

    public void setTarget(IExpr target) {
        this.target = target;
    }

    public IExpr getIterator() {
        return iterator;
    }

    public void setIterator(IExpr iterator) {
        this.iterator = iterator;
    }

    public JPBody getBody() {
        return body;
    }

    public void setBody(JPBody body) {
        this.body = body;
    }

    public JPBody getOrelseBody() {
        return orelseBody;
    }

    public void setOrelseBody(JPBody orelseBody) {
        this.orelseBody = orelseBody;
    }


    @Override
    public int getLineno() {
        return lineno;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
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
    public Object clone() {
        ForStmtPy forStmtPy=new ForStmtPy();
        forStmtPy.setBody((JPBody) body.clone());
        forStmtPy.setIterator((IExpr) iterator.clone());
        forStmtPy.setOrelseBody((JPBody) orelseBody.clone());
        forStmtPy.setTarget((IExpr)target.clone());
        forStmtPy.setSourceCode(sourceCode);
        forStmtPy.setLineno(lineno);
        return forStmtPy;
    }
    @Override
    public String toString(){
        return "for " + this.getTarget().toString() +" in " + this.getIterator().toString()+":";
    }

    @Override
    public List<Name> getDataFramesDefined() {
        return new ArrayList<>();
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return iterator.getDataFrames();
    }
}
