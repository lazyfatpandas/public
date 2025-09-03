package parse.IRMaker;

import ir.util.SourceBuffer;
import json.ReadPythonIR;
import org.json.simple.JSONArray;
import soot.*;
import soot.jimple.Stmt;
import soot.options.Options;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestSoot extends BodyTransformer {
    public static void main(String[] args) {
        //Self start
        String sourcePath="/home/bhushan/intellijprojects/scirpy/pythonIR";
        String path="/home/bhushan/intellijprojects/scirpy/pythonIR";
        String dataPath="/home/bhushan/intellijprojects/scirpy/pythonIR";
        String filename="temp.json";
        JSONArray stmtList = ReadPythonIR.ReadFile(filename);
        MainParser mainParser=new MainParser(stmtList);
        List<SootClass> sootClasses=mainParser.getClasses();
        SootClass mainClass=null;
        //self end

        String mainclass = "MainClass";

        //set classpath
        String javapath = System.getProperty("java.class.path");
        String jredir = System.getProperty("java.home")+"/lib/rt.jar";
        String pathSoot = javapath+File.pathSeparator+jredir;
        Scene.v().setSootClassPath(pathSoot);

        //add an intra-procedural analysis phase to Soot
        TestSoot analysis = new TestSoot();
        PackManager.v().getPack("jtp").add(new Transform("jtp.TestSoot", analysis));

        //load and set main class
        Options.v().set_app(true);
        for(SootClass sootClass:sootClasses){
            //Scene.v().addClass(sootClass);
            if(sootClass.getName().equalsIgnoreCase("MainClass")){
                mainClass=sootClass;
                Scene.v().addClass(sootClass);

            }
//            else{
//                argsList.add(sootClass.getName());
//            }
        }
        Scene.v().setMainClass(mainClass);
        Scene.v().loadNecessaryClasses();

        //start working
        PackManager.v().runPacks();
    }

    @Override
    protected void internalTransform(Body b, String phaseName,
                                     Map<String, String> options) {

        Iterator<Unit> it = b.getUnits().snapshotIterator();
        while(it.hasNext()){
            Stmt stmt = (Stmt)it.next();

            System.out.println(stmt);
        }
    }
}