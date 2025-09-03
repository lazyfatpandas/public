package ir.Stmt;

import soot.*;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.util.Switch;

import java.util.List;

public class InvokeExpressionStmt extends JInvokeStmt {
    CallExprStmt stmt;

    public InvokeExpressionStmt(Value c, CallExprStmt stmt) {
        super(c);
        this.stmt = stmt;
    }

    public InvokeExpressionStmt(ValueBox invokeExprBox, CallExprStmt stmt) {
        super(invokeExprBox);
        this.stmt = stmt;
    }
}
