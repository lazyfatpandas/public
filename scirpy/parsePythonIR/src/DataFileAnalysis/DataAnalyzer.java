package DataFileAnalysis;
/* bhu created on 19/5/20  */


import DataFileAnalysis.DateAnalyzer.DateFormatAnalyzer;
import DataFileAnalysis.model.DataType;
import DataFileAnalysis.model.FileInfo;
import DataFileAnalysis.model.MetaData;
import analysis.PythonScene;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;
import java.lang.Math;


public class DataAnalyzer {
    static String DELIMITER="";
    public static MetaData csvMetaDataGenerator(MetaData metaData) {
        MetaData md=null;
        final int ROWS=1000;
        int i=0;

        String path=metaData.getFileInfo().getAbsPath();
        FileInfo fileInfo=metaData.getFileInfo();

        ArrayList<String> clmnList=new ArrayList();
        Map<String, String> clmnTypeMap=new HashMap<>();
        String fileCache[][]=new String[ROWS][];
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

            fileCache=new String[ROWS][clmnList.size()];
            for(i=0;i<ROWS;i++){
                int j=0;
                line = lineReader.readLine();
                sc = new Scanner(line);
                sc.useDelimiter(DELIMITER);
                while(sc.hasNext()) {
                    fileCache[i][j]=sc.next();
                    //System.out.print(fileCache[i][j]+",");
                    j++;
                }
                j=0;
                //System.out.println();
                sc.close();
            }
            System.out.println();
            updateTableDataType(clmnTypeMap,clmnList,fileCache);
            boolean[] isCategory=getIsCategory(fileCache);
            String metaPath=metaData.getMetaPath();
            long[] rowVals=avgRowSize(clmnTypeMap,clmnList,fileCache,clmnSizeMapFile,clmnSizeMapDataFrame,metaData);
            //3.01 Aug 06,2023 This update happens in random..here it is just not required any more..
            //storeUpdateMD(fileInfo, clmnList,clmnTypeMap,isCategory, metaPath, rowVals[0], rowVals[1]);

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

    static String datePattern="";
    static boolean isDate=false;
    static boolean isDateColumn=false;
    public static DataType getType(String value){
        //Modified on 30/01/21 for date datatype
        DateFormatAnalyzer dfa=new DateFormatAnalyzer();
        //TODO maintain column level info to fasten it up//
        //if(datePattern!=null && datePattern.equals("")){
            datePattern=dfa.getDatePatten(value);
            if(!datePattern.equals("NOTDATE")) {
                isDate = true;
                return DataType.date;
           // }
        }
        if(isDate && datePattern!=null && datePattern!="NOTDATE"){
            if(dfa.isDatePattern(datePattern,value)){
                return DataType.date;

            }
        }
        //modified on 30/01/21 end


        String type="Ambiguos";
        int iv;
        String sv;
        float fv=0f;
        double dv;
        try{
            iv=Integer.parseInt(value);
            if(value.contains(".")){

            }
            else {
                return DataType.int64;
            }
        }
        catch (Exception e){}
        try{
            fv= Float.parseFloat(value);
            return DataType.float32;
        }
        catch (Exception e){}
        try{
            dv=Double.parseDouble(value);
            return DataType.float64;
        }
        catch (Exception e){}

        return DataType.str;
    }

    public static void updateTableDataType(Map clmnTypeMap, List clmnList, String[][] fileCache ){
        DataType[][] fileDataTypes=new DataType[fileCache.length][fileCache[0].length];
        for(int i=0;i< fileCache.length;i++){
            for (int j=0;j<clmnList.size();j++){
                updateIndividualDataType(clmnList.get(j).toString(), getType(fileCache[i][j]),clmnTypeMap);
            }

        }
       // System.out.println("here"+clmnTypeMap);
    }

    public static void updateTableDataTypeCSVRecord(Map clmnTypeMap, List clmnList,List<CSVRecord> fileCache ){
        DataType[][] fileDataTypes=new DataType[fileCache.size()][fileCache.get(0).size()];
        for(int i=0;i< fileCache.size();i++){
            for (int j=0;j<clmnList.size();j++){
//                System.out.println("clmType:"+clmnTypeMap.get(clmnList.get(j)));
                updateIndividualDataType(clmnList.get(j).toString(), getType(fileCache.get(i).get(j)),clmnTypeMap);
//                System.out.println("clmType After:"+clmnTypeMap.get(clmnList.get(j)));

            }
        }
        System.out.println(clmnTypeMap);

        // System.out.println("here"+clmnTypeMap);
    }
    public static void updateIndividualDataType(String clmn, DataType dataType, Map clmnTypeMap){
        switch(dataType){
            case date:
                if(clmnTypeMap.get(clmn).equals("")){
                    clmnTypeMap.replace(clmn, DataType.date.name());
                }
                break;
            case int64:
                if(clmnTypeMap.get(clmn).equals("") || clmnTypeMap.get(clmn).equals(DataType.date)){
                    clmnTypeMap.replace(clmn, DataType.int64.name());
                }
                break;
            case float32:
                if(clmnTypeMap.get(clmn).equals("") || clmnTypeMap.get(clmn).equals(DataType.int64)){
                    clmnTypeMap.replace(clmn, DataType.float32.name());
                }
                break;
            case float64:
                if(clmnTypeMap.get(clmn).equals("") || clmnTypeMap.get(clmn).equals(DataType.int64) || clmnTypeMap.get(clmn).equals(DataType.float32) ||  clmnTypeMap.get(clmn).equals(DataType.date)){
                    clmnTypeMap.replace(clmn, DataType.float64.name());
                }
                break;
            case str:
                clmnTypeMap.replace(clmn, DataType.str.name());
                break;
            default:
                clmnTypeMap.replace(clmn, DataType.ambiguos.name());
                break;
        }

    }

    public static boolean[] getIsCategory(String[][] fileCache){
        boolean[] isCategory=new boolean[fileCache[0].length];
        Set<String> set=new HashSet<>();
        for(int i=0;i<fileCache[0].length;i++) {
            String columnName=fileCache[0][i];
            for(int j=0;j<fileCache.length;j++){
                set.add(fileCache[j][i]);
            }
            if(set.size()*2<=fileCache.length){
                isCategory[i]=true;
            }
            set=new HashSet<>();
        }
        for(boolean val:isCategory)
            System.out.print(val +" ");

        return isCategory;

    }

    public static void storeUpdateMD(FileInfo fileInfo,  ArrayList clmnlist, Map clmnTypeMap, boolean[] isCategory, String metaPath, long rowSize, long totalRows) throws IOException {
        storeFileMD(fileInfo, clmnlist, clmnTypeMap, isCategory, metaPath, rowSize,totalRows);

    }

    /*
    If metadata exists, it populates metadata metadata from metastore.
    @param metaData the metatdata object that is checked for existence and if exists, is populated from metastore.
    @return True if metadata exists (along with metaData), False if it does not exist.
     */
    public static boolean isExistingFileMD( MetaData metaData){
        FileInfo fileInfo =metaData.getFileInfo();
        System.out.println("fileInfo.getFile().lastModified():"+fileInfo.getFile().lastModified());
        boolean exists =false;
        try {
            File file = new File(metaData.getMetaPath());
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            for (line = br.readLine(); line != null; line = br.readLine()) {
                if (line.equalsIgnoreCase("####: " + "" + fileInfo.getName())) {
                    line = br.readLine();
                    // String lastModified=

                    if(line.equalsIgnoreCase(String.valueOf(fileInfo.getFile().lastModified())))
                    {
                        //SET METADATA as it already exists
                        System.out.println("Existing Metadata, Filename is:"+ fileInfo.getName());

                        line = br.readLine();//ignore address//could be compared later
                        Scanner sc;
                        line = br.readLine();//clmnList
                        sc = new Scanner(line);
                        sc.useDelimiter(",");
                        while(sc.hasNext()) {
                            String clmn=sc.next();
                            metaData.getClmnList().add(clmn);
                            metaData.getClmnTypeMap().put(clmn,"");
                        }
                        sc.close();
                        //NOW get MAp
                        int j=0;
                        line = br.readLine();//Type of columns
                        sc = new Scanner(line);
                        sc.useDelimiter(",");
                        while(sc.hasNext()) {
                            String type=sc.next();
                            metaData.getClmnTypeMap().replace(metaData.getClmnList().get(j), type);
                            j++;
                        }
                        //Category Information
                        line = br.readLine();//category
                        sc = new Scanner(line);
                        sc.useDelimiter(",");
                        boolean[] isCategory=new boolean[metaData.getClmnList().size()];
                        j=0;
                        while(sc.hasNext()) {
                            isCategory[j]=sc.nextBoolean();
                            j++;
                        }
                        metaData.setIsCategory(isCategory);

                        //rowsize and totalrows
                        line = br.readLine();//category
                        sc = new Scanner(line);
                        metaData.setRowSize(sc.nextLong());
                        line = br.readLine();//category
                        sc = new Scanner(line);
                        metaData.setTotalRows(sc.nextLong());
                        exists = true;
                        break;
                    }
                }
            }
            br.close();
            fr.close();
        }
        catch (IOException e){
            System.out.println("Meta data IO Error:"+e.getMessage());
        }
        return exists;
    }

    //NOT WORKING
    public static void deleteFileMD(FileInfo fileInfo, String metaPath) throws IOException{
        try {
            File file = new File(metaPath);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            int lineno=0;
            for (line = br.readLine(),lineno=0; line != null; line = br.readLine(),lineno++) {
                if (line.equalsIgnoreCase("####: " + "" + fileInfo.getName())) {
                    break;
                }

            }
            br.close();
            fr.close();
        }
        catch (IOException e){
            System.out.println("Meta data IO Error:"+e.getMessage());
        }


    }

    public static void storeFileMD(FileInfo fileInfo,  ArrayList clmnlist, Map clmnTypeMap, boolean[] isCategory, String metaPath, long rowSize, long totalRows) throws IOException{
        try {
            File file = new File(metaPath);
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);

            br.write("####: " + "" + fileInfo.getName() + "\n");
            br.write(fileInfo.getFile().lastModified() + "\n");
            br.write(fileInfo.getAbsPath() + "\n");
            for (int i = 0; i < clmnlist.size(); i++){
                br.write(clmnlist.get(i) + ",");
            }
            br.write("\n");
            for ( int i = 0; i < clmnlist.size(); i++){
                br.write(clmnTypeMap.get(clmnlist.get(i)) + ",");
            }
            br.write("\n");
            for (boolean cat : isCategory) {
                br.write(cat + ",");
            }
            br.write("\n");
            br.write(rowSize+ "\n");
            br.write(totalRows+ "\n");


            br.write("\n");

            br.close();
            fr.close();

        }
        catch (IOException e){
            System.out.println("Error in creating META DATA FILE: "+e.getMessage());
        }


    }
    public static long avgColumnDataSize(String[][] fileCache,int j){
        // find avg size of the string values stored in a column
        long avgSize=0L;

        for(int i=0;i< fileCache.length;i++){
            avgSize += fileCache[i][j].getBytes().length;

        }
        double avg =  Math.ceil(((double)avgSize)/fileCache.length);
        return (long)avg;
    }


    public static long[] avgRowSize(Map clmnTypeMap,List clmnList,String[][] fileCache,Map clmnSizeMapFile,Map clmnSizeMapDataFrame,MetaData metaData){
        // TODO modify implementation as per python's objects size.
        long[] rowvals=new long[2];
        for (int j=0;j<clmnList.size();j++) {
            String clmn = clmnList.get(j).toString();

            long avgSize = avgColumnDataSize(fileCache,j);
            clmnSizeMapFile.put(clmn,avgSize);

            /*else if (clmnTypeMap.get(clmn) != "ambigous") {
                String dataType = (String) clmnTypeMap.get(clmn);
                switch(dataType){
                    case "int64":
                        clmnSizeMapFile.put(clmn,8L);
                        break;
                    case "float32":
                        clmnSizeMapFile.put(clmn,4L);
                        break;
                    case "float64":
                        clmnSizeMapFile.put(clmn,8L);
                        break;
                    case "category":
                        // TODO : change this
                        clmnSizeMapFile.put(clmn,1L);
                        break;
                    default:
                        System.out.println("Unknown Data Type");
                        break;
                }
            }*/

        }
        Long rowSize= 0L, totalRows=0L,fileSize=0L;
        for (int j=0;j<clmnList.size();j++) {
            String clmn = clmnList.get(j).toString();
            rowSize += (Long)clmnSizeMapFile.get(clmn);

        }

        try {
            fileSize = metaData.getFileInfo().getFileSize(metaData.getFileInfo().getAbsPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        totalRows = (long)Math.ceil((double)fileSize/rowSize);
        Long temp= 0L;
        for (int j=0;j<clmnList.size();j++) {
            String clmn = clmnList.get(j).toString();
            long singleValueSize = (long)clmnSizeMapFile.get(clmn);
            temp += totalRows*singleValueSize;
        }
        //System.out.println(clmnTypeMap);
        //TODO : This part is dependent on python version(current implementation for python3)
        for (int j=0;j<clmnList.size();j++) {
            String clmn = clmnList.get(j).toString();
            int listReferencePointerSize = 8;
            int overhead;
            if(clmnTypeMap.get(clmn) == "str"){
                overhead = 49; // for python3 string objects in Bytes
                long singleValueSize = (long)clmnSizeMapFile.get(clmn);
                //System.out.println(clmn+" "+singleValueSize);
                clmnSizeMapDataFrame.put(clmn,totalRows*(overhead+listReferencePointerSize+singleValueSize));
            }
            else if (clmnTypeMap.get(clmn) != "ambigous") {
                String dataType = (String) clmnTypeMap.get(clmn);
                switch(dataType){
                    case "int64":
                        //overhead = 36;
                        clmnSizeMapDataFrame.put(clmn,totalRows*8);
                        break;
                    case "float32":
                        //overhead = 24;
                        clmnSizeMapDataFrame.put(clmn,totalRows*4);
                        break;
                    case "float64":
                        //overhead = 32;
                        clmnSizeMapDataFrame.put(clmn,totalRows*8);
                        break;
                    case "category":

                        clmnSizeMapDataFrame.put(clmn,totalRows);
                        break;
                    default:
                        System.out.println("Unknown Data Type");
                        break;
                }
            }


        }

        System.out.println("total rows : "+totalRows);
        System.out.println("column size : "+clmnSizeMapFile);
        System.out.println("actual file size : "+fileSize);
        System.out.println("estimated file size : " +temp);
        System.out.println("column size for dataframe : "+clmnSizeMapDataFrame);
        rowvals[0]=rowSize;
        rowvals[1]=totalRows;
//        metaData.setRowSize(rowSize);
//        metaData.setTotalRows(totalRows);
        return rowvals;
    }

    public static MetaData getExistingFileMD( MetaData metaData){
        FileInfo fileInfo =metaData.getFileInfo();
        boolean exists =false;
        try {
            File file = new File(metaData.getMetaPath());
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            for (line = br.readLine(); line != null; line = br.readLine()) {
                //this condition checks if metadata exists already, if yes, set it
                if (line.equalsIgnoreCase("####: " + "" + fileInfo.getName())) {
                    line = br.readLine();
                    // String lastModified=
                    //This checks if existing file has not been modified
                    if(line.equalsIgnoreCase(String.valueOf(fileInfo.getFile().lastModified())))
                    {
                        //SET METADATA as it already exists
                        line = br.readLine();//ignore address//could be compared later
                        Scanner sc;
                        line = br.readLine();//clmnList
                        sc = new Scanner(line);
                        sc.useDelimiter(DELIMITER);
                        while(sc.hasNext()) {
                            String clmn=sc.next();
                            metaData.getClmnList().add(clmn);
                            metaData.getClmnTypeMap().put(clmn,"");
                        }
                        sc.close();
                        //NOW get MAp
                        int j=0;
                        line = br.readLine();//Type of columns
                        sc = new Scanner(line);
                        sc.useDelimiter(DELIMITER);
                        while(sc.hasNext()) {
                            String type=sc.next();
                            metaData.getClmnTypeMap().replace(metaData.getClmnList().get(j), type);
                            j++;
                        }
                        //Category Information
                        line = br.readLine();//category
                        sc = new Scanner(line);
                        sc.useDelimiter(DELIMITER);
                        boolean[] isCategory=new boolean[metaData.getClmnList().size()];
                        j=0;
                        while(sc.hasNext()) {
                            isCategory[j]=sc.nextBoolean();
                            j++;
                        }
                        metaData.setIsCategory(isCategory);
                        exists = true;
                        break;
                    }
                }
            }
            br.close();
            fr.close();
        }
        catch (IOException e){
            System.out.println("Meta data IO Error:"+e.getMessage());
        }
        if(!exists) {
            return null;
        }
            return metaData;
    }
}
