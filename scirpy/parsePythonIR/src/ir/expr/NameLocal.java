package ir.expr;

import ast.ExprAST;
import ir.IExpr;
import ir.JPExpr;
import soot.Local;
import soot.Type;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.jimple.StringConstant;
import soot.util.Switch;

import java.util.ArrayList;
import java.util.List;
//public class Name extends JPExpr {
//public class Name extends StringConstant implements IExpr {
public class NameLocal implements Local {
    //TODO see if this is actually req
    private ValueBox valueBox;
    int lineno;
    int col_offset;
    public String id;
    Expr_Context expr_context;
    ExprAST ast_type=ExprAST.Name;
    Type type=null;
    public NameLocal(String id, Expr_Context expr_context) {
        //super(id);
        this.id = id;
        this.expr_context = expr_context;

    }

    public NameLocal(int lineno, int col_offset, String id, Expr_Context expr_context) {
        //super(id);
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.id = id;
        this.expr_context = expr_context;
    }

    @Override
    public List getUseBoxes() {
        ArrayList<ValueBox> useBoxes = new ArrayList<>();

        useBoxes.add(valueBox);
        // useBoxes.add(rangeTabValueBox);
        return useBoxes;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public void setName(String s) {
        this.id=s;
    }

    @Override
    public void setType(Type type) {
        this.type=type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public void toString(UnitPrinter unitPrinter) {

    }

    @Override
    public boolean equivTo(Object o) {
        return false;
    }

    @Override
    public int equivHashCode() {
        return 0;
    }

    @Override
    public void setNumber(int i) {

    }

    @Override
    public int getNumber() {
        return 0;
    }

    @Override
    public void apply(Switch aSwitch) {

    }
}
