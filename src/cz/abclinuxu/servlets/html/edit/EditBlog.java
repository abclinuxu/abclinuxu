/*
 * User: literakl
 * Date: 23.11.2004
 * Time: 8:44:27
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Servlet used to manipulate with user blogs.
 */
public class EditBlog implements AbcAction, Configurable {
    public static final String PREF_RE_INVALID_BLOG_NAME = "regexp.invalid.blogname";
    public static final String PARAM_BLOG_NAME = "blogName";

    static {
        EditBlog instance = new EditBlog();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    static RE reBlogName;

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return null;
    }

    /**
     * Sets blog name. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param category category to be updated
     * @return false, if there is a major error.
     */
    static boolean setBlogName(Map params, Category category, Map env) {
        String name = (String) params.get(PARAM_BLOG_NAME);
        if ( name==null || name.trim().length()<1) {
            ServletUtils.addError(PARAM_BLOG_NAME, "Zadejte jméno blogu!", env, null);
            return false;
        }
        name = name.trim();
        if (reBlogName.match(name)) {
            ServletUtils.addError(PARAM_BLOG_NAME, "Jméno blogu smí obsahovat jen znaky a-z, 0-9 a _!", env, null);
            return false;
        }
        category.setSubType(name);
        return true;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String re = prefs.get(PREF_RE_INVALID_BLOG_NAME, null);
        try {
            reBlogName = new RE(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Cannot compile regular expression '"+re+"' given by "+PREF_RE_INVALID_BLOG_NAME);
        }
    }
}
