package ir.manipulation;

import analysis.PythonScene;
import ir.IExpr;
import ir.Stmt.CallExprStmt;
import ir.expr.*;
import ir.internalast.JPValueBox;
import ir.internalast.Slice;
import soot.Local;
import soot.Unit;
import soot.ValueBox;

import java.util.ArrayList;
import java.util.List;

public class UpdateValueBoxes {
    IExpr callExpr;
    List<ValueBox> usedVars=null;
    List<Local> listL=null;
    Unit currentUnit;
    public UpdateValueBoxes(){

    }
    public UpdateValueBoxes(Unit unit,IExpr callExpr,List<ValueBox> usedVars,  List<Local> listL ){
        this.callExpr=callExpr;
        this.usedVars=usedVars;
        this.listL=listL;
        this.currentUnit=unit;
        updateValueBoxes(callExpr);
    }
    public void updateValueBoxesCommon(Unit unit, IExpr callExpr,List<ValueBox> usedVars,  List<Local> listL ){
        this.callExpr=callExpr;
        this.usedVars=usedVars;
        this.listL=listL;
        this.currentUnit=unit;
        updateValueBoxes(callExpr);
    }
    private void updateValueBoxes(IExpr callExpr) {
        Name dfName=null;
        if(callExpr instanceof Call){
            Call call=(Call)callExpr;
            //for example of london data
            IExpr func =  ((Call) callExpr).getFunc();
            if( func instanceof Attribute){
                Attribute atr= (Attribute) func;
                if(atr.getValue() instanceof Name){
                    dfName=(Name)atr.getValue();
                }
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
                    //this file
                    else if(value instanceof Call){
                        //this file
                        updateValueBoxes(value);
                        String attr=attribute.getAttr();
                        Name dfNameLocal=getParentnameAttribute(value);
                        Name name=new Name(((Call) value).getLineno(), ((Call) value).getCol_offset(), attr, Expr_Context.Load);
                        //changed on 31/08/21
                        if(dfNameLocal!=null) {
                            name.setParent(dfNameLocal.getName());
                            JPValueBox valueBox = new JPValueBox(name);
                            usedVars.add(valueBox);
                        }
                    }

                }
                //by bhu for Magpie example case
                else if(atr.getValue() instanceof Subscript){
                    Subscript subscript=(Subscript)atr.getValue();
                    updateSubscriptValueBox(subscript);
                }
            }
            //End for example of london data
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
                    //3.0
                    else if(value instanceof Attribute){
                        updateValueBoxes(value);
                    }
                    //3.0 end
                }
                else if(arg instanceof Name){
                    JPValueBox valueBox = new JPValueBox(arg);
                    //TODO adding only local and not expressions, may not be able to use all analysis
                    //london data
                    if(dfName!=null)
                        ((Name)arg).setParent(dfName.id);
                    //london data end
                    usedVars.add(valueBox);
                    listL.add((Local) arg);

                }
                else if(arg instanceof ListComp){
                    List<IExpr> elts=((ListComp)arg).getElts();
                    for(IExpr elt:elts){
                        if(elt instanceof  Name){
                            //london data
                            if(dfName!=null)
                                ((Name)elt).setParent(dfName.id);
                            //london data end
                            JPValueBox valueBox = new JPValueBox(elt);
                            usedVars.add(valueBox);
                            listL.add((Local) elt);

                        }
                        else if(elt instanceof Str) {
                            //JPValueBox valueBox = new JPValueBox(elt);
                            //london data
                            Name name=new Name(((Str)elt).getS(),Expr_Context.Load);
                            if(dfName!=null)
                                name.setParent(dfName.id);
                            //london data end
                            JPValueBox valueBox = new JPValueBox(name);
                            usedVars.add(valueBox);
                            //usedVars.add(valueBox);

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

//                    JPValueBox valueBox = new JPValueBox(arg);
//                    //TODO adding only local and not expressions, may not be able to use all analysis
//                    usedVars.add(valueBox);
                    listL.addAll(arg.getLocals());

                }
                else if(arg instanceof Call){

                    CallExprStmt callExprStmt =new CallExprStmt();
                    callExprStmt.setCallExpr(arg);
                    usedVars.addAll(callExprStmt.usedVars);
                }
                else if(arg instanceof Subscript){
                    updateSubscriptValueBox((Subscript) arg);
                }
            }

//            //new changes by msahu : originally updateValueBoxes did not accept a parameter
//            IExpr func =  ((Call) callExpr).getFunc();
//            if( func instanceof Attribute){
//                Attribute atr= (Attribute) func;
//                if (atr.getValue() instanceof Call){
//                    updateValueBoxes(atr.getValue());
//                }
//                else if(atr.getValue() instanceof Attribute){
//                    Attribute attribute=(Attribute)atr.getValue();
//                    IExpr value =attribute.getValue();
//                    if(value instanceof Name){ //or Local
//                        ((Name) value).setParent(((Name) value).getName());
//                        Name name=new Name(((Name) value).getLineno(), ((Name) value).getCol_offset(), attribute.getAttr(), Expr_Context.Load);
//                        name.setParent(((Name) value).getName());
//                        JPValueBox valueBox = new JPValueBox(name);
//                        usedVars.add(valueBox);
//                    }
//                }
//                //by bhu for Magpie example case
//                else if(atr.getValue() instanceof Subscript){
//                    Subscript subscript=(Subscript)atr.getValue();
//                    updateSubscriptValueBox(subscript);
//                }
//            }
        }
        //Added after Nytaxi example
        else if(callExpr instanceof Subscript){
            Subscript subscript=(Subscript)callExpr;
            updateSubscriptValueBox(subscript);
        }

        else if(callExpr instanceof Attribute){
            Attribute attribute=(Attribute)callExpr;
            while(attribute.getValue() instanceof Attribute){
                attribute=(Attribute)(attribute.getValue());
            }
            if(attribute.getValue() instanceof Name){
                updateAttributeValueBox(attribute);
            }
        }
        else if(callExpr instanceof Compare){
            updateCompareValueBox(callExpr);
        }
        else if(callExpr instanceof BinOp){
            BinOp binOp=(BinOp) callExpr;
            updateValueBoxes(binOp.getLeft());
            updateValueBoxes(binOp.getRight());
        }


    }
    private void updateAttributeValueBox(Attribute attribute){
        IExpr value =attribute.getValue();
        if(value!=null && value instanceof Name){ //or Local
            ((Name) value).setParent(((Name) value).getName());
            Name name=new Name(((Name) value).getLineno(), ((Name) value).getCol_offset(), attribute.getAttr(), Expr_Context.Load);
            name.setParent(((Name) value).getName());
            JPValueBox valueBox = new JPValueBox(name);
            usedVars.add(valueBox);
        }
    }

    private void updateCompareValueBox(IExpr expr){
        Compare compare=(Compare)expr;
        for(IExpr comparator: compare.getComparators()){
            if(comparator instanceof Attribute){
                //TODO implement
                Attribute attribute=(Attribute)comparator;
                updateAttributeValueBox(attribute);
                addCompareColumn(attribute);

            }
        }
        if(compare.getLeft()!=null &&  compare.getLeft() instanceof Attribute){
            Attribute attribute=(Attribute)compare.getLeft();
            updateAttributeValueBox(attribute);
            addCompareColumn(attribute);
        }
        else if(compare.getLeft()!=null &&  compare.getLeft() instanceof Call){
            updateValueBoxes(compare.getLeft());
        }
    }


    private void updateSubscriptValueBox(Subscript subscript){
        boolean isGroupBy=false;
        //for groupby
        IExpr valueDFName=null;
        if(subscript.getValue()!=null){
            IExpr value= subscript.getValue();
            if(value instanceof Call){
                Call valueCall=(Call)value;
                if(valueCall.getFunc()!=null && valueCall.getFunc() instanceof Attribute){
                    Attribute valueFuncA=(Attribute)valueCall.getFunc();
                    //Since this is groupby statement followed by a count, add the two columns that are being used
                    if(valueFuncA.getAttr().equals("count")){

                    }
                    //Since this is groupby statement, add the two columns that are being used
                    else if(valueFuncA.getAttr().equals("groupby")){
                        isGroupBy=true;
                        PythonScene.isGroupBy=true;
                        PythonScene.groupbyStmts.add(this.currentUnit);
                        //if it is a groupby, it should be on some dataframe..
                        assert (valueFuncA.getValue() instanceof Name);
                        //if it is a groupby, it should have atleast one column
                        assert (valueCall.getArgs()!=null && valueCall.getArgs().size()>=1);
                        //this contains Name of df type is Name
                        valueDFName =valueFuncA.getValue();
                        //Here, we are taking all the columns in the groupby clause, can be more than one
                        for(IExpr arg:valueCall.getArgs()){
                            if(arg instanceof Str){
                                Name name=new Name(((Str) arg).getLineno(), ((Str) arg).getCol_offset(), ((Str) arg).getS(), Expr_Context.Load);
                                name.setParent(((Name) valueDFName).getName());
                                JPValueBox valueBox = new JPValueBox(name);
                                usedVars.add(valueBox);
                            }
                            else if(arg instanceof ListComp) {
                                ListComp listComp=(ListComp)arg;
                                if(listComp.getElts()!=null && listComp.getElts().size()>=1){
                                    for(IExpr elt: listComp.getElts()) {
                                        if (elt instanceof Str) {
                                            Name name = new Name(listComp.getLineno(), listComp.getCol_offset(),((Str) elt).getS(), Expr_Context.Load);
                                            name.setParent(((Name) valueDFName).getName());
                                            JPValueBox valueBox = new JPValueBox(name);
                                            usedVars.add(valueBox);
                                        }
                                    }
                                }
                            }
                            else {
                                //TODO if it is of type local/name, not handling that case...
                            }
                        }

                    }
                }

            }
            else if(value instanceof Name){
                valueDFName=(Name)value;
            }
        }

        //Subscript subscript=(Subscript)callExpr;
        if(subscript.getSlice()!=null){

            Slice slice=subscript.getSlice();
            //TODO not implemented lower upper and step
            if(slice.getIndex()!=null){
                IExpr index=slice.getIndex();
                if(index instanceof Compare){
                    Compare compare=(Compare)index;
                    for(IExpr comparator: compare.getComparators()){
                        if(comparator instanceof Attribute){
                            //TODO implement
                            Attribute attribute=(Attribute)comparator;
                            updateAttributeValueBox(attribute);
                            addCompareColumn(attribute);

                        }
                        else if(comparator instanceof BinOp){
                            BinOp binOp=(BinOp) callExpr;
                            updateValueBoxes(binOp.getLeft());
                            updateValueBoxes(binOp.getRight());
                        }
                    }
                    if(compare.getLeft()!=null &&  compare.getLeft() instanceof Attribute){
                        Attribute attribute=(Attribute)compare.getLeft();
                        updateAttributeValueBox(attribute);
                        addCompareColumn(attribute);
                    }
                    if(compare.getLeft()!=null &&  compare.getLeft() instanceof Subscript){
                        updateSubscriptValueBox((Subscript)(compare.getLeft()));
                        //addCompareColumn(attribute);
                    }

                }
                else if(index instanceof Str) {
                    // as part of groupby
                    Name name = new Name(((Str) index).getLineno(), ((Str) index).getCol_offset(), ((Str) index).getS(), Expr_Context.Load);
                    if (valueDFName!=null){
                        name.setParent(((Name) valueDFName).getName());
                    }
                    //not a groupBy, normal name shd come
                    else {
                        //todo improve this
                        //name.setParent(((Name) valueDFName).getName());
                    }
                    JPValueBox valueBox = new JPValueBox(name);
                    usedVars.add(valueBox);

                }
                else if(index instanceof BinOp){
                    BinOp binOp=(BinOp)index;
                    updateValueBoxes(binOp.getLeft());
                    updateValueBoxes(binOp.getRight());
                }
            }
            if(slice.getValue()!=null){
                IExpr sliceValue=slice.getValue();
                if(sliceValue instanceof Str){
                    Str str=(Str) sliceValue;
                    Name name = new Name(((Str) str).getLineno(), ((Str) str).getCol_offset(), ((Str) str).getS(), Expr_Context.Load);
                    if (valueDFName!=null){
                        name.setParent(((Name) valueDFName).getName());
                    }
                    JPValueBox valueBox=new JPValueBox(name);
                    usedVars.add(valueBox);

                }
                updateValueBoxes(slice.getValue());

            }
        }
        //TODO duplicate code commented
//        if(subscript.getValue()!=null){
//            updateValueBoxes(subscript.getValue());
//
//        }
    }
    private void updateSliceValueBox(Subscript subscript){

    }

    private void addCompareColumn(Attribute attribute){
        String columnName=attribute.getAttr();
        assert(attribute.getValue() instanceof Name);
        //Assuming it of type Name, will return id
        String dfname=attribute.getValue().toString();
        if(PythonScene.attributesUsedinCompareMap.get(dfname)==null){
            ArrayList<String> clmNames=new ArrayList<>();
            clmNames.add(columnName);
            PythonScene.attributesUsedinCompareMap.put(dfname,clmNames);
            PythonScene.compareStmtsDFFiltering.add(this.currentUnit);
        }
        else{
            PythonScene.attributesUsedinCompareMap.get(dfname).add(columnName);
        }
    }

    private Name getParentnameAttribute(IExpr attriOrCall){
        //Add info for groupby here
        Name dfName=null;
        if(attriOrCall instanceof Call){
            Call call=(Call)attriOrCall;
            IExpr func=call.getFunc();
            return getParentnameAttribute(func);
        }
        if(attriOrCall instanceof Attribute){
            Attribute attribute=(Attribute) attriOrCall;
            if(attribute.getValue() instanceof Name){
                dfName=(Name) (attribute.getValue());
            }
            else if(attribute.getValue() instanceof Call){
                return getParentnameAttribute(attribute.getValue());
            }
        }
        return dfName;
    }
}
