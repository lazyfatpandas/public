package rewrite.pd1;

import ir.util.CodeLine;
import java.util.List;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

public class Pd1Rewrite {

public void rewriteSource(String inputPath, String outputPath, String fileName, List<CodeLine> rewrites, List<CodeLine> deletes, List<CodeLine>additions){
    //FileWriter input=new FileWriter(inputPath +"/"+fileName+".py");
    try{
    boolean modified=false,deleted=false,edited=false;
    FileWriter output=new FileWriter(outputPath+"/"+"rewritten_"+fileName+".py");
    String strCurrentLine;
    BufferedReader bufferreader = new BufferedReader(new FileReader(inputPath +"/"+fileName+".py"));


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
    output.close();
    }
catch (Exception e){
        System.out.println("Rewrite error:"+ e.getMessage());
}

}


    public void rewritePd1(String inputPath, String outputPath, String fileName, List<Pd1Elt> pd1Elts){
        //FileWriter input=new FileWriter(inputPath +"/"+fileName+".py");
        try{
            boolean modified=false,deleted=false,edited=false;
            FileWriter output=new FileWriter(outputPath+"/"+"rewritten_"+fileName+".py");
            String strCurrentLine;
            BufferedReader bufferreader = new BufferedReader(new FileReader(inputPath +"/"+fileName+".py"));
            int lineNo=0, pdNo=0;

            while ((strCurrentLine = bufferreader.readLine()) != null) {
                //Check if this line has been modified or deleted or removed
                lineNo++;
                //Lineno is same as the line where we have to insert the code
                if(pdNo<pd1Elts.size() && lineNo==pd1Elts.get(pdNo).getLineno()) {
                    String cols="[";
                    for(String s:pd1Elts.get(pdNo).getCols()){
                        cols=cols+"\""+s+"\""+",";
                    }
                    cols=cols.substring(0,cols.lastIndexOf(","));
                    cols=cols+"]";
                    output.write("cols=" + cols + System.getProperty("line.separator"));
                    pdNo++;
                    strCurrentLine=strCurrentLine.substring(0,strCurrentLine.length()-1)+", usecols=cols"+")";

                    output.write(strCurrentLine+System.getProperty( "line.separator" ));
                }
                else{
                    output.write(strCurrentLine+System.getProperty( "line.separator" ));
                }



            }
            output.close();
        }
        catch (Exception e){
            System.out.println("Rewrite error:"+ e.getMessage());
        }

    }

}
