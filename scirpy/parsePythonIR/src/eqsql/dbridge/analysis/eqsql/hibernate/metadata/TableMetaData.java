package eqsql.dbridge.analysis.eqsql.hibernate.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * 
 */

public class TableMetaData {

    public String classType;
    public String className;
    public String tableName;
    public String extendClassName;
    public List<String> keyColumn = new ArrayList<String>();
    public List<String> keyAttrName = new ArrayList<String>();
    public List<String> keyAttrType = new ArrayList<String>();

    public HashMap<String, TableReferences> tableReferences = new HashMap<String, TableReferences>();
    public HashMap<String, TableAttribute> tableAttributes = new HashMap<String, TableAttribute>();
    public HashMap<String, TableReferences> classTableReferences = new HashMap<String, TableReferences>();

    public void addTableRefence(String tableName, TableReferences tableReference) {

        this.tableReferences.put(tableName, tableReference);
    }

    public void addClassTableRefence(String className, TableReferences tableReference) {

        this.classTableReferences.put(className, tableReference);
    }


    public HashMap<String, TableReferences> getClassTableReferences() {
        return classTableReferences;
    }

    public void setClassTableReferences(
            HashMap<String, TableReferences> classTableReferences) {
        this.classTableReferences = classTableReferences;
    }

    public TableMetaData() {
        super();
        // TODO Auto-generated constructor stub
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public void addTableAttribute(String attribute, TableAttribute tableAttribute) {

        this.tableAttributes.put(attribute, tableAttribute);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getExtendClassName() {
        return extendClassName;
    }

    public void setExtendClassName(String extendClassName) {
        this.extendClassName = extendClassName;
    }


    public HashMap<String, TableReferences> getTableReferences() {
        return tableReferences;
    }

    public void setTableReferences(HashMap<String, TableReferences> tableReferences) {
        this.tableReferences = tableReferences;
    }

    public HashMap<String, TableAttribute> getTableAttributes() {
        return tableAttributes;
    }

    public void setTableAttributes(HashMap<String, TableAttribute> tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    @Override
    public String toString() {
        String metaData = "Class Type : " + this.classType;
        metaData += "\nHibernate Class : " + this.className;
        metaData += "\nDataBase Table : " + this.tableName;
        metaData += "\nExtended Class : " + this.extendClassName;
        metaData += "\nKey Column : " + this.keyColumn.toString();
        metaData += "\n\nAttribute List :";
        for (String key : tableAttributes.keySet()) {
            metaData += "\n" + key + " : " + tableAttributes.get(key).toString();
        }

        metaData += "\n\nMapping List :";
        for (String key : tableReferences.keySet()) {
            metaData += "\n" + key + " : " + tableReferences.get(key).toString();
        }
        return metaData;
    }

}
