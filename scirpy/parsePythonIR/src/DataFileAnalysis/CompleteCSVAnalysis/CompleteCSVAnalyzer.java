package DataFileAnalysis.CompleteCSVAnalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//NOTE: Deprecated--not used---used CSVTypeInference
public class CompleteCSVAnalyzer {

    public static void main(String[] args) {
        String filePath = "/home/bhushan/Downloads/s1.csv"; // Replace with your CSV file path

        // Process the file
        processCSVFile(filePath);
    }



    public static void processCSVFile(String filePath) {
        // Initialize variables for column names and column type map
        List<String> columnNames = new ArrayList<>();
        Map<String, String> columnTypeMap = new HashMap<>();
        Map<String, Boolean> columnCategoryCandidateMap = new HashMap<>();
        Map<String,Set> columnUniqueValSetList=new HashMap<>();

        // Use try-with-resources to ensure BufferedReader is closed automatically
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Read the first row for column names
            if ((line = reader.readLine()) != null) {
                columnNames = Arrays.asList(line.split(","));
            }

            // Read the second row to determine the column types
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                // Create a map of column names to column types based on second row
                for (int i = 0; i < columnNames.size(); i++) {
                    String columnName = columnNames.get(i);
                    String value = values[i].trim();

                    // Determine column type based on the second row's value
                    String columnType = determineColumnType(value);
                    columnTypeMap.put(columnName, columnType);
                    columnCategoryCandidateMap.put(columnName,false);
                    columnUniqueValSetList.put(columnName,new HashSet());
                }
            }

            // Output the column names and types
            System.out.println("Column Names: " + columnNames);
            System.out.println("Column Types: " + columnTypeMap);
            System.out.println("Column Categories: " + columnCategoryCandidateMap);
            System.out.println("Column Set: " + columnUniqueValSetList);


        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    // Helper method to determine the column type based on value in the second row
    public static String determineColumnType(String value) {
        try {
            // Check for Integer type
            Integer.parseInt(value);
            return "Integer";
        } catch (NumberFormatException e1) {
            try {
                // Check for Double type
                Double.parseDouble(value);
                return "Double";
            } catch (NumberFormatException e2) {
                // Default to String type if neither Integer nor Double
                return "String";
            }
        }
    }
}
