package eqsql.dbridge.analysis.eqsql.util;

/**
 * Created by K. Venkatesh Emani on 12/21/2016.
 */
public class SootClassHelper {
    private static String CLASS_METHOD_SEPARATOR = ": ";

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
