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
    /** default prefix to URL */
    String prefix;

    public UrlUtils(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id.
     * @param url URL to be encoded
     * @param prefix Prefix overiding default value or null
     * @param response Valid HttpServletResponse object
     */
    public String constructURL(String url, String prefix, HttpServletResponse response) {
        String out = null;
        if ( prefix!=null ) {
            out = prefix+url;
        } else if ( this.prefix!=null ) {
            out = this.prefix+url;
        } else out = url;
        return response.encodeURL(out);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id.
     * @param url URL to be encoded
     * @param response Valid HttpServletResponse object
     */
    public String constructURL(String url, HttpServletResponse response) {
        return constructURL(url,null,response);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id. This will work
     * with response.redirect().
     * @param url URL to be encoded
     * @param response Valid HttpServletResponse object
     */
    public String constructRedirectURL(String url, HttpServletResponse response) {
        String out = ( prefix!=null )? prefix+url : url;
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
}
