package ir;
import ir.Stmt.FunctionDefStmt;
import ir.expr.Attribute;
import ir.util.SourceSnippet;
import soot.*;
import soot.jimple.toolkits.callgraph.Sources;

import java.util.ArrayList;
import java.util.List;

public class JPMethod  extends SootMethod{
    protected SourceSnippet sourceSnippet;
    private List<IExpr> paramNames = new ArrayList<>()	;

    //Changed on 29/11/2019 to add function definition
    FunctionDefStmt functionDefStmt;



    //TODO add more arguements later
    public JPMethod(String name, List parameterTypes, JPBody body) {
        //TODO check whether this call is correct or not!!
        super(name, parameterTypes, VoidType.v());
         if(body!=null){
             this.setActiveBody(body); //from SootMethod ;)
         }


    }
    public JPMethod(String name, List parameterTypes, JPBody body, int modifiers) {
        //TODO check whether this call is correct or not!!
        super(name, parameterTypes, VoidType.v(),modifiers);
        if(body!=null){
            this.setActiveBody(body); //from SootMethod ;)
        }


    }
    //JPMethod("main",Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),VoidType.v(), Modifier.PUBLIC | Modifier.STATIC)
    public JPMethod(String name, List parameterTypes, Type type, int modifiers) {
        //TODO check whether this call is correct or not!!
        super(name, parameterTypes, type, modifiers);



    }

    //Returns active body(using SootMethod's active body ;))
    public JPBody getBody() {
        return (JPBody) getActiveBody();
    }
    //Sets active body(using SootMethod's active body ;))
    public void setBody(JPBody body) {
        setActiveBody(body);
    }

    public void setFunctionDefStmt(FunctionDefStmt functionDefStmt) {
        this.functionDefStmt = functionDefStmt;
        paramNames=functionDefStmt.getArgs();
    }

    public String toString(){
        return getName();
    }
}
