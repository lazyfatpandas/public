package ir.manipulation;

import ir.IExpr;
import ir.Stmt.AssignmentStmtSoot;
import ir.expr.*;
import ir.internalast.JPValueBox;
import soot.Unit;

public class UpdateMergeSelectionValueBox {
    public UpdateMergeSelectionValueBox() {

    }

    public void updateValueBoxesMerge(Unit unit){

        if(((AssignmentStmtSoot)unit).getAssignStmt().getRHS() instanceof Call){
            Call call=(Call)((AssignmentStmtSoot)unit).getAssignStmt().getRHS();
            for(IExpr arg:call.getArgs()){
                if(arg instanceof Subscript){
                    Subscript subscript=(Subscript)arg;
                    if(subscript.getValue() instanceof Name) {
                        Name dfName = (Name) subscript.getValue();
                        if(subscript.getSlice()!=null && subscript.getSlice().getValue()!=null && subscript.getSlice().getValue() instanceof ListComp){
                            ListComp listComp=(ListComp) subscript.getSlice().getValue();
                            for(IExpr columnName:listComp.getElts()){
                                if(columnName instanceof Str){
                                    Name name = new Name(((Str) columnName).getLineno(), ((Str) columnName).getCol_offset(), ((Str) columnName).getS(), Expr_Context.Load);
                                    name.setParent(((Name) dfName).getName());
                                    JPValueBox valueBox = new JPValueBox(name);
                                    ((AssignmentStmtSoot)unit).getUsedVars().add(valueBox);
                                }
                            }
                        }

                    }
                }
            }

        }

    }
}
