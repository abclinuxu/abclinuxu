/*
 * User: literakl
 * Date: 21.2.2004
 * Time: 22:01:30
 */
package cz.abclinuxu.servlets.utils;

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
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.data.Relation;

import java.util.prefs.Preferences;
import java.util.*;
import java.io.IOException;

/**
 * Handler for URLs. Responsible for redirecting deprecated URLs,
 * selects Action based on URL, extracts request parameters from URL.
 */
public final class URLMapper implements Configurable {
    static Logger log = Logger.getLogger(URLMapper.class);

    public static final String PREF_FILE = "config";
    public static final String PREF_DOMAIN = "domain";

    private static URLMapper htmlVersion, wapVersion;
    static {
        htmlVersion = new URLMapper();
        wapVersion = new URLMapper();
        ConfigurationManager.getConfigurator().configureAndRememberMe(htmlVersion);
    }

    List actionMapping;
    List deprecatedMapping;
    String domain;
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
        PatternAction patternAction;

        String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
        Relation relation = SQLTool.getInstance().findRelationByURL(url);
        if (relation!=null) {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            params.put(ShowObject.PARAM_RELATION_SHORT, Integer.toString(relation.getId()));
            return showObject;
        }

        for ( Iterator iter = actionMapping.iterator(); iter.hasNext(); ) {
            patternAction = (PatternAction) iter.next();
            if (patternAction.getRe().match(url))
                return patternAction.getAction();
        }
        throw new NotFoundException("Nezname URL: "+url+" !");
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
        domain = prefs.get(PREF_DOMAIN, "abclinuxu.cz");
    }

    /**
     * @return domain name handled by this instance.
     */
    public String getDomain() {
        return domain;
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
        List actions = new ArrayList(40);
        String pattern = null, action = null;
        Element element;
        REProgram regexp;
        Object o;
        try {
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                element = (Element) iter.next();
                pattern = element.elementText("pattern");
                action = element.elementText("action");
                regexp = new RECompiler().compile(pattern);
                o = Class.forName(action).newInstance();
                if ( AbcAction.class.isInstance(o) )
                    actions.add(new PatternAction(regexp, (AbcAction) o));
                else
                    log.warn("Action "+action+" does not implement AbcAction interface!");
                if ( ShowObject.class.isInstance(o) )
                    showObject = (AbcAction) o;
            }
        } catch (RESyntaxException e) {
            log.error("Pattern '"+pattern+"' cannot be compiled!", e);
        } catch (InstantiationException e) {
            log.error("Action '"+action+"' cannot be instantiated!", e);
        } catch (IllegalAccessException e) {
            log.error("Action '"+action+"' cannot be instantiated!", e);
        } catch (ClassNotFoundException e) {
            log.error("Action '"+action+"' cannot be instantiated!", e);
        }
        instance.actionMapping = actions;
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
