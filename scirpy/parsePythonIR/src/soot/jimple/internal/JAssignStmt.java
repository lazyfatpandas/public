


package soot.jimple.internal;

import java.util.List;
import soot.Immediate;
import soot.IntType;
import soot.Local;
import soot.Unit;
import soot.UnitBox;
import soot.UnitBoxOwner;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.baf.Baf;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConvertToBaf;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleToBafContext;
import soot.jimple.StaticFieldRef;
import soot.jimple.StmtSwitch;
import soot.jimple.SubExpr;
import soot.util.Switch;

public class JAssignStmt extends AbstractDefinitionStmt implements AssignStmt {
    public JAssignStmt(Value variable, Value rvalue) {
        this((ValueBox)(new JAssignStmt.LinkedVariableBox(variable)), (ValueBox)(new JAssignStmt.LinkedRValueBox(rvalue)));
        ((JAssignStmt.LinkedVariableBox)this.leftBox).setOtherBox(this.rightBox);
        ((JAssignStmt.LinkedRValueBox)this.rightBox).setOtherBox(this.leftBox);
        if (!this.leftBox.canContainValue(variable) || !this.rightBox.canContainValue(rvalue)) {
            throw new RuntimeException("Illegal assignment statement.  Make sure that either left side or right hand side has a local or constant.");
        }
    }

    protected JAssignStmt(ValueBox variableBox, ValueBox rvalueBox) {
        super(variableBox, rvalueBox);
    }

    public boolean containsInvokeExpr() {
        return this.getRightOp() instanceof InvokeExpr;
    }

    public InvokeExpr getInvokeExpr() {
        if (!this.containsInvokeExpr()) {
            throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
        } else {
            return (InvokeExpr)this.rightBox.getValue();
        }
    }

    public ValueBox getInvokeExprBox() {
        if (!this.containsInvokeExpr()) {
            throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
        } else {
            return this.rightBox;
        }
    }

    public boolean containsArrayRef() {
        return this.getLeftOp() instanceof ArrayRef || this.getRightOp() instanceof ArrayRef;
    }

    public ArrayRef getArrayRef() {
        if (!this.containsArrayRef()) {
            throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
        } else {
            return this.leftBox.getValue() instanceof ArrayRef ? (ArrayRef)this.leftBox.getValue() : (ArrayRef)this.rightBox.getValue();
        }
    }

    public ValueBox getArrayRefBox() {
        if (!this.containsArrayRef()) {
            throw new RuntimeException("getArrayRefBox() called with no ArrayRef present!");
        } else {
            return this.leftBox.getValue() instanceof ArrayRef ? this.leftBox : this.rightBox;
        }
    }

    public boolean containsFieldRef() {
        return this.getLeftOp() instanceof FieldRef || this.getRightOp() instanceof FieldRef;
    }

    public FieldRef getFieldRef() {
        if (!this.containsFieldRef()) {
            throw new RuntimeException("getFieldRef() called with no FieldRef present!");
        } else {
            return this.leftBox.getValue() instanceof FieldRef ? (FieldRef)this.leftBox.getValue() : (FieldRef)this.rightBox.getValue();
        }
    }

    public ValueBox getFieldRefBox() {
        if (!this.containsFieldRef()) {
            throw new RuntimeException("getFieldRefBox() called with no FieldRef present!");
        } else {
            return this.leftBox.getValue() instanceof FieldRef ? this.leftBox : this.rightBox;
        }
    }

    public List<UnitBox> getUnitBoxes() {
        Value rValue = this.rightBox.getValue();
        return rValue instanceof UnitBoxOwner ? ((UnitBoxOwner)rValue).getUnitBoxes() : super.getUnitBoxes();
    }

    public String toString() {
        return this.leftBox.getValue().toString() + " = " + this.rightBox.getValue().toString();
    }

    public void toString(UnitPrinter up) {
        this.leftBox.toString(up);
        up.literal(" = ");
        this.rightBox.toString(up);
    }

    public Object clone() {
        return new JAssignStmt(Jimple.cloneIfNecessary(this.getLeftOp()), Jimple.cloneIfNecessary(this.getRightOp()));
    }

    public void setLeftOp(Value variable) {
        this.getLeftOpBox().setValue(variable);
    }

    public void setRightOp(Value rvalue) {
        this.getRightOpBox().setValue(rvalue);
    }

    public void apply(Switch sw) {
        ((StmtSwitch)sw).caseAssignStmt(this);
    }

    public void convertToBaf(final JimpleToBafContext context, final List<Unit> out) {
        Value lvalue = this.getLeftOp();
        final Value rvalue = this.getRightOp();
        if (lvalue instanceof Local && (rvalue instanceof AddExpr || rvalue instanceof SubExpr)) {
            Local l = (Local)lvalue;
            BinopExpr expr = (BinopExpr)rvalue;
            Value op1 = expr.getOp1();
            Value op2 = expr.getOp2();
            if (l.getType().equals(IntType.v())) {
                boolean isValidCase = false;
                int x = 0;
                if (op1 == l && op2 instanceof IntConstant) {
                    x = ((IntConstant)op2).value;
                    isValidCase = true;
                } else if (expr instanceof AddExpr && op2 == l && op1 instanceof IntConstant) {
                    x = ((IntConstant)op1).value;
                    isValidCase = true;
                }

                if (isValidCase && x >= -32768 && x <= 32767) {
                    Unit u = Baf.v().newIncInst(context.getBafLocalOfJimpleLocal(l), IntConstant.v(expr instanceof AddExpr ? x : -x));
                    u.addAllTagsOf(this);
                    out.add(u);
                    return;
                }
            }
        }

        context.setCurrentUnit(this);
        lvalue.apply(new AbstractJimpleValueSwitch() {
            public void caseArrayRef(ArrayRef v) {
                ((ConvertToBaf)((ConvertToBaf)v.getBase())).convertToBaf(context, out);
                ((ConvertToBaf)((ConvertToBaf)v.getIndex())).convertToBaf(context, out);
                ((ConvertToBaf)rvalue).convertToBaf(context, out);
                Unit u = Baf.v().newArrayWriteInst(v.getType());
                u.addAllTagsOf(JAssignStmt.this);
                out.add(u);
            }

            public void caseInstanceFieldRef(InstanceFieldRef v) {
                ((ConvertToBaf)((ConvertToBaf)v.getBase())).convertToBaf(context, out);
                ((ConvertToBaf)rvalue).convertToBaf(context, out);
                Unit u = Baf.v().newFieldPutInst(v.getFieldRef());
                u.addAllTagsOf(JAssignStmt.this);
                out.add(u);
            }

            public void caseLocal(Local v) {
                ((ConvertToBaf)rvalue).convertToBaf(context, out);
                Unit u = Baf.v().newStoreInst(v.getType(), context.getBafLocalOfJimpleLocal(v));
                u.addAllTagsOf(JAssignStmt.this);
                out.add(u);
            }

            public void caseStaticFieldRef(StaticFieldRef v) {
                ((ConvertToBaf)rvalue).convertToBaf(context, out);
                Unit u = Baf.v().newStaticPutInst(v.getFieldRef());
                u.addAllTagsOf(JAssignStmt.this);
                out.add(u);
            }
        });
    }

    private static class LinkedRValueBox extends RValueBox {
        ValueBox otherBox;

        private LinkedRValueBox(Value v) {
            super(v);
            this.otherBox = null;
        }

        public void setOtherBox(ValueBox otherBox) {
            this.otherBox = otherBox;
        }

        public boolean canContainValue(Value v) {
            if (super.canContainValue(v)) {
                if (this.otherBox == null) {
                    return true;
                } else {
                    Value o = this.otherBox.getValue();
                    return v instanceof Immediate || o instanceof Immediate;
                }
            } else {
                return false;
            }
        }
    }

    private static class LinkedVariableBox extends VariableBox {
        ValueBox otherBox;

        private LinkedVariableBox(Value v) {
            super(v);
            this.otherBox = null;
        }

        public void setOtherBox(ValueBox otherBox) {
            this.otherBox = otherBox;
        }

        public boolean canContainValue(Value v) {
            if (super.canContainValue(v)) {
                if (this.otherBox == null) {
                    return true;
                } else {
                    Value o = this.otherBox.getValue();
                    return v instanceof Immediate || o instanceof Immediate;
                }
            } else {
                return false;
            }
        }
    }
}

