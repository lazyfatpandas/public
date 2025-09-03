package ir.expr;
/* bhu created on 16/5/20  */


import ast.ExprAST;
import ir.IExpr;
import ir.JPExpr;
import ir.internalast.Slice;
import ir.util.CRegion;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ConditionExpr;

import java.util.ArrayList;
import java.util.List;

public class Subscript extends JPExpr implements CRegion, Local, ConditionExpr  {
    ExprAST ast_type=ExprAST.Subscript;

    int col_offset;
    int lineno;
    Expr_Context ctx=Expr_Context.Load;
    IExpr value;
    //TODO check if this is the right place for slice
    Slice slice=null;
    String baseName = null;

    public Subscript(ExprAST ast_type, int col_offset, int lineno, Expr_Context ctx, IExpr value, Slice slice) {
        this.ast_type = ast_type;
        this.col_offset = col_offset;
        this.lineno = lineno;
        this.ctx = ctx;
        this.value = value;
        this.slice = slice;
    }
    public Subscript() {

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

    public Expr_Context getCtx() {
        return ctx;
    }

    public void setCtx(Expr_Context ctx) {
        this.ctx = ctx;
    }

    public IExpr getValue() {
        return value;
    }

    public void setValue(IExpr value) {
        this.value = value;
    }

    public Slice getSlice() {
        return slice;
    }

    public void setSlice(Slice slice) {
        this.slice = slice;
    }

    public String getBaseName() {
        return baseName == null ? "$_#DUMMY$_#" : baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    //See if this right,,
    @Override
    public String toString() {
        //TODO implement properly toString of call
        String stmt=value.toString();
            String attribute="";
            if(slice!=null &&((slice.getLower()!=null || slice.getUpper()!=null || slice.getStep()!=null) && slice.getValue()!=null)){
                attribute=attribute+"[";
                //TODO streamline this for corner cases
                if(slice.getLower()!=null && slice.getUpper()==null ) {
                    attribute=attribute+slice.getLower().toString()+":";
                }
                else if(slice.getLower()!=null && (slice.getUpper()!=null ||slice.getStep()!=null) ) {
                    attribute=attribute+slice.getLower().toString();
                }
                if(slice.getUpper()!=null){
                    attribute=attribute+ ":" + slice.getUpper().toString();
                }
                if(slice.getStep()!=null){
                    attribute=attribute+ ":" + slice.getStep().toString();
                }
                if(slice.getValue()!=null){
                    //Did not use single quotes as quotes will come from name
                    attribute=attribute+  slice.getValue().toString();
                }
                attribute=attribute+"]";
                stmt=stmt+"."+attribute;
            }
            //TODO added for US_Pop Error
            else if(slice!=null &&((slice.getLower()==null || slice.getUpper()==null || slice.getStep()==null) && slice.getValue()!=null && slice.getValue() instanceof Str )){
                attribute=attribute+"[";
                attribute=attribute+  slice.getValue().toString();
                attribute=attribute+"]";
                stmt=stmt+attribute;

            }
            //21-11-2021 added for merge example 1 (phone device error)
            else if(slice!=null &&((slice.getLower()==null || slice.getUpper()==null || slice.getStep()==null) && slice.getValue()!=null && slice.getValue() instanceof ListComp )){
                attribute=attribute+"[";
                attribute=attribute+  slice.getValue().toString();
                attribute=attribute+"]";
                stmt=stmt+attribute;

            }
            //TODO verify this if problem comes again in detail
            //3.01
            else if(slice!=null && slice.getIndex()!=null){
                attribute=attribute+"[";
                attribute=attribute+slice.getIndex().toString();
                attribute=attribute+"]";
                stmt=stmt+attribute;
            }
//           else if(slice!=null && slice.getIndex()!=null && !(slice.getIndex() instanceof Constant)){
//                attribute=attribute+"[";
//                attribute=attribute+slice.getIndex().toString();
//                attribute=attribute+"]";
//                stmt=stmt+attribute;
//            }
//           //2.01 Python 3.9 change
//            else if(slice!=null && slice.getIndex()!=null && slice.getIndex() instanceof Constant){
//                attribute=attribute+"[";
//                attribute=attribute+"'"+slice.getIndex().toString()+"'";
//                attribute=attribute+"]";
//                stmt=stmt+attribute;
//            }
            // 3.1 MSDF update
         else if(slice!=null && slice.getIndex()==null && slice.getStep()==null && slice.getValue()==null && ( slice.getLower()!=null || slice.getUpper()!=null ) ){
            attribute=attribute+"[";
            if(slice.getLower()!=null) {
              attribute = attribute + slice.getLower().toString();
              }
            attribute=attribute+":";
            if(slice.getUpper()!=null) {
               attribute = attribute + slice.getUpper().toString();
               }

            attribute=attribute+"]";
            stmt=stmt+attribute;
        }
         //3.2 for listcomp
            else if(slice!=null && slice.getIndex()==null && slice.getStep()==null && slice.getValue()==null && slice.getListComp()!=null ){
                ListComp listComp=(ListComp)slice.getListComp();
                attribute=attribute+listComp.toString();
                stmt=stmt+attribute;
            }
        return stmt;

    }

    @Override
    public Object clone() {
        Subscript sc;
        Slice sliceClone=null;
        IExpr valClone=null;
        if(this.slice!=null)
            sliceClone=(Slice)this.slice.clone();
        if(this.value!=null)
            valClone=(IExpr)this.value.clone();
        sc=new Subscript(this.ast_type, this.col_offset, this.lineno, this.ctx,valClone , sliceClone);
        return sc;
    }

    @Override
    public String getName() {

        return null;
    }

    @Override
    public void setName(String s) {

    }

    @Override
    public void setType(Type type) {

    }

    @Override
    public Value getOp1() {
        return null;
    }

    @Override
    public Value getOp2() {
        return null;
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

    @Override
    public void setNumber(int i) {

    }

    @Override
    public int getNumber() {
        return 0;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        if(value!=null)
            listL.addAll(value.getLocals());
        if(slice!=null)
            listL.addAll(slice.getLocals());
        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        return value.getDataFrames();
    }

    @Override
    public boolean isDataFrame() {
        return value != null && value.isDataFrame();
    }
}
