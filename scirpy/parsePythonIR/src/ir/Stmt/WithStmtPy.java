package ir.Stmt;

import ir.JPAbstractStmt;
import ir.JPBody;
import ir.expr.Name;
import org.jboss.util.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class WithStmtPy extends JPAbstractStmt {

    JPBody body;
    JPBody itemsBody;



    public JPBody getBody() {
        return body;
    }

    public void setBody(JPBody body) {
        this.body = body;
    }



    public JPBody getItemsStmtBody() {
        return itemsBody;
    }

    public void  setItemsStmtBody(JPBody itemsBody) {
        this.itemsBody = itemsBody;
    }

    //modify later
    @Override
    public int getLineno() {
        return 0;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public String getOriginalSource() {
        return null;
    }

    @Override
    public List<Name> getDataFramesDefined() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFramesDefined not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }

    @Override
    public List<Name> getDataFramesUsed() {
        List<Name> dataframes = new ArrayList<>();
        if(true)
            throw new NotImplementedException("getDataFramesUsed not implemented in " + this.getClass().getSimpleName());
        return dataframes;
    }
}
