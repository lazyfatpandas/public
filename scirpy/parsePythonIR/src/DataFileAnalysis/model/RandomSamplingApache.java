package DataFileAnalysis.model;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class RandomSamplingApache {

    public static void main(String[] args) {
        int numberOfLinesToRead = 5; // Specify the number of random lines to read

        try (Reader reader = new FileReader("../../../data/b3/s7.csv");
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            for (CSVRecord csvRecord : csvParser) {
                // Process each record
                String column1 = csvRecord.get(0); // Access specific columns by index
//              String column2 = csvRecord.get("ColumnName"); // Access specific columns by name
                System.out.println(column1);
                // Perform your desired operations with the CSV data
                // ...
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<CSVRecord> fetchFileCacheRandomA(String filePath, int rows, int cols) throws Exception{
        Random rand = new Random();
        System.out.println("File Cache Random initiated Apache");
        String[][]  fileCache=new String[rows][cols];
        List<CSVRecord> csvRecordsList=new ArrayList<CSVRecord>();
        String line;
        Scanner sc;
        int skipRows=0;
        long itr=0;
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            int i=0;
            skipRows=rand.nextInt(5);
            for (CSVRecord csvRecord : csvParser) {
                if(skipRows>0) {
                    skipRows--;
                    continue;
                }
                //fileCache[i]= (String[]) csvRecord.stream().toArray();
                i++;
                csvRecordsList.add(csvRecord);
                skipRows=rand.nextInt(5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("File Cache random completed Apache");
//        return fileCache;
        return csvRecordsList;
    }
}
