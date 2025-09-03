package DataFileAnalysis.model;
/* bhu created on 23/4/20  */


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInfo {
File file;
String name;
String absPath;
String extensionType;


    public FileInfo(String absPath) {
        this.absPath = absPath;
        if(absPath!=null) {
            this.file = new File(absPath);
            this.name=file.getName();
            extensionType = name.substring(name.lastIndexOf(".")+1);
        }


    }


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public String getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(String extensionType) {
        this.extensionType = extensionType;
    }

    public long getFileSize(String absPath) throws IOException
    {
        Path path = Paths.get(absPath);
        long fileSize = Files.size(path);
        return fileSize;
    }
}


