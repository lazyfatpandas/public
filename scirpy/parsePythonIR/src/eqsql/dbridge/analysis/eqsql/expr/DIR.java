package eqsql.dbridge.analysis.eqsql.expr;

import dbridge.analysis.region.regions.ARegion;
import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.RetVarNode;
import eqsql.dbridge.analysis.eqsql.expr.node.VarNode;
import eqsql.dbridge.analysis.eqsql.trans.TransDriver;
import soot.Type;
import soot.Unit;
import soot.jimple.Stmt;

import java.util.*;

public class DIR {
    private final Map<VarNode, Node> veMap;
    private ARegion region;

    public DIR() {
        veMap = new HashMap<>();
    }

    public void insert(VarNode target, Node expr) {
        //Simplify expression and insert. Simplification here is optional,
        //but we do it to reduce tree size as early as possible (mainly to
        //make debugging easier).
        expr = TransDriver.applySimpliRules(expr);
        veMap.put(target, expr);
    }

    /** Insert an entry into the veMap, also keeping track of the original
     * program statment that was responsible for creating this entry. This
     * information is used only in code generation,
     * for those nodes which are unaffected by the transformations we
     * perform (example: Updates). Consequently, this information is stored
     * along with the expression.*/
    public void insert(VarNode target, Node expr, Unit... units){
        for (Unit unit : units) {
            if(unit instanceof Stmt) {
                expr.getOperator().addStmts((Stmt) unit);
            }
        }

        insert(target, expr);
    }

    public Map<VarNode, Node> getVeMap() {
        return veMap;
    }

    /**
     * copy  constructor - shallow copy
     */
    public DIR(DIR eeDag) {
        this.veMap = new HashMap<>();
        for (Map.Entry<VarNode, Node> entry : eeDag.getVeMap().entrySet()) {
            this.veMap.put(entry.getKey(), entry.getValue());
        }
    }

    public boolean contains(VarNode key){
        Node dag = this.find(key);
        return (dag != null);
    }

    public Node find(VarNode key) {
        if (veMap.containsKey(key)) {
            return veMap.get(key);
        }
        return null;
    }

    /* Return the type of the return variable if present,
    * if not, return null. */
    public Type findRetVarType(){
        VarNode retVarKey = RetVarNode.getARetVar();
        for (Map.Entry<VarNode, Node> entry : veMap.entrySet()) {
            VarNode key = entry.getKey();
            if(key.equals(retVarKey)){
                assert key instanceof RetVarNode;
                return ((RetVarNode)key).getOrigRetVarType();
            }
        }
        return null;
    }

    public Map.Entry findEntry(VarNode key){
        for (Map.Entry<VarNode, Node> entry : veMap.entrySet()) {
            if(entry.getKey().equals(key)){
                return entry;
            }
        }
        return null;
    }

    public Set<VarNode> getVars(){
        if(!isEmpty()) {
            return veMap.keySet();
        }
        return new HashSet<>(); //empty Set
    }

    public boolean isEmpty() {
        return veMap == null || veMap.isEmpty();
    }

    public String toString() {
        /* Sort the keys so that they are concatenated in order */
        List<VarNode> keys = new ArrayList<>();
        keys.addAll(veMap.keySet());
        Collections.sort(keys);

        String toStr = "";
        for (VarNode key : keys) {
            toStr += "~~~ " + key + " ~~~\n";
            toStr += veMap.get(key) + "\n\n";
        }

        return toStr;
    }

    public ARegion getRegion() {
		return region;
	}

    /** Update the region for each node in the DIR.veMap */
	public void updateRegion(ARegion region) {
        for (Map.Entry<VarNode, Node> entry : veMap.entrySet()) {
            entry.getValue().setRegion(region);
        }

    }

    public boolean hasUpdate() {
        return getVars().contains(VarNode.getUpdateVar());
    }
}