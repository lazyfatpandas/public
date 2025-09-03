package ir.expr;

public class Backup_Code {
    //Name with soot Local
    /*
    package ir.expr;

import ast.ExprAST;
import ir.IExpr;
import ir.JPExpr;
import soot.Local;
import soot.Type;
import soot.UnitPrinter;
import soot.jimple.StringConstant;
import soot.util.Switch;

import java.util.List;
//public class Name extends JPExpr {
public class Name implements IExpr, Local {

    int lineno;
    int col_offset;
    public String id;
    Expr_Context expr_context;
    ExprAST ast_type=ExprAST.Name;


    public Name(String id, Expr_Context expr_context) {
        super(id);
        this.id = id;
        this.expr_context = expr_context;

    }

    public Name(int lineno, int col_offset, String id, Expr_Context expr_context) {
        super(id);
        this.lineno = lineno;
        this.col_offset = col_offset;
        this.id = id;
        this.expr_context = expr_context;
    }
//TODO V Imp: Have not checked usage of this overwritten method
    @Override
    public List getUseBoxes() {
        return null;
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

    }

    @Override
    public Type getType() {
        return null;
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

    @Override
    public Object clone() {
        return null;
    }
}

     */
}
