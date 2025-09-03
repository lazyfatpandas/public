package eqsql.dbridge.analysis.eqsql.trans;

import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.trans.fold.*;
import eqsql.dbridge.analysis.eqsql.trans.simplify.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by venkatesh on 2/16/2018.
 */
public class TransDriver {
    private static final int NUM_TRANS_ITERS = 2;
    private static List<Rule> simpliRules = getSimplificationRules();
    private static List<Rule> foldRules = getFoldTransRules();

    public static Node applyAllTransRules(Node expr){
        expr = applySimpliRules(expr);
        expr = applyFoldRules(expr);
        expr = applySimpliRules(expr);
        return expr;
    }

    public static Node applySimpliRules(Node expr) {
        return applyTransRules(expr, simpliRules);
    }

    public static Node applyFoldRules(Node expr) {
        return applyTransRules(expr, foldRules);
    }

    private static Node applyTransRules(Node inNode, List<Rule> rules){
        Node transNode = inNode;

        for(int i=0;i<NUM_TRANS_ITERS;i++) {
            for (Rule rule : rules) {
                transNode = transNode.accept(rule);
            }
        }
        return transNode;
    }

    /* public because it is used in pyrojforcbt project */
    public static List<Rule> getSimplificationRules(){
        List<Rule> simpliRules = new ArrayList<>();
        simpliRules.add(new RuleS1A());
        simpliRules.add(new RuleS1B());
        simpliRules.add(new RuleS2());
        simpliRules.add(new RuleS3());
        simpliRules.add(new RuleS4());
        simpliRules.add(new RuleS5());
        simpliRules.add(new RuleS6());
        simpliRules.add(new RuleS7A());
        simpliRules.add(new RuleS7B());
        simpliRules.add(new RuleS7C());
        simpliRules.add(new RuleS8A());
        simpliRules.add(new RuleS8B());
        simpliRules.add(new RuleS9());

        return simpliRules;
    }

    /* public because it is used in pyrojforcbt project */
    public static List<Rule> getFoldTransRules(){
        List<Rule> foldTransRules = new ArrayList<>();
        foldTransRules.add(new RuleT1A());
        foldTransRules.add(new RuleT1B());
        foldTransRules.add(new RuleT1C());
        foldTransRules.add(new RuleT1D());
        foldTransRules.add(new RuleT2());
        foldTransRules.add(new RuleT3A());
        foldTransRules.add(new RuleT3B());
        foldTransRules.add(new RuleT4());
        foldTransRules.add(new RuleT5());

        return foldTransRules;
    }
}
