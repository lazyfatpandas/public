package analysis.Pandas;

import java.util.ArrayList;
import java.util.List;

public class PandasAPIs {
    static  List<String> APIs=new ArrayList();

    private static void updatePandasAPIs() {
        //IO
        APIs.add("dtypes");
        APIs.add("read_pickle");
        APIs.add("read_csv");
        APIs.add("read_table");
        APIs.add("read_fwf");
        APIs.add("read_clipboard");
        APIs.add("read_excel");
        APIs.add("ExcelFile.parse");
        APIs.add("ExcelWriter");
        APIs.add("read_json");
        APIs.add("json_normalize");
        APIs.add("build_table_schema");
        //TODO add few more IO APIs

        //Dataframe
        APIs.add("index");
        APIs.add("columns");
        APIs.add("dtypes");
        APIs.add("info()");//args are exclude
        APIs.add("select_dtypes()");//args are exclude
        APIs.add("values");
        APIs.add("axes");
        APIs.add("ndim");
        APIs.add("size");
        APIs.add("shape");
        APIs.add("memory_usage()");
        APIs.add("empty");
        APIs.add("empty");
        APIs.add("set_flags()");
        APIs.add("datatypes");


        //Conversion
        APIs.add("astype()");
        APIs.add("convert_dtypes()");
        APIs.add("infer_objects()");
        APIs.add("copy()");
        APIs.add("bool()");

        //Indexing, iteration
        APIs.add("values");
        APIs.add("loc");


    }







    public static  List getAPIs(){
        if(APIs.size()==0){
            updatePandasAPIs();
        }
        return APIs;
    }
}
