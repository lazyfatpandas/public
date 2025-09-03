package dbridge.analysis.region;

import dbridge.analysis.region.regions.ARegion;
import dbridge.analysis.region.regions.RegionGraph;
import soot.*;

import java.util.Map;

/**
 * Created by venkatesh on 2/12/2018.
 */
public class RegionGraphBuilder {

    /*private String funcSignature;

    private static String CLASS_METHOD_SEPARATOR = ": ";


    public RegionGraphBuilder(String funcSignature) {
        this.funcSignature = funcSignature;
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {
        String className = getClassName(this.funcSignature);
        String methodSubsign = getMethodSubsignature(this.funcSignature);

        SootClass c = Scene.v().forceResolve(className, SootClass.BODIES);
        SootMethod method = c.getMethod(methodSubsign);

        Body body = method.retrieveActiveBody();
        RegionGraph regionGraph = new RegionGraph(body);
        ARegion topRegion = regionGraph.getHeads().get(0);

        System.out.println("Regions for method:" + methodSubsign);
        System.out.println(topRegion.toString());
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
    */

}
