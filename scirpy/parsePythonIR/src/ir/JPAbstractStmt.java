package ir;
import ast.StmtAST;
import soot.jimple.internal.AbstractStmt;
import soot.Value;
import soot.ValueBox;
import soot.Unit;
import soot.UnitBox;
import soot.UnitPrinter;

public abstract class JPAbstractStmt extends AbstractStmt implements IStmt {

    public int lineno; //-10 indicates don't print
    public int col_offset;


    public boolean branches() {
        return true;
    }

    public boolean fallsThrough() {
        return true;
    }

    @Override
    //TODO check this and do deep cloning if required
    public Object clone() {
        return this;
    }

    @Override
    //TODO check this and do string formatting if required or remove it
    public void toString(UnitPrinter unitPrinter) {
        //System.out.println(this.toString());
    }
}
