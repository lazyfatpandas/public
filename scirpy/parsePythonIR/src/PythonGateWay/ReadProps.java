package PythonGateWay;

import java.io.*;
import java.util.Properties;

public class ReadProps {
    public static void main(String[] args) {

        try (OutputStream output = new FileOutputStream("/home/bhushan/intellijprojects/scirpy/parsePythonIR/src/PythonGateWay/Scapa.properties")) {

            Properties prop = new Properties();

            // set the properties value
            prop.setProperty("ScapaName", "lazyfatpandas.pandas");
            prop.setProperty("analyzemethodName", "analyze");

            // save properties to project root folder
            prop.store(output, null);
            System.out.println(prop);

        } catch (IOException io) {
            io.printStackTrace();
        }

    }

    public static String read(String propName) {

        try (InputStream input = new FileInputStream("/home/bhushan/intellijprojects/scirpy/parsePythonIR/src/PythonGateWay/Scapa.properties")) {

            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            return prop.getProperty(propName);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    return null;
    }
}

