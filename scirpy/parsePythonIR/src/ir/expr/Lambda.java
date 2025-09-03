package ir.expr;

import ir.IExpr;
import ir.JPExpr;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class Lambda extends JPExpr {
    //Lambda(arguments args, expr body)
    List<IExpr> args=new ArrayList<>();
    IExpr body;
    int lineno;
    int col_offset;

    public List<IExpr> getArgs() {
        return args;
    }

    public void setArgs(List<IExpr> args) {
        this.args = args;
    }

    public IExpr getBody() {
        return body;
    }

    public void setBody(IExpr body) {
        this.body = body;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public int getCol_offset() {
        return col_offset;
    }

    public void setCol_offset(int col_offset) {
        this.col_offset = col_offset;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Object iExpr:args) {
                listL.addAll(((IExpr)iExpr).getLocals());
        }
        listL.addAll(body.getLocals());
        return listL;
    }

    @Override
    public String toString() {
        //TODO implement properly toString of call

        String stmt="lambda ";
        for(IExpr arg:args){
            stmt=stmt+ arg +", ";
        }
        stmt=stmt.substring(0,stmt.length()-2);
        stmt=stmt+": ";
        stmt=stmt+body.toString();
        return stmt;
    }


    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>();
//        if(true)
//            throw new NotImplementedException("getDataFrames not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public boolean isDataFrame() {
//        if(true)
//            throw new NotImplementedException("isDataFrameOp not implemented in " + this.getClass().getSimpleName());
        return false;
    }
}
