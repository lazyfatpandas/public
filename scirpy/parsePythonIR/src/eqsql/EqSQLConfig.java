package eqsql;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Created by venkatesh on 5/7/17.
 */
public class EqSQLConfig {
    /* If rt.jar is on the java classpath, then leave this null.
     * Otherwise specify the path to rt.jar. */
    public static final String rtJarPath = null;

    /**
     * Generate HQL queries if true, SQL queries if false.
     */
    public static final boolean HQL = false;

    public static final String LOGPATH = "tmp/eqsql.log";
    /**
     * root of input sources relative to project root
     */
    public static final String INPUTS_ROOT = "inputs/";
    /**
     * one of class/dava
     */
    public static final String OUTPUT_FORMAT = "dava";

    //log settings
    private static boolean logSet = false;
    public static Logger getLogger(){
        if(!logSet) {
            FileAppender fa = new FileAppender();
            fa.setName("FileLogger");
            fa.setFile(LOGPATH);
            fa.setLayout(new PatternLayout("%d %-5p %M [%c{1}] %m%n"));
            fa.setThreshold(Level.DEBUG);
            fa.setAppend(true);
            fa.activateOptions();

            Logger.getRootLogger().addAppender(fa);
            logSet = true;
        }
        return Logger.getRootLogger();
    }
}
