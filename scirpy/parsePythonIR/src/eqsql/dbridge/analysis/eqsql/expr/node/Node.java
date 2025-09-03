package eqsql.dbridge.analysis.eqsql.expr.node;

import dbridge.analysis.region.regions.ARegion;
import dbridge.analysis.region.regions.LoopRegion;
import eqsql.EqSQLConfig;
import eqsql.dbridge.analysis.eqsql.expr.operator.Operator;
import eqsql.dbridge.analysis.eqsql.util.PrettyPrinter;
import eqsql.dbridge.visitor.NodeVisitor;
import eqsql.dbridge.visitor.Visitable;

import java.util.*;

public class Node implements Visitable{

    /** Tree. Core of the data structure. */
    protected Operator operator;
    protected Node[] children;

    /** Book keeping information for code rewriting purposes.
     * Hope you like the pun :) */
    private class NodeBook{
        /** This node represents the expression for a variable over some program region.
         * This is that region. */
        ARegion scopeRegion;
        /** The loops (if any) which can now be removed as this node represents them using an
         * expression */
        List<LoopRegion> loopsSwallowed;

        NodeBook(){
            scopeRegion = null;
            loopsSwallowed = new ArrayList<>();
        }
    }
    NodeBook book;

    protected Node(Operator _op, Node... _children) {
        this(); //Call to the default constructor

        operator = _op;

        int arity = operator.getArity();
        children = new Node[arity];
        assert _children.length >= arity;
        for (int i = 0; i < arity; i++) {
            children[i] = _children[i];
        }

        /* Note that we only copy as many children as the arity of the operator.
        The rest are ignored. */
        if(_children.length > arity){
            EqSQLConfig.getLogger().warn("DEBUG Warning: Ignored " + (_children.length - arity) + " arguments" +
                    "in construction of " + operator + " Node");
        }
    }

    /** Dummy constructor for use by special kinds of nodes such as BottomNode.
     * Do not use this unless you know what you are doing. */
    protected Node(){
        book = new NodeBook();
    }

    public Node getChild(int i){
        if(i <= children.length){
            return children[i];
        }
        return null;
    }

    /** If <code>this</code> node contains </code>child</code>, then
     * return the index of <code>child</code>. Otherwise return -1. */
    public int indexOf(Node child){
        return Arrays.asList(children).indexOf(child);
    }

    public void setChild(int i, Node child){
        children[i] = child;
    }

    public int getNumChildren(){
        if(children == null){
            return 0;
        }
        return children.length;
    }

    @Override
    public String toString(){
        List<Node> childrenList = (this.children == null) ?
                new ArrayList<Node>() : Arrays.asList(this.children);
        return PrettyPrinter.makeTreeString(operator, childrenList);
    }



    public Operator getOperator() {
        return operator;
    }

    /**
     * Calculate and return the set of variables (JimpleLocals) that are read in the subtree
     * rooted at this OpNode. Class VarNode extends this function to return itself. All other
     * OpNode derivatives use the definition provided below.
     */
    public Set<VarNode> readSet() {
        if(operator.getArity() == 0){
            return new HashSet<>(); //empty
        }

        Set<VarNode> rs = new HashSet<>();
        for (Node child : children) {
            rs.addAll( child.readSet() );
        }
        return rs;
    }

    public boolean isLeaf(){
        return (this instanceof Leaf);
    }

    /** Visitor pattern. Performs a post-order depth first traversal.
     * Works as follows: Visits each child of a node, processes the child,
     * collects the returned node (newChild) and replaces the original child with
     * newChild. In the end, processes itself and returns the resultant node.
     *
     * Note that this is not the most general purpose visitor, and is tailored
     * to our use cases. */
    @Override
    public Node accept(NodeVisitor v) {
        if(isLeaf()){
            return v.visit(this);
        }

        int i=0;
        for (Node child : children) {
            Node result = child.accept(v);
            if(child != result) {
                this.setChild(i, result);
            }
            i++;
        }
        return v.visit(this);
    }

    public Node[] getChildren() {
        return children;
    }

    /** This node represents the expression for a variable over some program region.
     * Set that region. */
    public void setRegion(ARegion region){
        this.book.scopeRegion = region;
    }

    /** Add a loop region which can now be removed as this node represents them using an
     * expression */
    public void addLoopSwallowed(LoopRegion loopRegion){
        this.book.loopsSwallowed.add(loopRegion);
    }

    /** This node represents the expression for a variable over some program region.
     * This gives that region. */
    public ARegion getRegion(){
        return this.book.scopeRegion;
    }

    /** The loops which can now be removed as this node represents them using an
     * expression */
    public List<LoopRegion> getLoopsSwallowed(){
        return this.book.loopsSwallowed;
    }

}
