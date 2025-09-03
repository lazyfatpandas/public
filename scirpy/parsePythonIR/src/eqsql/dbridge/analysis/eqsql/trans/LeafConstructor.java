package eqsql.dbridge.analysis.eqsql.trans;

import eqsql.dbridge.analysis.eqsql.expr.node.LeafNode;
import eqsql.dbridge.analysis.eqsql.expr.operator.Operator;

/**
 * Created by venkatesh on 14/7/17.
 * Function wrapper to construct a leaf node using an operator.
 */
public interface LeafConstructor {
    LeafNode consLeaf(Operator op);
}
