package ir;
import ir.expr.Name;
import soot.Local;
import soot.jimple.Expr;

import java.util.List;

public interface IExpr extends Expr {

   public List<Local> getLocals();
    public List<Name> getDataFrames(); // Chiranmoy 8-2-24, get dataframes in this expr
    public boolean isDataFrame();  // Chiranmoy: is the expr a dataframe

}
