package ir.manipulation.domain;

import analysis.PythonScene;
import ir.IExpr;
import ir.Stmt.AssignmentStmtSoot;
import ir.expr.*;
import ir.internalast.JPValueBox;
import ir.internalast.Keyword;
import ir.manipulation.UpdateMergeSelectionValueBox;
import soot.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinOp {
    //other, on=None, how='left', lsuffix='', rsuffix='', sort=False
    IExpr other=null;
    IExpr on=null;
    IExpr how=null;
    IExpr lsuffix=null;
    IExpr rsuffix=null;
    IExpr sort=null;
    Call call=null;
    MergeOp mergeop=null;

    //List<String> attributes= new ArrayList<>("other", "on", "how", "lsuffix", "rsuffix");
    Map<String, IExpr> nameAttributeMap = new HashMap<>();



    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }
    public void updateCall() {

        //Original: result_opt_merge = pd.merge(user_usage_opt,user_device_opt,on='use_id', how='outer',indicator=True)
        //Opt:              user_usage.join(user_device_opt,on='use_id',how='outer')
        //Till now:result = user_usage.join(user_usage,user_device[["use_id","platform","device"]],on="use_id",how="outer",indicator=True)
        //result =          user_usage.join(user_device[["use_id","platform","device"]],on="use_id",how="outer",indicator=True)
        IExpr funcV=call.getFunc();
        if(funcV instanceof Attribute && this.nameAttributeMap.get("on")!=null){
            Attribute funcAttribute=(Attribute)funcV;
            String funcVal=funcAttribute.getValue().toString();
            //only if Pandas merge for safer side
            if(funcVal.equals(PythonScene.pandasAliasName)) {
                assert(funcAttribute.getValue() instanceof Name);

               Name mergeCallPdName= (Name) funcAttribute.getValue();
               //set index for main df on on col
                Name otherDfName=null;
                if(this.nameAttributeMap.get("other") instanceof Name){
                    otherDfName=(Name)nameAttributeMap.get("other");
                }
                else if((this.nameAttributeMap.get("other") instanceof Subscript)){
                    Subscript otherSub=(Subscript)nameAttributeMap.get("other");
                    otherDfName=(Name)otherSub.getValue();
                }
               Unit otherDfUnit=PythonScene.dfNametoUnitMap.get(otherDfName.getName());

               Call otherDfCreationCall=(Call) ((AssignmentStmtSoot)otherDfUnit).getAssignStmt().getRHS();
               mergeCallPdName.setName(this.nameAttributeMap.get("mainDf").toString());
               Keyword index_keyword=new Keyword();
               index_keyword.setArg("index_col");
                if(this.mergeop.nameAttributeMap.get("on")!=null) {
                    index_keyword.setValue(this.nameAttributeMap.get("on"));
                } else if(this.mergeop.nameAttributeMap.get("right_on")!=null) {
                    index_keyword.setValue(this.nameAttributeMap.get("on"));
                }
                if(index_keyword.getValue()!=null){
                   otherDfCreationCall.getKeywords().add(index_keyword);
               }

                if (funcAttribute.getAttr().equals("merge")) {
                    funcAttribute.setAttr("join");
                }
                List<IExpr> newArgsList=new ArrayList<>();
                //Assuming that any column selection has been performed while fetching df
                //newArgsList.add(this.nameAttributeMap.get("other"));
                //Todo: Test if the optimization for column selection has taken place indeed
                newArgsList.add(otherDfName);
                call.setArgs(newArgsList);
                call.getKeywords().removeIf(keyword -> (this.nameAttributeMap.get(keyword.getArg())==null));
            }//if PandasAlias
        }    }

    public MergeOp getMergeop() {
        return mergeop;
    }

    public void setMergeop(MergeOp mergeop) {
        this.mergeop = mergeop;
    }
}
