package json;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ReadPythonIR {
    //String pythonIRFile;

//    public ReadPythonIR(String pythonIRFile) {
//        this.pythonIRFile = pythonIRFile;
//        JSONParser jsonParser = new JSONParser();
//    }
    public static JSONArray ReadFile(String pythonIRFile) {
        JSONParser jsonParser = new JSONParser();
        JSONArray codeBodies=null;

        try (FileReader reader = new FileReader(pythonIRFile))
        {
            //Read JSON file
            JSONObject codeObj=(JSONObject)jsonParser.parse(reader);
            //This is returning code lines as of now
            codeBodies = (JSONArray)codeObj.get("body");
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found:"+pythonIRFile);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception for:"+pythonIRFile);
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Incorrect json file:"+pythonIRFile);
            e.printStackTrace();
        }
        return codeBodies;
    }
    //Redundant here::not used any more
    private static void parseCodeBody(JSONObject body)
    {
        //get keys in each statement
        for(Iterator iterator = body.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            System.out.println(key);
            System.out.println(body.get(key));
            System.out.println("END of body");

        }




    }
    }

