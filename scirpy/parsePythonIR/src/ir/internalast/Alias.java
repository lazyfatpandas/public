package ir.internalast;


public class Alias {
    String name;
    String asname;
    InternalASTType ast_type;
    boolean isAsname;

    public Alias(String name, String asname, boolean isAsname, InternalASTType ast_type) {
        this.name = name;
        this.asname = asname;
        this.isAsname = isAsname;
        this.ast_type=ast_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAsname() {
        return asname;
    }

    public void setAsname(String asname) {
        this.asname = asname;
    }

    @Override
    public String toString() {
        String stmt=name;
        if(asname!=null){
            stmt=stmt+" as "+ asname;
        }
        return stmt;

    }
}
