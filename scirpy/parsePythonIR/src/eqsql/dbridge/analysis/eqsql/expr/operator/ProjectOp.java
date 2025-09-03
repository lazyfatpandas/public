package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by ek on 23/5/16.
 */
public class ProjectOp extends Operator {
    public ProjectOp() {
        super("Pi", OpType.Project, 2);
        /* 2 operands will be: select node, expression (string or list?) representing what to project */
    }
}
