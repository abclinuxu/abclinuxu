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

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.format.HtmlToTextFormatter;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.JobOfferManager;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.News;
import cz.abclinuxu.data.view.Article;
import cz.abclinuxu.servlets.Constants;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * Sends weekly email to every user, who has subscribed this channel.
 */
public class WeeklyEmail extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WeeklyEmail.class);

    public static final String PREF_SUBJECT = "subject";
    public static final String PREF_SENDER = "from";
    public static final String PREF_TEMPLATE = "template";

    public static final String VAR_ARTICLES = "ARTICLES";
    public static final String VAR_NEWS = "NEWS";
    public static final String VAR_JOBS = "JOBS";
    public static final String VAR_WEEK = "WEEK";
    public static final String VAR_YEAR = "YEAR";

    String subject, sender, template;

    /**
     * Default constructor.
     */
    public WeeklyEmail() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void run() {
        try {
            Calendar calendar = Calendar.getInstance();
            int week = calendar.get(Calendar.WEEK_OF_YEAR);
            int year = calendar.get(Calendar.YEAR);

            Map params = new HashMap();
            params.put(EmailSender.KEY_FROM,sender);
            params.put(EmailSender.KEY_SUBJECT,subject+" "+week+"/"+year);
            params.put(EmailSender.KEY_TEMPLATE,template);
            params.put(Constants.VAR_TOOL,new Tools());
            params.put(Constants.VAR_DATE_TOOL,new DateTool());
            params.put(VAR_WEEK,new Integer(week));
            params.put(VAR_YEAR,new Integer(year));

            pushData(params);

            log.info("Time to send weekly emails. Let's find subscribed users first.");
            List users = SQLTool.getInstance().findUsersWithWeeklyEmail(null);
            log.info("Weekly emails have subscribed "+users.size()+" users.");
            int count = EmailSender.sendEmailToUsers(params,users);
            log.info("Weekly email sucessfully sent to "+count+" addressses.");
        } catch (Exception e) {
            log.warn("Cannot sent weekly emails!",e);
        }
    }

    /**
     * Stores articles and news in params.
     */
    private void pushData(Map params) {
        SQLTool sqlTool = SQLTool.getInstance();
        HtmlToTextFormatter formatter = new HtmlToTextFormatter();
        Item item;
        String title, content;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        Date from = calendar.getTime();
        Date until = new Date();

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List relations = sqlTool.findArticleRelationsWithinPeriod(from, until, qualifiers);
        Tools.syncList(relations);
        List articles = new ArrayList(relations.size());

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            item = (Item) relation.getChild();
            title = Tools.xpath(item, "data/name");
            Article article = new Article(title, item.getCreated(), relation.getUrl());
            String tmp = Tools.xpath(item, "/data/author");
            article.setAuthor(Tools.createUser(tmp).getName());
            article.setPerex(Tools.xpath(item, "data/perex"));
            article.setComments(Tools.findComments(item).getResponseCount());
            articles.add(article);
        }

        relations = sqlTool.findNewsRelationsWithinPeriod(from, until, qualifiers);
        Tools.syncList(relations);
        List news = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            item = (Item) relation.getChild();
            title = Tools.xpath(item, "data/title");
            content = Tools.xpath(item, "data/content");
            content = formatter.format(content);
            News newz = new News(title, content, item.getCreated(), relation.getUrl());
            newz.setAuthor(Tools.createUser(item.getOwner()).getName());
            newz.setComments(Tools.findComments(item).getResponseCount());
            news.add(newz);
        }

        List offers = JobOfferManager.getOffersAfter(from);

        params.put(VAR_ARTICLES, articles);
        params.put(VAR_NEWS, news);
        params.put(VAR_JOBS, offers);
    }

    /**
     * Configures this instance.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT, null);
        sender = prefs.get(PREF_SENDER, null);
        template = prefs.get(PREF_TEMPLATE, null);
    }

    /**
     * mainly for debug purposes
     * @param args
     */
    public static void main(String[] args) {
        JobOfferManager jobs = new JobOfferManager();
        jobs.run();
        WeeklyEmail weeklyEmail = new WeeklyEmail();
        weeklyEmail.run();
    }
}
