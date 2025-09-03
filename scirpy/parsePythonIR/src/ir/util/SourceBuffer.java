package ir.util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class SourceBuffer {

        public HashMap makeBuffer(String file) {
            HashMap<Integer, String> sourceBuffer=new HashMap<>();
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
                String currentLine ="";
                int lineno=1;
                while (currentLine != null) {
                    currentLine = reader.readLine();
                    sourceBuffer.put(lineno,currentLine);
                    lineno++;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sourceBuffer;
        }
    }

