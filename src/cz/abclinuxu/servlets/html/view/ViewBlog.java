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
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import freemarker.ext.dom.NodeModel;

/**
 * Display blogs.
 */
public class ViewBlog implements AbcAction, Configurable {
    private static Logger log = Logger.getLogger(ViewBlog.class);

    public static final String PARAM_FROM = "from";

    public static final String VAR_BLOG_RELATION = "REL_BLOG";
    public static final String VAR_BLOG = "BLOG";
    public static final String VAR_BLOGS = "BLOGS";
    public static final String VAR_BLOG_XML = "BLOG_XML";
    public static final String VAR_SUMMARY = "SUMMARY";
    public static final String VAR_STORIES = "STORIES";
    public static final String VAR_STORY = "STORY";
    public static final String VAR_YEAR = "YEAR";
    public static final String VAR_MONTH = "MONTH";
    public static final String VAR_DAY = "DAY";
    public static final String VAR_CURRENT_STORIES = "CURRENT_STORIES";
    public static final String VAR_UNPUBLISHED_STORIES = "UNPUBLISHED_STORIES";
    public static final String VAR_ARCHIVE = "ARCHIVE";
    public static final String VAR_DIGEST = "DIGEST";
    public static final String VAR_RELATION = "RELATION";

    static final String PREF_BLOG_URL = "regexp.blog.url";
    static final String PREF_SUMMARY_URL = "regexp.blog.summary.url";
    static final String PREF_ARCHIVE_URL = "regexp.blog.archive.url";
    static final String PREF_EXPORT_URL = "regexp.blog.export.url";
    static final String PREF_DIGEST_URL = "regexp.blog.digest.url";
    static final String PREF_PAGE_SIZE = "page.size";
    static final String PREF_SUMMARY_SIZE = "summary.size";
    static final String PREF_FRESH_STORIES_SIZE = "fresh.stories.size";
    static final String PREF_URL_SUMMARY = "url.summary";

    static REProgram reUrl, reSummaryUrl, reArchiveUrl, reDigestUrl, reExportUrl;
    static int defaultPageSize, freshStoriesSize, summarySize;
    static String urlSummary;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new ViewBlog());
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation blogRelation = null, relation = null;
        int year, month, day, rid;
        year = month = day = rid = 0;
        String name = null;
        boolean summary = false, archive = false, digest = false, export = false;

        String uri = (String) env.get(Constants.VAR_REQUEST_URI);
        if (uri.startsWith("/blogy"))
            return processBlogs(request, env);

        RE regexp = new RE(reSummaryUrl);
        if (regexp.match(uri)) { // uri = "/blog/jmeno/souhrn";
            summary = true;
            env.put(VAR_SUMMARY, Boolean.TRUE);
            int matched = regexp.getParenCount();
            if (matched>4)
                name = regexp.getParen(4);
        } else {
            regexp = new RE(reDigestUrl);
            if (regexp.match(uri)) { // uri = "/blog/vyber";
                digest = true;
                env.put(VAR_DIGEST, Boolean.TRUE);
            } else {
                RE regexpExport = new RE(reExportUrl);
                regexp = new RE(reArchiveUrl);
                if (regexp.match(uri)) { // uri = "/blog/jmeno/archiv";
                    archive = true;
                    name = regexp.getParen(1);
                } else if (regexpExport.match(uri)) { // uri = "/blog/jmeno/export"
                    export = true;
                    name = regexpExport.getParen(1);
                } else { // uri = "/blog/jmeno/2004/12/12/124545";
                    regexp = new RE(reUrl);
                    regexp.match(uri);
                    int matched = regexp.getParenCount();
                    try {
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
                    } catch (NumberFormatException e) {
                        log.warn(uri, e);
                    }
                }
            }
        }

        Category blog = null;
        if (rid != 0) {
            // ochrana pred hratky s URL
            relation = new Relation(rid);
            Tools.sync(relation);
            GenericObject child = relation.getChild();
            if (! (child instanceof Item))
                throw new InvalidInputException("Tato relace nepatří blogu!");
            int type = ((Item) child).getType();
            if (type != Item.BLOG && type != Item.UNPUBLISHED_BLOG)
                throw new InvalidInputException("Tato relace nepatří blogu!");

            env.put(VAR_RELATION, relation);

            blog = (Category) relation.getParent();
            blogRelation = (Relation) persistence.findById(new Relation(relation.getUpper()));
        } else {
            if (name != null) {
                CompareCondition condition = new CompareCondition(Field.SUBTYPE, Operation.EQUAL, name);
                SQLTool sqlTool = SQLTool.getInstance();
                List list = sqlTool.findCategoryRelationsWithType(Category.BLOG, new Qualifier[]{condition});
                if (list.size() == 0)
                    throw new NotFoundException("Blog "+name+" nebyl nalezen!");

                blogRelation = (Relation) list.get(0);
                blog = (Category) blogRelation.getChild();

                env.put(VAR_RELATION, blogRelation);
            }
        }

        if (blogRelation!=null) {
            blog = (Category) persistence.findById(blog);
            if (blog.getType() != Category.BLOG)
                throw new InvalidInputException("Tato relace nepatří blogu!");

            blogRelation.setChild(blog);
            env.put(VAR_BLOG_RELATION, blogRelation);
            env.put(VAR_BLOG, blog);
            env.put(VAR_BLOG_XML, NodeModel.wrap((new DOMWriter().write(blog.getData()))));

            if (export)
                return processExport(request, env, blog);

            fillFreshStories(blog, env);
            if (! archive)
                fillMonthStatistics(blog, env);
            if (user != null && user.getId() == blog.getOwner())
                fillUnpublishedStories(blog, env);

            if (relation != null)
                return processStory(request, relation, env);
            else if (archive)
                return processArchive(request, blog, env);
            else
                return processStories(request, blogRelation, summary, year, month, day, env);
        } else
            return processBlogSpace(request, summary, digest, year, month, day, env);
    }

    /**
     * Displays one blogRelation content. Its stories may be limited to given year, month or day.
     */
    public static String processStory(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Category blog = (Category) persistence.findById(relation.getParent());
        Item story = (Item) relation.getChild();
        env.put(VAR_STORY, relation);

        User user = (User) env.get(Constants.VAR_USER);
        if (user == null || user.getId() != story.getOwner())
            ReadRecorder.log(story, Constants.COUNTER_READ, env);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        env.put(Constants.VAR_RSS, FeedGenerator.getBlogFeedUrl(blog));
        return FMTemplateSelector.select("ViewBlog", "story", env, request);
    }

    /**
     * Displays one blogRelation content. Its stories may be limited to given year, month or day.
     */
    protected String processArchive(HttpServletRequest request, Category blog, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation blogRelation = (Relation) env.get(VAR_BLOG_RELATION);
        List parents = persistence.findParents(blogRelation);
        env.put(ShowObject.VAR_PARENTS, parents);
        env.put(Constants.VAR_RSS, FeedGenerator.getBlogFeedUrl(blog));
        return FMTemplateSelector.select("ViewBlog", "archive", env, request);
    }

    protected String processExport(HttpServletRequest request, Map env, Category blog) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        SQLTool sqlTool = SQLTool.getInstance();

        // verify permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if (user.getId() != blog.getOwner() && !user.hasRole(Roles.ROOT))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        Qualifier[] qa = new Qualifier[]{ new CompareCondition(Field.OWNER, Operation.EQUAL, blog.getOwner()), Qualifier.SORT_BY_CREATED };
        List stories = sqlTool.findItemRelationsWithType(Item.BLOG, qa);
        Tools.syncList(stories);

        env.put(VAR_STORIES, stories);
        fillUnpublishedStories(blog, env);
        env.put(Constants.VAR_CONTENT_TYPE, "text/plain; charset=UTF-8");
        return FMTemplateSelector.select("ViewBlog", "export", env, request);
    }

    /**
     * Displays one blogRelation content. Its stories may be limited to given year, month or day.
     */
    protected String processStories(HttpServletRequest request, Relation blogRelation, boolean summary, int year, int month, int day, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();
        Persistence persistence = PersistenceFactory.getPersistence();

        Category blog = (Category) env.get(VAR_BLOG);
        int count = 0;
        if (summary)
            count = summarySize;
        else {
            Element element = (Element) blog.getData().selectSingleNode("//settings/page_size");
            count = Misc.parseInt((element != null) ? element.getText() : null, defaultPageSize);
        }

        List qualifiers = new ArrayList();
        qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL, blog.getOwner()));
        if (!summary)
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

        if (!summary) {
            User user = (User) env.get(Constants.VAR_USER);
            if (user==null || user.getId()!=blog.getOwner()) {
                for (Iterator iter = stories.iterator(); iter.hasNext();) {
                    Relation relation = (Relation) iter.next();
                    Item story = (Item) relation.getChild();
                    if (Tools.xpath(story,"/data/perex")==null)
                        ReadRecorder.log(story, Constants.COUNTER_READ, env);
                }
            }
        }

        List parents = persistence.findParents(blogRelation);
        env.put(ShowObject.VAR_PARENTS, parents);

        env.put(Constants.VAR_RSS, FeedGenerator.getBlogFeedUrl(blog));
        return FMTemplateSelector.select("ViewBlog", "blog", env, request);
    }

    /**
     * Entry page for blogs. Displays most fresh stories across all blogs.
     */
    protected String processBlogSpace(HttpServletRequest request, boolean summary, boolean digest, int year, int month, int day, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation;
        Item story;

        List qualifiers = new ArrayList();
        if (!summary)
            addTimeLimitsFQ(year, month, day, qualifiers);

        Set<String> values = Collections.singleton("yes");
        Map<String, Set<String>> filters = Collections.singletonMap(Constants.PROPERTY_BLOG_DIGEST, values);

        Qualifier[] qa = new Qualifier[qualifiers.size()];
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int total = -1;
        if (digest)
            total = sqlTool.countItemRelationsWithTypeWithFilters(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa), filters);
        else
            total = sqlTool.countItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        int count = (summary) ? summarySize : defaultPageSize;

        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, Tools.getPreloadedStoryCount(count) ));

        qa = new Qualifier[qualifiers.size()];
        List stories = null;
        if (digest)
            stories = sqlTool.findItemRelationsWithTypeWithFilters(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa), filters);
        else
            stories = sqlTool.findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        Tools.syncList(stories);

        // filter out stories written by users that current user has blocked
        if (user != null)
            stories = Tools.filterBannedStories(stories, user, count, false);

        List parentBlogs = new ArrayList(stories.size());
        for (Iterator iter = stories.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            if (!relation.getParent().isInitialized())
                parentBlogs.add(relation.getParent());
        }
        if (parentBlogs.size()>0)
            Tools.syncList(parentBlogs);

        Paging paging = new Paging(stories, from, stories.size(), total);
        env.put(VAR_STORIES, paging);

        if (!summary) {
            for (Iterator iter = stories.iterator(); iter.hasNext();) {
                relation = (Relation) iter.next();
                story = (Item) relation.getChild();
                if (user == null || user.getId() != story.getOwner()) {
                    if (Tools.xpath(story, "/data/perex") == null)
                        ReadRecorder.log(story, Constants.COUNTER_READ, env);
                }
            }
        }

        relation = (Relation) persistence.findById(new Relation(Constants.REL_BLOGS));
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        env.put(Constants.VAR_RSS, FeedGenerator.getBlogsFeedUrl());
        return FMTemplateSelector.select("ViewBlog", "blogspace", env, request);
    }

    /**
     * Displays all available blogs
     * todo paging, sort by number of stories
     */
    protected String processBlogs(HttpServletRequest request, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation;
        Category blog;
        User author;
        Map map;
        List months;

        List blogs = sqlTool.findCategoryRelationsWithType(Category.BLOG, null);
        Tools.syncList(blogs);
        List users = new ArrayList(blogs.size());
        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            blog = (Category) relation.getChild();
            users.add(new User(blog.getOwner()));
        }
        Tools.syncList(users);

        List result = new ArrayList(blogs.size());
        int i = 0;
        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            blog = (Category) relation.getChild();
            author = (User) users.get(i++);
            if (author.getId()!=blog.getOwner()) {
                User owner = new User(blog.getOwner());
                int j = users.indexOf(owner);
                if (j != -1)
                    author = (User) users.get(j);
                else
                    author = (User) persistence.findById(owner);
            }

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

        env.put(Constants.VAR_RSS, FeedGenerator.getBlogsFeedUrl());
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
                end.set(Calendar.DAY_OF_MONTH, day+1);
            }
        }

        Date startDate = start.getTime();
        Date endDate = end.getTime();
        findQualifiers.add(new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, startDate));
        findQualifiers.add(new CompareCondition(Field.CREATED, Operation.SMALLER, endDate));
    }

    /**
     * Stores into env list of fresh stories from selected blog.
     * @param blog
     * @param env
     */
    private void fillFreshStories(Category blog, Map env) {
        List qualifiers = new ArrayList();
        qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL, new Integer(blog.getOwner())));
        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(0, freshStoriesSize));
        Qualifier[] qa = new Qualifier[qualifiers.size()];

        SQLTool sqlTool = SQLTool.getInstance();
        List currentStories = sqlTool.findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        Tools.syncList(currentStories);
        env.put(VAR_CURRENT_STORIES, currentStories);
    }

    /**
     * Puts into env list of unpublished stories, if there are any.
     * The list will contain initialized story relations.
     * @param blog
     */
    private void fillUnpublishedStories(Category blog, Map env) {
        List elements = blog.getData().selectNodes("/data/unpublished/rid");
        if (elements==null || elements.size()==0)
            return;

        List stories = new ArrayList(elements.size());
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            int rid = Integer.parseInt(element.getTextTrim());
            stories.add(new Relation(rid));
        }
        Tools.syncList(stories);
        env.put(VAR_UNPUBLISHED_STORIES, stories);
    }

    /**
     * Puts into env information about last six months, when user submitted
     * some stories.
     */
    private void fillMonthStatistics(Category blog, Map env) {
        List archive = new ArrayList(6);
        env.put(VAR_ARCHIVE, archive);
        Element element;
        int year, month, size;
        String  tmp;

        List months = blog.getData().selectNodes("/data/archive/year/month");
        for (int i = months.size(), j = 0; i > 0 && j < 6; i--, j++) {
            element = (Element) months.get(i - 1);
            tmp = element.getParent().attributeValue("value");
            year = Misc.parseInt(tmp, 0);
            tmp = element.attributeValue("value");
            month = Misc.parseInt(tmp, 0);
            tmp = element.getText();
            size = Misc.parseInt(tmp, 0);
            archive.add(new ArchiveItem(year, month, size));
        }
    }

    /**
     * default number of stories per single page
     */
    public static int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        // TODO k cemu tato hodnota je? Nevidim zadne pouziti
        urlSummary = prefs.get(PREF_URL_SUMMARY, null);
        if (urlSummary==null)
            throw new ConfigurationException("Chybí nastavení "+PREF_URL_SUMMARY);

        defaultPageSize = prefs.getInt(PREF_PAGE_SIZE, 10);
        summarySize = prefs.getInt(PREF_SUMMARY_SIZE, 20);
        freshStoriesSize = prefs.getInt(PREF_FRESH_STORIES_SIZE, 5);

        String re = prefs.get(PREF_BLOG_URL, null);
        try {
            reUrl = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '"+re+"'!");
        }
        re = prefs.get(PREF_SUMMARY_URL, null);
        try {
            reSummaryUrl = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '"+re+"'!");
        }
        re = prefs.get(PREF_ARCHIVE_URL, null);
        try {
            reArchiveUrl = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '"+re+"'!");
        }
        re = prefs.get(PREF_EXPORT_URL, null);
        try {
            reExportUrl = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '"+re+"'!");
        }
        re = prefs.get(PREF_DIGEST_URL, null);
        try {
            reDigestUrl = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '"+re+"'!");
        }
    }

    /**
     * Helper class for view to store info about number of stories in certain month of year.
     */
    public static class ArchiveItem {
        int year, month, size;

        public ArchiveItem(int year, int month, int size) {
            this.year = year;
            this.month = month;
            this.size = size;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getSize() {
            return size;
        }
    }
}
