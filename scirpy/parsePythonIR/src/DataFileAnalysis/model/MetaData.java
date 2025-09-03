package DataFileAnalysis.model;
/* bhu created on 26/4/20  */


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetaData {

    FileInfo fileInfo;
    ArrayList<String> clmnList=new ArrayList();
    Map<String, String> clmnTypeMap=new HashMap<>();

    Map<String,Long> clmnSizeMap = new HashMap<>();
    boolean[] isCategory;
    //TODO change this to relative path

    String metaPath="/home/bhushan/intellijprojects/scirpy/parsePythonIR/src/METADATA.DATA";
    //String metaPath="/home/mudra/IDEA/scirpy/parsePythonIR/src/METADATA.DATA";
    //added in May 2021 for rowsize snf total rows
    long rowSize=0;
    long totalRows=0;

    ArrayList<String> usedCols=new ArrayList();
    ArrayList<String> parseDates=new ArrayList();



    public MetaData(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public MetaData(FileInfo fileInfo, ArrayList<String> clmnList, Map<String, String> clmnTypeMap, boolean[] isCategory,Map<String,Long> clmnSizeMap) {
        this.fileInfo = fileInfo;
        this.clmnList = clmnList;
        this.clmnTypeMap = clmnTypeMap;
        this.isCategory = isCategory;
        this.clmnSizeMap = clmnSizeMap;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public ArrayList<String> getClmnList() {
        return clmnList;
    }

    public void setClmnList(ArrayList<String> clmnList) {
        this.clmnList = clmnList;
    }

    public Map<String, String> getClmnTypeMap() {
        return clmnTypeMap;
    }

    public void setClmnTypeMap(Map<String, String> clmnTypeMap) {
        this.clmnTypeMap = clmnTypeMap;
    }


    public Map<String, Long> getClmnSizeMap() {
        return clmnSizeMap;
    }

    public void setClmnSizeMap(Map<String, String> clmnTypeMap) {
        this.clmnSizeMap = clmnSizeMap;
    }


    public boolean[] getIsCategory() {
        return isCategory;
    }

    public void setIsCategory(boolean[] isCategory) {
        this.isCategory = isCategory;
    }

    public String getMetaPath() {
        return metaPath;
    }

    public void setMetaPath(String metaPath) {
        this.metaPath = metaPath;
    }

    public long getRowSize() {
        return rowSize;
    }

    public void setRowSize(long rowSize) {
        this.rowSize = rowSize;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
    }

    //Added bh Bhushan on Nov 2024 for metadata json emmittor
    public ArrayList<String> getUsedCols() {
        return usedCols;
    }

    public void setUsedCols(ArrayList<String> usedCols) {
        this.usedCols = usedCols;
    }

    public ArrayList<String> getParseDates() {
        return parseDates;
    }

    public void setParseDates(ArrayList<String> parseDates) {
        this.parseDates = parseDates;
    }
}
