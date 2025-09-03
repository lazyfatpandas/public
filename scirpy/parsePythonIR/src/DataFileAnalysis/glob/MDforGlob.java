package DataFileAnalysis.glob;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import rewrite.pd2.InputFileDataTypeMapper;
import soot.Unit;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.jar.JarOutputStream;

public class MDforGlob {
    public static boolean buildMDforGlob(Unit unit, InputFileDataTypeMapper ifdtm, String fullPath){
        boolean isGlob=false;


        return isGlob;
    }
    public static String getFilePathGlob(Unit unit, InputFileDataTypeMapper ifdtm, String fullPath){
        String firstFilePath="";
        File fileWithPattern = new File(fullPath);
        File parenDir=fileWithPattern.getParentFile();
        String pattern=fileWithPattern.getName();
        WildcardFileFilter fileFilter=new WildcardFileFilter(pattern);
        File[] matchingFiles=parenDir.listFiles((FileFilter) fileFilter);
        if(matchingFiles!=null && matchingFiles.length>0) {
            firstFilePath = matchingFiles[0].getAbsoluteFile().getAbsolutePath();
            return firstFilePath;
        }
        //Assuming no matching File--> File is passed as pattern to glob and not pattern
        //return pattern;
        return fullPath;
    }

    public static void main(String args[]){
        Unit unit=null;
        InputFileDataTypeMapper ifd=null;
        String fullPath="../../../data/yt2/yellow_tripdata_*.csv";
        testFetFilePathGlob(unit,ifd,fullPath);

    }
    public static String testFetFilePathGlob(Unit unit, InputFileDataTypeMapper ifdtm, String fullPath){
        String firstFilePath="";
        File fileWithPattern = new File(fullPath);
        File parenDir=fileWithPattern.getParentFile();
        String pattern=fileWithPattern.getName();
        WildcardFileFilter fileFilter=new WildcardFileFilter(pattern);
        File[] matchingFiles=parenDir.listFiles((FileFilter) fileFilter);
        firstFilePath=matchingFiles[0].getAbsoluteFile().getName();
        return firstFilePath;
    }

}
