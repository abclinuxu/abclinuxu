/*
 * User: literakl
 * Date: 24.11.2004
 * Time: 7:13:32
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditBlog;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistance.extra.CompareCondition;
import cz.abclinuxu.persistance.extra.Field;
import cz.abclinuxu.persistance.extra.Operation;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.exceptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Display blogs.
 */
public class ViewBlog implements AbcAction, Configurable {
    public static final String PARAM_FROM = "from";

    public static final String VAR_BLOG_RELATION = "REL_BLOG";
    public static final String VAR_BLOG = "BLOG";
    public static final String VAR_STORIES = "STORIES";
    public static final String VAR_STORY = "STORY";

    public static final String PREF_BLOG_URL = "regexp.blog.url";
    static RE reUrl;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new ViewBlog());
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation blogRelation = null;
        int year, month, day, rid;
        year = month = day = rid = 0;
        String name = null;

        String uri = (String) env.get(Constants.VAR_REQUEST_URI);
//        uri = "/blog/Yeti/archive/2004/12/12/124545";
        synchronized(reUrl) {
            reUrl.match(uri);
            int matched = reUrl.getParenCount();
            if (matched>=1)
                name = reUrl.getParen(1);
            if (matched>=3)
                year = Integer.parseInt(reUrl.getParen(3));
            if (matched>=4)
                month = Integer.parseInt(reUrl.getParen(4));
            if (matched>=5)
                day = Integer.parseInt(reUrl.getParen(5));
            if (matched>=6)
                rid = Integer.parseInt(reUrl.getParen(6));
        }

        if (name!=null) {
            CompareCondition condition = new CompareCondition(Field.SUBTYPE, Operation.EQUAL, name);
            SQLTool sqlTool = SQLTool.getInstance();
            List list = sqlTool.findSectionRelationsWithType(Category.SECTION_BLOG, new Qualifier[]{condition});
            if (list.size()==0) {
                throw new NotFoundException("Blog "+name+" nebyl nalezen!");
            }
            blogRelation = (Relation) list.get(0);
        }

        if (blogRelation!=null) {
            Category blog = (Category) blogRelation.getChild();
            Tools.sync(blog);
            env.put(VAR_BLOG_RELATION, blogRelation);
            env.put(VAR_BLOG, blog);
            EditBlog.storeCategories(blog, env);

            if (rid!=0)
                return processBlog(blogRelation, rid, request, response, env);
            else
                return processBlogs(blogRelation, year, month, day, request, response, env);
        } else
            return processBlogSpace(request, response, env);
    }

    /**
     * Displays one blogRelation content. Its stories may be limited to given year, month or day.
     */
    protected String processBlog(Relation blogRelation, int rid, HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) persistance.findById(new Relation(rid));
        Tools.sync(relation);
        persistance.incrementCounter(relation.getChild());
        env.put(VAR_STORY, relation);

        return FMTemplateSelector.select("ViewBlog", "blog", env, request);
    }

    /**
     * Displays one blogRelation content. Its stories may be limited to given year, month or day.
     */
    protected String processBlogs(Relation blogRelation, int year, int month, int day, HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();

        List stories = new ArrayList();
        env.put(VAR_STORIES, stories);

        return FMTemplateSelector.select("ViewBlog", "blogs", env, request);
    }

    /**
     * Entry page for blogs. Displays all available blogs
     * plus most fresh entries.
     */
    protected String processBlogSpace(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return FMTemplateSelector.select("ViewBlog", "blogspace", env, request);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String re = prefs.get(PREF_BLOG_URL, null);
        try {
            reUrl = new RE(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '"+re+"'!");
        }
    }
}
