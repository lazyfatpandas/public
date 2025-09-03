package ir.Stmt;

import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.UnitGraph;

import java.util.ArrayList;
import java.util.List;

public class ForStmt  {
    Stmt head;
    List<Stmt> loopStatements;
    UnitGraph g;
    Loop loop;
    public ForStmt(Stmt head, List<Stmt> loopStatements, UnitGraph g) {
        this.head = head;
        this.loopStatements = loopStatements;
        this.g = g;
        //TODO see how to use LoopFinder here
        //this.loop=new Loop(head,loopStatements,g);
    }
    public ForStmt() {

    }

    public Stmt getHead() {
        return head;
    }

    public void setHead(Stmt head) {
        this.head = head;
    }

    public List<Stmt> getLoopStatements() {
        return loopStatements;
    }

    public void setLoopStatements(List<Stmt> loopStatements) {
        this.loopStatements = loopStatements;
    }

    public UnitGraph getG() {
        return g;
    }

    public void setG(UnitGraph g) {
        this.g = g;
    }

    public Loop getLoop() {
        return loop;
    }

    public void setLoop(Loop loop) {
        this.loop = loop;
    }
    @Override
    public Object clone() {
        //TODO left this incomplete as not used as of now...
        Stmt headClone=(Stmt) head.clone();
        List<Stmt> loopStatementsClone=new ArrayList<>();
        for(Stmt stmt:loopStatements){
            loopStatementsClone.add((Stmt) stmt.clone());
        }
        ForStmt forStmt=new ForStmt();
        forStmt.setHead(headClone);
        forStmt.setLoopStatements(loopStatementsClone);
        //TODO clone these
        forStmt.setG(this.g);
        forStmt.setLoop(this.loop);

        return forStmt;


    }



}
