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

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Relation;

import java.util.prefs.Preferences;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

/**
 * Handler for URLs. Responsible for redirecting deprecated URLs,
 * selects Action based on URL, extracts request parameters from URL.
 */
public final class URLMapper implements Configurable {
    static Logger log = Logger.getLogger(URLMapper.class);

    public static final String PREF_FILE = "config";

    private static URLMapper htmlVersion, wapVersion;
    private static Pattern reTrailingRid;
    static {
        htmlVersion = new URLMapper();
        wapVersion = new URLMapper();
        ConfigurationManager.getConfigurator().configureAndRememberMe(htmlVersion);
        reTrailingRid = Pattern.compile("/([0-9]+)$");
    }

    List actionMapping;
    List priorityMapping;
    List deprecatedMapping;
    static AbcAction showObject;

    private URLMapper() {
    }

    /**
     * @return singleton instance of this class.
     */
    public static URLMapper getInstance(Version version) {
        if (version==Version.HTML)
            return htmlVersion;
        else
            return wapVersion;
    }

    /**
     * If URL represented by request is deprecated, response is redirected
     * to new location.
     * @return whether response was redirected
     */
    public boolean redirectDeprecated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PatternRegexpReplacement mapping = null;
        boolean matches = false;

        String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
        for ( Iterator iter = deprecatedMapping.iterator(); iter.hasNext(); ) {
            mapping = (PatternRegexpReplacement) iter.next();
            if (mapping.getType().equals(InputType.HOST))
                matches = mapping.getRe().match(request.getServerName());
            else
                matches = mapping.getRe().match(url);
            if (matches)
                break;
        }
        if ( ! matches )
            return false;

        url = ServletUtils.getURL(request);
        String newURL = mapping.getRegexp().subst(url, mapping.getReplacement(), RE.REPLACE_FIRSTONLY + RE.REPLACE_BACKREFERENCES);
        if (url.equals(newURL)) {
            log.warn("Selhalo presmerovani adresy "+url);
            String msg = "Adresa byla zmenena. Zkuste hledani";
            response.sendError(HttpServletResponse.SC_GONE, msg);
            return true;
        }

        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.sendRedirect(newURL);
        return true;
    }

    /**
     * Finds AbcAction for URL represented by this request.
     * @return AbcAction for this URL.
     * @throws NotFoundException if there is no mapping for this URL
     */
    public AbcAction findAction(HttpServletRequest request, Map env) throws NotFoundException {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        PatternAction patternAction;

        String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
        Matcher matcher = reTrailingRid.matcher(url);
        boolean custom = ! (matcher.find() || url.startsWith(UrlUtils.PREFIX_DICTIONARY));

        if (custom) {
            Relation relation = loadCustomRelation(url);
            if (relation != null)
                params.put(ShowObject.PARAM_RELATION_SHORT, Integer.toString(relation.getId()));
            else
                custom = false;
        }

        for (Iterator iter = priorityMapping.iterator(); iter.hasNext();) {
            patternAction = (PatternAction) iter.next();
            if (patternAction.getRe().match(url)) {
                setActionParams(patternAction, params);
                return patternAction.getAction();
            }
        }

        if (custom)
            return showObject;

        for ( Iterator iter = actionMapping.iterator(); iter.hasNext(); ) {
            patternAction = (PatternAction) iter.next();
            if (patternAction.getRe().match(url)) {
                setActionParams(patternAction, params);
                return patternAction.getAction();
            }
        }
        throw new NotFoundException("Nezname URL: "+url+" !");
    }

    /**
     * Loads relation for given uri. The uri may either be custom (/clanky/gimp-v-prikladech)
     * or constructed: ending with relation id (/clanky/12345).
     * @param url normalized uri
     * @return initialized relation or null
     * @throws NotFoundException if the relation id points to nonexisting relation
     */
    public static Relation loadRelationFromUrl(String url) {
        Matcher matcher = reTrailingRid.matcher(url);
        if (matcher.find()) {
            String found = matcher.group(1);
            int rid = Misc.parseInt(found, -1);
            return (Relation) PersistanceFactory.getPersistance().findById(new Relation(rid));
        }
        return loadCustomRelation(url);
    }

    /**
     * Loads relation where url.equals(relation.getUrl)
     * todo known custom URLs must be cached
     * @param url normalized uri
     * @return initialized relation or null
     */
    private static Relation loadCustomRelation(String url) {
        SQLTool sqlTool = SQLTool.getInstance();
        Relation relation = null;
        if (url.startsWith(UrlUtils.PREFIX_DICTIONARY) && url.length() > UrlUtils.PREFIX_DICTIONARY.length()) {
            url = url.substring(UrlUtils.PREFIX_DICTIONARY.length() + 1);
            relation = sqlTool.findDictionaryByURLName(url);
        } else
            relation = sqlTool.findRelationByURL(url);
        return relation;
    }

    /**
     * If patternAction defines some params, add them to params.
     * @param patternAction
     * @param params map of HTTP params
     */
    private void setActionParams(PatternAction patternAction, Map params) {
        Map actionParams = patternAction.getParams();
        if (actionParams==null)
            return;
        String name;
        for (Iterator iter = actionParams.keySet().iterator(); iter.hasNext();) {
            name = (String) iter.next();
            params.put(name, actionParams.get(name));
        }
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        String file = prefs.get(PREF_FILE, null);
        if (file==null)
            log.error("Config file preference is missing!");
        else
            initialize(file);
    }

    /**
     * Reconfigures mappings.
     * @param filename
     */
    private static void initialize(String filename) {
        log.info("Initializing from file "+filename);
        String version;
        Element element;

        try {
            Document document = new SAXReader().read(filename);
            List nodes = document.getRootElement().elements("format");
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                element = (Element) iter.next();
                version = element.attributeValue("version");
                if (Version.HTML.toString().equals(version))
                    readFormat(element.elements("mapping"), htmlVersion);
                else if (Version.WAP.toString().equals(version))
                    readFormat(element.elements("mapping"), wapVersion);
            }

            nodes = document.getRootElement().elements("deprecated");
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                element = (Element) iter.next();
                version = element.attributeValue("version");
                if (Version.HTML.toString().equals(version))
                    readDeprecated(element.elements("mapping"), htmlVersion);
                else if (Version.WAP.toString().equals(version))
                    readDeprecated(element.elements("mapping"), wapVersion);
            }
        } catch (DocumentException e) {
            log.error("File "+filename+" is not valid XML!", e);
        }
    }

    /**
     * Reads format mappings.
     */
    private static void readFormat(List nodes, URLMapper instance) {
        List actions = new ArrayList(60), priorActions = new ArrayList(10);
        String pattern = null, action = null;
        Element element;
        REProgram regexp;
        Object o;
        PatternAction mapping;
        for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
            try {
                element = (Element) iter.next();
                pattern = element.elementText("pattern");
                action = element.elementText("action");
                regexp = new RECompiler().compile(pattern);
                o = Class.forName(action).newInstance();
                if ( ! AbcAction.class.isInstance(o) ) {
                    log.warn("Action "+action+" does not implement AbcAction interface!");
                    continue;
                }

                mapping = new PatternAction(regexp, (AbcAction) o);
                if ("true".equals(element.attributeValue("priority")))
                    priorActions.add(mapping);
                else
                    actions.add(mapping);

                List paramList = element.elements("param");
                if (paramList!=null && paramList.size()>0)
                    for (Iterator iterParams = paramList.iterator(); iterParams.hasNext();) {
                        Element param = (Element) iterParams.next();
                        mapping.addParam(param.attributeValue("name"), param.attributeValue("value"));
                    }

                if ( ShowObject.class.isInstance(o) )
                    showObject = (AbcAction) o;
            } catch (RESyntaxException e) {
                log.error("Pattern '" + pattern + "' cannot be compiled!", e);
            } catch (InstantiationException e) {
                log.error("Action '" + action + "' cannot be instantiated!", e);
            } catch (IllegalAccessException e) {
                log.error("Action '" + action + "' cannot be instantiated!", e);
            } catch (ClassNotFoundException e) {
                log.error("Action '" + action + "' cannot be instantiated!", e);
            }
        }
        instance.actionMapping = actions;
        instance.priorityMapping = priorActions;
    }

    /**
     * Reads deprecated mappings.
     */
    private static void readDeprecated(List nodes, URLMapper instance) {
        List deprecated = new ArrayList(10);
        String pattern = null, replacement;
        Element element;
        RE regexp, regexp2;
        InputType inputType;
        for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
            element = (Element) iter.next();
            pattern = element.elementText("regexp");
            replacement = element.elementText("replacement");
            regexp2 = new RE(pattern);
            element = element.element("pattern");
            pattern = element.getText();
            regexp = new RE(pattern);
            inputType = InputType.get(element.attributeValue("input"));
            deprecated.add(new PatternRegexpReplacement(regexp, inputType, regexp2, replacement));
        }
        instance.deprecatedMapping = deprecated;
    }

    public static class Version {
        public static final Version HTML = new Version("www");
        public static final Version WAP = new Version("wap");
        String value;

        private Version(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    static class PatternAction {
        REProgram re;
        AbcAction action;
        Map params;

        public PatternAction(REProgram re, AbcAction action) {
            this.re = re;
            this.action = action;
        }

        public RE getRe() {
            return new RE(re);
        }

        public AbcAction getAction() {
            return action;
        }

        public Map getParams() {
            return params;
        }

        public void addParam(String name, String value) {
            if (params==null)
                params = new HashMap(3, 1.0f);
            params.put(name, value);
        }
    }

    static class PatternRegexpReplacement {
        RE re, regexp;
        String replacement;
        InputType type;

        public PatternRegexpReplacement(RE re, InputType inputType, RE regexp, String replacement) {
            this.re = re;
            this.type = inputType;
            this.regexp = regexp;
            this.replacement = replacement;
        }

        /**
         * @return regular expression, that finds out, whether URL is deprecated.
         */
        public RE getRe() {
            return re;
        }

        /**
         * @return regular expression used to mark portions of URL, that shall be replaced.
         */
        public RE getRegexp() {
            return regexp;
        }

        /**
         * @return string that replaces portions of URL
         */
        public String getReplacement() {
            return replacement;
        }

        /**
         * @return type of input, on which re shall be executed.
         */
        public InputType getType() {
            return type;
        }
    }

    static class InputType {
        public static final InputType HOST = new InputType("host");
        public static final InputType PATH = new InputType("path");
        String desc;

        private InputType(String desc) {
            this.desc = desc;
        }

        public String toString() {
            return desc;
        }

        /**
         * Lookups string between defined constants.
         * @return instance of matching type, or null
         */
        public static InputType get(String type) {
            if (HOST.toString().equals(type))
                return HOST;
            if (PATH.toString().equals(type))
                return PATH;
            return null;
        }

        public boolean equals(Object o) {
            if ( this==o ) return true;
            if ( !(o instanceof InputType) ) return false;

            final InputType inputType = (InputType) o;

            if ( desc!=null ? !desc.equals(inputType.desc) : inputType.desc!=null ) return false;

            return true;
        }

        public int hashCode() {
            return (desc!=null ? desc.hashCode() : 0);
        }
    }
}
