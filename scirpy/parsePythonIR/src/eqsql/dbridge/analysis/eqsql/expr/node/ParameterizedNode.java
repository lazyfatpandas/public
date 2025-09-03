package eqsql.dbridge.analysis.eqsql.expr.node;

/**
 * Created by ek on 6/11/16.
 */
interface ParameterizedNode {
    /**
     * Find the placeholders from <code>node</code> and store them. This function is intended to be called from the
     * constructor that builds the object from a given <code>node</code> without using explicit placeholder information.
     * @param node A parameterized node
     */
    void findPlaceholders(Node node);
}
