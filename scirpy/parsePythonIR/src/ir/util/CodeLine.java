package ir.util;

public class CodeLine {

    boolean isDelete=false;
    boolean isAddition=false;
    boolean isRewrite=false;

    String originalCode="";
    String rewrittenCode="";
    int lineno;

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public boolean isAddition() {
        return isAddition;
    }

    public void setAddition(boolean addition) {
        isAddition = addition;
    }

    public boolean isRewrite() {
        return isRewrite;
    }

    public void setRewrite(boolean rewrite) {
        isRewrite = rewrite;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public String getRewrittenCode() {
        return rewrittenCode;
    }

    public void setRewrittenCode(String rewrittenCode) {
        this.rewrittenCode = rewrittenCode;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }
}
