package ir.expr;

import ir.IExpr;
import org.jboss.util.NotImplementedException;
import soot.Local;
import soot.Value;
import soot.jimple.internal.JEqExpr;

import java.util.ArrayList;
import java.util.List;

public class JPEqExpr extends JEqExpr implements IExpr {
    Value op1;
    Value op2;
    public JPEqExpr(Value op1, Value op2) {

        super(op1, op2);
        assert (op1!=null && op2!=null);
        this.op1=op1;
        this.op2=op2;
    }

    public String toString(){
        return ( getOp1().toString() + " == " + getOp2().toString());
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        if(op1 instanceof Local )
            listL.add((Local) op1);
        if(op2 instanceof Local )
            listL.add((Local) op2);

        return listL;
    }


    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFrames not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
        if(true)
            throw new NotImplementedException("isDataFrameOp not implemented in " + this.getClass().getSimpleName());
        return false;
    }
}
