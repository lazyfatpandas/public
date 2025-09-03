package ir.Stmt;

import ir.IExpr;
import ir.IStmt;
import ir.expr.*;
import ir.manipulation.UpdateValueBoxes;
import org.jboss.util.NotImplementedException;
import soot.Local;
import soot.ValueBox;
import soot.jimple.Expr;
//import soot.JastAddJ.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/*
listL is added for localchain for callgraph
 */
//TODO This is intuition based...check if just expr statement is good enough for  statement
public class CallExprStmt extends ExprStmt implements Expr, IStmt {
    List<Local> listL=new ArrayList<>();
    IExpr callExpr=null;
    public List<ValueBox> usedVars=new ArrayList<>();
    List<ValueBox> modifiedVars=new ArrayList<>();
    public IExpr getCallExpr() {
        return callExpr;
    }
    int lineno;
    boolean modified=false;

    public void setCallExpr(IExpr callExpr) {
        this.callExpr = callExpr;
        updateValueBoxes(callExpr);
    }




    //This is to return used variables in the expression
    private void updateValueBoxes(IExpr callExpr) {
        UpdateValueBoxes updateValueBoxes=new UpdateValueBoxes();
        updateValueBoxes.updateValueBoxesCommon(this,callExpr,usedVars,listL);
        /*
        //CODE to update useboxes when expression is converted to IR
        if(callExpr instanceof Call){
            Call call=(Call)callExpr;
            //For each arguement, put it in use box
            for(IExpr arg:call.getArgs()){
                if(arg instanceof Attribute){
                    Attribute attribute=(Attribute)arg;
                    IExpr value =attribute.getValue();
                    if(value instanceof Name){ //or Local
                        //TODO adding only local and not expressions, may not be able to use all analysis
                        //Value v=value;

                        //TODO : THis is for testing: remove it if required
                        //((Name) value).setName(((Name) value).getName()+"_"+attribute.getAttr());
//                        ((Name) value).setName(attribute.getAttr());
//                        ((Name) value).setParent(((Name) value).getName());
                        ((Name) value).setParent(((Name) value).getName());
                        //((Name) value).setName(((Name) value).getName()+"_"+attribute.getAttr());

                        //TODO verify this fix
                        //((Name) value).setName(attribute.getAttr());

                        Name name=new Name(((Name) value).getLineno(), ((Name) value).getCol_offset(), attribute.getAttr(), Expr_Context.Load);
                        name.setParent(((Name) value).getName());
                        //JPValueBox valueBox = new JPValueBox(value);
                        JPValueBox valueBox = new JPValueBox(name);

                        //System.out.println(((Name) value).getName());
                        usedVars.add(valueBox);
                        //TODO this may(Very less Chance-->0) be unsafe
                        listL.add((Local) name);

                    }
                }
                else if(arg instanceof Name){
                    JPValueBox valueBox = new JPValueBox(arg);
                    //TODO adding only local and not expressions, may not be able to use all analysis
                    usedVars.add(valueBox);
                    listL.add((Local) arg);

                }
                else if(arg instanceof ListComp){
                    List <IExpr> elts=((ListComp)arg).getElts();
                    for(IExpr elt:elts){
                       if(elt instanceof  Name){
                           JPValueBox valueBox = new JPValueBox(elt);
                           usedVars.add(valueBox);
                           listL.add((Local) elt);

                       }
                       else if(elt instanceof Str) {
                           JPValueBox valueBox = new JPValueBox(elt);
                           usedVars.add(valueBox);
                           listL.add((Local) elt);

                       }
                       // change added by msahu
                       else if(elt instanceof  Attribute){
                           Attribute attribute=(Attribute)elt;
                           IExpr value =attribute.getValue();
                           if(value instanceof Name){ //or Local
                               //TODO adding only local and not expressions, may not be able to use all analysis
                               //Value v=value;

                               //TODO : THis is for testing: remove it if required
                               //((Name) value).setName(((Name) value).getName()+"_"+attribute.getAttr());
//                        ((Name) value).setName(attribute.getAttr());
//                        ((Name) value).setParent(((Name) value).getName());
                               ((Name) value).setParent(((Name) value).getName());
                               //((Name) value).setName(((Name) value).getName()+"_"+attribute.getAttr());

                               //TODO verify this fix
                               //((Name) value).setName(attribute.getAttr());

                               Name name=new Name(((Name) value).getLineno(), ((Name) value).getCol_offset(), attribute.getAttr(), Expr_Context.Load);
                               name.setParent(((Name) value).getName());
                               //JPValueBox valueBox = new JPValueBox(value);
                               JPValueBox valueBox = new JPValueBox(name);

                               //System.out.println(((Name) value).getName());
                               usedVars.add(valueBox);

                           }

                       }

                    }

                    JPValueBox valueBox = new JPValueBox(arg);
                    //TODO adding only local and not expressions, may not be able to use all analysis
                    usedVars.add(valueBox);
                    listL.addAll(arg.getLocals());

                }
                else if(arg instanceof Call){

                    CallExprStmt callExprStmt =new CallExprStmt();
                    callExprStmt.setCallExpr(arg);
                    usedVars.addAll(callExprStmt.usedVars);
                }
            }

            //new changes by msahu : originally updateValueBoxes did not accept a parameter
            IExpr func =  ((Call) callExpr).getFunc();
            if( func instanceof Attribute){
                Attribute atr= (Attribute) func;
                if (atr.getValue() instanceof Call){
                    updateValueBoxes(atr.getValue());
                }
                else if(atr.getValue() instanceof Attribute){
                    Attribute attribute=(Attribute)atr.getValue();
                    IExpr value =attribute.getValue();
                    if(value instanceof Name){ //or Local
                        ((Name) value).setParent(((Name) value).getName());
                        Name name=new Name(((Name) value).getLineno(), ((Name) value).getCol_offset(), attribute.getAttr(), Expr_Context.Load);
                        name.setParent(((Name) value).getName());
                        JPValueBox valueBox = new JPValueBox(name);
                        usedVars.add(valueBox);
                    }
                }
            }
            }

    */
    }
    public List getUseBoxes() {
        return usedVars;
    }

    public List getDefBoxes() {
        return modifiedVars;
    }


    @Override
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

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        //for testing
        //System.out.println(lineno);
        if(lineno==0){
            return callExpr.toString();
        }
        //for testing end
        return callExpr.toString();

    }
    String sourceCode="";
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    @Override
    public String getOriginalSource() {
        return sourceCode;
    }
    @Override
    public Object clone() {
        IExpr callExprClone=(IExpr) callExpr.clone();

        CallExprStmt callExprStmtClone=new CallExprStmt();
        callExprStmtClone.setCallExpr(callExprClone);
        callExprStmtClone.setLineno(lineno);
        callExprStmtClone.setSourceCode(sourceCode);
        return callExprStmtClone;

    }
    public List<Local> getLocals(){

        return this.listL;
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return callExpr.getDataFrames();
    }

    @Override
    public List<Name> getDataFramesDefined() {
        return new ArrayList<>();
    }

    public String getBaseName() {
        if(callExpr instanceof Attribute)
            return ((Attribute) callExpr).getBaseName();
        else if(callExpr instanceof Subscript)
            return ((Subscript) callExpr).getBaseName();
        else if(callExpr instanceof Call)
            return ((Call) callExpr).getBaseName();
        else if(callExpr instanceof Name)
            return ((Name) callExpr).getName();
        else
            throw new NotImplementedException("getBaseName not implemented in " + this.getClass().getSimpleName());
    }

    @Override
    public List<Name> getDataFrames() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDataFrame() {
        return callExpr.isDataFrame();
    }
}
