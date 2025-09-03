package ir.Stmt;

import ir.IExpr;
import ir.JPAbstractStmt;
import ir.JPBody;
import ir.expr.Name;

import java.util.ArrayList;
import java.util.List;

public class WhileStmtPy extends JPAbstractStmt {

    JPBody body;
    JPBody whileBody;
    JPBody orelseBody;
    IExpr test;
    int lineno=0;





    public JPBody getBody() {
        return body;
    }

    public void setBody(JPBody body) {
        this.body = body;
    }



    public JPBody getWhileBody() {
        return whileBody;
    }

    public void setWhileBody(JPBody whileBody) {
        this.whileBody = whileBody;
    }

    public IExpr getTest() {
        return test;
    }

    public void setTest(IExpr test) {
        this.test = test;
    }

    public JPBody getOrelseBody() {
        return orelseBody;
    }

    public void setOrelseBody(JPBody orelseBody) {
        this.orelseBody = orelseBody;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    //modify later
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
        return null;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        return new ArrayList<>();
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return test.getDataFrames();
    }
}
