package ir.expr;

import ast.ExprAST;
import ir.IExpr;
import ir.JPExpr;
import ir.internalast.JPValueBox;
import ir.internalast.Slice;
import ir.util.CRegion;
import org.jboss.util.NotImplementedException;
import soot.Local;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ConditionExpr;

import java.util.ArrayList;
import java.util.List;

public class Attribute extends JPExpr implements CRegion, Local, ConditionExpr {
    ExprAST ast_type=ExprAST.Attribute;
    String attr;
    int col_offset;
    int lineno;
    Expr_Context ctx=Expr_Context.Load;
    IExpr value;
    //TODO check if this is the right place for slice
    Slice slice=null;

    String baseName = null; // Chiranmoy 8-2-24: the base of attribute calls like pd in pd.merge() and df in df.fare_amount.min()

    public Attribute(ExprAST ast_type, String attr, int col_offset, int lineno, Expr_Context ctx, IExpr value, Slice slice) {
        this.ast_type = ast_type;
        this.attr = attr;
        this.col_offset = col_offset;
        this.lineno = lineno;
        this.ctx = ctx;
        this.value = value;
        this.slice = slice;
    }

    public Attribute(String attr, IExpr value) {
        this.attr = attr;
        this.value = value;
    }

    public Attribute() {
    }

    //GETTER SETTER


    public ExprAST getAst_type() {
        return ast_type;
    }

    public void setAst_type(ExprAST ast_type) {
        this.ast_type = ast_type;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
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

    @Override
    public String getName() {
        return attr;
    }

    @Override
    public void setName(String s) {

    }

    @Override
    public void setType(Type type) {

    }

    @Override
    public void setNumber(int i) {

    }

    @Override
    public int getNumber() {
        return 0;
    }

    @Override
    public Value getOp1() {
        return null;
    }

    @Override
    public Value getOp2() {
        return value;
    }

    @Override
    public ValueBox getOp1Box() {
        return null;
    }

    @Override
    public ValueBox getOp2Box() {
        JPValueBox valueBox = new JPValueBox(value);
        return valueBox;

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


    //See if this right,,
    @Override
    public String toString() {
        //TODO implement properly toString of call
        String stmt=value.toString();
        if(attr!=null){
            String attribute=attr;
            if(slice!=null &&(slice.getLower()!=null || slice.getUpper()!=null || slice.getStep()!=null)){
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
                attribute=attribute+"]";
            }
            //3.01
            if(slice!=null && slice.getIndex()!=null ){
//                if(slice!=null && slice.getIndex()!=null && !(slice.getIndex() instanceof Constant)){
                    attribute=attribute+"[";
                    attribute=attribute+slice.getIndex().toString();
                    attribute=attribute+"]";

            }
            //3.01
//            if(slice!=null && slice.getIndex()!=null && slice.getIndex() instanceof Constant){
//                attribute=attribute+"[";
//                attribute=attribute+"'"+slice.getIndex().toString()+"'";
//                attribute=attribute+"]";
//            }
            stmt=stmt+"."+attribute;
        }
        return stmt;

    }

    @Override
    public Object clone() {
        Attribute attribute;
        Slice sliceClone=null;
        IExpr valClone=null;
        if(this.slice!=null)
                sliceClone=(Slice)this.slice.clone();
        if(this.value!=null)
            valClone=(IExpr)this.value.clone();
        attribute=new Attribute(this.ast_type,  this.attr, this.col_offset, this.lineno, this.ctx,valClone , sliceClone);
        return attribute;
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        listL.addAll(value.getLocals());
        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        return value.getDataFrames();
    }

    public String getBaseName() {
        return baseName == null ? "$_#DUMMY$_#" : baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public boolean isDataFrame() {
        return value.isDataFrame();
    }
}
