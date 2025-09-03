/*
 * EeDag implementation for Hibernate code
 */

package eqsql.dbridge.analysis.eqsql;

import dbridge.analysis.region.api.RegionAnalyzer;
import eqsql.EqSQLConfig;
import eqsql.dbridge.analysis.eqsql.analysis.*;
import eqsql.dbridge.analysis.eqsql.expr.node.HQLTranslatable;
import eqsql.dbridge.analysis.eqsql.expr.node.Node;
import eqsql.dbridge.analysis.eqsql.expr.node.SQLTranslatable;
import eqsql.dbridge.analysis.eqsql.trans.TransDriver;
import eqsql.dbridge.analysis.eqsql.util.SootClassHelper;
import eqsql.exceptions.QueryTranslationException;
import eqsql.exceptions.RewriteException;
import org.apache.log4j.Logger;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.options.Options;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class EqSQLDriver {

    private static final Logger LOGGER = EqSQLConfig.getLogger();

    /**
     * Signature of the class whose functions are to be rewritten
     */
    private String classSignature;
    /**
     * Full signature (including class) of the individual functions to be rewritten
     */
    private String funcSignature;
    /**
     * Object that contains cross-method information about the function being analyzed.
     */
    private FuncStackAnalyzer fsa;
    /**
     * Path to locate the byte-compiled classes
     */
    private String inputPath;
    /**
     * Path to write the output files
     */
    private String outputPath;

    public static void main(String[] args) {
        if(args.length != 4){
            LOGGER.error("Wrong number of arguments. Quitting now.");
            return;
        }
        new EqSQLDriver(args[0], args[1], args[2], args[3]).doExtractQuery();
    }

    private static void setSootLogToFile() {
        try {
            G.v().out = new PrintStream(new BufferedOutputStream(new FileOutputStream(EqSQLConfig.LOGPATH, true)), true);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.warn(e.getStackTrace());
        }
    }

    /**
     * @param classSignature Signature of the class whose function is to be rewritten
     * @param funcSignature <b>Subsignature</b> of function to be analyzed and rewritten.
     */
    public EqSQLDriver(String inputPath, String outputPath, String classSignature, String funcSignature){
        this.classSignature = classSignature;

        /* We are doing redundant work here by combining class name and method name which is later again
        * split in some places.
        * //TODO Identify whether the class + method sign is needed anywhere. If not
        * use only method sign and remove this redundancy */
        this.funcSignature = SootClassHelper.appendClassName(classSignature, funcSignature);
        this.fsa = new FuncStackAnalyzer(this.funcSignature);

        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

	public Node getExpr() {
        /* Set soot entry points */
        doSetEntryPoints();

        /* Initialize region analysis framework */
        RegionAnalyzer.initialize(DIRRegionAnalyzer.INSTANCE, DIRBranchRegionSpecialAnalyzer.INSTANCE, DIRBranchRegionAnalyzer.INSTANCE, DIRLoopRegionAnalyzer.INSTANCE, DIRSequentialRegionAnalyzer.INSTANCE);

        /*  Run soot packs to get required information.
         * Our implementation constructs a stack of all functions called from each top level function we wish
         * to rewrite, and soot packs populate information about all functions in each stack.
         */
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.newSample", new FuncStackInfoBuilder(fsa)));
        // cg pack is applied because FuncInfoStackBuilder transformation in wjtp phase needs call graph
        PackManager.v().getPack("cg").apply();
        PackManager.v().getPack("wjtp").apply();//updates fsa.success

        /* run the analysis to compute the algebraic expression
         * and other necessary information. */
        if (fsa.isSuccess()) {
            try {
                fsa.run(); //fsa state gets updated by this call.
            } catch (Exception | AssertionError e) {
                e.printStackTrace();
                fsa.setSuccess(false);
            }
        }
        return fsa.isSuccess() ? fsa.getExpr() : null;
    }

    /**
     * choose different name
     * TODO
     * @param bestExpr
     * @return
     */
    public boolean rewrite(Node bestExpr) {
        assert bestExpr != null;
        try {
            String query = getQuery(bestExpr);
            LOGGER.info("Query: " + query);

            JInstanceFieldRef daoIfr = findDaoInstanceRef(fsa.getTopLevelFuncBody());
            BodyRewriter br = new BodyRewriter(query, fsa.getRetRegion(),
                    fsa.getTopLevelFuncBody(), fsa.getVarType(), fsa.getLoopsSwallowed(),daoIfr);
            br.rewriteBody();
            rewriteClass(classSignature);
        } catch (Exception | AssertionError e){
            LOGGER.error(e.getMessage());
            fsa.setSuccess(false);
        }

        return fsa.isSuccess();
	}

	public boolean printQuery(Node bestExpr){
        assert bestExpr != null;
        try {
            System.out.println(getQuery(bestExpr));
            return true;
        } catch (QueryTranslationException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getStackTrace());
            System.out.println("Error ");
        }
        return false;
    }

	public String getQuery(Node bestExpr) throws QueryTranslationException {
        return EqSQLConfig.HQL ?
                getHQLQuery(bestExpr) :
                getSQLQuery(bestExpr);
    }

    private String getHQLQuery(Node bestExpr) throws QueryTranslationException {
        if(!(bestExpr instanceof HQLTranslatable)){
            throw new QueryTranslationException(bestExpr + " is not HQLTranslatable");
        }
        return ((HQLTranslatable) bestExpr).toHibQuery();
    }

    private String getSQLQuery(Node bestExpr) throws QueryTranslationException {
        if(!(bestExpr instanceof SQLTranslatable)){
            throw new QueryTranslationException(bestExpr + " is not SQLTranslatable");
        }
        String sqlQuery = ((SQLTranslatable) bestExpr).toSQLQuery();
        if(sqlQuery.startsWith("from")){
            //append select *, as column to be projected is not explicitly specified
            sqlQuery = "select * " + sqlQuery;
        }
        return sqlQuery;
    }


    private JInstanceFieldRef findDaoInstanceRef(Body funcBody) throws RewriteException {
        for (Unit unit : funcBody.getUnits()) {
            if(unit instanceof JAssignStmt){
                JAssignStmt assignStmt = (JAssignStmt)unit;
                Value value = assignStmt.rightBox.getValue();
                if(value instanceof JInstanceFieldRef){
                    JInstanceFieldRef ifr = (JInstanceFieldRef) value;
                    SootFieldRef fieldRef = ifr.getFieldRef();
                    if(fieldRef.type().toString().endsWith("Dao")){
                        return ifr;
                    }
                }
            }
        }

        throw new RewriteException("No dao object found");
    }

    /**
     * Generate modified source code for the class <code>className</code>
     * @param className Signature of the class to be rewritten
     */
    private void rewriteClass(String className) {
        String[] args;
        try {
            String sootOptions;
            sootOptions = " -O -p cg enabled:false -p jb " +
                    "use-original-names:true " +
                    "-d " + outputPath + " " +
                    "-p tag enabled:true " +
                    "--keep-line-number " +
                    "-p jb.dae enabled:true -p jop.dae " +
                    "enabled:true -p jb.uce enabled:true -p jb.ule enabled:true -p jop.lcm enabled:true -p db" +
                    ".deobfuscate enabled:true -f " + EqSQLConfig.OUTPUT_FORMAT;
            args = (className + sootOptions).split(" ");

            Main.main(args);
            G.v().reset();
        } catch (Exception e) {
            LOGGER.error("Exception in rewriteClass " + e.getStackTrace());
        }
    }

    /**
     * Set the function signatures as soot entry points.
     */
    private void doSetEntryPoints() {

        String rtJarPath = EqSQLConfig.rtJarPath;
        String options = "-soot-class-path " + inputPath +
                (rtJarPath == null ? "" : (";" + rtJarPath)) +
                " -w -p jb " +
                "preserve-source-annotations:true -p jb " +
                "use-original-names:true " +
                "-p tag enabled:true " +
                "--keep-line-number " +
                "-x java -x org -x javax -x soot -x sun -allow-phantom-refs -no-bodies-for-excluded -f " + EqSQLConfig.OUTPUT_FORMAT;

        Options.v().set_soot_classpath("");//to avoid duplicate classpath error
        String[] args = options.split(" ");
        Options.v().parse(args);

        SootClass c = Scene.v().forceResolve(classSignature, SootClass.BODIES);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        List entryPoints = new ArrayList();
        String subsignature = SootClassHelper.getMethodSubsignature(funcSignature);
        SootMethod method = c.getMethod(subsignature);
        entryPoints.add(method);
        Scene.v().setEntryPoints(entryPoints);
    }

    public boolean doEqSQLRewrite() {
        setSootLogToFile();

        Node expr = getExpr();
        boolean success = false;
        if(expr != null){
            expr = TransDriver.applyAllTransRules(expr);
            success = rewrite(expr);
        }

        return success;
    }

    public boolean doExtractQuery(){
        setSootLogToFile();
        Node expr = getExpr();
        boolean success = false;
        if(expr != null){
            expr = TransDriver.applyAllTransRules(expr);
            success = printQuery(expr);
        }
        return success;
    }

}