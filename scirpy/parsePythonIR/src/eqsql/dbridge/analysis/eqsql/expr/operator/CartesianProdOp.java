package eqsql.dbridge.analysis.eqsql.expr.operator;

/**
 * Created by venkatesh on 7/7/17.
 */
public class CartesianProdOp extends Operator {

    /**@param arity Number of relations in the cartesian product.
     If arity = 1, then it is a single relation. */
    public CartesianProdOp(int arity) {
        super("Cartesian", OpType.CartesianProd, arity);
    }
}
