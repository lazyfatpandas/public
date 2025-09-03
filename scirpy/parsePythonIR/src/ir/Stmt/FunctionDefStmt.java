package ir.Stmt;
import ir.IExpr;
import ir.IStmt;
import ir.JPAbstractStmt;
import ir.JPBody;
import ir.expr.Name;
import ir.expr.Str;
import org.jboss.util.NotImplementedException;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

//TODO Check this abstract and convert to normal if req: implement clone of soot.Body
public class FunctionDefStmt extends JPAbstractStmt implements IStmt{
    Name name;
    List<IExpr> args=new ArrayList<>();
    JPBody body;
    int col_offset;
    int lineno;
    boolean modified=false;

    //TODO not implemented
    List<IExpr> defaults=new ArrayList<>(),kw_defaults=new ArrayList<>(),kwonlyargs=new ArrayList<>();
    IExpr kwarg=null,vararg=null;


    public List<IExpr> getArgs() {
        return args;
    }

    public void setArgs(List<IExpr> args) {
        this.args = args;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public JPBody getBody() {
        return body;
    }

    public void setBody(JPBody body) {
        this.body = body;
    }

    public int getCol_offset() {
        return col_offset;
    }

    public void setCol_offset(int col_offset) {
        this.col_offset = col_offset;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    @Override
    public boolean isModified() {
        return modified;
    }
    String sourceCode="";
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    @Override
    public String getOriginalSource() {
        return sourceCode;
    }


    //TODO verify this
    public String toString() {

        String str="def " + name +"(";
        for(IExpr arg: args){
            if(arg instanceof Str){
             str=str +((Str)((Str) arg)).getS()+",";
            }
            else {
                str = str + arg.toString() + (",");
            }
        }
        if(args!=null && args.size()>0) {
            str = str.substring(0, str.length() - 1);
        }
        str=str+"):";
        return str;
    }

    public List<Local> getLocals(){
        List<Local> listL=new ArrayList<>();
        for(IExpr target:getArgs()){
            listL.addAll(target.getLocals());
        }
        return listL;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        List<Name> dataframes = new ArrayList<>();
//        if(true)
//            throw new NotImplementedException("getDataFramesDefined not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public List<Name> getDataFramesUsed() {
        List<Name> dataframes = new ArrayList<>();
//        if(true)
//            throw new NotImplementedException("getDataFramesUsed not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }
}


