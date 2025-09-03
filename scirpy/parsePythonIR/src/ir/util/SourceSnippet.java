package ir.util;

import ir.JPFile;

import java.io.Serializable;

public class SourceSnippet implements Serializable {
    private int beginLine;
    private int endLine;
    private JPFile jpFile;

    public SourceSnippet(int beginLine, int endLine, JPFile jpFile) {
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.jpFile=jpFile;
    }

    public int getBeginLine() {
        return beginLine;
    }

    public void setBeginLine(int beginLine) {
        this.beginLine = beginLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public JPFile getJpFile() {
        return jpFile;
    }

    public void setJpFile(JPFile jpFile) {
        this.jpFile = jpFile;
    }
}
