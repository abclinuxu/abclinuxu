/*
 * User: literakl
 * Date: Dec 4, 2001
 * Time: 7:33:21 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.utils;

import java.util.List;


/**
 * This class tokenizes logical expressions as defined in findByExample.
 */
public class LogicalExpressionTokenizer {

    private String expression;

    public LogicalExpressionTokenizer(String expression) {
        this.expression = expression;
    }

    /**
     * @return next token of logical expression (variable name, relation or parentheses)
     */
    public String nextToken() {
        int i = 0;
        for (; i<expression.length(); i++) {
            char c = expression.charAt(i);
            if (c=='(' || c==')' || (c>='0' && c<='9')) {
                expression = expression.substring(i + 1);
                return ""+c;
            }
            if (c==' ') continue;
            if (i>0) {
                expression = expression.substring(i);
            }
            break;
        }
        if (i==expression.length()) return null;

        if (expression.startsWith("AND")) {
            expression = expression.substring(3);
            return "AND";
        }
        if (expression.startsWith("OR")) {
            expression = expression.substring(2);
            return "OR";
        }
        return null; //throw exception, indicates error
    }

    /**
     * @return String, where each object in list <code>objects</code>
     * is represented by its index (starting at 0, maximum size is 10)
     * and there is OR relation between all objects.
     */
    public static String makeOrRelation(List objects) {
        StringBuffer sb = new StringBuffer();
        if (objects.size()==1) return "0";

        int size = objects.size() - 1;
        if (size>9) size = 9;

        for (int i = 0; i<size; i++) {
            sb.append(i);
            sb.append(" OR ");
        }

        sb.append(size);
        return sb.toString();
    }
}
