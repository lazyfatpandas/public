package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.SelfRefOp;

/**
 * Created by ek on 27/10/16.
 * Node used for self-reference of an object inside its own methods. For example:
 * baseObj.func(x) where: func(x) { return this.y + x; }
 * In the DIR for above function, "this" is represented using "SelfRefNode". It
 * is later replaced by baseObj during post processing after DIR construction.
 */
public class SelfRefNode extends LeafNode {
    public SelfRefNode() {
        super(new SelfRefOp());
    }

    @Override
    public String toString() {
        return "Self";
    }
}
