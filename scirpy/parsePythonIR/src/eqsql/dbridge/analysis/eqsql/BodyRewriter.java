package eqsql.dbridge.analysis.eqsql;

import dbridge.analysis.region.regions.ARegion;
import dbridge.analysis.region.regions.LoopRegion;
import eqsql.EqSQLConfig;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JVirtualInvokeExpr;

import java.util.*;

/**
 * Created by K. Venkatesh Emani on 12/22/2016.
 * Logic to update contents of a soot method body with new code that uses SQL
 */
public class BodyRewriter {
    private static final Logger LOGGER = EqSQLConfig.getLogger();

    private String queryStr;
    private ARegion region;
    private Body funcBody;
    private Type varType;
    /** The statements which can be removed as a result of translation to SQL
     * (dead code). */
    private List<Unit> removableUnits;
    /** Reference to the dao object used in the function */
    private JInstanceFieldRef daoIfr;
    private static Map<String, Local> locals = new HashMap<>();

    private static Map<String, SootMethodRef> methodMap = new HashMap<>();
    public static final String SESSION_FACTORY = "sessionFactory";
    public static final String SESSION = "session";
    public static final String QUERY = "query";
    public static final String RESULTS = "results";
    public static final String RESULT_VAL = "resultVal";
    public static final String RET_VAR_SET = "retVarSet";
    public static final String DAO_LOCAL = "daoLocal";
    public static final String GET_SESSION_FACT = "getSessionFactory";
    public static final String GET_CURRENT_SESSION = "getCurrentSession";
    public static final String CREATE_QUERY = EqSQLConfig.HQL ? "createQuery" : "createSQLQuery";
    public static final String SET_PARAMETER = "setParameter";
    public static final String RESULTS_LIST = "list";
    public static final String UNIQUE_RESULT = "uniqueResult";
    public static final String COLLECTION_INIT = "init";
    public static final String LIST_GET = "get";
    public static final String CASTED_RESULTS = "castedResults";
    public static final String CASTED_RESULT_VAL = "castedResultVar";
    public static final String CONS_MAP = "constructMap";

    static {
        setupLocalsAndMethods();
    }

    public BodyRewriter(String _hibQuery, ARegion _region, Body _funcBody, Type _type, List<LoopRegion> loopsSwallowed, JInstanceFieldRef _daoIfr) {
        this.queryStr = _hibQuery;
        this.region = _region;
        this.funcBody = _funcBody;
        this.varType = _type;
        this.removableUnits = _region.getUnits();
        this.daoIfr = _daoIfr;
    }

    /**
     * Currently, we assume a single aggregate variable per loop and mark
     * the entire loop as removable if the aggregate variable can be translated to fold.
     * //TODO Have a more sophisticated method to identify dead code
     */
    private Set<Unit> getRemovableUnits(List<LoopRegion> loopsSwallowed) {
        Set<Unit> removableUnits = new HashSet<>();
        for (LoopRegion loopRegion : loopsSwallowed) {
            removableUnits.addAll(loopRegion.getUnits());
        }
        return removableUnits;
    }

    public boolean rewriteBody(){
        //maintain the set of newly added statements so that we can remove
        //them if rewriting fails midway.
        Set<Unit> addedStmts = new HashSet<>();

        Unit uLast = region.lastStmt();
        Unit uLastOrig = uLast; /* will be used later to handle the special case of
        return statement being the last stmt */

        //sessionFactory = SessionFactoryUtils.getInstance()
        Local sessionFactory = locals.get(SESSION_FACTORY);
        uLast = genAndAddSessFacStmt(uLast, sessionFactory);

        //session = sessionFactory.getCurrentSession()
        Local session = locals.get(SESSION);
        uLast = genAndAddSessionStmt(uLast, sessionFactory, session);

        //query = session.createQuery("...")
        Local query = locals.get(QUERY);
        uLast = genAndAddQueryStmt(uLast, session, query);

        /* The query can result in either a single scalar value (which we assume
         * can only be an int as of now), or a list of query results. */
        Local castedResults; //this local is returned.
        if(varType.toString().equals("int") ||
                varType.toString().equals("boolean")){
            //resultVal = query.uniqueResult()
            Local resultVal = locals.get(RESULT_VAL);
            uLast = genAndAddResultValStmt(uLast, query, resultVal);
            //castedResulVal = (int) resultVar
            castedResults = makeAndAddLocal(CASTED_RESULT_VAL, varType);
            uLast = genAndAddCastToType(uLast, resultVal, castedResults, varType.toString());
        }
        else if (varType.toString().equals("java.util.HashMap")){
            Local results = locals.get(RESULTS);
            uLast = genAndAddResultsStmt(uLast, query, results);

            castedResults = makeAndAddLocal(CASTED_RESULTS, varType);
            uLast = genAndAddCastToHashMap(uLast, results, castedResults);
        }
        else{
            //results = query.list()
            Local results = locals.get(RESULTS);
            uLast = genAndAddResultsStmt(uLast, query, results);
            castedResults = results;

            //if its a list, we dont need to do anything more. If not do cast.
            if(!varType.toString().equals("java.util.List")){
                if(varType.toString().equals("java.util.HashSet")){
                    //castedResults = new HashSet
                    //castedResults.init(results)
                    castedResults = makeAndAddLocal(CASTED_RESULTS, varType);
                    uLast = genAndAddCastToHashSet(uLast, results, castedResults);
                }
                else{
                    //TODO add support from list to other types
                    LOGGER.error("Conversion from List to " + varType.toString()
                            + " not currently supported.");
                    removeStmts(addedStmts);
                    return false;
                }
            }
        }


        /* If we inserted code after return statement, we need to do remove the original
         * return statement and add a new one at the end of the new code */
        if(uLastOrig instanceof JReturnStmt) {
            insertReturn(uLast, uLastOrig, castedResults);
        }

        /* Remove statements corresponding to swallowed loops */
        removeStmts(removableUnits);
        return true;
    }


    /**
     * generate and all the session factory assignment statement
     * @param uLast The statement after which to insert new code
     * @param sessionFactory session factory local
     */
    private Unit genAndAddSessFacStmt(Unit uLast, Local sessionFactory) {
        /* daoLocal = this.someDao */
        Type daoType = daoIfr.getType();
        Local daoLocal = makeAndAddLocal(DAO_LOCAL, daoType);
        funcBody.getLocals().add(daoLocal);
        Unit daoLocalAssignStmt = Jimple.v().newAssignStmt(
                daoLocal, daoIfr);
        funcBody.getUnits().insertAfter(daoLocalAssignStmt, uLast);
        uLast = daoLocalAssignStmt;

        /* sessionFactory = daoLocal.getSessionFactory()*/
        makeAndAddMethod(daoType.toString(),
                GET_SESSION_FACT, sessionFactory.getType(), false);
        JVirtualInvokeExpr invokeExpr = new JVirtualInvokeExpr(daoLocal, methodMap.get(GET_SESSION_FACT), new ArrayList<>());

        funcBody.getLocals().add(sessionFactory);
        Unit uSessFac = Jimple.v().newAssignStmt(sessionFactory, invokeExpr);
        funcBody.getUnits().insertAfter(uSessFac, uLast);
        uLast = uSessFac;
        return uLast;
    }

    /**
     * Set up the local variables required to create a hibernate query execution statement
     */
    private static void setupLocalsAndMethods() {
        makeAndAddLocal(SESSION_FACTORY, "org.hibernate.SessionFactory");
        Local session = makeAndAddLocal(SESSION, "org.hibernate.Session");
        Local query = makeAndAddLocal(QUERY, "org.hibernate.Query");
        makeAndAddLocal(RESULTS, "java.util.List");
        makeAndAddLocal(RESULT_VAL, "int");
        makeAndAddLocal(RET_VAR_SET, "java.util.Set");

        makeAndAddMethod("org.hibernate.SessionFactory",
                GET_CURRENT_SESSION, session.getType(), false);
        makeAndAddMethod("org.hibernate.Session",
                CREATE_QUERY, query.getType(), false, "java.lang.String");
        makeAndAddMethod("org.hibernate.Query",
                SET_PARAMETER, query.getType(), false, "java.lang.String", "java.lang.Object");
        makeAndAddMethod("org.hibernate.Query",
                RESULTS_LIST, ArrayType.v(RefType.v("java.util.List"), 1), false);
        makeAndAddMethod("org.hibernate.Query",
                UNIQUE_RESULT, "java.lang.Object", false);
        makeAndAddMethod("java.util.HashSet",
                COLLECTION_INIT, ArrayType.v(RefType.v("java.util.Set"), 1), false, "java.util.Collection");
        makeAndAddMethod("java.util.List",
                LIST_GET, "java.lang.Object", false, "int");
        makeAndAddMethod("eqsql.shared.Utils",
                CONS_MAP, "java.util.Map", true, "java.util.List");
    }

    /**
     * Create a method and add it to the map of methodRefs.
     * Note: This does not add the method to soot body.
     * This method is a simple wrapper arounf Scene.v().makeMethodRef
     */
    private static void makeAndAddMethod(String declaringClass,
                                         String methodName, Type returnType, boolean isStatic,
                                         String... paramTypes){
        SootClass c1 = Scene.v().forceResolve(declaringClass, SootClass.BODIES);
        List<Type> lParamTypes = new ArrayList<>();
        for (String paramType : paramTypes) {
            lParamTypes.add(RefType.v(paramType));
        }

        SootMethodRef sMetRef = Scene.v().makeMethodRef(c1, methodName, lParamTypes,
                returnType, isStatic);
        methodMap.put(methodName, sMetRef);
    }

    /**
     * Create a method and add it to the map of methodRefs.
     * Convenience method to specify return type as a string
     * Note: This does not add the method to soot body.
     * This method is a simple wrapper around Scene.v().makeMethodRef
     */
    private static void makeAndAddMethod(String declaringClass,
                                         String methodName, String returnType, boolean isStatic,
                                         String... paramTypes){
        makeAndAddMethod(declaringClass, methodName,
                RefType.v(returnType), isStatic, paramTypes);
    }

    /**
     * Create a new local and add it to the list of locals.
     * Return the newly created local.
     * Note: this does not add the local to the soot method body.
     */
    private static Local makeAndAddLocal(String name, Type type){
        Local local = Jimple.v().newLocal(name, type);
        locals.put(name, local);
        return local;
    }

    /**
     * Create a new local and add it to the list of locals.
     * Return the newly created local.
     * Convenience method to pass type name as string.
     * Note: this does not add the local to the soot method body.
     */
    private static Local makeAndAddLocal(String name, String type){
        return makeAndAddLocal(name, RefType.v(type));
    }

    private Unit genAndAddCastToHashMap(Unit uLast, Local results, Local castedResults) {
        //castedResults = Utils.constructMap(results)
        funcBody.getLocals().add(castedResults);
        Unit def = Jimple.v().newAssignStmt(castedResults,
                Jimple.v().newStaticInvokeExpr(methodMap.get(CONS_MAP),results));
        funcBody.getUnits().insertAfter(def, uLast);
        uLast = def;
        return uLast;
    }

    private Unit genAndAddCastToHashSet(Unit uLast, Local results, Local castedResults) {
        //castedResults = new HashSet
        funcBody.getLocals().add(castedResults);
        Unit def = Jimple.v().newAssignStmt(castedResults, Jimple.v().newNewExpr(RefType.v("java.util.HashSet")));
        funcBody.getUnits().insertAfter(def, uLast);
        uLast = def;

        //castedResults.init(results)
        List<Value> lArgs = new ArrayList<Value>();
        lArgs.add(results);
        Unit populateCastedResults = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(castedResults, methodMap.get(COLLECTION_INIT), lArgs));
        funcBody.getUnits().insertAfter(populateCastedResults, uLast);
        uLast = populateCastedResults;
        return uLast;
    }

    private Unit genAndAddResultsStmt(Unit uLast, Local query, Local results) {
        funcBody.getLocals().add(results);
        Unit uResults = Jimple.v().newAssignStmt(results, Jimple.v().newInterfaceInvokeExpr(query, methodMap.get(RESULTS_LIST)));
        funcBody.getUnits().insertAfter(uResults, uLast);
        uLast = uResults;
        return uLast;
    }

    private Unit genAndAddResultValStmt(Unit uLast, Local query, Local resultVal) {
        funcBody.getLocals().add(resultVal);
        Unit uResultVal = Jimple.v().newAssignStmt(resultVal, Jimple.v().newInterfaceInvokeExpr(query, methodMap.get(UNIQUE_RESULT)));
        funcBody.getUnits().insertAfter(uResultVal, uLast);
        uLast = uResultVal;
        return uLast;
    }

    private Unit genAndAddQueryStmt(Unit uLast, Local session, Local query) {
        funcBody.getLocals().add(query);
        Unit uQuery = Jimple.v().newAssignStmt(query, Jimple.v().newInterfaceInvokeExpr(session, methodMap.get(CREATE_QUERY), StringConstant.v(queryStr)));
        funcBody.getUnits().insertAfter(uQuery, uLast);
        uLast = uQuery;
        return uLast;
    }


    private Unit genAndAddSessionStmt(Unit uLast, Local sessionFactory, Local session) {
        funcBody.getLocals().add(session);
        Unit uSess = Jimple.v().newAssignStmt(session, Jimple.v().newInterfaceInvokeExpr(sessionFactory, methodMap.get(GET_CURRENT_SESSION)));
        funcBody.getUnits().insertAfter(uSess, uLast);
        uLast = uSess;
        return uLast;
    }

    private void insertReturn(Unit uLast, Unit uLastOrig, Local castedResults) {
        Unit uNewReturn = Jimple.v().newReturnStmt(castedResults);
        funcBody.getUnits().remove(uLastOrig);
        funcBody.getUnits().insertAfter(uNewReturn, uLast);
    }

    private void removeStmts(Collection<Unit> removableUnits) {
        for (Unit unit : removableUnits) {
            if(funcBody.getUnits().contains(unit)) {
                funcBody.getUnits().remove(unit);
            }
        }
    }

    private Unit genAndAddCastToType(Unit uLast, Local resultVal, Local castedResults, String varType) {
        funcBody.getLocals().add(castedResults);
        Unit def = Jimple.v().newAssignStmt(castedResults, Jimple.v().newCastExpr(resultVal, RefType.v(varType)));
        funcBody.getUnits().insertAfter(def, uLast);
        uLast = def;

        return uLast;
    }

}
