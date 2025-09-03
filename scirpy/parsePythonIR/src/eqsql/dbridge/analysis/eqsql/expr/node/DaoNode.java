package eqsql.dbridge.analysis.eqsql.expr.node;

import eqsql.dbridge.analysis.eqsql.expr.operator.DaoOp;

/**
 * Created by K. Venkatesh Emani on 1/6/2017.
 * Node that represents a Data Access Object reference (Data Access Objects
 * provide functions to retrieve and store data to/from the database).
 * In our implementation, we do not access any specific state of Dao objects,
 * so we can remove them from the DIR during simplifications. This node
 * facilitates that removal.
 */
public class DaoNode extends LeafNode {
    public DaoNode(){
        super(new DaoOp());
    }
}
