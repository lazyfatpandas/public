package eqsql.dbridge.analysis.eqsql.expr.operator;

import java.lang.reflect.Constructor;

/**
 * Created by ek on 23/5/16.
 */
public enum OpType {
    And (AndOp.class),
    Bottom (BottomOp.class),
    ClassRef (ClassRefOp.class),
    Eq (EqOp.class),
    Exist (ExistOp.class),
    FieldRef (FieldRefOp.class),
    Fold (FoldOp.class),
    FuncParams (FuncParamsOp.class, true),
    MethodHasNext (MethodHasNextOp.class),
    If (IfOp.class),
    InvokeMethod (InvokeMethodOp.class),
    MethodInsert (MethodInsertOp.class),
    MethodIterator (MethodIteratorOp.class),
    MethodNext (MethodNextOp.class),
    NotEq (NotEqOp.class),
    NotExist (NotExistOp.class),
    Or (OrOp.class),
    Param (ParamOp.class),
    Project (ProjectOp.class),
    Select (SelectOp.class),
    SelfRef (SelfRefOp.class),
    StringConst (StringConstOp.class),
    Ternary (TernaryOp.class),
    Value (ValueOp.class),
    Zero (ZeroOp.class),
    Any (AnyOp.class),
    FuncExpr (FuncExprOp.class),
    MethodBooleanValue (MethodBooleanValueOp.class),
    Dao (DaoOp.class),
    ArithAdd (ArithAddOp.class),
    CountStar (CountStarOp.class),
    One (OneOp.class),
    Gt (GtOp.class),
    Lt (LtOp.class),
    GroupBy (GroupByOp.class),
    CartesianProd (CartesianProdOp.class, true),
    Var (VarOp.class),
    MethodMapPut (MethodMapPutOp.class),
    PlaceholderVar(PlaceholderVarOp.class),
    LazyFetch(LazyFetchOp.class),
    Seq(SeqOp.class), Update(UpdateOp.class), Prefetch(PrefetchOp.class);

    /* Enum state definition follows. */
    private Class assocClass;
    private final boolean hasVarArity;

    OpType(Class _assocClass){
        this(_assocClass, false);
    }

    OpType(Class _assocClass, boolean hasVarArity){
        this.assocClass = _assocClass;
        this.hasVarArity = hasVarArity;
    }

    /**
     * If the operator has variable arity, use the passed arity to construct an
     * operator with that varity. Otherwise, simply construct an operator.
     * Note: a new operator is created.
     */
    public Operator getOperator(int arity){
        return hasVarArity ?
                getOperatorHelper(arity) :
                getOperatorHelper();
    }

    private Operator getOperatorHelper(int arity) {
        Operator operator;
        try {
            Constructor cons = assocClass.getDeclaredConstructor(int.class);
            operator = (Operator) cons.newInstance(arity);
        } catch (Exception e) {
            e.printStackTrace();
            operator = null;
        }
        return operator;
    }

    private Operator getOperatorHelper(){
        Operator operator;
        try {
            Constructor cons = assocClass.getDeclaredConstructor();
            operator = (Operator) cons.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            operator = null;
        }
        return operator;
    }

}
