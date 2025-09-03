package ir.expr;

import analysis.PythonScene;
import ast.ExprAST;
import ir.IExpr;
import ir.internalast.Targets;
import soot.*;
import soot.jimple.ConditionExpr;
import soot.jimple.internal.JimpleLocal;

import java.util.ArrayList;
import java.util.List;
//public class Name extends JPExpr {
public class Name extends JimpleLocal implements IExpr, ConditionExpr {

    int lineno;
    int col_offset;
    public String id;
    Expr_Context expr_context;
    ExprAST ast_type=ExprAST.Name;
    //for attribute with df
    String parent=null;


    public Name(String id, Expr_Context expr_context) {
        super(id,null);
        this.id = id;
        this.expr_context = expr_context;

    }

    public Name(int lineno, int col_offset, String id, Expr_Context expr_context) {
        super(id,null);
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.id = id;
        this.expr_context = expr_context;
    }
//TODO V Imp: Have not checked usage of this overwritten method

    public String getName() {
        return id;
    }

    public void setName(String s) {
        this.id=s;
        super.setName(s);
    }

    public boolean equals(Object o) {
        if(!(o instanceof Name || o instanceof Targets))
            return false;

        if(o instanceof Targets) {
            Targets target = (Targets) o;
            if (this.id.equals(target.getName()))
                return true;
        }
        else if(o instanceof Name){
            Name name=(Name) o;
            if (this.id.equals(name.id))
                return true;
        }
        else
            return false;
        return false;
    }


    public int getLineno() {
        return lineno;
    }

    public int getCol_offset() {
        return col_offset;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String toString() {

        return (id);
    }
    @Override
    public Object clone() {
        Name name=new Name(lineno,col_offset,id,expr_context);
        return name;

    }

    @Override
    public Value getOp1() {
        return this;
    }

    @Override
    public Value getOp2() {
        return null;
    }

    @Override
    public ValueBox getOp1Box() {
        return null;
    }

    @Override
    public ValueBox getOp2Box() {
        return null;
    }

    @Override
    public void setOp1(Value value) {

    }

    @Override
    public void setOp2(Value value) {

    }

    @Override
    public String getSymbol() {
        return null;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        listL.add(this);

        return listL;
    }

    // Chiranmoy 8-2-24, get dataframes in this expr/stmt
    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        if(PythonScene.allDfNames.contains(id))
            dataframes.add(this);
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        return PythonScene.allDfNames.contains(id);
    }
}
