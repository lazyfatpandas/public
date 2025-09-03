package DataFileAnalysis.DateAnalyzer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DateFormatAnalyzer {
    String patterns[]=new String[20];
    Date dt=null;
    //yyyy-MM-dd is ISO...
    String pattern9 = "HH:mm:ss.SSSZ";
    String pattern10 = "dd MMM yyyy HH:mm:ss z";
    String pattern11 = "E, dd MMM yyyy HH:mm:ss z";
    String pattern12 = "dd MMMM yyyy";
    String pattern13 = "dd MMMM yyyy zzzz";

    String pattern14 = "dd-M-yyyy hh:mm:ss";

    SimpleDateFormat sdf =null;

    Map<String, SimpleDateFormat> sdfMap=new HashMap<>();
    public DateFormatAnalyzer(){
        patterns[0] = "yyyy-MM-dd";
        patterns[1] = "dd-mm-YYYY";
        patterns[2] = "dd/MM/yyyy";
        patterns[3] = "HH:mm:ss.SSSZ";
        patterns[4] = "dd MMM yyyy HH:mm:ss z";
        patterns[5] = "E, dd MMM yyyy HH:mm:ss z";
        patterns[6] = "dd MMMM yyyy";
        patterns[7] = "dd MMMM yyyy zzzz";
        patterns[8] = "dd-M-yyyy hh:mm:ss";


        for(int i=0;i<9;i++){
            sdfMap.put(patterns[i], new SimpleDateFormat(patterns[i]) );
        }
    }

    public String getDatePatten(String date){
        boolean isDate=false;
        String result="NOTDATE";

            Iterator dateIterator = sdfMap.entrySet().iterator();
            while (dateIterator.hasNext()) {
                try {
                Map.Entry mapElement = (Map.Entry)dateIterator.next();
                dt = ((SimpleDateFormat)mapElement.getValue()).parse(date);
                //System.out.println(dt);
                return (String) mapElement.getKey();
            }
                catch (ParseException e) {

                }
        }

        return result;
    }

    public boolean isDatePattern(String pattern, String date){
        boolean isDate=false;

        try {
            sdf=sdfMap.get(pattern);
            if(sdf!=null) {
                Date date1 = sdf.parse(date);
                isDate=true;
            }
            else
                isDate= false;
        } catch (ParseException e) {

        }
        return isDate;
    }

    public static void main(String[] args) {
        DateFormatAnalyzer dateFormatAnalyzer=new DateFormatAnalyzer();
        String date="15-15-2020";
        String pr="dd-mm-YYYY";
        System.out.println(dateFormatAnalyzer.isDatePattern(pr,date));
        System.out.println(dateFormatAnalyzer.getDatePatten("31/12/1998"));

    }





}
