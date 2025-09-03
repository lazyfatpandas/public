package ir.manipulation.domain;

import ir.IExpr;
import ir.expr.Call;
import ir.expr.Name;
import ir.expr.Subscript;
import ir.internalast.Keyword;

import java.util.*;

public class MergeOp {
    //left, right, how='inner', on=None, left_on=None, right_on=None, left_index=False, right_index=False, sort=False,
    // suffixes=('_x', '_y'), copy=True, indicator=False, validate=None
    public MergeOp(Call call) {
//        nameAttributeMap.put("left", this.left);
//        nameAttributeMap.put("right", this.right);
//        nameAttributeMap.put("how", this.how);
//        nameAttributeMap.put("on", this.on);
//        nameAttributeMap.put("left_on", this.left_on);
//        nameAttributeMap.put("right_on", this.right_on);
//        nameAttributeMap.put("left_index", this.left_index);
//        nameAttributeMap.put("right_index", this.right_index);
//        nameAttributeMap.put("sort", this.sort);
//        nameAttributeMap.put("suffixes", this.suffixes);
//        nameAttributeMap.put("copy", this.copy);
//        nameAttributeMap.put("indicator", this.indicator);
//        nameAttributeMap.put("validate", this.validate);
        this.call=call;
        List<Keyword> keywords= call.getKeywords();
        for (Keyword keyword : keywords) {
            String arg=keyword.getArg();
            nameAttributeMap.put(arg, keyword.getValue());
        }
        this.setOptimizable();
        IExpr arg1=null;
        IExpr arg2=null;
        final List<IExpr> argList=new ArrayList<>();
        if(call.getArgs().size()!=0 && call.getArgs().size()>1){
            arg1=call.getArgs().get(0);
            arg2=call.getArgs().get(1);
        }
        else{
            call.getKeywords().forEach(keyword -> {
                if(keyword.getArg().equals("left")){
                    argList.add(keyword.getValue());
                }
                if(keyword.getArg().equals("right")){
                    argList.add(keyword.getValue());
                }
            });
        }
        if(arg1==null) {
            arg1 = argList.get(0);
            arg2 = argList.get(1);
        }
        nameAttributeMap.put("left", arg1);
        nameAttributeMap.put("right", arg2);
    }

    public MergeOp() {

        }

    private void  setOptimizable(){
        //Todo: Implement this
        this.isOptimizable=true;
    }

    public boolean isOptimizable() {
        return isOptimizable;
    }

    List<IExpr> availableAttributes = new ArrayList<>();
        Map<String, IExpr> nameAttributeMap = new HashMap<>();
        Call call=null;

        boolean isOptimizable=false;
        IExpr left = null;
        IExpr right = null;
        IExpr how = null;
        IExpr on = null;

        IExpr left_on = null;
        IExpr right_on = null;

        IExpr left_index = null;
        IExpr right_index = null;

        IExpr sort = null;

        IExpr suffixes = null;
        IExpr copy = null;

        IExpr indicator = null;
        IExpr validate = null;


    public JoinOp transformToJoinOp(){
        JoinOp joinOp=new JoinOp();
        MergeOp mergeOp=this;
        String[] joinAtt={"other", "on", "how", "lsuffix", "rsuffix"};
        joinOp.nameAttributeMap.put("on",mergeOp.nameAttributeMap.get("on"));
        joinOp.nameAttributeMap.put("how",mergeOp.nameAttributeMap.get("how"));
        joinOp.nameAttributeMap.put("other",mergeOp.nameAttributeMap.get("right"));
        joinOp.nameAttributeMap.put("mainDf",mergeOp.nameAttributeMap.get("left"));
        joinOp.setCall(mergeOp.call);
        joinOp.setMergeop(this);
        return joinOp;

    }




}


