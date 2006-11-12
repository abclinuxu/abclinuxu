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

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.Relation;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;

/**
 * This class is responsible for enforcing rules
 * defined in design/hierarchicka_url.txt.
 * User: literakl
 * Date: 17.4.2005
 */
public class URLManager implements Configurable {
    public static final String PREF_INVALID_CHARACTERS_RELATIVE = "regexp.invalid.characters.relative.url";
    public static final String PREF_INVALID_CHARACTERS_ABSOLUTE = "regexp.invalid.characters.absolute.url";
    public static final String PREF_FORBIDDEN_EXTENSIONS = "forbidden.extensions";

    private static REProgram reInvalidCharactersRelative, reInvalidCharactersAbsolute, rePlus;
    private static List forbiddenExtensions;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new URLManager());
    }

    private URLManager() {}


    /**
     * This method shall be used on last part of URL (e.g. after last slash).
     * It converts it to obey common rules.
     * @param url must not be null
     * @return normalized URL that obeys all rules
     * @throws AbcException if URL is after conversions empty
     */
    public static String enforceRelativeURL(String url) {
        if (url==null || url.length() == 0)
            throw new AbcException("URL nesmí být prázdné!");
        if (url.charAt(0)=='/')
            url = url.substring(1);

        String fixedURL = normalizeUrl(url, false);
        fixedURL = enforceValidExtension(fixedURL);
        return fixedURL;
    }

    /**
     * Validates absolute URL, whether it conforms abclinuxu rules. It tries to fix
     * some errors.
     * @param url
     * @return URL according all rules.
     * @throws AbcException if URL is wrong and cannot be fixed
     */
    public static String enforceAbsoluteURL(String url) {
        if (url == null || url.length() == 0)
            throw new AbcException("URL nesmí být prázdné!");
        if (!url.startsWith("/"))
            throw new AbcException("Adresa stránky musí být absolutní!");

        String fixedURL = normalizeUrl(url, true);
        fixedURL = enforceValidExtension(fixedURL);
        return fixedURL;
    }

    private static String normalizeUrl(String url, boolean absolute) {
        if (Character.isDigit(url.charAt(0)))
            url = "-" + url;
        String fixedURL = normalizeCharacters(url, absolute);
        if (fixedURL.length() == 0)
            throw new AbcException("Zvolte jiné URL bez speciálních znakù!");
        return fixedURL;
    }

    /**
     * URL may end with extension that collidates with servlet mappings.
     * If it happens, we must alter it.
     * @param url
     * @return URL that does not end with forbidden extension
     */
    private static String enforceValidExtension(String url) {
        for (Iterator iter = forbiddenExtensions.iterator(); iter.hasNext();) {
            String ext = (String) iter.next();
            if (url.endsWith(ext)) {
                int position = url.lastIndexOf('.');
                url = url.substring(0, position) + '-' + url.substring(position+1);
                return url;
            }
        }
        return url;
    }

    /**
     * Normalizes content of URL. For example it removes
     * diacritics, replaces invalid characters with dashes,
     * removes traling dashes, slashes and dots and converts to lowercase.
     * @param url non-null string
     * @return normalized URL, it may have zero length.
     */
    private static String normalizeCharacters(String url, boolean absoluteURL) {
        String fixedURL = DiacriticRemover.getInstance().removeDiacritics(url);
        fixedURL = new RE(rePlus, RE.REPLACE_ALL).subst(fixedURL, "plus"); // e.g. c++
        if (absoluteURL)
            fixedURL = new RE(reInvalidCharactersAbsolute, RE.REPLACE_ALL).subst(fixedURL, "-");
        else
            fixedURL = new RE(reInvalidCharactersRelative, RE.REPLACE_ALL).subst(fixedURL, "-");
        fixedURL = fixedURL.toLowerCase();

        int length = fixedURL.length();
        char c = fixedURL.charAt(length-1);
        while (length > 0 && (c == '-' || c == '.' || c == '/')) {
            length--;
            c = fixedURL.charAt(length-1);
        }

        if (fixedURL.length() > length)
            fixedURL = fixedURL.substring(0, length);
        return fixedURL;
    }

    /**
     * This method checks whether the url already exists in database.
     * If it does, it returns URL with suffix, that has not been already
     * stored. No URL format check is performed.
     * @param url
     * @return unique URL
     */
    public static String protectFromDuplicates(String url) {
        SQLTool sqlTool = SQLTool.getInstance();
        int counter = 2;
        String testedURL = url;
        Relation relation = sqlTool.findRelationByURL(testedURL);

        while (relation!=null) {
            testedURL = url + "-" + counter;
            counter++;
            relation = sqlTool.findRelationByURL(testedURL);
        }
        return testedURL;
    }

    /**
     * Tests, whether URL exists in database. URL format is not checked or modified.
     * @param url URL to be tested
     * @return true, if database contains this URL already
     */
    public static boolean exists(String url) {
        SQLTool sqlTool = SQLTool.getInstance();
        Relation relation = sqlTool.findRelationByURL(url);
        return relation!=null;
    }

    /**
     * Tests, whether URL exists in database. URL format is not checked or modified.
     * If the URL already exists, its relation id is compared to relationId param.
     * @param url URL to be tested
     * @param relationId id of existing relation or 0
     * @return true, URL is not present in database or its relation id is equal to relationId
     */
    public static boolean isURLUnique(String url, int relationId) {
        SQLTool sqlTool = SQLTool.getInstance();
        Relation existingRelation = sqlTool.findRelationByURL(url);
        if (existingRelation == null)
            return true;
        if (relationId == 0)
            return true;
        if (relationId != existingRelation.getId())
            return false;
        return true;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        rePlus = new RECompiler().compile("\\+");

        String tmp = prefs.get(PREF_INVALID_CHARACTERS_RELATIVE, null);
        reInvalidCharactersRelative = new RECompiler().compile(tmp);

        tmp = prefs.get(PREF_INVALID_CHARACTERS_ABSOLUTE, null);
        reInvalidCharactersAbsolute = new RECompiler().compile(tmp);

        List tmpList = new ArrayList();
        tmp = prefs.get(PREF_FORBIDDEN_EXTENSIONS, null);
        if (tmp!=null && tmp.length()!=0) {
            StringTokenizer stk = new StringTokenizer(tmp,",");
            while (stk.hasMoreTokens())
                tmpList.add(stk.nextToken());
        }
        forbiddenExtensions = tmpList;
    }
}
