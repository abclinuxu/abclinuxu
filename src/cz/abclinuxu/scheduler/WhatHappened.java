/*
 * User: literakl
 * Date: 28.8.2004
 * Time: 19:01:30
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditArticle;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.CompareCondition;
import cz.abclinuxu.persistance.extra.Field;
import cz.abclinuxu.persistance.extra.Operation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.News;
import cz.abclinuxu.data.view.Article;
import cz.abclinuxu.data.view.DiscussionHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.prefs.Preferences;
import java.util.*;
import java.text.MessageFormat;

import freemarker.template.Configuration;
import org.apache.log4j.Logger;

/**
 * Generates weekly summary article.
 */
public class WhatHappened extends TimerTask implements AbcAction, Configurable {
    static Logger log = Logger.getLogger(WhatHappened.class);

    public static final String PREF_TITLE = "title";
    public static final String PREF_PEREX = "perex";
    public static final String PREF_AUTHOR = "author";

    static String title, perex;
    static int author;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new WhatHappened());
    }

    static final String VAR_TITLE = "title";
    static final String VAR_ARTICLES = "ARTICLES";
    static final String VAR_NEWS = "NEWS";
    static final String VAR_QUESTIONS = "QUESTIONS";
    static final String VAR_DRIVERS = "DRIVERS";
    static final String VAR_CONCEPTS = "CONCEPTS";
    static final String VAR_HARDWARE = "HARDWARE";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        feedDataToMap(env);
        return FMTemplateSelector.select("WhatHappened", "content", env, request);
    }

    public void run() {
        try {
            Map map = new HashMap();
            feedDataToMap(map);
            String template = FMTemplateSelector.select("WhatHappened", "content", map, "print");
            String content = FMUtils.executeTemplate(template, map);

            Map params = new HashMap();
            map.put(Constants.VAR_PARAMS, params);
            Persistance persistance = PersistanceFactory.getPersistance();
            Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ACTUAL_ARTICLES));
            map.put(EditArticle.VAR_RELATION, articles);
            User articleAuthor = (User) persistance.findById(new User(author));
            map.put(Constants.VAR_USER, articleAuthor);
            params.put(EditArticle.PARAM_TITLE, map.get(VAR_TITLE));
            params.put(EditArticle.PARAM_AUTHOR, Integer.toString(author));
            params.put(EditArticle.PARAM_PEREX, map.get(PREF_PEREX));
            params.put(EditArticle.PARAM_PUBLISHED, Constants.isoFormat.format(new Date()));
            params.put(EditArticle.PARAM_CONTENT, content);

            EditArticle editArticle = new EditArticle();
            editArticle.actionAddStep2(null, null, map, false);
            Relation relation = (Relation) map.get(EditArticle.VAR_RELATION);
            Item article = (Item) relation.getChild();
            article.getData().getRootElement().addAttribute("do_not_index", "true");
            persistance.update(article);
        } catch (Exception e) {
            log.error("WhatHappened generation failed!", e);
        }
    }

    public static void main(String[] args) throws Exception {
        FMTemplateSelector.initialize("/home/literakl/abc/deploy/WEB-INF/conf/templates.xml");
        Configuration.getDefaultConfiguration().setSharedVariable(Constants.VAR_DATE_TOOL, new DateTool());
        new WhatHappened().run();
    }

    public void feedDataToMap(Map env) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        env.put(PREF_PEREX, perex);
        Integer week = new Integer(calendar.get(Calendar.WEEK_OF_YEAR));
        Integer year = new Integer(calendar.get(Calendar.YEAR));
        String computedTitle = MessageFormat.format(title, new Object[]{week,year});
        env.put(VAR_TITLE, computedTitle);
        setData(env, calendar.getTime(), new Date());
    }

    /**
     * Finds articles, news and other data and puts them into params.
     */
    private void setData(Map params, Date from, Date to) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Tools tools = new Tools();
        Item item;

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List relations = SQLTool.getInstance().findArticleRelationsWithinPeriod(from, to, qualifiers);
        List articles = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            item = (Item) persistance.findById(relation.getChild());
            String tmp = tools.xpath(item, "/data/author");
            Article article = new Article(tools.xpath(item, "data/name"), item.getCreated(), relation.getId());
            User author = tools.createUser(tmp);
            article.setAuthor(author.getName());
            article.setAuthorId(author.getId());
            article.setPerex(tools.xpath(item, "data/perex"));
            article.setComments(tools.findComments(item).getResponseCount());
            article.setReads(tools.getCounterValue(item));
            articles.add(article);
        }
        params.put(VAR_ARTICLES, articles);

        relations = SQLTool.getInstance().findNewsRelationsWithinPeriod(from, to, qualifiers);
        List news = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            item = (Item) persistance.findById(relation.getChild());
            News newz = new News(tools.xpath(item, "data/content"), item.getCreated(), relation.getId());
            User author = tools.createUser(item.getOwner());
            newz.setAuthor(author.getName());
            newz.setAuthorId(author.getId());
            newz.setComments(tools.findComments(item).getResponseCount());
            news.add(newz);
        }
        params.put(VAR_NEWS, news);

        Qualifier fromCondition = new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, from);
        Qualifier toCondition = new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, to);
        qualifiers = new Qualifier[]{fromCondition, toCondition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        relations = SQLTool.getInstance().findDiscussionRelations(qualifiers);
        List dizs = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            tools.sync(relation);
            DiscussionHeader diz = tools.analyzeDiscussion(relation);
            dizs.add(diz);
        }
        params.put(VAR_QUESTIONS, dizs);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        title = prefs.get(PREF_TITLE, null);
        perex = prefs.get(PREF_PEREX, null);
        author = prefs.getInt(PREF_AUTHOR, 1);
    }
}
