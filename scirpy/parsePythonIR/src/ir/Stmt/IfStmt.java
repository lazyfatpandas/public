package ir.Stmt;

import ast.StmtAST;
import ir.IExpr;
import ir.IStmt;
import ir.JPBody;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;
import soot.*;
import soot.jimple.internal.JIfStmt;

import java.util.ArrayList;
import java.util.List;

public class IfStmt  extends JIfStmt implements IStmt { //extends JPAbstractStmt{//
    StmtAST ast_type= StmtAST.If;
    IExpr test;
    JPBody ifBody;
    JPBody orelseBody;
    Unit pyStmt=null;
    String typePyStmt;
    int lineno=0;
    boolean modified=false;
    Value condition;


    public IfStmt(Value condition, Unit target) {
        super(condition, target);
        assert (condition != null);
        this.condition=condition;

    }

    public IfStmt(Value condition, UnitBox target) {
        super(condition, target);
        assert (condition != null);
        this.condition=condition;
    }

    protected IfStmt(ValueBox conditionBox, UnitBox targetBox) {
        super(conditionBox, targetBox);
        assert (conditionBox != null);
    }

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

    public void copyIf(IfStmtPy ifStmtPy) {
        this.setTest(ifStmtPy.getTest());
        this.setIfBody(ifStmtPy.getIfBody());
        this.setOrelseBody(ifStmtPy.getOrelseBody());

    }

    public Unit getPyStmt() {
        return pyStmt;
    }

    public void setPyStmt(Unit pyStmt) {
        this.pyStmt = pyStmt;
    }

    public String getTypePyStmt() {
        return typePyStmt;
    }

    public void setTypePyStmt(String typePyStmt) {
        this.typePyStmt = typePyStmt;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public String toString() {


        return (pyStmt.toString());
    }

    @Override
    //TODO write lineno
    public int getLineno() {
        return lineno;
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
        return this;
        //ERROR FOR FORSTMT--rectify
        /*
        NopStmt nop= new ir.Stmt.NopStmt();//Jimple.v().newNopStmt();
        IfStmt ifStmt=new IfStmt(condition,nop);
        IfStmtPy ifStmtPy=(IfStmtPy)pyStmt.clone();
        ifStmt.copyIf(ifStmtPy);
        ifStmt.setPyStmt(ifStmtPy);
        ifStmt.setLineno(lineno);
        ifStmt.setTypePyStmt(typePyStmt);
        ifStmt.setSourceCode(sourceCode);
        return ifStmt;
    */
    }
    public List<Local> getLocals(){
        List<Local> listL=new ArrayList<>();
        if(test!=null)
        listL.addAll(test.getLocals());
        return listL;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        return ((IStmt) pyStmt).getDataFramesDefined();
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return ((IStmt) pyStmt).getDataFramesUsed();
    }
}
