package passes.lasp;

import ir.IExpr;
import ir.Stmt.CallExprStmt;
import ir.expr.*;
import rewrite.pd1.Pd1Elt;
import soot.Unit;

import java.util.Iterator;
import java.util.List;

public class UpdatePrintStmts {


    public static void update(Unit unit, List<Pd1Elt> pdLists){

        assert(unit instanceof CallExprStmt);
        CallExprStmt callExprStmt=(CallExprStmt)unit;
        if(callExprStmt.getCallExpr() instanceof Call){
            Call call =(Call) callExprStmt.getCallExpr();
            if(call.getFunc() instanceof Name){
                Name funcName=(Name) call.getFunc();
                if(UpdatePrintStmts.requiresCompute(funcName.getName())){
                    //The args of the call may require compute as the results are being used
                    int i=0;
                    for(IExpr arg: call.getArgs()){

                        if(arg instanceof Name){
                            if(Common.isExistingDF((Name) arg, pdLists)){
                                Call computeCall=new Call();
                                Attribute attribute=new Attribute();
                                attribute.setAttr("compute");
                                attribute.setValue(arg);
                                computeCall.setFunc(attribute);
                                call.getArgs().set(i,computeCall);
                            }
                        }
                        else  if(arg instanceof JoinedStr){
                            //This is formatted print statement
                            List<IExpr> vals= ((JoinedStr) arg).getValues();
                            int j=0;
                            for(IExpr val:vals){
                                updateJoinedStrArgCallForCompute(val,pdLists, (JoinedStr)arg,j);
                                j++;
                            }

                        }
                        else if(arg instanceof Attribute){

                        }
                        else  if(arg instanceof Call){

                        }
                        i++;
                    }
                }
            }

        }
    }

    private static boolean requiresCompute(String api){
        boolean reqCompute=false;
        //Right now, updates only print statements, can update other APIs like matplot
        if(api.equals("print")){
            return true;
        }
        return reqCompute;
    }


    private static void updateJoinedStrArgCallForCompute(IExpr arg, List<Pd1Elt> pdLists, JoinedStr joinedStrArg, int i ){
        if(arg instanceof Name){
            if(Common.isExistingDF((Name) arg, pdLists)){
                joinedStrArg.getValues().set(i,getComputeCall(arg));
            }
        }
        else if(arg instanceof BinOp){
            joinedStrArg.getValues().set(i,updateBinOpForCompute(arg,pdLists));

        }
    }

    private static IExpr getComputeCall(IExpr arg){
                Call computeCall=new Call();
                Attribute attribute=new Attribute();
                attribute.setAttr("compute");
                attribute.setValue(arg);
                computeCall.setFunc(attribute);
                return computeCall;
    }
    private static IExpr updateBinOpForCompute(IExpr arg, List<Pd1Elt> pdLists){
        BinOp binOp=(BinOp)arg;
        IExpr left=binOp.getLeft();
        IExpr right=binOp.getRight();
        if(left instanceof Name){
            if(Common.isExistingDF((Name) left, pdLists)) {
                binOp.setLeft(getComputeCall(left));
            }
        }
        if(right instanceof Name){
            if(Common.isExistingDF((Name) right, pdLists)) {
                binOp.setRight(getComputeCall(left));
            }
        }
    return binOp;
    }


}

//    private static void updateArgCallForCompute(IExpr arg, List<Pd1Elt> pdLists, Call call, int i){
//        if(arg instanceof Name){
//            if(Common.isExistingDF((Name) arg, pdLists)){
//                Call computeCall=new Call();
//                Attribute attribute=new Attribute();
//                attribute.setAttr("compute");
//                attribute.setValue(arg);
//                computeCall.setFunc(attribute);
//                call.getArgs().set(i,computeCall);
//            }
//        }
//        else  if(arg instanceof JoinedStr){
//            //This is formatted print statement
//            List<IExpr> vals= ((JoinedStr) arg).getValues();
//            int j=0;
//            for(IExpr val:vals){
//                updateJoinedStrArgCallForCompute(val,pdLists,call,j);
//                j++;
//            }
//
//        }
//
//
//    }