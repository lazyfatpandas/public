package eqsql.dbridge.analysis.eqsql.hibernate.construct;

import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.NodeFactory;
import eqsql.dbridge.analysis.eqsql.expr.node.RetVarNode;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;
import eqsql.exceptions.UnknownConstructException;
import soot.Unit;
import soot.jimple.internal.JReturnStmt;

/**
 * Created by ek on 18/5/16.
 */
public class JReturnStmtCons implements StmtDIRConstructor {

    @Override
    public StmtInfo construct(Unit stmt) throws UnknownConstructException {
        assert (stmt instanceof JReturnStmt);
        JReturnStmt returnStmt = (JReturnStmt)stmt;

        VarNode dest = RetVarNode.getARetVar(returnStmt.getOp().getType());
        Node source = NodeFactory.constructFromValue(returnStmt.getOp());

        return new StmtInfo(dest, source);
    }
}
