package eqsql.dbridge.analysis.eqsql.expr.operator;

public class PrefetchOp extends Operator {

    private String relName;
    public PrefetchOp(String _relName) {
        super("Prefetch", OpType.Prefetch, 0);
        relName = _relName;
    }

    public String getRelName() {
        return relName;
    }

    public void setRelName(String relName) {
        this.relName = relName;
    }
}

