package ir.Stmt;

import ast.StmtAST;
import ir.*;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class AugAssignStmt extends AssignStmt  {

    Operator op ;
    //Now here writing code to get type of assignment expression
    //public AssignStmt(int lineno, int col_offset,List targets) {
    public AugAssignStmt(int lineno, int col_offset) {
        super(lineno,col_offset);
        ast_type= StmtAST.AugAssign;
    }
    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
    }
}