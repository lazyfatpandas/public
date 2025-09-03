package DataFileAnalysis.model;

import DataFileAnalysis.DataAnalyzer;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class RandomSampling {
    static String DELIMITER="";

    public static int get_sampling_ratio(long nrows){
    double no_samples=0.0;
    if(nrows > 100000000){
        no_samples=(nrows-100000000)*0.005+get_sampling_ratio(nrows-100000000);
    }
    else if (nrows>10000000) {
        no_samples = (nrows - 10000000) * 0.01 + get_sampling_ratio(nrows - 10000000);
    }
    else if(nrows>1000000) {
        no_samples = (nrows - 1000000) * 0.02 + get_sampling_ratio(nrows - 1000000);
    }
    else if (nrows>100000) {
        no_samples = (nrows - 100000) * 0.04 + get_sampling_ratio(nrows - 100000);
    }
    else if (nrows>10000) {
        no_samples = (nrows - 10000) * 0.08 + get_sampling_ratio(nrows - 10000);
    }
    else {
        no_samples = nrows;
    }
    return (int)no_samples;
    }

    public static MetaData csvRandomMetaDataGenerator(MetaData metaData) {
        System.out.println("CSV RANDOM");
        MetaData md=null;
        final int ROWS=100;
        int i=0;
        String path=metaData.getFileInfo().getAbsPath();
        FileInfo fileInfo=metaData.getFileInfo();
        ArrayList<String> clmnList=new ArrayList();
        Map<String, String> clmnTypeMap=new HashMap<>();
        String fileCache[][]=new String[ROWS][];
        List<CSVRecord> csvRecordList=new ArrayList<CSVRecord>();
        FileReader fr=null;
        BufferedReader lineReader=null;

        Map<String,Long> clmnSizeMapFile = new HashMap<>();
        Map<String,Long> clmnSizeMapDataFrame = new HashMap<>();
        DELIMITER=",";

        try {
            fr= new FileReader(path);
            lineReader = new BufferedReader(fr);
            String line;
            Scanner sc;
            line = lineReader.readLine();
            long countComma = line.chars().filter(ch -> ch == ',').count();
            long countSemiColon=line.chars().filter(ch -> ch == ';').count();
            if(countComma<countSemiColon){
                DELIMITER=";";
            }
            sc = new Scanner(line);
            sc.useDelimiter(DELIMITER);
            while(sc.hasNext()) {
                String clmn=sc.next();
                clmn=clmn.replace("\"","");
                clmnList.add(clmn);
                clmnTypeMap.put(clmn,"");
            }
            sc.close();
            //System.out.println(clmnList);

//            fileCache=new String[ROWS][clmnList.size()];
            fileCache=fetchFileCache(lineReader, ROWS, clmnList.size());
//            for(i=0;i<ROWS;i++){
//                int j=0;
//                line = lineReader.readLine();
//                sc = new Scanner(line);
//                sc.useDelimiter(",");
//                while(sc.hasNext()) {
//                    fileCache[i][j]=sc.next();
//                    j++;
//                }
//                j=0;
//                sc.close();
//            }
            //PART 1 to estimate size of each row and get estimated number of rows
            long[] rowVals=DataAnalyzer.avgRowSize(clmnTypeMap,clmnList,fileCache,clmnSizeMapFile,clmnSizeMapDataFrame,metaData);

            long rowsize=rowVals[0];
            long nrows=rowVals[1];
            int rowsforSamplling=get_sampling_ratio(nrows);
            //3.01
//            fileCache=fetchFileCacheRandom(lineReader, rowsforSamplling, clmnList.size());

            csvRecordList=RandomSamplingApache.fetchFileCacheRandomA(path, rowsforSamplling, clmnList.size());
//3.01
//            DataAnalyzer.updateTableDataType(clmnTypeMap,clmnList,fileCache);
            DataAnalyzer.updateTableDataTypeCSVRecord(clmnTypeMap,clmnList,csvRecordList);
            boolean[] isCategory=DataAnalyzer.getIsCategory(fileCache);
            String metaPath=metaData.getMetaPath();

            DataAnalyzer.storeUpdateMD(fileInfo, clmnList,clmnTypeMap,isCategory, metaPath, rowVals[0], rowVals[1]);

            md=new MetaData(fileInfo,clmnList,clmnTypeMap,isCategory,clmnSizeMapDataFrame);
            lineReader.close();
            fr.close();
        }

        catch(Exception e){
            System.out.println("Error in data File, can't create meta data properly,");
        }
        finally {

        }
        return md;
    }

    public static String[][] fetchFileCache(BufferedReader lineReader, int rows, int cols) throws Exception{
      String[][]  fileCache=new String[rows][cols];
        String line;
        Scanner sc;
        for(int i=0;i<rows;i++){
            int j=0;
             line = lineReader.readLine();
            sc = new Scanner(line);
            sc.useDelimiter(",");
            while(sc.hasNext()) {
                fileCache[i][j]=sc.next();
                j++;
            }
            j=0;
            sc.close();


        }
        return fileCache;

    }

    public static String[][] fetchFileCacheRandom(BufferedReader lineReader, int rows, int cols) throws Exception{
        Random rand = new Random();
        System.out.println("File Cache Random initiated");
        String[][]  fileCache=new String[rows][cols];
        String line;
        Scanner sc;
        int skipRows=0;
        long itr=0;
        for(int i=0;i<rows;i++){
            int j=0;
            skipRows=rand.nextInt(5);
//            System.out.println(itr+skipRows);
            while(skipRows>0) {
//                line = lineReader.readLine();
               lineReader.readLine();
//                lineReader.skip(".*\n");
                skipRows--;
            }
            line = lineReader.readLine();
            sc = new Scanner(line);
            sc.useDelimiter(",");
            while(sc.hasNext()) {
                fileCache[i][j]=sc.next();
                j++;
            }
            j=0;
            sc.close();
        }
        System.out.println("File Cache random completed");
        return fileCache;

    }

    public static void main(String[] args) {

    }



}
