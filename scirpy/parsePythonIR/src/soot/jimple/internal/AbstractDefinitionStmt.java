
package soot.jimple.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;

public abstract class AbstractDefinitionStmt extends AbstractStmt implements DefinitionStmt {
    public final ValueBox leftBox;
    public final ValueBox rightBox;

    protected AbstractDefinitionStmt(ValueBox leftBox, ValueBox rightBox) {
        this.leftBox = leftBox;
        this.rightBox = rightBox;
    }

    public final Value getLeftOp() {
        return this.leftBox.getValue();
    }

    public final Value getRightOp() {
        return this.rightBox.getValue();
    }

    public final ValueBox getLeftOpBox() {
        return this.leftBox;
    }

    public final ValueBox getRightOpBox() {
        return this.rightBox;
    }

    public  List<ValueBox> getDefBoxes() {
        return Collections.singletonList(this.leftBox);
    }

    public List<ValueBox> getUseBoxes() {
        List<ValueBox> list = new ArrayList();
        list.addAll(this.getLeftOp().getUseBoxes());
        list.add(this.rightBox);
        list.addAll(this.getRightOp().getUseBoxes());
        return list;
    }

    public boolean fallsThrough() {
        return true;
    }

    public boolean branches() {
        return false;
    }
}

