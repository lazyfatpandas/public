package eqsql.dbridge.analysis.eqsql.hibernate.construct;

import eqsql.dbridge.analysis.eqsql.expr.node.*;
import eqsql.dbridge.analysis.eqsql.scirpyUtils.IfStmtConsHelper;
import eqsql.exceptions.UnknownConstructException;
import ir.expr.Compare;
import soot.Unit;
import soot.Value;
import soot.jimple.IfStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JNeExpr;

/**
 * Created by ek on 18/5/16.
 */
public class IfStmtCons implements StmtDIRConstructor {

    @Override
    public StmtInfo construct(Unit stmt) throws UnknownConstructException {
        assert (stmt instanceof IfStmt);
        IfStmt ifStmt = (IfStmt)stmt;
        Value condition = ifStmt.getCondition();

        Node condNode = null;

        if(condition instanceof JEqExpr) {
            JEqExpr eqExpr = (JEqExpr) condition;
            Value op1 = eqExpr.getOp1();
            Value op2 = eqExpr.getOp2();

            condNode = new NotEqNode(NodeFactory.constructFromValue(op1), NodeFactory.constructFromValue(op2));//true and false conditions are inverted for our regions. Probably because jimple inverts the condition. This is functionally correct but a bit convoluted. //TODO fix.
            return new StmtInfo(VarNode.getACondVar(), condNode);
        }
        else if(condition instanceof JNeExpr){
            JNeExpr neExpr = (JNeExpr) condition;
            Value op1 = neExpr.getOp1();
            Value op2 = neExpr.getOp2();

            condNode = new EqNode(NodeFactory.constructFromValue(op1), NodeFactory.constructFromValue(op2));
            //true and false conditions are inverted for our regions. Probably because jimple inverts the condition. This is functionally correct but a bit convoluted. //TODO fix.

            return new StmtInfo(VarNode.getACondVar(), condNode);
        }
        //bhu Start
        else if(condition instanceof Compare){
            IfStmtConsHelper ifStmtConsHelper=new IfStmtConsHelper();

            condNode = ifStmtConsHelper.getCondNode(condition);
            //true and false conditions are inverted for our regions. Probably because jimple inverts the condition. This is functionally correct but a bit convoluted. //TODO fix.

            return new StmtInfo(VarNode.getACondVar(), condNode);
        }

        //bhu End

        return StmtInfo.nullInfo;
    }
}
