package DataFileAnalysis.CompleteCSVAnalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CSVTypeInference {
    public static void main(String[] args) {
        String filePath = "../../../data/b1/s5.csv";  // Replace with the path to your CSV file
        long startTime = System.nanoTime();  // You can also use System.currentTimeMillis() for milliseconds

        // Use a map to hold column names and their corresponding data types
        List<String> columnNames = new ArrayList<>();
        Map<String, String> columnTypes = new LinkedHashMap<>();
        Map<String, Boolean> columnCategories = new LinkedHashMap<>();
        Map<String,Set> columnUniqueValSetList=new HashMap<>();

        long no_of_rows=0;
        // Read the CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            // Read through the file line by line
            while ((line = br.readLine()) != null) {
                no_of_rows++;
                String[] values = line.split(",");

                if (isHeader) {
                    // First row contains column names, so initialize the columnNames list
                    columnNames = Arrays.asList(values);
                    // Initialize column types (defaulting to String)
                    for (String columnName : columnNames) {
                        columnTypes.put(columnName, "int");
//                        columnCategories.put(columnName,false);
                        columnUniqueValSetList.put(columnName,new HashSet());

                    }
                    isHeader = false;
                } else {
                    // Process each data row and determine the type for each column
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i].trim();  // Trim whitespace

                        // Check for empty values
                        if (value.isEmpty()) {
                            continue; // Skip empty values, don't change type
                        }

                        String columnName = columnNames.get(i); // Get column name from header

                        // Upgrade data type based on the content of the value
                        String currentType = columnTypes.get(columnName);
                        currentType = inferDataType(currentType, value);

                        // Update the column's data type if it needs upgrading
                        columnTypes.put(columnName, currentType);
                        if(columnUniqueValSetList.get(columnName).size()<1000){
                            columnUniqueValSetList.get(columnName).add(value);
                        }
                    }
                }
            }

            // Print out the column types after processing all rows
            for (Map.Entry<String, String> entry : columnTypes.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        //Check if more rows and set has unique value less than 1/10th of total values, mark as potential candidate for cat type
        System.out.println("No of rows:"+no_of_rows);
        if(no_of_rows>9999) {
            for (String columnName : columnNames) {
                if(columnUniqueValSetList.get(columnName).size()<999){
                    columnCategories.put(columnName,true);
                }
                else{
                    columnCategories.put(columnName,false);
                }

            }
        }
        for (Map.Entry<String, Boolean> entry : columnCategories.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        for (Map.Entry<String, Set> entry : columnUniqueValSetList.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().size());
        }
        System.out.println(columnCategories.values());
        // Record end time
        long endTime = System.nanoTime();  // Capture the end time

        // Calculate the elapsed time
        long duration = endTime - startTime;  // Elapsed time in nanoseconds

        System.out.println("Execution Time: " + duration / 1_000_000_000.0 + " seconds");
    }

    // Method to infer the data type of a value and upgrade if necessary
    private static String inferDataType(String currentType, String value) {
        if (value == null || value.trim().isEmpty()) {
            return currentType;  // Empty or null values don't change the type
        }

        // Check if the value can be an integer
        boolean isInt = false;
        boolean isFloat = false;

        try {
            Integer.parseInt(value);  // Try parsing as an integer
            isInt = true;
        } catch (NumberFormatException e) {
            // Not an int, do nothing
        }

        try {
            Float.parseFloat(value);  // Try parsing as a float
            isFloat = true;
        } catch (NumberFormatException e) {
            // Not a float, do nothing
        }

        // Upgrade the type to the least restrictive type based on the current type and the value
        if (currentType.equals("int") && isInt ) {
            return "int";
        }
        if ( currentType.equals("float") && (isFloat || isInt)) {
            return "float";
        }
            else {
            return "String";  // Keep string as default
        }

//        return currentType;
    }
}
