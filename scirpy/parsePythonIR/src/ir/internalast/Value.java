package ir.internalast;

import ir.IExpr;

import java.util.List;

public class Value {
    List<IExpr> args;
    IExpr kwargs;
    List<IExpr> kwonlyargs;
    List<IExpr> kw_defaults;
    List<IExpr> defaults;
    InternalASTType ast_type;
    IExpr varargs;



}
