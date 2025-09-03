package parse.IRMaker;

import org.json.simple.JSONArray;
import soot.*;
import soot.JastAddJ.Signatures;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SceneMaker {
    JSONArray stmtList;

    public SceneMaker(JSONArray stmtList) {
        this.stmtList = stmtList;
    }

    public void UpdateScene(){
        MainParser mainParser=new MainParser(stmtList);
        List<SootClass> sootClasses=new ArrayList<>();

        String javapath = System.getProperty("java.class.path");
        String jredir = System.getProperty("java.home")+"/lib/rt.jar";
        String path = javapath+ File.pathSeparator+jredir;
        SootClass mainClass=null;
        Scene.v().setSootClassPath(path);
        sootClasses=mainParser.getClasses();
        List<String> argsList = new ArrayList<String>();

        argsList.addAll(Arrays.asList(new String[]{
                "-w",
                "-main-class",
                "MainClass",//main-class
                "-allow-phantom-refs",

        }));
        for(SootClass sootClass:sootClasses){
            Scene.v().addClass(sootClass);

            if(sootClass.getName().equalsIgnoreCase("MainClass")){
                mainClass=sootClass;
            }
            else{
                argsList.add(sootClass.getName());
            }
        }
        //TEST
        Scene.v().loadNecessaryClasses();

        //TEST END
        //SootClass c = Scene.v().loadClassAndSupport(name);
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new ScirpySceneTransformer() ));
       //load and set main class
        Options.v().set_app(true);
        //SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
        String[] args = argsList.toArray(new String[0]);
       // Scene.v().addBasicClass(java.lang.Thread, SIGNATURE);
        soot.Main.main(args);

        //TEMP TEST
        CallGraphBuilder callGraphBuilder=new CallGraphBuilder();
        //TEMP TEST OFF
        CallGraph cg = Scene.v().getCallGraph();


    }

}
