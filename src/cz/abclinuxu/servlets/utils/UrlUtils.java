/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 6, 2002
 * Time: 5:17:40 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.utils;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Simple class used for generating URLs, which remembers
 * their prefix and session id.
 */
public class UrlUtils {

    public static final String PREFIX_HARDWARE = "/hardware";
    public static final String PREFIX_SOFTWARE = "/software";
    public static final String PREFIX_CLANKY = "/clanky";
    public static final String PREFIX_NONE = "";

    static List prefixes = null;
    static {
        prefixes = new ArrayList(3);
        prefixes.add(PREFIX_HARDWARE);
        prefixes.add(PREFIX_SOFTWARE);
        prefixes.add(PREFIX_CLANKY);
    }

    /** default prefix to URL */
    String prefix;
    HttpServletResponse response;

    /**
     * Creates new UrlUtils instance.
     * @param url Request URI
     */
    public UrlUtils(String url, HttpServletResponse response) {
        this.prefix = getPrefix(url);
        this.response = response;
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id.
     * @param url URL to be encoded
     * @param prefix Prefix overiding default value or null
     * @param response Valid HttpServletResponse object
     */
    public String make(String url, String prefix) {
        String out = null;
        if ( prefix==null ) prefix = this.prefix;
        if ( getPrefix(url)!=PREFIX_NONE ) {
            out = url;
        } else {
            out = prefix+url;
        }
        return response.encodeURL(out);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id.
     * @param url URL to be encoded
     * @param response Valid HttpServletResponse object
     */
    public String make(String url) {
        return make(url,null);
    }

    /**
     * Constructs new URL, which doesn't contains prexif and doesn't loose session id.
     */
    public String makePrefixless(String url) {
        return response.encodeURL(url);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id. This will work
     * with response.redirect().
     * @param url URL to be encoded
     * @param response Valid HttpServletResponse object
     */
    public String constructRedirectURL(String url) {
        String out = url;
        if ( getPrefix(url)!=PREFIX_NONE ) out = prefix+url;
        return response.encodeRedirectURL(out);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix. This version is dedicated
     * to response.getDispatcher().dispatch().
     * @param url URL to be encoded
     */
    public String constructDispatchURL(String url) {
        return ( prefix!=null )? prefix+url : url;
    }

    /**
     * @return Prefix used by this instance
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Finds prefix used in presented url.
     */
    public static String getPrefix(String url) {
        if ( url==null || url.length()==0 ) return PREFIX_NONE;
        for (Iterator iter = prefixes.iterator(); iter.hasNext();) {
            String prefix = (String) iter.next();
            if ( url.startsWith(prefix) ) return prefix;
        }
        return PREFIX_NONE;
    }
}
