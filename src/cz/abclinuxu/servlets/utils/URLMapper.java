/*
 * User: literakl
 * Date: 21.2.2004
 * Time: 22:01:30
 */
package cz.abclinuxu.servlets.utils;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
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
import cz.abclinuxu.exceptions.NotFoundException;

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

    private static URLMapper singleton;
    static {
        singleton = new URLMapper();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    List actionMapping;
    List deprecatedMapping;

    private URLMapper() {
    }

    /**
     * @return singleton instance of this class.
     */
    public static URLMapper getInstance() {
        return singleton;
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
        String newURL = mapping.getRegexp().subst(url, mapping.getReplacement(), RE.REPLACE_FIRSTONLY);
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.sendRedirect(newURL);
        return true;
    }

    /**
     * Finds AbcAction for URL represented by this request. As side effect, parameters may
     * be extracted from URL and added to env.
     * @return AbcAction for this URL.
     * @throws NotFoundException if there is no mapping for thsi URL
     */
    public AbcAction findAction(HttpServletRequest request, Map env) throws NotFoundException {
        PatternAction patternAction;

        String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
        for ( Iterator iter = actionMapping.iterator(); iter.hasNext(); ) {
            patternAction = (PatternAction) iter.next();
            if (patternAction.getRe().match(url))
                return patternAction.getAction();
        }
        throw new NotFoundException("Nemohu najit zadnou akci pro URL '"+url+"' !");
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
    private void initialize(String filename) {
        log.info("Initializing from file "+filename);
        List actions = new ArrayList(40);
        List deprecated = new ArrayList(10);

        String pattern = null, action = null, replacement;
        Element element;
        RE regexp, regexp2;
        InputType inputType;
        Object o;

        try {
            Document document = new SAXReader().read(filename);
            List nodes = document.selectNodes("//valid/mapping");
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                element = (Element) iter.next();
                pattern = element.elementText("pattern");
                action = element.elementText("action");
                regexp = new RE(pattern);
                o = Class.forName(action).newInstance();
                if (AbcAction.class.isInstance(o))
                    actions.add(new PatternAction(regexp, (AbcAction) o));
                else
                    log.warn("Action "+action+" does not implement AbcAction interface!");
            }
            actionMapping = actions;

            nodes = document.selectNodes("//deprecated/mapping");
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
            deprecatedMapping = deprecated;
        } catch (DocumentException e) {
            log.error("File "+filename+" is not valid XML!", e);
        } catch (RESyntaxException e) {
            log.error("Pattern '"+pattern+"' cannot be compiled!", e);
        } catch (InstantiationException e) {
            log.error("Action '"+action+"' cannot be instantiated!", e);
        } catch (IllegalAccessException e) {
            log.error("Action '"+action+"' cannot be instantiated!", e);
        } catch (ClassNotFoundException e) {
            log.error("Action '"+action+"' cannot be instantiated!", e);
        }
    }

    class PatternAction {
        RE re;
        AbcAction action;

        public PatternAction(RE re, AbcAction action) {
            this.re = re;
            this.action = action;
        }

        public RE getRe() {
            return re;
        }

        public AbcAction getAction() {
            return action;
        }
    }

    class PatternRegexpReplacement {
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
