/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.servlets.utils.url;

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.utils.freemarker.Tools;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.*;
import java.io.IOException;

/**
 * Simple class used for generating URLs, which remembers
 * their prefix and session id.
 */
public class UrlUtils {

    public static final String PREFIX_HARDWARE = "/hardware";
    public static final String PREFIX_SOFTWARE = "/software";
    public static final String PREFIX_CLANKY = "/clanky";
    public static final String PREFIX_DRIVERS = "/ovladace";
    public static final String PREFIX_NEWS = "/zpravicky";
    public static final String PREFIX_FORUM = "/forum";
    public static final String PREFIX_DICTIONARY = "/slovnik";
    public static final String PREFIX_POLLS = "/ankety";
    public static final String PREFIX_FAQ = "/faq";
    public static final String PREFIX_BLOG = "/blog";
    public static final String PREFIX_NONE = "";

    static List prefixes = null;
    static {
        prefixes = new ArrayList();
        prefixes.add(PREFIX_NEWS);
        prefixes.add(PREFIX_FORUM);
        prefixes.add(PREFIX_HARDWARE);
        prefixes.add(PREFIX_CLANKY);
        prefixes.add(PREFIX_DRIVERS);
        prefixes.add(PREFIX_FAQ);
        prefixes.add(PREFIX_DICTIONARY);
        prefixes.add(PREFIX_POLLS);
        prefixes.add(PREFIX_SOFTWARE);
        prefixes.add(PREFIX_BLOG);
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
     */
    public String make(String url, String prefix) {
        String out;
        if ( prefix==null ) prefix = this.prefix;
        if (! PREFIX_NONE.equals(getPrefix(url)))
            out = url;
        else
            out = prefix+url;
        return response.encodeURL(out);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id.
     * @param url URL to be encoded
     */
    public String make(String url) {
        return make(url,null);
    }

    /**
     * Constructs new URL, which doesn't contain prefix and doesn't loose session id.
     */
    public String noPrefix(String url) {
        return response.encodeURL(url);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix and session id. This will work
     * with response.redirect().
     * @param url URL to be encoded
     */
    public String constructRedirectURL(String url) {
        String out = url;
        if (PREFIX_NONE.equals(getPrefix(url))) out = prefix+url;
        return response.encodeRedirectURL(out);
    }

    /**
     * Constructs new URL, which doesn't lose context prefix. This version is dedicated
     * to response.getDispatcher().dispatch().
     * @param url URL to be encoded
     */
    public String constructDispatchURL(String url) {
        String out = url;
        if (PREFIX_NONE.equals(getPrefix(url))) out = prefix+url;
        return out;
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

    /**
     * Redirects to desired URL, keeping session and prefix.
     */
    public void redirect(HttpServletResponse response, String url) throws IOException {
        String url2 = constructRedirectURL(url);
        response.sendRedirect(url2);
    }

    /**
     * Redirects to desired URL, keeping session and optionally prefix.
     */
    public void redirect(HttpServletResponse response, String url, boolean keepPrefix) throws IOException {
        if (keepPrefix) {
            redirect(response, url);
            return;
        }
        String url2 = response.encodeRedirectURL(url);
        response.sendRedirect(url2);
    }

    /**
     * Dispatches to desired URL, keeping prefix.
     */
    public void dispatch(HttpServletRequest request, HttpServletResponse response, String url) throws ServletException, IOException {
        String url2 = constructDispatchURL(url);
        RequestDispatcher dispatcher = request.getRequestDispatcher(url2);
        dispatcher.forward(request,response);
    }

    /**
     * Creates url for relation. If relation url is set, it will be used, otherwise
     * it will be constructed from prefix and relation id.
     * @param relation relation for which we need url.
     * @param prefix url prefix, one of constants from this class
     * @return url to display this relation
     */
    public static String getRelationUrl(Relation relation, String prefix) {
        if (relation == null)
            throw new AbcException("©patný vstup: relace nesmí být prázdná!");
        relation = (Relation) Tools.sync(relation);
        if (relation.getUrl() != null)
            return relation.getUrl();
        GenericObject child = relation.getChild();
        if (child instanceof Category)
            return prefix + "/dir/" + relation.getId();
        else
            return prefix + "/show/" + relation.getId();
    }

    /**
     * Creates url for relation. If relation url is set, it will be used, otherwise
     * it will be constructed from prefix and relation id.
     * @param relation relation for which we need url
     * @return url to display this relation
     */
    public String getRelationUrl(Relation relation) {
        return getRelationUrl(relation, prefix);
    }
}
