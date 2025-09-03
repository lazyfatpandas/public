//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package soot.jimple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.validation.FieldRefValidator;
import soot.jimple.validation.IdentityStatementsValidator;
import soot.jimple.validation.IdentityValidator;
import soot.jimple.validation.InvokeArgumentValidator;
import soot.jimple.validation.JimpleTrapValidator;
import soot.jimple.validation.NewValidator;
import soot.jimple.validation.ReturnStatementsValidator;
import soot.jimple.validation.TypesValidator;
import soot.options.Options;
import soot.validation.BodyValidator;
import soot.validation.ValidationException;

public class JimpleBody extends StmtBody {
    private static BodyValidator[] validators;

    private static synchronized BodyValidator[] getValidators() {
        if (validators == null) {
            validators = new BodyValidator[]{IdentityStatementsValidator.v(), TypesValidator.v(), ReturnStatementsValidator.v(), InvokeArgumentValidator.v(), FieldRefValidator.v(), NewValidator.v(), JimpleTrapValidator.v(), IdentityValidator.v()};
        }

        return validators;
    }

    public JimpleBody(SootMethod m) {
        super(m);
    }

    public JimpleBody() {
    }

    public Object clone() {
        Body b = new JimpleBody(this.getMethod());
        b.importBodyContentsFrom(this);
        return b;
    }

    public void validate() {
        List<ValidationException> exceptionList = new ArrayList();
        this.validate(exceptionList);
        if (!exceptionList.isEmpty()) {
            throw (ValidationException)exceptionList.get(0);
        }
    }

    public void validate(List<ValidationException> exceptionList) {
        super.validate(exceptionList);
        boolean runAllValidators = Options.v().debug() || Options.v().validate();
        BodyValidator[] var3 = getValidators();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            BodyValidator validator = var3[var5];
            if (validator.isBasicValidator() || runAllValidators) {
                validator.validate(this, exceptionList);
            }
        }

    }

    public void validateIdentityStatements() {
        this.runValidation(IdentityStatementsValidator.v());
    }

    public void insertIdentityStmts() {
        Unit lastUnit = null;
        if (!this.getMethod().isStatic()) {
            Local l = Jimple.v().newLocal("this", RefType.v(this.getMethod().getDeclaringClass()));
            Stmt s = Jimple.v().newIdentityStmt(l, Jimple.v().newThisRef((RefType)l.getType()));
            this.getLocals().add(l);
            this.getUnits().addFirst(s);
            lastUnit = s;
        }

        int i = 0;

        for(Iterator var8 = this.getMethod().getParameterTypes().iterator(); var8.hasNext(); ++i) {
            Type t = (Type)var8.next();
            Local l = Jimple.v().newLocal("parameter" + i, t);
            Stmt s = Jimple.v().newIdentityStmt(l, Jimple.v().newParameterRef(l.getType(), i));
            this.getLocals().add(l);
            if (lastUnit == null) {
                this.getUnits().addFirst(s);
            } else {
                this.getUnits().insertAfter(s, lastUnit);
            }

            lastUnit = s;
        }

    }

    public Stmt getFirstNonIdentityStmt() {
        Iterator<Unit> it = this.getUnits().iterator();
        Object o = null;

        while(it.hasNext() && (o = it.next()) instanceof IdentityStmt) {
        }

        if (o == null) {
            throw new RuntimeException("no non-id statements!");
        } else {
            return (Stmt)o;
        }
    }
}
