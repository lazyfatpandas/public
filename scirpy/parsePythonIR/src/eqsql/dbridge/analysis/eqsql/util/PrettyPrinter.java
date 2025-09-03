package eqsql.dbridge.analysis.eqsql.util;

import java.util.List;

/**
 * Created by ek on 2/11/16.
 */
public class PrettyPrinter {
    public static String makeTreeString(Object root, List children){
        String prettyString = root.toString();

        if(children == null || children.size() == 0)
            return prettyString;

        for (Object child : children) {
            String childStr = (child == null) ? "Null" : child.toString();
            childStr = doIndent(childStr);
            prettyString = prettyString + "\n" +
                    childStr;
        }

        return prettyString;
    }

    /**
     * Indent each new line in str (including the first)
     */
    public static String doIndent(String str) {
        String indent = "| ";
        str = indent + str;
        str = str.replace("\n","\n" + indent);

        return str;
    }
}
