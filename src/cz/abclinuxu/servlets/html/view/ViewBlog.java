/*
 * User: literakl
 * Date: 24.11.2004
 * Time: 7:13:32
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.persistance.extra.*;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.exceptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import freemarker.ext.dom.NodeModel;

/**
 * Display blogs.
 */
public class ViewBlog implements AbcAction, Configurable {
    public static final String PARAM_FROM = "from";

    public static final String VAR_BLOG_RELATION = "REL_BLOG";
    public static final String VAR_BLOG = "BLOG";
    public static final String VAR_BLOGS = "BLOGS";
    public static final String VAR_BLOG_XML = "BLOG_XML";
    public static final String VAR_STORIES = "STORIES";
    public static final String VAR_STORY = "STORY";
    public static final String VAR_YEAR = "YEAR";
    public static final String VAR_MONTH = "MONTH";
    public static final String VAR_DAY = "DAY";

    static final String PREF_BLOG_URL = "regexp.blog.url";
    static final String PREF_PAGE_SIZE = "page.size";

    static REProgram reUrl;
    /** default number of stories per single page */
    static int defaultPageSize;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new ViewBlog());
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation blogRelation = null;
        int year, month, day, rid;
        year = month = day = rid = 0;
        String name = null;

        String uri = (String) env.get(Constants.VAR_REQUEST_URI);
        if (uri.startsWith("/blogy"))
            return processBlogs(request, env);

//        uri = "/blog/Yeti/2004/12/12/124545";
        RE regexp = new RE(reUrl);
        regexp.match(uri);
        int matched = regexp.getParenCount();
        if (matched > 1)
            name = regexp.getParen(1);
        if (matched > 2) {
            year = Integer.parseInt(regexp.getParen(2));
            env.put(VAR_YEAR, new Integer(year));
        }
        if (matched > 3) {
            month = Integer.parseInt(regexp.getParen(3));
            env.put(VAR_MONTH, new Integer(month));
        }
        if (matched > 4) {
            day = Integer.parseInt(regexp.getParen(4));
            env.put(VAR_DAY, new Integer(day));
        }
        if (matched > 5)
            rid = Integer.parseInt(regexp.getParen(5));

        if (name!=null) {
            CompareCondition condition = new CompareCondition(Field.SUBTYPE, Operation.EQUAL, name);
            SQLTool sqlTool = SQLTool.getInstance();
            List list = sqlTool.findSectionRelationsWithType(Category.BLOG, new Qualifier[]{condition});
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
            env.put(VAR_BLOG_XML, NodeModel.wrap((new DOMWriter().write(blog.getData()))));

            if (rid!=0)
                return processStory(blogRelation, rid, request, env);
            else
                return processStories(blogRelation, year, month, day, request, env);
        } else
            return processBlogSpace(request, year, month, day, env);
    }

    /**
     * Displays one blogRelation content. Its stories may be limited to given year, month or day.
     */
    protected String processStory(Relation blogRelation, int rid, HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) persistance.findById(new Relation(rid));
        Tools.sync(relation);
        Item story = (Item) relation.getChild();
        env.put(VAR_STORY, relation);

        User user = (User) env.get(Constants.VAR_USER);
        if (user==null || user.getId()!=story.getOwner())
            persistance.incrementCounter(story);

        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        return FMTemplateSelector.select("ViewBlog", "story", env, request);
    }

    /**
     * Displays one blogRelation content. Its stories may be limited to given year, month or day.
     */
    protected String processStories(Relation blogRelation, int year, int month, int day, HttpServletRequest request, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Persistance persistance = PersistanceFactory.getPersistance();

        Category blog = (Category) blogRelation.getChild();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Element element = (Element) blog.getData().selectSingleNode("//settings/page_size");
        int count = Misc.parseInt((element!=null)? element.getText():null, defaultPageSize);

        List qualifiers = new ArrayList();
        qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL,new Integer(blog.getOwner())));
        addTimeLimitsFQ(year, month, day, qualifiers);

        Qualifier[] qa = new Qualifier[qualifiers.size()];
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int total = sqlTool.countItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));

        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, count));

        qa = new Qualifier[qualifiers.size()];
        List stories = sqlTool.findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        Tools.syncList(stories);

        Paging paging = new Paging(stories, from, count, total);
        env.put(VAR_STORIES, paging);

        User user = (User) env.get(Constants.VAR_USER);
        if (user==null || user.getId()!=blog.getOwner()) {
            for (Iterator iter = stories.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Item story = (Item) relation.getChild();
                if (Tools.xpath(story,"/data/perex")==null)
                    persistance.incrementCounter(story);
            }
        }

        List parents = persistance.findParents(blogRelation);
        env.put(ShowObject.VAR_PARENTS, parents);

        return FMTemplateSelector.select("ViewBlog", "blog", env, request);
    }

    /**
     * Entry page for blogs. Displays most fresh stories across all blogs.
     */
    protected String processBlogSpace(HttpServletRequest request, int year, int month, int day, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();
        Persistance persistance = PersistanceFactory.getPersistance();

        List qualifiers = new ArrayList();
        addTimeLimitsFQ(year, month, day, qualifiers);

        Qualifier[] qa = new Qualifier[qualifiers.size()];
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int total = sqlTool.countItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));

        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, defaultPageSize));

        qa = new Qualifier[qualifiers.size()];
        List stories = sqlTool.findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        for (Iterator iter = stories.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            if (!relation.getChild().isInitialized())
                persistance.synchronize(relation.getChild());
            if (!relation.getParent().isInitialized())
                persistance.synchronize(relation.getParent());
        }

        Paging paging = new Paging(stories, from, defaultPageSize, total);
        env.put(VAR_STORIES, paging);

        User user = (User) env.get(Constants.VAR_USER);
        for (Iterator iter = stories.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            Item story = (Item) relation.getChild();
            if (user==null || user.getId()!=story.getOwner()) {
                if (Tools.xpath(story,"/data/perex")==null)
                    persistance.incrementCounter(story);
            }
        }

        Relation relation = (Relation) persistance.findById(new Relation(Constants.REL_BLOGS));
        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        return FMTemplateSelector.select("ViewBlog", "blogspace", env, request);
    }

    /**
     * Displays all available blogs
     * todo paging, sort by number of stories
     */
    protected String processBlogs(HttpServletRequest request, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation;
        Category blog;
        User author;
        Map map;
        List months;

        List blogs = sqlTool.findSectionRelationsWithType(Category.BLOG, null);
        List result = new ArrayList(blogs.size());
        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            blog = (Category) persistance.findById(relation.getChild());
            author = (User) persistance.findById(new User(blog.getOwner()));

            int count = 0;
            months = blog.getData().selectNodes("//archive/year/month");
            for (Iterator iter2 = months.iterator(); iter2.hasNext();) {
                Element month = (Element) iter2.next();
                count += Misc.parseInt(month.getText(), 0);
            }

            map = new HashMap();
            map.put("blog", blog);
            map.put("author", author);
            map.put("stories", new Integer(count));
            result.add(map);
        }
        env.put(VAR_BLOGS, result);

        return FMTemplateSelector.select("ViewBlog", "blogs", env, request);
    }

    /**
     * Appends findQualifiers restricting blogs to selected time range.
     * @param year 0 if not set
     * @param month 0 if not set
     * @param day 0 if not set
     * @param findQualifiers
     */
    protected void addTimeLimitsFQ(int year, int month, int day, List findQualifiers) {
        if (year==0)
            return;
        Calendar start = Calendar.getInstance(), end = Calendar.getInstance();
        start.clear();end.clear();

        // 2004,0,0 => 1.1.2004 - 1.1.2005
        // 2004,12,0 => 1.12.2004 - 1.1.2005
        // 2004,12,31 => 31.12.2004 - 1.1.2005
        start.set(Calendar.YEAR, year);
        end.set(Calendar.YEAR, year);
        if (month==0) {
            end.add(Calendar.YEAR, 1);
        } else {
            start.set(Calendar.MONTH, month-1);
            end.set(Calendar.MONTH, month-1);

            if (day==0) {
                end.add(Calendar.MONTH, 1);
            } else {
                start.set(Calendar.DAY_OF_MONTH, day);
                end.set(Calendar.DAY_OF_MONTH, day);
            }
        }

        Date startDate = start.getTime();
        Date endDate = end.getTime();
        findQualifiers.add(new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, startDate));
        findQualifiers.add(new CompareCondition(Field.CREATED, Operation.SMALLER, endDate));
    }

    /**
     * default number of stories per single page
     */
    public static int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        defaultPageSize = prefs.getInt(PREF_PAGE_SIZE, 10);
        String re = prefs.get(PREF_BLOG_URL, null);
        try {
            reUrl = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '"+re+"'!");
        }
    }
}
