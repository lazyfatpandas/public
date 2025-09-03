package eqsql.shared;

import java.util.Collection;

/**
 * Created by K. Venkatesh Emani on 5/12/2017.
 * Wrapper around custom test function whose results can be compared
 * using {@link EqSQLComparator}. Users who wish to compare results
 * of EqSQL rewriting should wrap their test code in the test() function.
 */
public interface EqSQLTest {
    Collection test();
}
