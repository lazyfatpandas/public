package eqsql.dbridge.analysis.eqsql.scirpyUtils;

import eqsql.dbridge.analysis.eqsql.expr.node.*;
import ir.expr.Compare;
import soot.Value;

public class IfStmtConsHelper {
    Node condNode = null;


    public Node getCondNode(Value condition) {
        Compare compare=(Compare)condition;
        String operator=compare.getOps().get(0).toString();
        if(operator.equals("Gt")){
            condNode = new GtNode(NodeFactory.constructFromValue(compare.getOp1()), NodeFactory.constructFromValue(compare.getOp2()));

        }
        else if(operator.equals("Gte")){

        }
        else if(operator.equals("Lt")){
            condNode = new LtNode(NodeFactory.constructFromValue(compare.getOp1()), NodeFactory.constructFromValue(compare.getOp2()));

        }
        else if(operator.equals("Lte")){

        }
        else if(operator.equals("NotEq")){
            condNode = new NotEqNode(NodeFactory.constructFromValue(compare.getOp1()), NodeFactory.constructFromValue(compare.getOp2()));
        }
        else if(operator.equals("Eq")){
            condNode = new EqNode(NodeFactory.constructFromValue(compare.getOp1()), NodeFactory.constructFromValue(compare.getOp2()));

        }


        assert (condNode!=null);
        return condNode;
    }


}
