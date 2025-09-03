package DataFileAnalysis;
import DataFileAnalysis.model.FileInfo;
import DataFileAnalysis.model.MetaData;
import DataFileAnalysis.model.RandomSampling;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors


public class AsyncDataAnalyzer implements Runnable{
    MetaData md=null;

    public AsyncDataAnalyzer (MetaData md){
        this.md=md;
    }
//private static CompletableFuture<Void> createFuture(){
//            Runnable runnable = () -> {
//                sleep(5000);
//                try {
//                    File myObj = new File("filename.txt");
//                    if (myObj.createNewFile()) {
//                        System.out.println("File created: " + myObj.getName());
//                    } else {
//                        System.out.println("File already exists.");
//                    }
//                } catch (IOException e) {
//                    System.out.println("An error occurred.");
//                    e.printStackTrace();
//                }
//            };
//            return CompletableFuture.runAsync(runnable);
//}

//private static void sleep(int millis){
//            try {
//                Thread.sleep(millis);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//}
public void run() {
    System.out.println("Inside thread for metadata");
    md = RandomSampling.csvRandomMetaDataGenerator(md);
    System.out.println("Completed run method for async data analysis");
}

public void generateMetaDataAsync(){
    (new Thread(new AsyncDataAnalyzer(md))).start();
    System.out.println("Completed thread for Metadata aynchronous");

}
public static void main(String[] args)  {
 }



}
