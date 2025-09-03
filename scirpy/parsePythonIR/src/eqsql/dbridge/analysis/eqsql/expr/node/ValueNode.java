package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.ValueOp;
import soot.Value;
import soot.jimple.NullConstant;

/**
 * Created by ek on 26/10/16.
 */
public class ValueNode extends LeafNode implements HQLTranslatable, SQLTranslatable {

    /** Intentionally package local. This constructor can only be accessed indirectly
    through NodeFactory.constructFromValue() */
    ValueNode(Value val) {
        super(new ValueOp(val));
    }

    public Value getValue() {
        return ((ValueOp)operator).getValue();
    }

    @Override
    public String toString() {
        return ((ValueOp)operator).getValue().toString();
    }


    public boolean isNull(){
        return ((ValueOp)operator).getValue() instanceof NullConstant;
    }

    @Override
    public String toHibQuery() {
        return toString();
    }

    @Override
    public String toSQLQuery() {
        return toString();
    }
}
