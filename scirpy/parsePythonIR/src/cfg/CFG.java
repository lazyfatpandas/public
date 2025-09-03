package cfg;

import ir.JPMethod;
import ir.JPStmt;

public class CFG {
        // Method to which this cfg belongs
        private JPMethod method;

        // cfg for method above
        private JPUnitGraph unitGraph;

    public CFG(JPMethod method) {
        this.method = method;
        this.unitGraph = new JPUnitGraph(method.getBody());
    }

    public JPMethod getMethod() {
        return method;
    }

    public void setMethod(JPMethod method) {
        this.method = method;
    }

    public JPUnitGraph getUnitGraph() {
        return unitGraph;
    }

    public void setUnitGraph(JPUnitGraph unitGraph) {
        this.unitGraph = unitGraph;
    }

    /* This is required because, every time we analyze the method,
     its cfg has to be constructed again ,
     as it may have been modified since last CFG construction
     */
    public void rebuildCFG(){
        this.unitGraph = new JPUnitGraph(method.getBody());

    }
}
