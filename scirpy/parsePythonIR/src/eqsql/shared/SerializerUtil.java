package eqsql.shared;

import eqsql.EqSQLConfig;

import java.io.*;
import java.util.Collection;
import java.util.Set;

/**
 * Created by K. Venkatesh Emani on 5/2/2017.
 * //TODO: Functionality duplicated across serializer functions. Replace with a single function.
 */
public class SerializerUtil {

    public static void serializeEqSQLInput(EqSQLMainInput input, String pathToFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(pathToFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(input);
        oos.close();
        fos.close();
        EqSQLConfig.getLogger().info("EqSQL input written to file.");
    }

    public static EqSQLMainInput deserializeEqSQLInput(String pathToFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(pathToFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        EqSQLMainInput input = (EqSQLMainInput)ois.readObject();

        ois.close();
        fis.close();
        EqSQLConfig.getLogger().info("EqSQL input read from file");
        return input;
    }

    public static void serializeEqSQLOutput(Set<String> failedFuncs, String pathToFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(pathToFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(failedFuncs);
        oos.close();
        fos.close();
        EqSQLConfig.getLogger().info("EqSQL output written to file.");
    }

    public static Set<String> deserializeEqSQLOutput(String pathToFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(pathToFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Set<String> output = (Set<String>)ois.readObject();

        ois.close();
        fis.close();
        EqSQLConfig.getLogger().info("EqSQL output read from file");
        return output;
    }

    public static void serializeEqSQLTestResult(Collection results, String pathToFile) throws IOException{
        File file = new File(pathToFile);
        if(!file.getParentFile().exists()){
            EqSQLConfig.getLogger().error("Directory for file: " + pathToFile + " not found.");
            return;
        }

        FileOutputStream fos = new FileOutputStream(pathToFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(results);
        oos.close();
        fos.close();
        EqSQLConfig.getLogger().info("EqSQL test result written to file.");
    }

    public static Collection deserializeEqSQLTestResult(String pathToFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(pathToFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Collection output = (Collection) ois.readObject();

        ois.close();
        fis.close();
        EqSQLConfig.getLogger().info("EqSQL test result read from file");
        return output;
    }

    public static void serializeEqSQLTestTime(double timeTaken, String pathToFile) throws IOException {
        File file = new File(pathToFile);
        if(!file.getParentFile().exists()){
            EqSQLConfig.getLogger().error("Directory for file: " + pathToFile + " not found.");
            return;
        }

        FileOutputStream fos = new FileOutputStream(pathToFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(timeTaken);
        oos.close();
        fos.close();
        EqSQLConfig.getLogger().info("EqSQL output written to file.");
    }

    public static double deserializeEqSQLTestTime(String pathToFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(pathToFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        double output = (double) ois.readObject();

        ois.close();
        fis.close();
        EqSQLConfig.getLogger().info("EqSQL test time read from file");
        return output;
    }
}
