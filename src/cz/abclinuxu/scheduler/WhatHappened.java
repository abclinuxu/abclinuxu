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
package cz.abclinuxu.scheduler;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Article;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.data.view.News;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.CompareCondition;
import cz.abclinuxu.persistance.extra.Field;
import cz.abclinuxu.persistance.extra.Operation;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.JobOfferManager;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditArticle;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Generates weekly summary article.
 */
public class WhatHappened extends TimerTask implements AbcAction, Configurable {
    static Logger log = Logger.getLogger(WhatHappened.class);
    /** article ahving this article must not be indexed for fulltext search */
    public static final String INDEXING_FORBIDDEN = "do_not_index";

    public static final String PREF_TITLE = "title";
    public static final String PREF_PEREX = "perex";
    public static final String PREF_AUTHOR = "author";
    public static final String PREF_SECTION = "section";

    static String title, perex;
    static int author, sectionRid;
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
    static final String VAR_JOBS = "JOBS";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        feedDataToMap(env);
        return FMTemplateSelector.select("WhatHappened", "content", env, request);
    }

    public void run() {
        try {
            log.info("Generating Weekly summary article");
            Map map = new HashMap();
            feedDataToMap(map);
            String template = FMTemplateSelector.select("WhatHappened", "content", map, "print");
            String content = FMUtils.executeTemplate(template, map);

            Map params = new HashMap();
            map.put(Constants.VAR_PARAMS, params);
            Persistance persistance = PersistanceFactory.getPersistance();
            Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ARTICLEPOOL));
            map.put(EditArticle.VAR_RELATION, articles);
            User articleAuthor = (User) persistance.findById(new User(author));
            map.put(Constants.VAR_USER, articleAuthor);
            params.put(EditArticle.PARAM_TITLE, map.get(VAR_TITLE));
            params.put(EditArticle.PARAM_AUTHOR, Integer.toString(author));
            params.put(EditArticle.PARAM_PEREX, map.get(PREF_PEREX));
            params.put(EditArticle.PARAM_DESIGNATED_SECTION, Integer.toString(sectionRid));
            synchronized (Constants.isoFormat) {
                params.put(EditArticle.PARAM_PUBLISHED, Constants.isoFormat.format(new Date()));
            }
            params.put(EditArticle.PARAM_CONTENT, content);

            EditArticle editArticle = new EditArticle();
            editArticle.actionAddStep2(null, null, map, false);
            Relation relation = (Relation) map.get(EditArticle.VAR_RELATION);
            Item article = (Item) relation.getChild();
            article.getData().getRootElement().addAttribute(INDEXING_FORBIDDEN, "true");
            persistance.update(article);
            log.info("Weekly summary article finished");
        } catch (Exception e) {
            log.error("WhatHappened generation failed!", e);
        }
    }

    public static void main(String[] args) throws Exception {
        FMTemplateSelector.initialize("/home/literakl/abc/deploy/WEB-INF/conf/templates.xml");
        FMUtils.getConfiguration().setSharedVariable(Constants.VAR_DATE_TOOL, new DateTool());
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
        SQLTool sqlTool = SQLTool.getInstance();
        Item item;
        String title, content;

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List relations = sqlTool.findArticleRelationsWithinPeriod(from, to, qualifiers);
        Tools.syncList(relations);
        List articles = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            item = (Item) relation.getChild();
            String tmp = Tools.xpath(item, "/data/author");
            title = Tools.xpath(item, "data/name");
            Article article = new Article(title, item.getCreated(), relation.getUrl());
            User author = Tools.createUser(tmp);
            article.setAuthor(author.getName());
            article.setAuthorId(author.getId());
            article.setPerex(Tools.xpath(item, "data/perex"));
            article.setComments(Tools.findComments(item).getResponseCount());
            article.setReads(Tools.getCounterValue(item, Constants.COUNTER_READ));
            article.setUrl(relation.getUrl());
            articles.add(article);
        }
        params.put(VAR_ARTICLES, articles);

        relations = sqlTool.findNewsRelationsWithinPeriod(from, to, qualifiers);
        Tools.syncList(relations);
        List news = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            item = (Item) relation.getChild();
            title = Tools.xpath(item, "data/title");
            content = Tools.xpath(item, "data/content");
            News newz = new News(title, content, item.getCreated(), relation.getUrl());
            User author = Tools.createUser(item.getOwner());
            newz.setAuthor(author.getName());
            newz.setAuthorId(author.getId());
            newz.setComments(Tools.findComments(item).getResponseCount());
            newz.setUrl(relation.getUrl());
            news.add(newz);
        }
        params.put(VAR_NEWS, news);

        Qualifier fromCondition = new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, from);
        Qualifier toCondition = new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, to);
        qualifiers = new Qualifier[]{fromCondition, toCondition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        relations = sqlTool.findDiscussionRelations(qualifiers);
        Tools.syncList(relations);
        List dizs = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            DiscussionHeader diz = Tools.analyzeDiscussion(relation);
            dizs.add(diz);
        }
        params.put(VAR_QUESTIONS, dizs);

        List offers = JobOfferManager.getOffersAfter(from);
        params.put(VAR_JOBS, offers);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        title = prefs.get(PREF_TITLE, null);
        perex = prefs.get(PREF_PEREX, null);
        author = prefs.getInt(PREF_AUTHOR, 1);
        sectionRid = prefs.getInt(PREF_SECTION, 251);
    }
}
