package passes;

import DataFileAnalysis.model.MetaData;
import ast.ExprAST;
import cfg.CFG;
import com.google.common.collect.Sets;
import java.util.Set;
import ir.IExpr;
import ir.JPBody;
import ir.JPMethod;
import ir.Stmt.AssignStmt;
import ir.Stmt.AssignmentStmtSoot;
import ir.Stmt.CallExprStmt;
import ir.expr.*;
import ir.internalast.JPValueBox;
import ir.internalast.Keyword;
//import jdk.nashorn.internal.ir.Assignment;
import parse.GetCall;
import parse.ListParser;
import regions.PythonWriter.RegionToPython;
import regions.RegionDriver;
import rewrite.IRtoPython;
import rewrite.pd1.Pd1Elt;
import rewrite.pd2.InputFileDataTypeMapper;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;
import soot.util.HashChain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;
import java.util.stream.Collectors;

import soot.toolkits.graph.*;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;

public class MultiStageDataFetchRewrite {
    MultiStageDataFetch msdf;
    CFG cfg ;
    Iterator unitIterator;
    long availMemory;
    FlowSet notFetched;
    FlowSet columnsInMemory = new ArraySparseSet();
    List<InputFileDataTypeMapper> pdLists ;
    PatchingChain<Unit> rewritten = new PatchingChain<Unit>(new HashChain<Unit>());
    Chain<Local> locals = new HashChain<Local>();
    long usedMemory ;
    UnitGraph ug ;
    Integer counter= 0;
    public MultiStageDataFetchRewrite(JPMethod jpMethod,List<InputFileDataTypeMapper> pdLists) {
        this.msdf = new MultiStageDataFetch(jpMethod);
        this.cfg = new CFG(jpMethod);
        this.unitIterator = cfg.getUnitGraph().iterator();
        this.pdLists = pdLists;
        this.ug = new BriefUnitGraph(jpMethod.getBody());
        /*for(Unit u : ug){
            System.out.println(u);
            System.out.println(ug.getPredsOf(u));
        }*/
    }

    public void PerformPass(){

        while (unitIterator.hasNext()) {
            Unit unit = (Unit) unitIterator.next();
            if(pdLists.get(0).getUnit().equals(unit)) { // read statement // assuming single read stmt in code
                //AssignStmt read_stmt = (AssignStmt)unit;
                //String df_name = read_stmt.getTargets().get(0).toString();



                notFetched = new ArraySparseSet();
                FlowSet usedCols = new ArraySparseSet();
                List<Unit> pred = ug.getPredsOf(ug.getPredsOf(unit).get(0));
                AssignStmt col_stmt = (AssignStmt) pred.get(0);


                //System.out.println(col_stmt.getRHS().getType());
                ListComp listComp = (ListComp)col_stmt.getRHS();

                for(IExpr s : listComp.getElts()){
                    String col = ((Str)s).getS();
                    Name name = new Name(col,Expr_Context.Load);
                    usedCols.add(name);

                }
                //System.out.println(listComp.getElts());
                usedCols.difference(columnsInMemory,notFetched);
                //notFetched=difference(usedCols,columnsInMemory);
                if (isMemorySufficient(notFetched)) {
                    rewritten.add(unit);
                    //columnsInMemory = union(columnsInMemory,notFetched);
                    columnsInMemory.union(notFetched);
                    updateMemory(notFetched);
                }
                else {
                    FlowSet<String> cols = ColumnsToFetch(unit);
                    Pd1Elt elt = new  Pd1Elt();
                    elt.setCols(cols.toList());
                    elt.setLineno(((AssignmentStmtSoot)unit).getLineno());
                    AssignStmt stmt = getListStmt(elt);
                    rewritten.add((Unit)stmt);
                    rewritten.add(unit);
                    //columnsInMemory = union(columnsInMemory,(Set)cols.stream().collect(Collectors.toSet()));
                    //updateMemory((Set)cols.stream().collect(Collectors.toSet()));
                    for(String col: cols.toList()){
                        columnsInMemory.add(new Name(col,Expr_Context.Load));
                    }
                    //columnsInMemory.union((FlowSet) cols);
                    updateMemory((FlowSet)cols);

                }

            }
            else {
                if (!IsColumnsFetched(msdf.usedColumns.get(unit))) {
                    if (isMemorySufficient(notFetched)) {
                        updateMemory(notFetched);
                        columnsInMemory.union(msdf.usedColumns.get(unit));
                        FlowSet<String> cols = ColumnsToFetch(unit);

                        Pd1Elt elt = new  Pd1Elt();
                        ArrayList colToFetch = new ArrayList();
                        List notFetchedList = notFetched.toList();
                        List colsList = cols.toList();
                        colToFetch.addAll(notFetchedList);
                        colToFetch.addAll(colsList);
                        elt.setCols(colToFetch);
                        elt.setLineno(0);
                        AssignStmt stmt = getListStmt(elt);



                        Unit unitStmt = pdLists.get(0).getUnit();
                        Unit readStmt = (Unit)((AssignStmt)((AssignmentStmtSoot)unitStmt).getAssignStmt()).clone();
                        //Unit readStmt = (Unit)unitStmt.clone();
                        updateReadStmt(readStmt);
                        Unit concatStmt = getConcatStmt(unitStmt);

                        rewritten.add((Unit)stmt);
                        rewritten.add(readStmt);
                        rewritten.add(concatStmt);
                        counter += 1;
                        rewritten.add(unit);
                        //updateMemory(notFetched);
                        updateMemory(cols);
                        for(String col: cols.toList()){
                            columnsInMemory.add(new Name(col,Expr_Context.Load));
                        }


                    }
                    else {
                        Unit dropStmt = getDropColStmt(unit);
                        rewritten.add(dropStmt);
                        Pd1Elt elt = new  Pd1Elt();
                        elt.setCols(notFetched.toList());
                        elt.setLineno(0);
                        AssignStmt stmt = getListStmt(elt);


                        //Assuming only one read stmt in original code.
                        Unit unitStmt = pdLists.get(0).getUnit();
                        Unit readStmt = (Unit)((AssignStmt)((AssignmentStmtSoot)unitStmt).getAssignStmt()).clone();
                        //Unit readStmt = (Unit)unitStmt.clone();
                        updateReadStmt(readStmt);
                        Unit concatStmt = getConcatStmt(unitStmt);

                        rewritten.add((Unit)stmt);
                        rewritten.add(readStmt);
                        rewritten.add(concatStmt);
                        counter += 1;
                        rewritten.add(unit);
                        updateMemory(notFetched);

                    }
                } else {
                    rewritten.add(unit);
                }
                //columnsInMemory = union(columnsInMemory,(Set)msdf.usedColumns.get(unit).toList().stream().collect(Collectors.toSet()));
                //updateMemory((Set)msdf.usedColumns.get(unit).toList().stream().collect(Collectors.toSet()));
                columnsInMemory.union(msdf.usedColumns.get(unit));

                if(!msdf.removeColumns.get(unit).isEmpty()) {
                    Unit dropStmt = getDropColStmt(msdf.removeColumns.get(unit));
                    rewritten.add(dropStmt);

                }

            }
        }
    }

    public boolean  IsColumnsFetched(FlowSet usedCols){
        notFetched = new ArraySparseSet();
        //notFetched = difference(usedCols,columnsInMemory);
        usedCols.difference(columnsInMemory,notFetched);
        if(notFetched.isEmpty())
            return true;
        return false;
    }

    public boolean isMemorySufficient(FlowSet notFetched){
        long reqMemory = 0;
        Map<String,Long> clmnSize = pdLists.get(0).getMd().getClmnSizeMap();
        //System.out.println(clmnSize);
        for(Object col: notFetched.toList()){
            //System.out.println(col);
            reqMemory += clmnSize.get(col.toString());
        }
        AvailableMemory();
        if (availMemory - usedMemory > reqMemory)
            return true;

        return false;
    }
    public FlowSet<String> ColumnsToFetch(Unit unit){
        //List<String> cols = new ArrayList<>();
        FlowSet<String> cols = new ArraySparseSet<>();
        LinkedHashSet<Name> neededCol = msdf.neededColumns.get(unit);
        LinkedList<Name>  needed = new LinkedList<>(neededCol);
        long mem= 0;
        Iterator it = needed.descendingIterator();
        while(it.hasNext()){
            String col = ((Local)it.next()).getName();
            if( !cols.contains(col)){
                mem += pdLists.get(0).getMd().getClmnSizeMap().get(col);
                AvailableMemory();
                if(availMemory-usedMemory-mem>0)
                    cols.add(col);
                else
                    return cols;
            }
        }
        /*for(int i= needed.size(); i>0 ;i--)
        {
            String col = needed;
            if(!cols.contains(msdf.allUsedColumns.get(i))){
                mem += pdLists.get(0).getMd().getClmnSizeMap().get(col);
                AvailableMemory();
                if(availMemory-usedMemory-mem>0)
                    cols.add(col);
            }
        }*/
        return cols;
    }

    public void updateMemory(FlowSet fetchedCols){
        Map<String,Long> map = pdLists.get(0).getMd().getClmnSizeMap();
        for(Object col: fetchedCols.toList()){
            usedMemory += map.get(col.toString());
        }
    }







    public AssignStmt getReadStmt(){
        int lineno=0;//pdList.getLineno();
        int col_offset=0;//lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        // TODO : modify it if there are multiple files read in original source code
        // value: read_csv()
        // args :  file name from inputdatatypemapper and usecols : notFetched columns
        //target: _d_f_




        return assignStmt;
    }

    public AssignStmt getListStmt(Pd1Elt elt){
        //TODO change to col_offset

        int lineno=elt.getLineno();
        int col_offset=lineno;
        AssignStmt assignStmt=new AssignStmt(lineno,col_offset);
        assignStmt.getTargets().add(new Name("columns", Expr_Context.Load));
        ListComp listComp=new ListComp();
        listComp.setCol_offset(col_offset);
        listComp.setLineno(lineno);
        List<String> cols= elt.getCols();

        for(Object colname: cols) {
            Name name = new Name(colname.toString(),Expr_Context.Load);
            listComp.getElts().add(new Str(lineno,col_offset,name.toString()));
        }
        assignStmt.setRHS(listComp);
        assignStmt.setModified(true);
        return assignStmt;
    }

    public void updateReadStmt(Unit readStmt){
        // modify target to "_d_f_"
        String df_name = "_d_f_" + counter.toString();
        Name name = new Name(df_name,Expr_Context.Load);

        AssignStmt stmt= (AssignStmt) readStmt;
        stmt.getTargets().clear();
        stmt.getTargets().add(name);



    }

    public CallExprStmt getConcatStmt(Unit stmt){
        int lineno = -1;
        int col_offset = -1;
        Call call= new Call();

        ListComp listComp=new ListComp();
        AssignmentStmtSoot assignmentStmtSoot = (AssignmentStmtSoot) stmt;
        String dfname = ((assignmentStmtSoot.getAssignStmt()).getTargets()).get(0).toString();
        Name name1 = new Name(lineno, col_offset, dfname, Expr_Context.Load);
        Name name2 = new Name(lineno, col_offset, "_d_f_"+counter.toString(), Expr_Context.Load);

        listComp.getElts().add(name1);
        listComp.getElts().add(name2);
        call.getArgs().add(listComp);

        // TODO : add keyword axis once Constant is implemented in IR

        Keyword keyword=new Keyword();
        keyword.setArg("axis");
        Constant c = new Constant(lineno, col_offset, "1");
        keyword.setValue(c);
        call.getKeywords().add(keyword);



        Attribute value = new Attribute();
        value.setAst_type(ExprAST.Attribute);
        value.setAttr("concat");
        Name alias = new Name(lineno, col_offset, msdf.pandasAlias, Expr_Context.Load);
        value.setValue(alias);
        value.setCol_offset(col_offset);
        value.setLineno(lineno);

        call.setFunc(value);
        CallExprStmt callExprStmt =new CallExprStmt();
        callExprStmt.setCallExpr(call);
        callExprStmt.setLineno(call.getLineno());

        return callExprStmt;
    }
    public CallExprStmt getDropColStmt(Unit unit){
        FlowSet colToDrop = new ArraySparseSet();


        LinkedHashSet<Name> neededCol = msdf.neededColumns.get(unit);
        LinkedList<Name>  needed = new LinkedList<>(neededCol);

        for(int i = 0 ; i< needed.size() ; i++){
            if(isMemorySufficient(notFetched))
                break;


            if(columnsInMemory.contains(needed.get(i).toString())){
                colToDrop.add(needed.get(i).toString());
                Long temp = pdLists.get(0).getMd().getClmnSizeMap().get(needed.get(i).toString());
                usedMemory -= pdLists.get(0).getMd().getClmnSizeMap().get(needed.get(i).toString());
            }
        }
        return dropColStmt(colToDrop);
    }

    public CallExprStmt getDropColStmt(FlowSet removeCols){
        List cols = removeCols.toList();
        for(int i = 0 ; i< cols.size() ; i++){
            Long temp = pdLists.get(0).getMd().getClmnSizeMap().get(cols.get(i).toString());
            usedMemory -= pdLists.get(0).getMd().getClmnSizeMap().get(cols.get(i).toString());

        }
        return dropColStmt(removeCols);
    }


    public CallExprStmt dropColStmt(FlowSet removeCols){
        int lineno = -1;
        int col_offset = -1;

        Call call= new Call();

        ListComp listComp=new ListComp();
        for(Object col: removeCols){
            listComp.getElts().add(new Name(0,0,"\""+(Name)col+"\"",Expr_Context.Load));
        }
        call.getArgs().add(listComp);
        

        Keyword keyword=new Keyword();
        keyword.setArg("axis");
        Constant c = new Constant(lineno, col_offset, "1");
        keyword.setValue(c);
        call.getKeywords().add(keyword);


        Keyword keyword1 = new Keyword();
        keyword1.setArg("inplace");
        Constant c1 = new Constant(lineno,col_offset,"True");
        keyword1.setValue(c1);
        call.getKeywords().add(keyword1);


        Attribute value = new Attribute();
        value.setAst_type(ExprAST.Attribute);
        value.setAttr("drop");
        String dfname = (((AssignmentStmtSoot)pdLists.get(0).getUnit()).getAssignStmt().getTargets()).get(0).toString();
        Name name = new Name(lineno, col_offset, dfname, Expr_Context.Load);
        value.setValue(name);
        value.setCol_offset(col_offset);
        value.setLineno(lineno);

        call.setFunc(value);
        CallExprStmt callExprStmt =new CallExprStmt();
        callExprStmt.setCallExpr(call);
        callExprStmt.setLineno(call.getLineno());
        return callExprStmt;
    }

    public void AvailableMemory()
    {
        // TODO : use BigInt to avoid overflow
        String osName = System.getProperty("os.name");
        if (osName.equals("Linux"))
        {
            try {
                BufferedReader memInfo = new BufferedReader(new FileReader("/proc/meminfo"));
                String line;
                while ((line = memInfo.readLine()) != null)
                {
                    if (line.startsWith("MemAvailable: "))
                    {
                        // Output is in KB which is close enough.
                        availMemory = java.lang.Long.parseLong(line.split("[^0-9]+")[1]) * 1024 / 8;
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            // We can also add checks for freebsd and sunos which have different ways of getting available memory
        } else
        {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean)osBean;
            availMemory = sunOsBean.getFreePhysicalMemorySize();
        }

    }



    public void  DebugOutput(){
        System.out.println("\n\n");
        System.out.println("Debug Output");
        for(Unit unit : rewritten){
            System.out.println(unit);
        }
        List<Local> parameterTypes=new ArrayList();

        JPMethod jpMethod=new JPMethod("Module", parameterTypes, null);
        String path = "/home/mudra/IDEA/scirpy/pythonIR";
        String srcFileName = "US_Accidents_Analysis.py";
        JPBody jpBody=new JPBody(jpMethod, rewritten , locals);
        jpBody.getMethod().setActiveBody(jpBody);
        //IRtoPython iRtoPython=new IRtoPython();
        //iRtoPython.toStdOut(jpBody);
        //iRtoPython.toFile(path,path,"US_Accidents_Analysis.py",jpBody);

        RegionDriver regionDriver=new RegionDriver();
        regionDriver.buildRegion(jpBody);
        RegionToPython regionToPython=new RegionToPython();
        regionToPython.toFile(path,path,srcFileName,regionDriver.getTopRegion());

    }



}
