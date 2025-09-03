package eqsql.exceptions;

/**
 * Created by K. Venkatesh Emani on 4/30/2017.
 */
public class QueryTranslationException extends Exception {
    public QueryTranslationException(String message) {
        super("QueryTranslationException: " + message);
    }
}
