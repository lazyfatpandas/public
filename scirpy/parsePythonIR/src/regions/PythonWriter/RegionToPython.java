package regions.PythonWriter;
/* bhu created on 4/5/20  */


import dbridge.analysis.region.regions.ARegion;
import dbridge.analysis.region.regions.BranchRegion;
import ir.IStmt;
import ir.JPBody;
import ir.Stmt.NopStmt;
import regions.RegionDriver;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class RegionToPython {
    ARegion head;

    public RegionToPython(ARegion head) {
        this.head = head;
    }

    public RegionToPython() {
    }


    public void toFile(String inputPath, String outputPath, String fileName, ARegion head){
        IStmt iStmt=null;
        try{
            FileWriter output=new FileWriter(outputPath+"/"+"Region_rewritten_"+fileName);
            output.write(head.toString2());
            output.close();
        }
        catch (Exception e){
            System.out.println("Rewrite error for stmt type:"+iStmt.getClass()+ " line no: "+ iStmt.getLineno()+"Error:"+e.getMessage());
        }

    }
    //for interprocedural or multiple methods, return region as strings
    public String toString(ARegion head){
        return head.toString2();

    }

    public void toFileInterprocedural(String inputPath, String outputPath, String fileName, List<SootClass> sootClasses){
        IStmt iStmt=null;
        try{
            FileWriter output=new FileWriter(outputPath+"/"+"Opt_Code_"+fileName);
            SootClass mainClass=null;
            //IP START
            for (SootClass sootClass : sootClasses) {
                //Scene.v().addClass(sootClass);

                if (sootClass.getName().equalsIgnoreCase("MainClass")) {
                    mainClass = sootClass;
                }
            }


            //TESTCASE
            String code="";
            for (SootMethod sootMethod : mainClass.getMethods()) {

                if(sootMethod.getName()!="main"){
                    JPBody jpBody=(JPBody)(sootMethod.getActiveBody());
                    RegionDriver regionDriver=new RegionDriver();
                    regionDriver.buildRegion(jpBody);

                    code=code+"\n"+regionDriver.getTopRegion().toString2("  ");
                    System.out.println("Method Code:"+code);
                }

            }
            SootMethod sootMethod = mainClass.getMethodByName("main");
            JPBody jpBody=(JPBody)(sootMethod.getActiveBody());
            RegionDriver regionDriver=new RegionDriver();
            regionDriver.buildRegion(jpBody);

            code=code+"\n"+regionDriver.getTopRegion().toString2("");
            System.out.println("Method Code:"+code);
            //IP END


            output.write(code);
            output.close();
        }
        catch (Exception e){
            System.out.println("Exception in region to python for:"+iStmt);
            System.out.println("Rewrite error for stmt type:"+iStmt.getClass()+ " line no: "+ iStmt.getLineno()+"Error:"+e.getMessage());
        }

    }
}
