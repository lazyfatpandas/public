package passes.JoinOptimization;

import ir.IExpr;
import ir.Stmt.AssignStmt;
import ir.Stmt.AssignmentStmtSoot;
import ir.expr.Call;
import ir.expr.PlainStr;
import ir.expr.Str;
import ir.internalast.Keyword;
import ir.manipulation.domain.JoinOp;
import ir.manipulation.domain.MergeOp;
import soot.Unit;

import java.util.ArrayList;
import java.util.List;

public class JoinAttributes {
    Unit joinUnit;
    List<String> attributes;
    boolean isLeftIndex;
    boolean isRightIndex;


    public JoinAttributes(Unit unit){
        this.joinUnit=unit;
        this.attributes=new ArrayList<>();
        processUnit();
    }
    private void processUnit(){
        //Original: result_opt_merge = pd.merge(user_usage_opt,user_device_opt,on='use_id', how='outer',indicator=True)
        //Opt: user_usage.join(user_device_opt,on='use_id',how='outer')
        //
        //Check pre-condtions for transformations

            if (joinUnit instanceof AssignmentStmtSoot) {
                AssignStmt assignStmt = ((AssignmentStmtSoot) joinUnit).getAssignStmt();
                if (assignStmt.getRHS() instanceof Call) {
                    Call call = (Call) assignStmt.getRHS();
                    MergeOp mergeOp = new MergeOp(call);
                    if(mergeOp.isOptimizable()){
                        JoinOp joinOp=mergeOp.transformToJoinOp();
                        joinOp.updateCall();
//                    List<Keyword> keywords = call.getKeywords();
//                    for (Keyword keyword : keywords) {
//                        if (keyword.getArg().equals("left_index")) {
//                            isLeftIndex = true;
//                        }
//                        if (keyword.getArg().equals("right_index")) {
//                            this.isRightIndex = true;
//                        }
//                    }
//                    if (!isLeftIndex) {
//                        Keyword keyword = new Keyword();
//                        keyword.setArg("left_index");
//                        keyword.setValue(new PlainStr("True"));
//                        call.getKeywords().add(keyword);
//                    }
//                    if (!isRightIndex) {
//                        Keyword keyword = new Keyword();
//                        keyword.setArg("right_index");
//                        keyword.setValue(new PlainStr("True"));
//                        call.getKeywords().add(keyword);
                    }
                }//isOptimizable
                }//call

            }//if


    }


