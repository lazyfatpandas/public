package ast;


import java.util.List;

public class ASTNode {

    public String asname;
    public String ast_type;
    public List<ASTNode> children;
    public ASTNode parent;
    public int lineno;
    public int col_offset;

}
