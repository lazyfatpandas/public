package ir.manipulation;

import ir.expr.Name;
//Todo this is not used
public class DfManager {
    boolean isLambda;
    Name lambdaDfName=null;



    public boolean isLambda() {
        return isLambda;
    }

    public void setLambda(boolean lambda) {
        isLambda = lambda;
    }

    public Name getLambdaDfName() {
        return lambdaDfName;
    }

    public void setLambdaDfName(Name lambdaDfName) {
        this.lambdaDfName = lambdaDfName;
    }
}
