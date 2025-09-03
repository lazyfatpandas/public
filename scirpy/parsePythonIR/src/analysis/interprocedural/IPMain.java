package analysis.interprocedural;

import analysis.LiveVariable.LiveDataFrame;
import ir.JPBody;
import ir.JPMethod;
import json.ReadPythonIR;
import org.json.simple.JSONArray;
import parse.IRMaker.MainParser;
import passes.DropColumnPass;
import regions.PythonWriter.RegionToPython;
import soot.SootClass;
import soot.SootMethod;
import java.util.List;
import ForceCompute.ForceCompute;

public class IPMain {
    static String sourcePath = "/home/chiranmoy/IdeaProjects/scirpy/pythonIR";
    static String path = "/home/chiranmoy/IdeaProjects/scirpy/pythonIR";
    static String dataPath = "/home/chiranmoy/IdeaProjects/scirpy/pythonIR";
    static String filename = "temp.json";
    static String srcFileName="test.py";
    static String destinationFileName="";


    public static void main(String args[]){
        //INIT PORTION
        long time0 = System.currentTimeMillis();

        //SOURCE TO IR
        JSONArray stmtList = ReadPythonIR.ReadFile(filename);
        MainParser mainParser = new MainParser(stmtList);
        List<SootClass> sootClasses = mainParser.getClasses();
        SootClass mainClass=null;

        //TRANSFORMATIONS
        mainClass=getMainClass(sootClasses);
        SootMethod mainMethod=mainClass.getMethodByName("main");
        JPBody mainJpBody=(JPBody) (mainMethod.getActiveBody());
        DropColumnPass dcp=new DropColumnPass((JPMethod)mainJpBody.getMethod(), dataPath,sootClasses);
        dcp.insertLists();
        dcp.insertClmnTypeStmt();

        // Chiranmoy: live dataframe analysis and force compute
        LiveDataFrame ldf = new LiveDataFrame((JPMethod)mainJpBody.getMethod());
//        ldf.print();
        ForceCompute forceCompute = new ForceCompute((JPMethod)mainJpBody.getMethod(), ldf);
        forceCompute.force();

        //03102021 TEST MERGE AND JOIN
        //JoinOptmizations joinOptmizations=new JoinOptmizations((JPMethod)mainJpBody.getMethod(), dataPath,sootClasses);
        //03102021 END TEST MERGE AND JOIN
        //IR TO PYTHON
        RegionToPython regionToPython=new RegionToPython();
        regionToPython.toFileInterprocedural(path,path,srcFileName,sootClasses);
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
