package ir.internalast;
/* bhu created on 10/5/20  */


import ir.IExpr;
import soot.Local;

import java.util.ArrayList;
import java.util.List;

public class Comprehension {
    /*
          "ast_type": "comprehension",
                                "ifs": [],
                                "is_async": 0,
                                "iter": {
                                    "ast_type": "Name",
                                    "col_offset": 37,
                                    "ctx": {
                                        "ast_type": "Load"
                                    },
                                    "id": "points",
                                    "lineno": 1
                                },
                                "target": {
                                    "ast_type": "Name",
                                    "col_offset": 28,
                                    "ctx": {
                                        "ast_type": "Store"
                                    },
                                    "id": "point",
                                    "lineno": 1
                                }
                            }
     */
    int lineno;
    int col_offset;
    IExpr target;
    IExpr iter;
    List<IExpr> ifs=new ArrayList<>();
    int is_async;

    public Comprehension() {
    }

    public IExpr getTarget() {
        return target;
    }

    public void setTarget(IExpr target) {
        this.target = target;
    }

    public IExpr getIter() {
        return iter;
    }

    public void setIter(IExpr iter) {
        this.iter = iter;
    }

    public List<IExpr> getIfs() {
        return ifs;
    }

    public void setIfs(List<IExpr> ifs) {
        this.ifs = ifs;
    }

    public int getIs_async() {
        return is_async;
    }

    public void setIs_async(int is_async) {
        this.is_async = is_async;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public int getCol_offset() {
        return col_offset;
    }

    public void setCol_offset(int col_offset) {
        this.col_offset = col_offset;
    }

    public String toString(){
        String str="";
        if(this!=null){
            //[ f(point) for point in points ]
            str=str+target.toString();
            str=str + " in ";
            str=str + iter.toString();

        }
        return str;
    }

    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();
        for(Object val:ifs) {
            if (val instanceof IExpr){
                listL.addAll(((IExpr)val).getLocals());
            }

        }
        listL.addAll(iter.getLocals());
        listL.addAll(target.getLocals());
        return listL;
    }
}
