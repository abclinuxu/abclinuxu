/*
 * User: literakl
 * Date: Mar 3, 2002
 * Time: 5:54:18 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Helper utilities for text manipulation.
 */
public class TextUtils {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TextUtils.class);

    static RE lineBreaks,emptyLine;

    static {
        try {
            lineBreaks = new RE("(<br>)|(<p>)|(div)",RE.MATCH_CASEINDEPENDENT);
            emptyLine = new RE("(\r\n){2}|(\n){2}",RE.MATCH_MULTILINE);
        } catch (RESyntaxException e) {
            log.error("Cannot create regexp to find line breaks!", e);
        }
    }

    /**
     * Returns string with correct line breaks. If <code>str</code> already contains
     * line breaks, original string is returned. Otherwise each empty line is replaced
     * by paragraph break and modified string is returned.
     */
    public static String fixLines(String str) {
        if ( str==null ) return null;
        if ( lineBreaks.match(str) ) return str;
        return emptyLine.subst(str,"<P>\n");
    }
}
