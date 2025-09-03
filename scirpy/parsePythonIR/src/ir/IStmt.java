package ir;
import ir.expr.Name;
import soot.jimple.Stmt;

import java.util.List;

/*
Interface that links statement to soot statement. Should be implemented by any type of statement in JP.
Extra methods to process for each statement can be added here ///

TODO  1. Check what extra methods are required and how to use these extra methods
TODO  2. What all statments types should be implemented
 */



public interface IStmt extends Stmt {

    public int getLineno();
    public boolean isModified();
    public String getOriginalSource();

    // Chiranmoy 9-2-24: for Live Dataframe Analysis
    public List<Name> getDataFramesDefined();
    public List<Name> getDataFramesUsed();
}
