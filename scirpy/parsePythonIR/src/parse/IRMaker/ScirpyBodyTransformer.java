package parse.IRMaker;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.Stmt;

import java.util.Iterator;
import java.util.Map;

public class ScirpyBodyTransformer extends BodyTransformer {
    public ScirpyBodyTransformer() {
        super();
    }


    @Override
    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
        Iterator<Unit> it = b.getUnits().snapshotIterator();
        while(it.hasNext()){
            Stmt stmt = (Stmt)it.next();
           // System.out.println(stmt);
        }
    }
}
