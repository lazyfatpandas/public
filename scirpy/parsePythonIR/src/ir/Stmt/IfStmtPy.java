package ir.Stmt;

        import ast.StmtAST;
        import ir.IExpr;
        import ir.JPAbstractStmt;
        import ir.JPBody;
        import ir.expr.Name;
        import org.jboss.util.NotImplementedException;

        import java.util.ArrayList;
        import java.util.List;

public class IfStmtPy extends JPAbstractStmt{//
    StmtAST ast_type= StmtAST.If;
    IExpr test;
    JPBody ifBody;
    JPBody orelseBody;
    int lineno=0;
    boolean modified=false;
    boolean isWhile=false;



    public StmtAST getAst_type() {
        return ast_type;
    }

    public void setAst_type(StmtAST ast_type) {
        this.ast_type = ast_type;
    }

    public IExpr getTest() {
        return test;
    }

    public void setTest(IExpr test) {
        this.test = test;
    }

    public JPBody getIfBody() {
        return ifBody;
    }

    public void setIfBody(JPBody ifBody) {
        this.ifBody = ifBody;
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

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public int getLineno() {
        return lineno;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public String toString(){
        if(isWhile){
         return "while " + test.toString() +":";
        }
        return "if " + test.toString() +":";
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
        IfStmtPy ifStmtPy=new IfStmtPy();
        ifStmtPy.setLineno(lineno);
        ifStmtPy.setIfBody((JPBody)ifBody.clone());
        ifStmtPy.setOrelseBody((JPBody) orelseBody.clone());
        ifStmtPy.setTest((IExpr)test.clone());
        ifStmtPy.setSourceCode(sourceCode);
        return ifStmtPy;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        return new ArrayList<>();
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return test.getDataFrames();
    }

    public boolean isWhile() {
        return isWhile;
    }

    public void setWhile(boolean aWhile) {
        isWhile = aWhile;
    }
}
