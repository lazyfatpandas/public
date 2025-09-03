package eqsql.exceptions;

/**
 * Created by K. Venkatesh Emani on 4/30/2017.
 */
public class DIRConstructionException extends Exception {
    public DIRConstructionException(String message) {
        super("DIRConstructionException:" + message);
    }
}
