package eqsql.hqlparse;

/**
 * Created by venkatesh on 2/15/2018.
 * Currently we handle only very simple queries such as loading all objects,
 * and filter with single where with equality condition.
 */
public enum HQLQueryType {
    LOAD_ALL, //eg: from WilosUser
    FILTER, //eg: from WilosUser wu where wu.abcd = xyz
}
