package rewrite.pd1;

import ir.Stmt.AssignStmt;
import rewrite.pd2.InputFileDataTypeMapper;
import soot.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pd1Elt {
    int lineno;
    Unit unit;
    List<String> cols=new ArrayList<>();
    //Modified this to store dropped columns and df name for drop
    String dfName="";
    List<String> dropCols=new ArrayList<>();
    List<Unit> dropsColsUnit=new ArrayList<>();
    Map<String, Unit> dropColUnitMap=new HashMap<>();
    InputFileDataTypeMapper ifdtm=null;
    //Not used
    //TODO remove
    List<String> columnsFromDataFile;
    String fileName="";
    AssignStmt usedColListStmt;

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    public List<String> getCols() {
        return cols;
    }

    public void setCols(List<String> cols) {
        this.cols = cols;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    //Modified this to store dropped columns and df name for drop


    public String getDfName() {
        return dfName;
    }

    public void setDfName(String dfName) {
        this.dfName = dfName;
    }

    public List<String> getDropCols() {
        return dropCols;
    }

    public void setDropCols(List<String> dropCols) {
        this.dropCols = dropCols;
    }

    public List<Unit> getDropsColsUnit() {
        return dropsColsUnit;
    }

    public void setDropsColsUnit(List<Unit> dropsColsUnit) {
        this.dropsColsUnit = dropsColsUnit;
    }

    public Map<String, Unit> getDropColUnitMap() {
        return dropColUnitMap;
    }

    public void setDropColUnitMap(Map<String, Unit> dropColUnitMap) {
        this.dropColUnitMap = dropColUnitMap;
    }

    public List<String> getColumnsFromDataFile() {
        return columnsFromDataFile;
    }

    public void setColumnsFromDataFile(List<String> columnsFromDataFile) {
        this.columnsFromDataFile = columnsFromDataFile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputFileDataTypeMapper getIfdtm() {
        return ifdtm;
    }

    public void setIfdtm(InputFileDataTypeMapper ifdtm) {
        this.ifdtm = ifdtm;
    }

    public AssignStmt getUsedColListStmt() {
        return usedColListStmt;
    }

    public void setUsedColListStmt(AssignStmt usedColListStmt) {
        this.usedColListStmt = usedColListStmt;
    }
}
