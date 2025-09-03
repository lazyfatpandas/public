package eqsql.dbridge.analysis.eqsql.hibernate.construct;

import eqsql.exceptions.UnknownConstructException;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.internal.*;

public class StmtDIRConstructionHandler {

    /**
     * Statements matching these patterns will not be processed
     * (as we currently do not need the information from these
     * statements).
     */
    private final static String[] IGNORE_STMT_REGEXES =
            {
                    ".+getHibernateTemplate().*", //eg: $r1 = virtualinvoke this.<wilos.hibernate.misc.project.ProjectDao: org.springframework.orm.hibernate3.HibernateTemplate getHibernateTemplate()>()
                    ".+getSessionFactory().*",
                    ".+getCurrentSession().*",
            };

    public static StmtInfo constructDagSS(Unit stmt) throws
            UnknownConstructException {
        if (!(stmt instanceof Stmt)) {
            throw new UnknownConstructException("Unknown statement: " + stmt.toString());
        }
        if (isUnaffectingStmt(stmt)) {
            return StmtInfo.nullInfo;
        }

        StmtInfo stmtInfo = StmtInfo.nullInfo;
        StmtDIRConstructor sdc = fetchStmtDagConstructor(stmt);
        if(sdc != null){
            stmtInfo = sdc.construct(stmt);
        }
        return stmtInfo;
    }

    /**
     * @param stmt
     * @return create and return an appropriate StmtDagConstructor object depending on the type of stmt
     */
    public static StmtDIRConstructor fetchStmtDagConstructor(Unit stmt) {
        StmtDIRConstructor sdc = null;
        if (stmt instanceof JIdentityStmt) {
            sdc = new JIdentityStmtCons();
        }
        else if (stmt instanceof JReturnStmt) {
            sdc = new JReturnStmtCons();
        }
        else if (stmt instanceof soot.jimple.IfStmt) {
            sdc = new IfStmtCons();
        }
        else if (stmt instanceof JInvokeStmt) {
            sdc = new JInvokeStmtCons();
        }
        else if (stmt instanceof JAssignStmt) {
            sdc = new JAssignStmtCons();
        }
        return sdc;
    }

    private static boolean isUnaffectingStmt(Unit stmt) {
        if ((stmt instanceof JNopStmt) ||
                (stmt instanceof JGotoStmt)){
            return true;
        }
        String stmtText = stmt.toString();
        for (String regex : IGNORE_STMT_REGEXES) {
            if(stmtText.matches(regex)){
                return true;
            }
        }

        return false;
    }
}