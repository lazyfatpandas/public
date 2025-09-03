package analysis;

import ir.IExpr;
import ir.expr.Name;
import soot.Unit;
import soot.Value;

import java.util.*;

public class PythonScene {
    public static boolean isGroupBy=false;
    public static boolean isCompareColumn=false;

    public static String pandasAliasName="";
    public static boolean hasPandas=false;
    public static boolean hasFilter=true;
    public static ArrayList<String> dfsFromFile=new ArrayList<>();
    public static Map<String,Unit> dfNametoUnitMap=new HashMap<>();
    public static Map<Unit, String> unitToDfNameMap=new HashMap<>();
    public static Map<Unit, String> unitGetDummiesMap=new HashMap<>();









    public static ArrayList<Unit> groupbyStmts=new ArrayList<>();
    public static ArrayList<Unit> compareStmtsDFFiltering=new ArrayList<>();
    public static Map<String,ArrayList<String>> attributesUsedinCompareMap=new HashMap<>();

    //public static ArrayList<String> pandasAPI=
    public static ArrayList<Unit> DFFilterOps=new ArrayList<>();


    public static boolean isMerge=false;
    public static ArrayList<Unit> mergeStmts=new ArrayList<>();

    public static ArrayList<Unit> printStmts=new ArrayList<>();
    public static Map<String, ArrayList<Unit>> varUnitMap=new HashMap<>();

    public static Map<String,ArrayList<String>> nameValueMap=new HashMap<>();

    // Chiranmoy 2-2-24
    public static Set<String> allDfNames = new HashSet<>();
    public static Set<String> imported = new HashSet<>();

    public static void updateVarUnitMap(String lvalue, Unit unit){
        if(varUnitMap.get(lvalue)==null){
            ArrayList<Unit> list=new ArrayList<>();
            list.add(unit);
            varUnitMap.put(lvalue,list);
        }
        else{
            varUnitMap.get(lvalue).add(unit);
        }

    }

}
