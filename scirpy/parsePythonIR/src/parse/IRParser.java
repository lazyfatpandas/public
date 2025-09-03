package parse;

import analysis.PythonScene;
import ast.StmtAST;
import ir.IExpr;
import ir.JPBody;
import ir.JPMethod;
import ir.Stmt.*;
import ir.expr.*;
import ir.internalast.Alias;
import ir.internalast.InternalASTType;
import ir.internalast.JPValueBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import soot.*;
import soot.jimple.EqExpr;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.options.Options;
import soot.util.Chain;
import soot.util.HashChain;

import java.util.*;

import static java.lang.System.exit;

public class IRParser {

    PatchingChain<Unit> units = new PatchingChain<Unit>(new HashChain<Unit>());
    Chain<Local> locals = new HashChain<Local>();
    int i=0;
    HashMap<Integer,String> sourceMap;

    public JPBody getIR(JSONArray stmtList){
        return getIR(stmtList,"dummy");
    }
    public JPBody   getIR(JSONArray stmtList, String methodName){
        return getIR(stmtList,methodName, null);
    }



    String sourceFile;
    //TODO  parameter types not initialized
    public JPBody getIR(JSONArray stmtList, String methodName, List parameterTypes){
        stmtList.forEach(stmt -> parseCodeBlock((JSONObject) stmt));
        JPMethod jpMethod=null;

        //TODO this is dummy, modify it with actual list later on

        //String name, List parameterTypes, JPBody body)
        //void main(java.lang.String[])
        // method = new SootMethod("main", Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
        // JPMethod jpMethod=new JPMethod("main",parameterTypes , null);
        //NEW METHOD FOR SOOTCLASS
        if(methodName.equals("main")){
            jpMethod = new JPMethod("main",Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
        }
        else{

            jpMethod=new JPMethod(methodName,  parameterTypes, null);
        }



        //TODO check this and implement again
        // locals=buildLocals(units);
        //JPMethod method, PatchingChain<Unit> unitChain,Chain<Local> localChain
        JPBody jpBody=new JPBody(jpMethod, units, locals);
        jpBody.getMethod().setActiveBody(jpBody);
        return jpBody;

    }


    public void parseCodeBlock(JSONObject block)
    {
        String statementType=block.get("ast_type").toString();
        switch(statementType)
        {
            case "Import":
                //System.out.println("Import statement keys:" + block.keySet());
                //getImportStatemnt(block);
                //allStmtASTS.add(getImportStatemnt(block));
                units.add(getImportStatemnt(block));
                break;
            case "ImportFrom":
                units.add(getImportFromStatemnt(block));
                break;
            case "Assign":
                //System.out.println("Assign statement keys:" + block.keySet());
                // System.out.println(block.toString());
                //TODO assignment currently can't handle a,b,c=1,2,3...add it..
                AssignStmt assignStmt=getAssignStatemnt(block);
                //This case is for type a=b=c=10 and normal assignments only, will fail for TODO above
                //TODO tried a,b=func call; lets c..
                int setNegLineno=0;
                List<IExpr> targetsAll=new ArrayList<>();

                for(IExpr target:assignStmt.getTargets()){
                    if(target instanceof ListComp){
                        targetsAll.addAll(((ListComp) target).getElts());
                    }
                    else{
                        targetsAll.add(target);
                    }
                }

                ////TODO VErify if targetsAll should be changed back to targets
                assignStmt.setTargets(targetsAll);
                for(IExpr target:targetsAll){
                    Value lvalue= target;
                    Value rvalue=assignStmt.getRHS();
                    AssignmentStmtSoot assignmentStmtSoot;


                    //TODO verify this, added to avoid reprint in regions ONLY
                    if(setNegLineno>0){
                        AssignStmt assignStmtClone=(AssignStmt)assignStmt.clone();
                        assignStmtClone.setLineno(-10);
                        assignmentStmtSoot=new AssignmentStmtSoot(lvalue,rvalue,assignStmtClone);
                    }
                    else{
                        assignmentStmtSoot=new AssignmentStmtSoot(lvalue,rvalue,assignStmt);
                    }

                    setNegLineno++;
                    units.add(assignmentStmtSoot);
                    locals.addAll(assignmentStmtSoot.getLocals());

                    // Chiranmoy 8-2-24: infer if the LHS is dataframe or not
                    if(rvalue instanceof Call && (((Call) rvalue).getBaseName().equals(PythonScene.pandasAliasName)))
                        PythonScene.allDfNames.add(((Name) lvalue).getName());
                    else if(((IExpr) rvalue).isDataFrame() && lvalue instanceof Name)
                        PythonScene.allDfNames.add(((Name) lvalue).getName());
                }

                //units.add(getAssignStatemnt(block));
                break;
            case "AugAssign":
                AugAssignStmt augassignStmt=getAugAssignStatement(block);
                for(IExpr target:augassignStmt.getTargets()){
                    Value lvalue= target;
                    Value rvalue=augassignStmt.getRHS();
                    AssignmentStmtSoot assignmentStmtSoot=new AssignmentStmtSoot(lvalue,rvalue,augassignStmt);
                    units.add(assignmentStmtSoot);

                }

                //System.out.println(statementType);
                break;
            case "FunctionDef":
                FunctionDefStmt fds=getFuncDef(block);
                units.add(fds);
                //System.out.println(block.keySet());
                //System.out.println(block.get("body"));
                //System.out.println(statementType + " statement Not implemented");
                // exit(1);
                locals.addAll(fds.getLocals());

                break;
            case "Expr":
                CallExprStmt ces=getExpr(block);
                units.add(ces);
//                locals.addAll(ces.getLocals());
                //System.out.println(statementType + "
                // statement Not implemented");
                //exit(1);
                break;
            case "If":

                //This codeGen will break complex conditions to simple conditions and insert extra statements
                codeGenIf(units,locals, block);

                break;
            case "While":
                //changed on Apr 29 2024
//                codeGenWhile(units,locals, block);
                codeGenWhile(units,locals, block);
                //dummy: to avoid error
                System.out.println();
                break;
            case "For":
                codeGenFor(units,locals, block);
                break;

            case "ClassDef":
                units.add(getClassDef(block));

            case "Return":
                units.add(getReturnStmt(block));
                break;
            case "With":
                codeGenWith(units,locals, block);
                break;
            case "withitem":
                units.add(getWithItem(block));
                break;
            case "Global":
                units.add(getGlobalStatemnt(block));
                break;
            default :
                System.out.println(statementType + " statement Not implemented");
                //exit(1);

                // Statements
        }

       /* for(Iterator iterator = block.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            //System.out.println(key+",");
            //System.out.println(body.get(key));
        }*/
        //System.out.println(i+":"+block.get("ast_type"));
        i++;
        //System.out.println("END of body");

        //TODO put analysis code here
    }

    private Unit getReturnStmt(JSONObject block) {
        ReturnStmtParser returnStmtParser=new ReturnStmtParser(block);
        return returnStmtParser.getReturnStmt();
    }


    private static ImportStmt getImportStatemnt(JSONObject block) {
        //TODO add checks for proper Import Statement
        //assert (xyz);

        ImportStmt importStmt = null;
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        String names=block.get("names").toString();
        JSONArray childarray=(JSONArray)block.get("names");
        JSONObject aliasObj=(JSONObject) childarray.get(0);
        Alias alias =null;
        if(aliasObj.get("asname")!=null) {
            alias = new Alias(aliasObj.get("name").toString(), aliasObj.get("asname").toString(), true, InternalASTType.alias);
        }
        else{
            alias=new Alias(aliasObj.get("name").toString(), null, false, InternalASTType.alias);
        }
        importStmt=new ImportStmt(lineno,col_offset,alias, StmtAST.Import);
        //System.out.println(importStmt);

        // Chiranmoy 2-2-24: note down all external modules except pandas
        if(alias.getName().equals("pandas")) {
            PythonScene.pandasAliasName = alias.getAsname() != null ? alias.getAsname() : alias.getName();
        } else {
            PythonScene.imported.add(alias.getAsname() != null ? alias.getAsname() : alias.getName());
        }

        return importStmt;
    }

    private static AssignStmt getAssignStatemnt(JSONObject block) {
        //TODO add checks for proper Import Statement
        //assert (xyz);

        AssignStmt assignStmt = null;
        GetAssignmentStatementHelper getAssignmentStatementHelper=new GetAssignmentStatementHelper(block);
        //assignStmt=
        assignStmt=getAssignmentStatementHelper.getAssignStmt();
        return assignStmt;
    }

    private static AugAssignStmt getAugAssignStatement(JSONObject block) {
        //TODO add checks for proper Import Statement
        //assert (xyz);

        AugAssignStmt assignStmt = null;
        GetAugAssignmentStatementHelper getAugAssignmentStatementHelper=new GetAugAssignmentStatementHelper(block);
        //assignStmt=
        assignStmt=getAugAssignmentStatementHelper.getAugAssignStmt();
        return assignStmt;
    }


    private static CallExprStmt getExpr(JSONObject block) {
        //TODO move this logic to GetExpr class if getting complicated here
        //System.out.println("Printing expression");
        //System.out.println(block.toString());
        JSONObject exprValueObj=(JSONObject) block.get("value");
        String exprType=exprValueObj.get("ast_type").toString();
        if(exprType.equals("Call")){
                GetCall getCall=new GetCall();
                Call call=getCall.GetFunctionalCall(exprValueObj);
                CallExprStmt callExprStmt =new CallExprStmt();
                callExprStmt.setCallExpr(call);
                callExprStmt.setLineno(call.getLineno());
                return callExprStmt;
        } else if(exprType.equals("BinOp")){
            BinOpParser binOpParser=new BinOpParser(exprValueObj);
            CallExprStmt callExprStmt =new CallExprStmt();
            callExprStmt.setCallExpr(binOpParser.getBinOp());
            callExprStmt.setLineno(binOpParser.getBinOp().getLineno());
            return callExprStmt;
        }
        else if(exprType.equals("Name")){
            GenericExpressionParser gep=new GenericExpressionParser();
            IExpr name =gep.getName(exprValueObj);
            CallExprStmt callExprStmt =new CallExprStmt();
            callExprStmt.setCallExpr(name);
            callExprStmt.setLineno(((Name)name).getLineno());
            return callExprStmt;
        }
        else if(exprType.equals("Constant")){
            GenericExpressionParser gep=new GenericExpressionParser();
            IExpr constant =gep.getConstant(exprValueObj);
            CallExprStmt callExprStmt =new CallExprStmt();
            callExprStmt.setCallExpr(constant);
            callExprStmt.setLineno(((Constant)constant).getLineno());
            return callExprStmt;
        }
        else if(exprType.equals("Subscript")){
            SubscriptParser ssp=new SubscriptParser();
            IExpr subscript =ssp.parseSubscript(exprValueObj);
            CallExprStmt callExprStmt =new CallExprStmt();
            callExprStmt.setCallExpr(subscript);
            callExprStmt.setLineno(((Subscript)subscript).getLineno());
            return callExprStmt;
        }
        else if(exprType.equals("Str")){
            GenericExpressionParser gep=new GenericExpressionParser();
            IExpr str =gep.getStr(exprValueObj);
            CallExprStmt callExprStmt =new CallExprStmt();
            callExprStmt.setCallExpr(str);
            callExprStmt.setLineno(((Str)str).getLineno());
            return callExprStmt;
        }
        //TODO refractor
        else if(exprType.equals("Num") ){

            GenericExpressionParser gep=new GenericExpressionParser();
            IExpr num =gep.getNumber(exprValueObj);
            CallExprStmt callExprStmt =new CallExprStmt();
            callExprStmt.setCallExpr(num);
            callExprStmt.setLineno(((Num)num).getLineno());
            return callExprStmt;
        }

        else if(exprType.equals("Attribute")){
            AttributeParser ap=new AttributeParser();
            IExpr att =ap.parseAttribute(exprValueObj);
            CallExprStmt callExprStmt =new CallExprStmt();
            callExprStmt.setCallExpr(att);
            callExprStmt.setLineno(((Attribute)att).getLineno());
            return callExprStmt;
        }
        else{
            System.out.println(exprType+ " still Not implemented in IR Parser for CallExpr for getExpr Method, Source Line no");
            exit(1);
        }

       // CallExprStmt callExprStmt =new CallExprStmt();
        //Should never reach here
        return null;

    }

    private static CallExprStmt getWithItem(JSONObject block) {
        //TODO move this logic to GetExpr class if getting complicated here
        //System.out.println("Printing expression");
        //System.out.println(block.toString());
        JSONObject exprValueObj=(JSONObject) block.get("context_expr");
        String exprType=exprValueObj.get("ast_type").toString();
        switch(exprType) {
            case "Call":
                GetCall getCall=new GetCall();
                Call call=getCall.GetFunctionalCall(exprValueObj);
                CallExprStmt callExprStmt =new CallExprStmt();
                callExprStmt.setCallExpr(call);
                return callExprStmt;
        }

        CallExprStmt callExprStmt =new CallExprStmt();
        //Should never reach here
        return callExprStmt;

    }


    private static FunctionDefStmt getFuncDef(JSONObject block) {
        FuncDefParser funcDefParser=new FuncDefParser(block);
        return funcDefParser.getFuncDef();
    }


    private Unit getIfStmt(JSONObject block) {
        IfStmtParser ifStmtParser=new IfStmtParser(block);
        //IfStmt ifStmt=ifStmtParser.getIfStmt();
        IfStmtPy ifStmtPy=ifStmtParser.getIfStmt();
        return ifStmtPy;

    }

    private Unit getForStmt(JSONObject block) {
        ForStmtParser forStmtParser=new ForStmtParser(block);
        ForStmtPy forStmtPy =forStmtParser.getForStmtPy();
        return forStmtPy;

    }
    private Unit getWithStmt(JSONObject block) {
        WithStmtParser withStmtParser=new WithStmtParser(block);
        WithStmtPy withStmtPy =withStmtParser.getWithStmtPy();
        return withStmtPy;

    }

    private Unit getWhileStmt(JSONObject block) {
        WhileStmtParser whileStmtParser=new WhileStmtParser(block);
        WhileStmtPy whileStmtPy =whileStmtParser.getWhileStmtPy();
        return whileStmtPy;

    }

    private static ClassDefStmt getClassDef(JSONObject block) {
        ClassDefParser classDefParser=new ClassDefParser(block);
        return classDefParser.getClassDefStmt();
    }

    private void codeGenIf(PatchingChain units, Chain locals, JSONObject block){
        //TODO break complex if conditions to simple condtions
        //TODO create new if statement with simple test
        //TODO align inserts of nop etc properly
        Unit ifStmtPy=getIfStmt(block);
        //units.add(ifStmt);
        codeGenCommon(units,locals, (IfStmtPy) ifStmtPy, false);

    }
    private void codeGenWhile(PatchingChain units, Chain locals, JSONObject block){
        //TODO break complex if conditions to simple condtions
        //TODO create new if statement with simple test
        //TODO align inserts of nop etc properly
        Unit ifStmtPy=getIfStmt(block);
        //units.add(ifStmt);
        codeGenCommon(units,locals, (IfStmtPy) ifStmtPy, true);

    }
    private void codeGenWhileN(PatchingChain units,  Chain locals, JSONObject block){

//        SimplifiedIf simplifiedIf=new SimplifiedIf();
//        Unit whileStmtPy=getWhileStmt(block);
//        PatchingChain<Unit> extendedWhileUnits=simplifiedIf.getSimplifiedIf()
//        SimplifiedWith simplifiedWith=new SimplifiedWith();
//        PatchingChain<Unit> extendedForUnits=simplifiedWith.getSimplifiedWith((WithStmtPy)withStmtPy,block);
//        for(Unit unit:extendedForUnits){
//            units.add(unit);
//        }
//        locals.addAll(((WithStmtPy) withStmtPy).getBody().getLocals());
//        codeGenWhileN(units,locals,(WhileStmtPy)whileStmtPy);
//
//
//        PatchingChain<Unit> extendedIfUnits=simplifiedIf.getSimplifiedIf(ifStmtPy,whileBoolean, locals);
//        for(Unit unit:extendedIfUnits){
//            units.add(unit);
//        }
//        locals.addAll(ifStmtPy.getIfBody().getLocals());
//        locals.addAll(ifStmtPy.getOrelseBody().getLocals());



    }
    private void codeGenFor(PatchingChain units,Chain locals, JSONObject block){
        //TODO break complex if conditions to simple condtions
        //TODO create new if statement with simple test
        //TODO align inserts of nop etc properly
        Unit forStmtPy=getForStmt(block);
        SimplifiedFor simplifiedFor=new SimplifiedFor();
        PatchingChain<Unit> extendedForUnits=simplifiedFor.getSimplifiedFor((ForStmtPy)forStmtPy, locals);
        for(Unit unit:extendedForUnits){
            units.add(unit);
        }
        locals.addAll(((ForStmtPy) forStmtPy).getBody().getLocals());

//        codeGenCommon(units,(IfStmtPy) ifStmtPy, false);

    }
    private void codeGenWith(PatchingChain units,  Chain locals, JSONObject block){

        Unit withStmtPy=getWithStmt(block);
        SimplifiedWith simplifiedWith=new SimplifiedWith();
        PatchingChain<Unit> extendedForUnits=simplifiedWith.getSimplifiedWith((WithStmtPy)withStmtPy,block);
        for(Unit unit:extendedForUnits){
            units.add(unit);
        }
        locals.addAll(((WithStmtPy) withStmtPy).getBody().getLocals());



    }
    private void codeGenCommon(PatchingChain units, Chain locals, IfStmtPy ifStmtPy, boolean whileBoolean){
        SimplifiedIf simplifiedIf=new SimplifiedIf();
        PatchingChain<Unit> extendedIfUnits=simplifiedIf.getSimplifiedIf(ifStmtPy,whileBoolean, locals);
        for(Unit unit:extendedIfUnits){
            units.add(unit);
        }
        locals.addAll(ifStmtPy.getIfBody().getLocals());
        locals.addAll(ifStmtPy.getOrelseBody().getLocals());
    }

    private static GlobalStmt getGlobalStatemnt(JSONObject block) {
        //TODO add checks for proper Import Statement
        //assert (xyz);

        GlobalStmt globalStmt = null;
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        JSONArray namesArray=(JSONArray)block.get("names");
        String[] names =new String[namesArray.size()];
        for(int i=0;i<namesArray.size();i++){
            names[i]=namesArray.get(i).toString();

        }
//        eltsArray.forEach(keywords -> parseElt((JSONObject) keywords,listComp));
//        for()
//        String[] names= (String[]) block.get("names");
        globalStmt=new GlobalStmt(lineno,col_offset,names);
        //System.out.println(importStmt);
        return globalStmt;
    }

//    private Unit createJimpleIfStmt(IfStmt ifStmt){
//
//        NopStmt nop = Jimple.v().newNop(); // create nop so that we have a temporary label to jump to
//
//        NopStmt nop=Jimple.v().newNopStmt();
//        IfStmt ifSt = Jimple.v().newIfStmt(ifStmt.getCondition(), nop);
//
//
//    }



    public void TestChains() {
        Chain<Unit> unitChain = units;


        for(Unit unit : unitChain) {

            List valueBoxes = unit.getUseAndDefBoxes();
            System.out.println(valueBoxes.size());
            for(Object obj : valueBoxes) {
                assert obj instanceof ValueBox;
                ValueBox valueBox = (ValueBox) obj;
                Value value = valueBox.getValue();
                System.out.println(value.toString());
            }

        }
    }

    public HashMap<Integer, String> getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(HashMap<Integer, String> sourceMap) {
        this.sourceMap = sourceMap;
    }
    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Chain<Local> getIRLocals(){
        return this.locals;
    }
    //TODO Verify this code, this is not used anymore....
    public Chain<Local> buildLocals(PatchingChain<Unit> units){
        Chain<Local> locals = new  HashChain<Local>();
        Iterator unitIterator =units.iterator();
        while(unitIterator.hasNext()){
            Unit unit=(Unit)unitIterator.next();
            System.out.println("Unit type is:"+unit.getClass());
            List<ValueBox> useAndDefBoxes=unit.getUseAndDefBoxes();
            Iterator boxIt=useAndDefBoxes.iterator();
            while(boxIt.hasNext()){
                ValueBox box=(ValueBox) boxIt.next();
                if(box instanceof JPValueBox){
                    System.out.println("JPVAlueBox");
                }
                Value val=box.getValue();
                System.out.println("value is:"+val);
                if(val instanceof  Local){
                    locals.add((Local)val);
                    System.out.println("Local                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          value is:"+val);

                }
                else if(val instanceof Call){

                }

            }
        }

        return locals;
    }
    private static ImportFrom getImportFromStatemnt(JSONObject block) {
        //TODO add checks for proper Import Statement
        //assert (xyz);

        ImportFrom importFromStmt = null;
        int lineno=Integer.parseInt(block.get("lineno").toString());
        int col_offset=Integer.parseInt(block.get("col_offset").toString());
        String names=block.get("names").toString();
        String module=block.get("module").toString();
        JSONArray childarray=(JSONArray)block.get("names");

        JSONObject aliasObj=(JSONObject) childarray.get(0);
        Alias alias =null;
        List<Alias> aliasList=null;
        if(aliasObj.get("asname")!=null) {
            alias = new Alias(aliasObj.get("name").toString(), aliasObj.get("asname").toString(), true, InternalASTType.alias);
        }
        else{
            alias=new Alias(aliasObj.get("name").toString(), null, false, InternalASTType.alias);
        }
        //4.01 import multiple from one module
        if(childarray.size()>1){
            Alias alias1 =null;  // Chiranmoy: alias contains the first import, dont overwrite it
            aliasList=new ArrayList();
            for(int method_num=1;method_num<childarray.size();method_num++){
                aliasObj=(JSONObject) childarray.get(method_num);
                if(aliasObj.get("asname")!=null) {
                    alias1 = new Alias(aliasObj.get("name").toString(), aliasObj.get("asname").toString(), true, InternalASTType.alias);
                }
                else{
                    alias1=new Alias(aliasObj.get("name").toString(), null, false, InternalASTType.alias);
                }
                aliasList.add(alias1);
            }
            //end 4.01

        }
        importFromStmt=new ImportFrom(lineno,col_offset,alias, StmtAST.ImportFrom,module,aliasList);
        //System.out.println(importStmt);

        // Chiranmoy 2-2-24: note down all external modules
        PythonScene.imported.add(alias.getAsname() != null ? alias.getAsname() : alias.getName());
        if(aliasList != null) {
            aliasList.forEach(aliasName -> PythonScene.imported.add(aliasName.getAsname() != null ? aliasName.getAsname() : aliasName.getName()));
        }

        return importFromStmt;
    }
}
