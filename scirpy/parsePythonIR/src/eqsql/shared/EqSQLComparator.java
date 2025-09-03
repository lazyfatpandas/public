package eqsql.shared;

import java.util.Collection;

/**
 * Created by K. Venkatesh Emani on 5/12/2017.
 * Wrapper for function to compare two collections.
 */
public interface EqSQLComparator {
    /**
     * Users writing tests should implement this function to compare
     * results of two collections.
     * @param c1 Any collection
     * @param c2 Any collection
     * @return True if results of c1 and c2 match, false otherwise.
     */
    boolean compare(Collection c1, Collection c2);
}
