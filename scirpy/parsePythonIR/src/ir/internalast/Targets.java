package ir.internalast;

import ir.IExpr;
import ir.expr.Expr_Context;
import ir.expr.Name;
import soot.Local;
import soot.jimple.internal.JimpleLocal;

import java.util.ArrayList;
import java.util.List;

public class Targets extends JimpleLocal implements Local,IExpr {
    /* Sample target from json file
       {
                    "ast_type": "Name",
                    "col_offset": 0,
                    "ctx": {
                        "ast_type": "Store"
                    },
                    "id": "df",
                    "lineno": 2
                }
     */
//TODO ast_type is in Name right now!!check if needs to be shifted
int lineno;
int col_offset;
Name name;

//TODO type is not included
int number=0;//why??

    public Targets(int lineno, int col_offset, String id ) {
        super(id,null);

        this.lineno = lineno;
        this.col_offset = col_offset;

        this.name=new Name(id, Expr_Context.Store);
    }

    @Override
    public String getName() {
        return name.id;
    }

    @Override
    public void setName(String s) {
    name.id=s;
    }


    public boolean equals(Object o) {
        if(!(o instanceof Name || o instanceof Targets))
            return false;

        if(o instanceof Targets) {
            Targets target = (Targets) o;
            if (this.getName().equals(target.getName()))
                return true;
        }
        else if(o instanceof Name){
            Name name=(Name) o;
            if (this.getName().equals(name.id))
                return true;
        }
        else
            return false;
        return false;

    }

    public int getLineno() {
        return lineno;
    }

    public int getCol_offset() {
        return col_offset;
    }
    //Added for print
    public String toString() {

        return (name.id);
    }

    @Override
    public List<Local> getLocals() {
        List<Local> listL=new ArrayList<>();

        listL.addAll(name.getLocals());
        return listL;
    }

    @Override
    public List<Name> getDataFrames() {
        return name.getDataFrames();
    }

    @Override
    public boolean isDataFrame() {
        return name.isDataFrame();
    }
}
