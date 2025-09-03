package PythonGateWay;

import ForceCompute.ForceCompute;
import analysis.LiveVariable.LiveDataFrame;
import ir.JPBody;
import ir.JPMethod;
import json.ReadPythonIR;
import org.json.simple.JSONArray;
import parse.IRMaker.MainParser;
import passes.DropColumnPass;
import passes.JoinOptimization.JoinOptmizations;
import passes.lasp.LaspRewrites;
import regions.PythonWriter.RegionToPython;
import rewrite.pd1.Pd1Elt;
import soot.SootClass;
import soot.SootMethod;

import java.util.List;

public class ScaPaOptimizations {
    static String sourcePath = "/home/bhushan/intellijprojects/scirpy/pythonIR";
    static String path =        "/home/bhushan/intellijprojects/scirpy/pythonIR";
    static String dataPath =    "/home/bhushan/intellijprojects/scirpy/pythonIR";
    static String filename =    "temp.json";
    static String srcFileName=  "test.py";
    static String destinationFileName="";
    static String destPath="/home/bhushan/intellijprojects/scirpy/pythonIR";


    public static void setPaths(String sourceNameFull, String sourceFileName, String destinationPath, String dataPathDirectory){
        filename=sourceNameFull;
        destPath=destinationPath;
        dataPath=dataPathDirectory;
        srcFileName=sourceFileName+".py";

    }
    public static void main(String args[]){
        //INIT PORTION
        long time0 = System.currentTimeMillis();



        //SOURCE TO IR
        JSONArray stmtList = ReadPythonIR.ReadFile(filename);
        MainParser mainParser = new MainParser(stmtList);
        List<SootClass> sootClasses = mainParser.getClasses();
        SootClass mainClass;
        mainClass=getMainClass(sootClasses);
        //TRANSFORMATIONS

//        SootMethod mainMethod=mainClass.getMethodByName("main");
//        JPBody mainJpBody=(JPBody) (mainMethod.getActiveBody());
//        DropColumnPass dcp=new DropColumnPass((JPMethod)mainJpBody.getMethod(), dataPath,sootClasses);
//        dcp.insertLists();
//        dcp.insertClmnTypeStmt();
//        List<Pd1Elt> pdList=dcp.getPdLists();

        //2023 start
        int i=0;
        for(i=0;i<mainClass.getMethods().size();i++){
        //for(SootMethod sootMethod: mainClass.getMethods()){
            SootMethod sootMethod=mainClass.getMethods().get(i);
            JPBody methodJpBody=(JPBody) (sootMethod.getActiveBody());
            DropColumnPass dcp=new DropColumnPass((JPMethod)methodJpBody.getMethod(), dataPath,sootClasses);
//            dcp.insertLists();
//            dcp.insertClmnTypeStmt();
            dcp.metaJSONGenerator();
            // Chiranmoy: live dataframe analysis and force compute
            LiveDataFrame ldf = new LiveDataFrame((JPMethod)methodJpBody.getMethod());
            //        ldf.print();
            ForceCompute forceCompute = new ForceCompute((JPMethod)methodJpBody.getMethod(), ldf);
            forceCompute.force();


            List<Pd1Elt> pdList=dcp.getPdLists();
            LaspRewrites laspRewrites=new LaspRewrites((JPMethod)methodJpBody.getMethod(), dataPath,sootClasses,pdList);

        }

        //2023 END

        //03102021 TEST MERGE AND JOIN
        //JoinOptmizations joinOptmizations=new JoinOptmizations((JPMethod)mainJpBody.getMethod(), dataPath,sootClasses);
        //03102021 END TEST MERGE AND JOIN

        //02032022 TEST LASP Rewrite
      //  LaspRewrites laspRewrites=new LaspRewrites((JPMethod)mainJpBody.getMethod(), dataPath,sootClasses,pdList);
        //02032022 END LASP Rewrite



        //IR TO PYTHON
        RegionToPython regionToPython=new RegionToPython();
        regionToPython.toFileInterprocedural(path,destPath,srcFileName,sootClasses);
        long time1 = System.currentTimeMillis();
        System.out.println("Total time: "+time(time1-time0));


        //TEST IP live attribute
        /*
        SootMethod otherMethod=mainClass.getMethodByName("get_percent_difference");
        JPBody otherBody=(JPBody) (otherMethod.getActiveBody());
        CFG cfg=new CFG((JPMethod) otherBody.getMethod());
        AttributeLiveVariableAnalysis alva = new AttributeLiveVariableAnalysis(cfg.getUnitGraph());
        Iterator unitIterator = cfg.getUnitGraph().iterator();
        //generate flowset for each unit
        int i=0;
        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            i++;
            System.out.println(i+" "+   unit);
            FlowSet set = (FlowSet) alva.getFlowBefore(unit);
            System.out.println("\tatt Before: " + set);
            break;
        }
            */
        //TEST END IP live attribute

    }

    private static SootClass getMainClass(List<SootClass> sootClasses) {
        for (SootClass sootClass : sootClasses) {
            if (sootClass.getName().equalsIgnoreCase("MainClass")) {
                return sootClass;
            }
        }
        return null;
    }
    private static String time(long t) {
        return t/1000 + "." + String.valueOf(1000+(t%1000)).substring(1);
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSrcFileName() {
        return srcFileName;
    }

    public void setSrcFileName(String srcFileName) {
        this.srcFileName = srcFileName;
    }

    public String getDestinationFileName() {
        return destinationFileName;
    }

    public void setDestinationFileName(String destinationFileName) {
        this.destinationFileName = destinationFileName;
    }
}
