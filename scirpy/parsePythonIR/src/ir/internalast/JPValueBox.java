package ir.internalast;

import soot.AbstractValueBox;
import soot.Value;

public class JPValueBox extends AbstractValueBox {
    public JPValueBox(Value value) {
        this.setValue(value);
    }

    @Override
    public boolean canContainValue(Value value) {
        return true;
    }
}
