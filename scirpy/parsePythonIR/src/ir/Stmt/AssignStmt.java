package ir.Stmt;
import ast.StmtAST;
import ir.IExpr;
import ir.IStmt;
import ir.JPAbstractStmt;
import ir.expr.Attribute;
import ir.expr.Call;
import ir.expr.Name;
import ir.expr.Subscript;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

//TODO  clone of soot.Body
//TODO remove extra code from AbstractStatement
public class AssignStmt extends JPAbstractStmt implements IStmt {
    //TODO check lineno and col_offset for abstract class which is now removed from extends "JPAbstractStmt"
    //int lineno;
    //int col_offset;
    //List<Targets> targets;
    List<IExpr> targets;
    StmtAST ast_type= StmtAST.Assign;
    IExpr rHS=null;//Value
    //Now here writing code to get type of assignment expression
    boolean modified=false;

    //public AssignStmt(int lineno, int col_offset,List targets) {
    public AssignStmt(int lineno, int col_offset) {
        this.lineno=lineno;
        this.col_offset=col_offset;
        this.targets = new ArrayList<>();
    }

    public AssignStmt() {
        this.targets = new ArrayList<>();
    }

    public void setRHS(IExpr rHS) {
        this.rHS = rHS;
    }

    public IExpr getRHS() {
        return rHS;
    }

    //public List<Targets> getTargets() {
    public List<IExpr> getTargets() {
        return targets;
    }

    //public void setTargets(List<Targets> targets) {
    public void setTargets(List<IExpr> targets) {
        this.targets = targets;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public int getLineno() {
        return lineno;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    //TODO verify this
    public String toString() {

        String str="";
        for(IExpr target:targets){
            str=str+target.toString()+",";

        }
        str=str.substring(0,str.length()-1);
        str=str+" = " + rHS.toString();
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
        AssignStmt assignStmt=new AssignStmt(this.lineno,this.col_offset);
        assignStmt.setSourceCode(this.sourceCode);
        assignStmt.setRHS((IExpr) this.rHS.clone());
        for(IExpr target:this.targets){
            assignStmt.getTargets().add((IExpr) target.clone());
        }
        return assignStmt;

    }
    public void setLineno(int lineno){
        this.lineno=lineno;
    }

    public List<Local> getLocals(){
        List<Local> listL=new ArrayList<>();
        for(IExpr target:getTargets()){
            listL.addAll(target.getLocals());
        }
        listL.addAll(getRHS().getLocals());
        return listL;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        List<Name> dataframes = new ArrayList<>();
        for(IExpr expr : targets)
            dataframes.addAll(expr.getDataFrames());
        return dataframes;
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return rHS.getDataFrames();
    }
}
