package soot;

import eqsql.EqSQLConfig;
import soot.options.Options;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SootHelper {
    public static final String FSIB_TRANS_NAME = "wjtp.xdatapro";
    public static final String WJTP_PACK = "wjtp";
    public static final String CG_PACK = "cg";
    public static boolean SOOT_CP_SET = false;
    private static String CLASS_METHOD_SEPARATOR = ": ";

    public static void setSootOptions(String inputPath) {
        if(!SootHelper.SOOT_CP_SET) {
            String rtJarPath = EqSQLConfig.rtJarPath;
            String options = "-soot-class-path " + inputPath +
                    (rtJarPath == null ? "" : (";" + rtJarPath)) +
                    " -w -p jb " +
                    "preserve-source-annotations:true -p jb " +
                    "use-original-names:true " +
                    "-p tag enabled:true " +
                    "--keep-line-number " +
                    "-x java -x org -x javax -x soot -x sun -allow-phantom-refs -no-bodies-for-excluded -f " + EqSQLConfig.OUTPUT_FORMAT;

            String[] args = options.split(" ");
            Options.v().parse(args);
            SootHelper.SOOT_CP_SET = true;
        }
    }

    public static void setSootOutputToLogFile(){
        try {
            G.v().out = new PrintStream(new BufferedOutputStream(new FileOutputStream(EqSQLConfig.LOGPATH, true)), true);
        } catch (FileNotFoundException ex) {
            EqSQLConfig.getLogger().error(ex.getMessage());
            EqSQLConfig.getLogger().error(ex.getStackTrace());
        }
    }

    /**
     * Set the function signatures as soot entry points.
     */
    public static void doSetEntryPoints(String classSignature, List<String> funcSignatures) {
        SootClass c = Scene.v().forceResolve(classSignature, SootClass.BODIES);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        List entryPoints = new ArrayList();
        for (String funcSignature : funcSignatures) {
            String subsignature = getMethodSubsignature(funcSignature);
            SootMethod method = c.getMethod(subsignature);
            entryPoints.add(method);
        }
        Scene.v().setEntryPoints(entryPoints);
    }

    /**
     * True if a transformer with the given name is
     * present in the given pack.
     * @param packName name of the transformation pack (such as wjtp)
     * @param transName name of the transformation
     */
    public static boolean hasTrans(String packName, String transName){
        return PackManager.v().getPack(packName).
                get(transName) != null;
    }

    /**
     * Add the given transformer with the specified name in the specified
     * pack
     * @param packName
     * @param transName
     * @param transformer
     */
    public static void addTrans(String packName, String transName, Transformer transformer){
        PackManager.v().getPack(packName).add(
                new Transform(transName, transformer));
    }

    public static Transformer getTrans(String packName, String transName){
        assert hasTrans(packName, transName);
        return PackManager.v().getPack(WJTP_PACK).get(FSIB_TRANS_NAME).getTransformer();
    }

    public static void applyPack(String packName){
        PackManager.v().getPack(packName).apply();
    }

    public static String getClassName(String fullMethodSignature){
        String[] split = fullMethodSignature.split(CLASS_METHOD_SEPARATOR);
        return split[0];
    }

    public static String getMethodSubsignature(String fullMethodSignature){
        String[] split = fullMethodSignature.split(CLASS_METHOD_SEPARATOR);
        return split[1];
    }

    public static String appendClassName(String classSignature, String funcSignature) {
        return classSignature + CLASS_METHOD_SEPARATOR + funcSignature;
    }
}
