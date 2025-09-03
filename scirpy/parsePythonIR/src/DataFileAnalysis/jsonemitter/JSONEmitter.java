package DataFileAnalysis.jsonemitter;
import DataFileAnalysis.model.MetaData;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
public class JSONEmitter {
    public void createJSONFromMD(MetaData metaData) {
        // Create the main JSONObject

        // Create "usecols" array
        JSONArray usecols = new JSONArray();
        for(String col:metaData.getUsedCols()){
            usecols.put(col);
        }


        // Create "parse_dates" array
        JSONArray parseDates = new JSONArray();
        for(String date:metaData.getParseDates()){
            parseDates.put(date);
        }
        // Create "dtype" object
        JSONObject dtype = new JSONObject();
        for(String col:metaData.getUsedCols()){
            if(!metaData.getClmnTypeMap().get(col).equals("date")) {
                dtype.put(col, metaData.getClmnTypeMap().get(col));
            }
        }

        JSONObject jsonObject = new JSONObject();
        // Put everything into the main JSON object
        if(dtype.length()>0) {
            jsonObject.put("dtype", dtype);
        }
        if(parseDates.length()>0) {
            jsonObject.put("parse_dates", parseDates);
        }
        if(usecols.length()>0) {
            jsonObject.put("usecols", usecols);
        }

        // Write the JSON object to a file
//        String path=metaData.getFileInfo().getAbsPath()+"/"+metaData.getFileInfo().getName();
        String path=metaData.getFileInfo().getAbsPath();
        System.out.println(path);
        try (FileWriter file = new FileWriter(path+".lafp.json")) {
            file.write(jsonObject.toString(4));  // Indentation for pretty print
            System.out.println("Successfully written metadata JSON to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
