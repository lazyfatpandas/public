package ir.callgraph;

import dbridge.analysis.region.regions.RegionGraph;
import ir.IExpr;
import ir.Stmt.AssignStmt;
import ir.Stmt.AssignmentStmtSoot;
import ir.Stmt.CallExprStmt;
import ir.expr.Attribute;
import ir.expr.Call;
import ir.expr.Name;
//import jdk.nashorn.internal.ir.Assignment;
import json.ReadPythonIR;
import org.json.simple.JSONArray;
import parse.IRMaker.MainParser;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CallGraphBuilder {

//    public Edge(MethodOrMethodContext src, Unit srcUnit, MethodOrMethodContext tgt, Kind kind) {
//        this.src = src;
//        this.srcUnit = srcUnit;
//        this.tgt = tgt;
//        this.kind = kind;
//    }
//gitgit add .
 //   git comm
    Map<String, String> objectClassMap = new HashMap<>();
    List<String> classNames = new ArrayList<>();
    Map<String, SootClass> nameClassMap = new HashMap<>();
    Map<String, ArrayList<String>> classMethodsMap = new HashMap<>();
    //List<SootMethod> methodsToVisit = new LinkedList<>();
    ConcurrentLinkedQueue<SootMethod> methodsToVisit = new ConcurrentLinkedQueue();
    Kind kind = Kind.VIRTUAL;

    List<SootMethod> allMethods = new ArrayList<>();
    List<String> allMethodNames = new ArrayList<>();
    Map<String, SootMethod> methodNameMap = new HashMap<>();


    Map<String, SootClass> methodNameClassMap = new HashMap<>();
    CallGraph cg = new CallGraph();
    public CallGraph callGraphMaker() {


        MethodContext src;
        Unit srcUnit;
        MethodContext tgt;


        //Self start
        String sourcePath = "/home/bhushan/intellijprojects/scirpy/pythonIR";
        String path = "/home/bhushan/intellijprojects/scirpy/pythonIR";
        String dataPath = "/home/bhushan/intellijprojects/scirpy/pythonIR";
        String filename = "temp.json";


        List<SootMethod> visitedMethods = new ArrayList<>();

        JSONArray stmtList = ReadPythonIR.ReadFile(filename);
        MainParser mainParser = new MainParser(stmtList);
        List<SootClass> sootClasses = mainParser.getClasses();
        SootClass mainClass = null;

        //self end
        for (SootClass sootClass : sootClasses) {
            //Scene.v().addClass(sootClass);

            if (sootClass.getName().equalsIgnoreCase("MainClass")) {
                mainClass = sootClass;
            }
            classNames.add(sootClass.getName());
            nameClassMap.put(sootClass.getName(), sootClass);

            //Method preprocessing
            classMethodsMap.put(sootClass.getName(), new ArrayList<>());
            for (SootMethod sootMethod : sootClass.getMethods()) {
                allMethods.add(sootMethod);
                methodNameMap.put(sootMethod.getName(), sootMethod);
                allMethodNames.add(sootMethod.getName());
                classMethodsMap.get(sootClass.getName()).add(sootMethod.getName());
                methodNameClassMap.put(sootMethod.getName(), sootClass);
            }
        }




        SootMethod mainMethod=mainClass.getMethodByName("main");
        methodsToVisit.add(mainMethod);
        while(!methodsToVisit.isEmpty()){
            SootMethod sootMethod=methodsToVisit.poll();
            parseMethod(sootMethod);
        }
        //parseMethod(mainMethod);
        //visitedMethods.add(mainMethod);

        return cg;

    }
        //check main method for edges out of it
//        Iterator callees = cg.edgesOutOf(mainMethod);
//        while (callees.hasNext()){
//            MethodOrMethodContext callee = ((Edge) callees.next()).getTgt();
//            String calleeStrNotrim = callee.toString();
//            //String calleeStr = trim(calleeStrNotrim);
//            System.out.println(calleeStrNotrim);
//
//        }


    private void parseMethod (SootMethod srcSootMethod){
        SootClass sourceSootClass=methodNameClassMap.get(srcSootMethod.getName());
        for (Unit unit : srcSootMethod.getActiveBody().getUnits()) {
            if (unit instanceof AssignmentStmtSoot) {
                AssignStmt assignStmt = ((AssignmentStmtSoot) unit).getAssignStmt();
                System.out.println(assignStmt);
                IExpr rhs = assignStmt.getRHS();
                if (rhs instanceof Call) {
                    IExpr callname = ((Call) rhs).getFunc();
                    if (classNames.contains(callname.toString())) {
                        objectClassMap.put(assignStmt.getTargets().get(0).toString(), callname.toString());
                    }
                }
            }
            if (unit instanceof CallExprStmt) {

                IExpr iExpr = ((CallExprStmt) unit).getCallExpr();
                if (iExpr instanceof Call) {
                    Call call = (Call) iExpr;
                    IExpr func = call.getFunc();
                    if (func instanceof Attribute) {
                        IExpr value = ((Attribute) func).getValue();
                        String attr = ((Attribute) func).getAttr();

                        SootMethod sootMethod=null;
                        //an object of a class is invoked
                        if (objectClassMap.get(value.toString()) != null) {
                            //now check if it is a method call,
                            //get class for this object
                            SootClass sootClass = nameClassMap.get(objectClassMap.get(value.toString()));
                            sootMethod = sootClass.getMethodByName(attr);
                        }
                        else if(value.toString().equals("self")){
                            sootMethod=methodNameMap.get(attr);

                        }
                            //TODO this doesnt take care of the class to which this method belongs, see how to do it///
                            if (sootMethod != null) {
                                methodsToVisit.add(sootMethod);
                                //Method called and exists, therefore the target points to this method, call graph edge can be added
                                MethodOrMethodContext srcMethodContext = MethodContext.v(srcSootMethod, unit);
                                MethodOrMethodContext tgtMethodContext = MethodContext.v(sootMethod, unit);
                                //create an edge with source as main method, unit as current stmt, target as called method and kind as dummy
                                Edge edge = new Edge(srcMethodContext, unit, tgtMethodContext, kind);
                                //add edge to method
                                cg.addEdge(edge);
                                System.out.println(cg.toString());
                            }


                    }
                    //check if it is the method in same class
                    else if (func instanceof Name) {
                        //TODO all library calls can be consumed here, like we have consumed print
                        if (func.toString().equals("print")) {

                        } else {
                            SootMethod sootMethod = sourceSootClass.getMethodByName(func.toString());

                            //TODO this is code similar to above code, refractor to move the code at a single place.
                            if (sootMethod != null) {
                                MethodOrMethodContext srcMethodContext = MethodContext.v(srcSootMethod, unit);
                                MethodOrMethodContext tgtMethodContext = MethodContext.v(sootMethod, unit);
                                //create an edge with source as main method, unit as current stmt, target as called method and kind as dummy
                                Edge edge = new Edge(srcMethodContext, unit, tgtMethodContext, kind);
                                //add edge to method
                                cg.addEdge(edge);

                            }

                        }
                    }
                }
            }

        }
    }
    private String trim(String methodSign){
        return methodSign.substring(1, methodSign.length() - 1);
    }
}
