package eqsql.dbridge.analysis.eqsql.expr.node;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by K. Venkatesh Emani on 3/4/2017.
 */
public class ClassToAliasMapper {
    /**
     * Mapping from Hibernate mapped class name to alias.
     * (Assumption: The same class will not be referred twice in the same
     * query)
     */
    private static final Map<String, String> classToAliasMap;
    static {
        classToAliasMap = new HashMap<>();
        classToAliasMap.put("ConcreteActivity", "ca");
        classToAliasMap.put("ConcreteBreakdownElement", "cbe");
        classToAliasMap.put("ConcreteIteration", "ci");
        classToAliasMap.put("ConcreteMilestone", "cm");
        classToAliasMap.put("ConcretePhase", "cp");
        classToAliasMap.put("ConcreteRoleDescriptor", "crd");
        classToAliasMap.put("ConcreteTaskDescriptor", "ctd");
        classToAliasMap.put("ConcreteWorkBreakdownElement", "cwbe");
        classToAliasMap.put("ConcreteWorkOrder", "cwo");
        classToAliasMap.put("ConcreteWorkProductDescriptor", "cwpd");
        classToAliasMap.put("DailyRemainingTime", "drt");
        classToAliasMap.put("Affectedto", "af");
        classToAliasMap.put("Project", "p");
        classToAliasMap.put("Participant", "pt");
        classToAliasMap.put("Role", "r");
        classToAliasMap.put("WilosUser", "wu");
        classToAliasMap.put("Activity", "a");
        classToAliasMap.put("BreakdownElement", "be");
        classToAliasMap.put("CheckList", "cl");
        classToAliasMap.put("Element", "e");
        classToAliasMap.put("Guidance", "g");
        classToAliasMap.put("Iteration", "i");
        classToAliasMap.put("Milestone", "m");
        classToAliasMap.put("Phase", "ph");
        classToAliasMap.put("Process", "pr");
        classToAliasMap.put("RoleDefinition", "rdf");
        classToAliasMap.put("RoleDescriptor", "rds");
        classToAliasMap.put("Section", "sec");
        classToAliasMap.put("Step", "step");
        classToAliasMap.put("TaskDefinition", "tdf");
        classToAliasMap.put("TaskDescriptor", "tds");
        classToAliasMap.put("WorkBreakdownElement", "wbe");
        classToAliasMap.put("WorkOrder", "wo");
        classToAliasMap.put("WorkProductDefinition", "wpdf");
        classToAliasMap.put("WorkProductDescriptor", "wpds");
        classToAliasMap.put("Order","o");
        classToAliasMap.put("DateDim", "d");
    }

    /**
     * Get an alias for the mapped hibernate class.
     * If the passed class does not contain a predefined alias,
     * return "x".
     */
    public static String getAlias(String mappedClassName){
        if(classToAliasMap.containsKey(mappedClassName)){
            return classToAliasMap.get(mappedClassName);
        }
        return "x";
    }
}
