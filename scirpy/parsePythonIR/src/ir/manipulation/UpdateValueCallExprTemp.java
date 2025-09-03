package ir.manipulation;

import ir.IExpr;
import ir.Stmt.CallExprStmt;
import ir.expr.*;
import ir.internalast.JPValueBox;
import soot.Local;

import java.util.List;

public class UpdateValueCallExprTemp {
    /*
    //This is to return used variables in the expression
    private void updateValueBoxes(IExpr callExpr) {
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
                    List<IExpr> elts=((ListComp)arg).getElts();
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


    }*/
}
