package ir.Stmt;

import analysis.Pandas.PandasAPIs;
import analysis.PythonScene;
import ir.IExpr;
import ir.IStmt;
import ir.JPBody;
import ir.JPMethod;
import ir.expr.*;
import ir.internalast.JPValueBox;
import ir.internalast.Slice;
import ir.manipulation.UpdateMergeSelectionValueBox;
import ir.manipulation.UpdateValueBoxes;
import ir.util.UtilityFuncs;
import soot.*;
import soot.jimple.internal.JAssignStmt;
import soot.util.Chain;
import soot.util.HashChain;
//import util.PythonCall;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

//TODO check if this can be removed i.e. merged with AssignmentStmt
public class AssignmentStmtSoot extends JAssignStmt implements IStmt {
    AssignStmt assignStmt;
    Value variable=null, rvalue=null;
    //2.0
    List<ValueBox> usedVars=new ArrayList<>();
    List<ValueBox> modifiedVars=new ArrayList<>();
    List<Local> listL=new ArrayList<>();


    public AssignmentStmtSoot(Value variable, Value rvalue, AssignStmt assignStmt) {

        super(variable, rvalue);
        this.assignStmt=assignStmt;
        this.variable=variable;
        this.rvalue=rvalue;
        updateValueBoxes( assignStmt.rHS);
        PythonScene.updateVarUnitMap(variable.toString(),this);

    }
    public AssignmentStmtSoot(Value variable, Value rvalue, AugAssignStmt assignStmt) {

        super(variable, rvalue);
        this.variable=variable;
        this.rvalue=rvalue;
        this.assignStmt=assignStmt;
        updateValueBoxes(assignStmt.rHS);

    }

    public AssignStmt getAssignStmt() {
        return assignStmt;
    }

    @Override
    public int getLineno() {
        return assignStmt.getLineno();
    }

    @Override
    public boolean isModified() {
        return assignStmt.isModified();
    }

    public void setModified(boolean value) {
        assignStmt.setModified(value);
    }

    @Override
    public String getOriginalSource() {
        return assignStmt.getOriginalSource();
    }

    @Override
    public Object clone() {
//        AssignStmt assignStmtClone=(AssignStmt) assignStmt.clone();
//        Value lvalue=null;
//        for(IExpr target:assignStmtClone.getTargets()){
//            lvalue= target;
//
//        }
//        Value rvalue=assignStmtClone.getRHS();
//        AssignmentStmtSoot assignmentStmtSoot=new AssignmentStmtSoot(lvalue,rvalue,assignStmtClone);
//        return assignmentStmtSoot;
          return this;

    }
    public String toString() {
    return assignStmt.toString();
    }

    //For cfg
    //TODO this might be unsafe::Unsafe code removed, still issues might be there..
    public List<Local> getLocals(){
        List<Local> listL=new ArrayList<>();
        for(IExpr target:assignStmt.getTargets()){
            listL.addAll(target.getLocals());
        }
        listL.addAll(assignStmt.getRHS().getLocals());
        return listL;
    }
/*
    //3.0 Valueboxes for Assignment Statement
    private void updateValueBoxes(IExpr callExpr) {
        UpdateValueBoxes updateValueBoxes=new UpdateValueBoxes();
        updateValueBoxes.updateValueBoxesCommon(this,callExpr,usedVars,listL);
    }
  */
    //2.0 Valueboxes for Assignment Statement
    //This is to return used variables in the expression

    private void updateValueBoxes(IExpr callExpr) {

        Name dfName=null;

        //CODE to update useboxes when expression is converted to IR
        if(callExpr instanceof Call){

            Call call=(Call)callExpr;
            //for example of london data

            IExpr funcV=call.getFunc();
            if(funcV instanceof Attribute){
                Attribute funcAttribute=(Attribute)funcV;
//                if (funcAttribute.getValue()  instanceof Call){
//                    funcAttribute=((Call)funcAttribute).getFunc();
//                }
                if(funcAttribute.getValue() instanceof Name){
                    dfName=(Name)funcAttribute.getValue();
                }

                //Added on 10-06-2021, to add another clause for groupby stmt
                if (funcAttribute.getAttr().equals("groupby")) {
                    PythonScene.isGroupBy = true;
                    PythonScene.groupbyStmts.add(this);
                }
                //on 21-11-2021, clause for merge
                if (funcAttribute.getAttr().equals("merge")) {
                    PythonScene.isMerge = true;
                    PythonScene.mergeStmts.add(this);
                    UpdateMergeSelectionValueBox umesvb=new UpdateMergeSelectionValueBox();
                    umesvb.updateValueBoxesMerge(this);
                }

            }
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
                    //3.0
                    //this file
                    else if(value instanceof Call){
                        //this file
                        updateValueBoxes(value);
                        String attr=attribute.getAttr();
                        Name dfNameLocal=getParentnameAttribute(value);
                        Name name=new Name(((Call) value).getLineno(), ((Call) value).getCol_offset(), attr, Expr_Context.Load);
                        //3.1 if added for error
                        if(dfNameLocal!=null && !PandasAPIs.getAPIs().contains(name.id)) {
                            name.setParent(dfNameLocal.getName());
                            JPValueBox valueBox = new JPValueBox(name);
                            usedVars.add(valueBox);
                        }
//                        //TODO temp Fix:::Delete 01/05/2021
//                        if(dfNameLocal!=null && PandasAPIs.getAPIs().contains(name.id) && name.id.equals("columns")) {
//                            System.out.println("Can't optimize");
//                            System.exit(1);
//                        }

                    }
                }
                //by bhu for Magpie example case
                else if(atr.getValue() instanceof Subscript){
                    Subscript subscript=(Subscript)atr.getValue();
                    updateSubscriptValueBox(subscript);
                }
                //2024 May for bm31
               else if(atr.getValue() instanceof Compare){
                    updateCompareValueBox(atr.getValue());

                }
            }
            //End for example of london data
            //For each arguement, put it in use box in case og getdummies
            if(call instanceof Call && call.getFunc() instanceof Attribute){
                Attribute funcAttribute= (Attribute) call.getFunc();
                if(funcAttribute.getAttr() instanceof String && funcAttribute.getAttr().equals("get_dummies")){
                    PythonScene.unitGetDummiesMap.put(this,"yes");
                    if(call.getKeywords().size()==1 && call.getKeywords().get(0).getArg().equals("columns")){
                        updateGetDummiesUsedVars((Name)call.getArgs().get(0), (ListComp) call.getKeywords().get(0).getValue());


                    }
                }
            }
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
                    else if(value instanceof Attribute){
                        updateValueBoxes(value);
                    }
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
                    List <IExpr> elts=((ListComp)arg).getElts();
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
                else if(arg instanceof Dict){
                    if(PythonScene.groupbyStmts.contains(this)){
                        ArrayList<Unit> groupbyStmts=PythonScene.groupbyStmts;
                        //Since this is gorup by, args contain column name and aggregator as dict
                        Dict argDict=(Dict) arg;
                        for(IExpr key:argDict.getKeys()){
                            if(key instanceof Str){
                                Str clmName=(Str)key;
                                Name name=new Name(0,0, ((Str) key).getS(), Expr_Context.Load);
                                name.setParent(UtilityFuncs.getParentnameAttribute(funcV).getName());
                                //JPValueBox valueBox = new JPValueBox(value);
                                JPValueBox valueBox = new JPValueBox(name);

                                //System.out.println(((Name) value).getName());
                                usedVars.add(valueBox);
                            }
                            else   if(key instanceof Constant){
                                Constant constant=(Constant)key;
                                Name name=new Name(0,0, (constant.getValue()), Expr_Context.Load);
//                                Name name=new Name(0,0, ((Str) key).getS(), Expr_Context.Load);
                                name.setParent(UtilityFuncs.getParentnameAttribute(funcV).getName());
                                //JPValueBox valueBox = new JPValueBox(value);
                                JPValueBox valueBox = new JPValueBox(name);

                                //System.out.println(((Name) value).getName());
                                usedVars.add(valueBox);
                            }
                        }


                    }

                }
                else if(arg instanceof Lambda){
                    //TODO verify if args is required..
                    updateValueBoxes(((Lambda) arg).getBody());

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
            // 2024 BHuhshan for df['Industry'].value_counts().iloc[:20]
            if(attribute.getValue() instanceof Call){
                updateValueBoxes(attribute.getValue());

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
        else if(callExpr instanceof ListComp){
            if(this.variable instanceof Name ){
                Name varName=(Name)variable;
                ArrayList<String> listCols=new ArrayList<>();
                for(Object obj:((ListComp) callExpr).getElts()){
                    if (obj instanceof Constant){
                        Constant constant=(Constant) obj;
                        listCols.add(constant.getValue());

                    }
                }
            PythonScene.nameValueMap.put(varName.getName(),listCols);
            }

        }


    }
    public List getUseBoxes() {
        return usedVars;
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
        else if(compare.getLeft()!=null &&  compare.getLeft() instanceof Subscript){
            updateSubscriptValueBox((Subscript) compare.getLeft());
        }
    }


    private void updateSubscriptValueBox(Subscript subscript){
        boolean isGroupBy=false;
        //for groupby
        IExpr valueDFName=null;
        if(subscript.getValue()!=null) {
            IExpr value = subscript.getValue();
            if (value instanceof Call) {
                Call valueCall = (Call) value;
                if (valueCall.getFunc() != null && valueCall.getFunc() instanceof Attribute) {
                    Attribute valueFuncA = (Attribute) valueCall.getFunc();
                    //Since this is groupby statement followed by a count, add the two columns that are being used
                    if (valueFuncA.getAttr().equals("count")) {

                    }
                    //Since this is groupby statement, add the two columns that are being used
                    else if (valueFuncA.getAttr().equals("groupby")) {
                        isGroupBy = true;
                        PythonScene.isGroupBy = true;
                        PythonScene.groupbyStmts.add(this);
                        //if it is a groupby, it should be on some dataframe..
                        assert (valueFuncA.getValue() instanceof Name);
                        //if it is a groupby, it should have atleast one column
                        assert (valueCall.getArgs() != null && valueCall.getArgs().size() >= 1);
                        //this contains Name of df type is Name
                        //updated on 08 -10 -2024 dias example problem
                        if(valueFuncA.getValue() instanceof  Name) {
                            valueDFName = valueFuncA.getValue();
                        }
                        else if(valueFuncA.getValue() instanceof Subscript){
                            Subscript valueSub= (Subscript) valueFuncA.getValue();
                            valueDFName=valueSub.getValue();
                        }
                            else{
                                System.out.println("Can't Identify valuedfName");
                                System.exit(1);
                        }
                        //Here, we are taking all the columns in the groupby clause, can be more than one
                        for (IExpr arg : valueCall.getArgs()) {
                            if (arg instanceof Str) {
                                Name name = new Name(((Str) arg).getLineno(), ((Str) arg).getCol_offset(), ((Str) arg).getS(), Expr_Context.Load);
                                name.setParent(((Name) valueDFName).getName());
                                JPValueBox valueBox = new JPValueBox(name);
                                usedVars.add(valueBox);
                            } else if (arg instanceof ListComp) {
                                ListComp listComp = (ListComp) arg;
                                if (listComp.getElts() != null && listComp.getElts().size() >= 1) {
                                    for (IExpr elt : listComp.getElts()) {
                                        if (elt instanceof Str) {
                                            Name name = new Name(listComp.getLineno(), listComp.getCol_offset(), ((Str) elt).getS(), Expr_Context.Load);
                                            name.setParent(((Name) valueDFName).getName());
                                            JPValueBox valueBox = new JPValueBox(name);
                                            usedVars.add(valueBox);
                                        }
                                    }
                                }
                            } else {
                                //TODO if it is of type local/name, not handling that case...
                            }
                        }

                    }

                }

            } else if (value instanceof Name) {
                valueDFName = (Name) value;
            }
            //Added on 10/06/21 to take care of filter df
         if(valueDFName!=null){
            if (((Name) valueDFName).getName().equals(this.variable.toString())) {
                PythonScene.hasFilter = true;
                PythonScene.DFFilterOps.add(this);
            }
        }
            // End of Added on 10/06/21 to take care of filter df
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
                            BinOp binOp=(BinOp)comparator;
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
                else if(index instanceof Constant) {
                    // as part of groupby
                    Name name = new Name(((Constant) index).getLineno(), ((Constant) index).getCol_offset(), ((Constant) index).getValue(), Expr_Context.Load);
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
                else if (index instanceof Name){
                    Name varName=(Name) index;
                    if(PythonScene.nameValueMap.get(varName.getName())!=null){
                        for(String name:PythonScene.nameValueMap.get(varName.getName())){
                            Name nameN = new Name(name, Expr_Context.Load);
                            nameN.setParent(((Name) valueDFName).getName());
                            JPValueBox valueBox = new JPValueBox(nameN);
                            usedVars.add(valueBox);
                        }
                    }
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
        /* commented on 10-06-2021 as this is giving false results for comaprison examples
        String columnName=attribute.getAttr();
        assert(attribute.getValue() instanceof Name);
        //Assuming it of type Name, will return id
        String dfname=attribute.getValue().toString();
        if(PythonScene.attributesUsedinCompareMap.get(dfname)==null){
            ArrayList<String> clmNames=new ArrayList<>();
            clmNames.add(columnName);
            PythonScene.attributesUsedinCompareMap.put(dfname,clmNames);
            PythonScene.compareStmtsDFFiltering.add(this);
        }
        else{
            PythonScene.attributesUsedinCompareMap.get(dfname).add(columnName);
        }


         */
    }

    private Name getParentnameAttribute(IExpr attriOrCall){
//        //Add info for groupby here
//        Name dfName=null;
//        if(attriOrCall instanceof Call){
//            Call call=(Call)attriOrCall;
//            IExpr func=call.getFunc();
//            return getParentnameAttribute(func);
//        }
//        if(attriOrCall instanceof Attribute){
//            Attribute attribute=(Attribute) attriOrCall;
//            if(attribute.getValue() instanceof Name){
//                dfName=(Name) (attribute.getValue());
//            }
//            else if(attribute.getValue() instanceof Call){
//                return getParentnameAttribute(attribute.getValue());
//            }
//        }
//        return dfName;
        return UtilityFuncs.getParentnameAttribute(attriOrCall);
    }

    public List<ValueBox> getUsedVars() {
        return usedVars;
    }
    private void updateGetDummiesUsedVars(Name dfName,ListComp listComp){
        List<IExpr> elts=listComp.getElts();
        for(IExpr elt:elts){
            if(elt instanceof Constant){
                Constant constant=(Constant)elt;
                Name name=new Name(0,0, (constant.getValue()), Expr_Context.Load);
//                                Name name=new Name(0,0, ((Str) key).getS(), Expr_Context.Load);
                name.setParent(dfName.getName());
                //JPValueBox valueBox = new JPValueBox(value);
                JPValueBox valueBox = new JPValueBox(name);

                //System.out.println(((Name) value).getName());
                usedVars.add(valueBox);
            }


        }
    }
    @Override
    public List<Name> getDataFramesDefined() {
        return ((IExpr) variable).getDataFrames();
    }

    @Override
    public List<Name> getDataFramesUsed() {
        return ((IExpr) rvalue).getDataFrames();
    }
}
