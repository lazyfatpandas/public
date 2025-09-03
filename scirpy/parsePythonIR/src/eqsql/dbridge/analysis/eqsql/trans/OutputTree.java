package eqsql.dbridge.analysis.eqsql.trans;

import eqsql.dbridge.analysis.eqsql.expr.node.LeafNode;
import eqsql.dbridge.analysis.eqsql.expr.operator.OpType;
import eqsql.dbridge.analysis.eqsql.expr.operator.Operator;
import eqsql.dbridge.analysis.eqsql.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 An OutputTree node can either be
 an id referring to a node in the input,
 or a node constructed from scratch,
 or it can consist of an opType and children.

 Constructors are provided accordingly. The implementation takes care of handling all these cases
 uniformly.
 */
public class OutputTree {

    /** Id referring to input node */
    private int id;

    /** Node constructed from scratch */
    private LeafNode node;
    private LeafConstructor lc;

    /** opType and children enable the tree structure */
    private OpType opType;
    private List<OutputTree> children;

    public OutputTree(int id, LeafConstructor leafConstructor) {
        this.id = id;
        this.source = OutSource.Scratch;
        this.lc = leafConstructor;
    }

    public OutputTree(LeafConstructor leafConstructor){
        this(-1, leafConstructor);
        //Dummy id. Wont be used.
    }

    /**
     * Source of the output expression:
     * input if it is from the input expression
     * scratch if it is constructed from scratch
     * tree if it is a tree structure created using opType and children
     */
    private enum OutSource {
        Input,
        Scratch,
        Tree
    }

    private OutSource source;

    public OutputTree(int id) {
        this.id = id;
        source = OutSource.Input;
    }

    public OutputTree(OpType opType, OutputTree... _children) {
        this.opType = opType;

        this.children = new ArrayList<>();
        Collections.addAll(this.children, _children);

        source = OutSource.Tree;
    }

    public int getId() {
        return id;
    }

    public OpType getOpType() {
        return opType;
    }

    public List<OutputTree> getChildren() {
        return children;
    }

    public boolean isFromInput(){
        return source == OutSource.Input;
    }

    public boolean isFromScratch(){
        return source == OutSource.Scratch;
    }

    public boolean isTree(){
        return source == OutSource.Tree;
    }

    public LeafNode getNode(Operator op) {
        node = lc.consLeaf(op);
        return node;
    }

    @Override
    public String toString() {
        String str;
        switch (source) {
            case Input:
                str =  "Id:" + id;
                break;
            case Scratch:
                str = node.toString();
                break;
            default: //case Tree
                str = PrettyPrinter.makeTreeString(opType, children);
        }

        return str;
    }

    public Operator getOperator(){
        int arity = children == null ? 0 : children.size();
        return opType.getOperator(arity);
    }
}
