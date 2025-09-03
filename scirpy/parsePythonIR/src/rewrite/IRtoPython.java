package rewrite;

import ir.IStmt;
import ir.JPBody;
import ir.JPStmt;
import ir.Stmt.IfStmt;
import ir.Stmt.NopStmt;
import soot.Unit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

public class IRtoPython {

    public void toStdOut(JPBody jpBody){
        for(Unit unit:jpBody.getUnits()){
            System.out.println(unit.toString());

        }
    }
    public void toFile(String inputPath, String outputPath, String fileName, JPBody jpBody){
        IStmt iStmt=null;
        try{
            boolean modified=false,deleted=false,edited=false;
            FileWriter output=new FileWriter(outputPath+"/"+"rewritten_"+fileName);
            String strCurrentLine;
            BufferedReader bufferreader = new BufferedReader(new FileReader(inputPath +"/"+fileName));
            strCurrentLine = bufferreader.readLine();
            int currentLineno=1;
            String indent="";

            //Trying to read file here
            //String file=inputPath +"/"+fileName+".py";
            //String line32 = (String) FileUtils.readLines(file).get(31);

            for(Unit unit:jpBody.getUnits()){
                iStmt=(IStmt)unit;
                NopStmt nopIfStmt=null;
                //t1 begin
//                if(iStmt instanceof IfStmt){
//                    IfStmt ifStmt=(IfStmt)unit;
//                    nopIfStmt=(NopStmt)ifStmt.getTarget();
//                }
//                if(unit instanceof  NopStmt){
//                if(nopIfStmt.equals(unit)){
//                    System.out.println("NOP found");
//                }}
                //t1 end
                if(iStmt.getLineno()>=0){
                    if(iStmt.isModified()){
                        //TODO this seems to be not pakka
                        indent=getIndent(strCurrentLine);
                        output.write(indent+ unit.toString()+System.getProperty( "line.separator" ));
                    }
                    else{
                        if(iStmt.getLineno()<currentLineno){
                            System.out.println("Rewrite error in IR to PYthon at line no:"+currentLineno+"for code\n"+unit.toString());
                        }
                        while(iStmt.getLineno()>currentLineno){
                            strCurrentLine = bufferreader.readLine();
                            currentLineno++;
                        }
                        if(iStmt.getLineno()==currentLineno){
                            output.write(strCurrentLine+System.getProperty( "line.separator" ));
                            currentLineno++;
                            strCurrentLine = bufferreader.readLine();

                        }


                    }

                }


            }


            /*

            while ((strCurrentLine = bufferreader.readLine()) != null) {
                //Check if this line has been modified or deleted or removed

                if(modified){

                }
                else if(deleted){

                }
                else if(edited){

                }
                else{
                    output.write(strCurrentLine+System.getProperty( "line.separator" ));
                }


            }


             */
            output.close();
        }

        catch (Exception e){
            System.out.println("Rewrite error for stmt type:"+iStmt.getClass()+ " line no: "+ iStmt.getLineno()+"Error:"+e.getMessage());
        }

    }
    public String getIndent(String strCurrentLine){
        String indent="";
        int i=0;
        if(!strCurrentLine.equals("")) {
            while (strCurrentLine.charAt(i) == ' ') {
                indent = indent + " ";
                i++;
            }

        }
        return indent;
    }

}
