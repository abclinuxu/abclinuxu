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

/**
 * Simple class used for generating URLs, which remembers
 * their prefix and session id.
 */
public class UrlUtils {

    public static final String PREFIX_HARDWARE = "/hardware";
    public static final String PREFIX_SOFTWARE = "/software";
    public static final String PREFIX_CLANKY = "/clanky";
    public static final String PREFIX_NONE = "";

    /** default prefix to URL */
    String prefix;
    HttpServletResponse response;

    /**
     * Creates new UrlUtils instance.
     * @param url Request URI
     */
    public UrlUtils(String url, HttpServletResponse response) {
        if ( url.startsWith(PREFIX_HARDWARE) ) {
            prefix = PREFIX_HARDWARE;
        } else if ( url.startsWith(PREFIX_SOFTWARE) ) {
            prefix = PREFIX_SOFTWARE;
        } else if ( url.startsWith(PREFIX_CLANKY) ) {
            prefix = PREFIX_CLANKY;
        } else {
            prefix = PREFIX_NONE;
        }
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
        if ( prefix!=null ) {
            out = prefix+url;
        } else {
            out = this.prefix+url;
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
        if ( prefix!=null && !url.startsWith(prefix) ) out = prefix+url;
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
}
