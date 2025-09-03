package ir.expr;

import ast.ExprAST;
import ir.IExpr;
import ir.JPExpr;
import ir.internalast.Keyword;
import ir.util.CRegion;
import soot.Local;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ConditionExpr;

import java.util.ArrayList;
import java.util.List;

public class Call extends JPExpr implements CRegion, ConditionExpr {
    List<IExpr> args=new ArrayList<>();
    ExprAST ast_type=ExprAST.Call;
    int col_offset;
    int lineno;
    //TODO add details of func here
    IExpr func;
    List<Keyword> keywords=new ArrayList<>();

    String baseName; // Chiranmoy 8-2-24: plt in plt.plot(...)

    public boolean isDataFrameOperation = false;

    //TODO: added for subscripted calls -check the best way to do this


    //GETTERS AND SETTERS


    public List<IExpr> getArgs() {
        return args;
    }

    public void setArgs(List<IExpr> args) {
        this.args = args;
    }

    public ExprAST getAst_type() {
        return ast_type;
    }

    public void setAst_type(ExprAST ast_type) {
        this.ast_type = ast_type;
    }

    public int getCol_offset() {
        return col_offset;
    }

    public void setCol_offset(int col_offset) {
        this.col_offset = col_offset;
    }

    public IExpr getFunc() {
        return func;
    }

    public void setFunc(IExpr func) {
        this.func = func;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List keywords) {
        this.keywords = keywords;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }


    @Override
    public String toString() {
        //TODO implement properly toString of call

        String stmt=func.toString();
        if(args==null || args.size()==0) {
            stmt = stmt + "(";
        }
        else{
            stmt=stmt+"(";
            for(IExpr iExpr:args){
                stmt=stmt+iExpr.toString()+ ",";
                //3.01
//                if(iExpr instanceof Constant) {
//                    stmt = stmt + "'"+ iExpr.toString() + "',";
//                }
//                else{
//                    stmt = stmt + iExpr.toString() + ",";
//                }
            }
            //to remove extra comma
            stmt=stmt.substring(0,stmt.length()-1);



        }
        if(keywords.size()>0){
            if(!(args==null || args.size()==0)){
                stmt=stmt+",";
            }

            for(Keyword keyword: keywords){
                //stmt=stmt+keyword.getArg()+"="+keyword.getValue().toString()+",";
                stmt=stmt+keyword.getArg()+"=";
                //3.01
//                if(keyword.getValue() instanceof Constant  && !keyword.getValue().toString().equals("False")){
//                    stmt=stmt+"'"+keyword.getValue().toString() + "',";
//                }
//                else {
//                    stmt=stmt+keyword.getValue().toString() + ",";
//                }
                stmt=stmt+keyword.getValue().toString() + ",";
            }
            //to remove extra comma
            stmt=stmt.substring(0,stmt.length()-1);

        }
        stmt=stmt+")";

        return stmt;

    }

    @Override
    public Object clone() {
    Call call=new Call();
    call.setLineno(this.lineno);
    call.setCol_offset(this.col_offset);
    call.setFunc((IExpr)this.func.clone());
    for(Keyword keyword:this.keywords){
        call.keywords.add((Keyword)keyword.clone());
    }
    for(IExpr arg:this.args){
        call.args.add((IExpr) arg.clone());
    }
    return call;
    }

    //START CONDITION***********************************************************************************
    //**************************************************************************************************
    @Override
    public Value getOp1() {
        return this.args.get(0);
    }

    @Override
    public Value getOp2() {
        return this.args.get(1);
    }

    @Override
    public ValueBox getOp1Box() {
        return null;
    }

    @Override
    public ValueBox getOp2Box() {
        return null;
    }

    @Override
    public void setOp1(Value value) {

    }

    @Override
    public void setOp2(Value value) {

    }

    @Override
    public String getSymbol() {
        return null;
    }
    //END CONDITION***********************************************************************************
    //    //**************************************************************************************************

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Object val:keywords) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }
        }
        for(Object val:args) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }
        }
         listL.addAll(func.getLocals());
        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        List<Name> dataframes = new ArrayList<>(func.getDataFrames());
        for(IExpr expr : args)
            dataframes.addAll(expr.getDataFrames());
        for(Keyword kwarg : keywords)
            dataframes.addAll(kwarg.getDataFrames());
        return dataframes;
    }

    public String getBaseName() {
        return baseName == null ? "$_#DUMMY$_#" : baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public boolean isDataFrame() {
        return func.isDataFrame();  // assuming df.some_func(), returns a dataframe
    }
}
