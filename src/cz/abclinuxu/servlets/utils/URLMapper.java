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

    Map actionMapping;
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
    public boolean redirectDeprecated(HttpServletRequest request, HttpServletResponse response) {
        return false;
    }

    /**
     * Finds AbcAction for URL represented by this request. As side effect, parameters may
     * be extracted from URL and added to env.
     * @return AbcAction for this URL.
     * @throws NotFoundException if there is no mapping for thsi URL
     */
    public AbcAction findAction(HttpServletRequest request, Map env) throws NotFoundException {
        RE re;

        String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
        Set regexps = actionMapping.keySet();
        for ( Iterator iter = regexps.iterator(); iter.hasNext(); ) {
            re = (RE) iter.next();
            if (re.match(url))
                return (AbcAction) actionMapping.get(re);
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
        Map actions = new LinkedHashMap(50);

        String pattern = null, action = null;
        Element element;
        RE regexp;
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
                    actions.put(regexp, o);
                else
                    log.warn("Action "+action+" does not implement AbcAction interface!");
            }

            actionMapping = actions;
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
}
