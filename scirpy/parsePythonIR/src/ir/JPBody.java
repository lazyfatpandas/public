package ir;
import ir.Stmt.FunctionDefStmt;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.jimple.StmtBody;
import soot.util.Chain;
import soot.util.HashChain;

import java.util.ArrayList;
import java.util.List;

public class JPBody extends JimpleBody {
    /* TODO  Implement soot method for local chain, trap chain and unitchain....gets following declared variables from Body for the same
    SootMethod method;
    Chain<Local> localChain;
    Chain<Trap> trapChain;
    PatchingChain<Unit> unitChain;
*/

    public JPBody(JPMethod method, PatchingChain<Unit> unitChain,
                  Chain<Local> localChain) {
        //calls constructor for soot.Body
        super(method);
        //set unitChain of soot.Body
        this.unitChain  = unitChain;
        //set localChain of soot.Body
        this.localChain = localChain;
    }

    public JPBody() {
        super();

    }







    @Override
    public Object clone() {
        //TODO verify its correctness and clone all individual statements
        PatchingChain<Unit> unitChainClone=new PatchingChain<Unit>(new HashChain<Unit>());
        Chain<Local> localsClone = new HashChain<Local>();
        for(Unit unit:unitChain){
            unitChainClone.add((Unit)unit.clone());
        }
        List<Local> parameterTypes=new ArrayList();
        //String name, List parameterTypes, JPBody body)
        JPMethod jpMethod=new JPMethod("Module", parameterTypes, null);
        JPBody jpBody=new JPBody(jpMethod, unitChainClone, localsClone);
        jpBody.getMethod().setActiveBody(jpBody);
        //return jpBody;
        return this;
    }
}
