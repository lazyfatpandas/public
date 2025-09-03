package eqsql.dbridge.analysis.eqsql.hibernate.construct;

import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.UpdateNode;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;
import eqsql.exceptions.UnknownConstructException;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ek on 18/5/16.
 */
public class JInvokeStmtCons implements StmtDIRConstructor {

    private static String LIST_ADD = "add";
    private static String MAP_PUT = "put";
    private static String SAVE_OR_UPDATE = "saveOrUpdate";
    private static Set<String> supportedMethods;
    static {
        supportedMethods = new HashSet<>();
        supportedMethods.add(LIST_ADD);
        supportedMethods.add(MAP_PUT);
        supportedMethods.add(SAVE_OR_UPDATE);
    }

    @Override
    public StmtInfo construct(Unit stmt) throws UnknownConstructException {
        assert (stmt instanceof InvokeStmt);

        InvokeStmt invokeStmt = (InvokeStmt) stmt;
        InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();

        String method = invokeExpr.getMethod().getName();
        VarNode dest;
        Node source;
        if(!supportedMethods.contains(method)){
            return StmtInfo.nullInfo;
        }
        else if (method.equalsIgnoreCase(SAVE_OR_UPDATE)){
            dest = VarNode.getUpdateVar();
            source = new UpdateNode();
        }
        else{
            dest = Utils.fetchBase(invokeExpr);
            source = Utils.parseInvokeExpr(invokeExpr);
        }

        return new StmtInfo(dest, source);
    }
}
