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
import cz.abclinuxu.utils.email.MailSession;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.JobOfferManager;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.News;
import cz.abclinuxu.data.view.Article;
import cz.abclinuxu.servlets.Constants;

import java.util.*;
import java.util.prefs.Preferences;
import java.io.IOException;

import org.apache.commons.mail.HtmlEmail;
import org.dom4j.Element;

import javax.mail.internet.MimeMessage;
import javax.mail.Session;

import freemarker.template.TemplateException;

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

    String subject;
    int count;

    /**
     * Default constructor.
     */
    public WeeklyEmail() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void run() {
        try {
            log.info("Time to send weekly emails. Let's find subscribed users first.");
            Calendar calendar = getWeekStart();
            int week = calendar.get(Calendar.WEEK_OF_YEAR);
            int year = calendar.get(Calendar.YEAR);

            Map params = new HashMap();
            params.put(EmailSender.KEY_SUBJECT, subject + " " + week + "/" + year);
            params.put(EmailSender.KEY_STATS_KEY, Constants.EMAIL_WEEKLY);

            params.put(Constants.VAR_TOOL, new Tools());
            params.put(Constants.VAR_DATE_TOOL, new DateTool());

            prepareData(params, calendar);

            List<Integer> users = SQLTool.getInstance().findUsersWithWeeklyEmail(), workingSet;
            List<MimeMessage> messages = new ArrayList<MimeMessage>(50);
            log.info("Weekly emails have been subscribed by " + users.size() + " users.");
            MailSession mailSession = EmailSender.openSession();

            for (int i = 0, total = users.size(); i < total;) {
                workingSet = Tools.sublist(users, i, 50);
                i += workingSet.size();
                List<User> userObjects = InstanceUtils.createUsers(workingSet);
                for (User user : userObjects) {
                    try {
                        MimeMessage message = generateMessage(user, params, mailSession.getSession());
                        messages.add(message);
                    } catch (Exception e) {
                        log.error("Failed to create the message for user " + user.getLogin(), e);
                    }
                }

                count = EmailSender.sendEmailToUsers(messages, params, mailSession);
                messages.clear();
            }

            EmailSender.closeSession(mailSession);
            log.info("Weekly email sucessfully sent to " + count + " addressses.");
        } catch (Exception e) {
            log.warn("Cannot sent weekly emails!",e);
        }
    }

    public MimeMessage generateMessage(User user, Map params, Session session) throws Exception {
        params.put(Constants.VAR_USER, user);

        HtmlEmail email = new HtmlEmail();
        email.setMailSession(session);
        email.setCharset("UTF-8");
        email.setFrom(EmailSender.getDefaultFrom());
        email.addTo(user.getEmail());

        Persistence persistence = PersistenceFactory.getPersistence();
        Item item = (Item) persistence.findById(new Item(Constants.ITEM_WEEKLY_SUMMARY_EMAIL));
        Element root = item.getData().getRootElement();

        params.put(Constants.VAR_USER, user);
        String textVariant = processPlainTextVariant(root, params);
        email.setTextMsg(textVariant);

        String htmlVariant = processHtmlVariant(root, params);
        EmailSender.processHtmlEmail(email, htmlVariant);
        email.buildMimeMessage();
        return email.getMimeMessage();
    }

    public static String processHtmlVariant(Element root, Map env) throws IOException, TemplateException {
        String fmCode = root.elementText("html");
        if (fmCode == null)
            return "";
        else
            return FMUtils.executeCode(fmCode, env);
    }

    public static String processPlainTextVariant(Element root, Map env) throws IOException, TemplateException {
        String fmCode = root.elementText("text");
        if (fmCode == null)
            return "";
        else
            return FMUtils.executeCode(fmCode, env);
    }

    /**
     * Stores articles and news in params.
     */
    public static void prepareData(Map params, Calendar startDate) {
        SQLTool sqlTool = SQLTool.getInstance();
        Item item;
        String title, content;
        Date from = startDate.getTime(), until = new Date();
        int week = startDate.get(Calendar.WEEK_OF_YEAR);
        int year = startDate.get(Calendar.YEAR);

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List<Relation> relations = sqlTool.findArticleRelationsWithinPeriod(from, until, qualifiers);
        Tools.syncList(relations);
        List articles = new ArrayList(relations.size());

        for (Relation relation : relations) {
            item = (Item) relation.getChild();
            title = item.getTitle();
            Article article = new Article(title, item.getCreated(), relation.getUrl());
            Set authors = item.getProperty(Constants.PROPERTY_AUTHOR);
            for (Iterator iterIn = authors.iterator(); iterIn.hasNext();) {
                int rid = Misc.parseInt((String) iterIn.next(), 0);
                Relation author = (Relation) Tools.sync(new Relation(rid));
                article.addAuthor(author);
            }
            article.setPerex(Tools.xpath(item, "data/perex"));
            article.setComments(Tools.findComments(item).getResponseCount());
            articles.add(article);
        }

        relations = sqlTool.findNewsRelationsWithinPeriod(from, until, qualifiers);
        Tools.syncList(relations);
        List news = new ArrayList(relations.size());

        for (Relation relation : relations) {
            item = (Item) relation.getChild();
            title = item.getTitle();
            content = Tools.xpath(item, "data/content");
            News newz = new News(title, content, item.getCreated(), relation.getUrl());
            newz.setAuthor(Tools.createUser(item.getOwner()).getName());
            newz.setComments(Tools.findComments(item).getResponseCount());
            news.add(newz);
        }

        params.put(VAR_WEEK, week);
        params.put(VAR_YEAR, year);
        params.put(VAR_ARTICLES, articles);
        params.put(VAR_NEWS, news);
        params.put(VAR_JOBS, JobOfferManager.getOffersAfter(from));
    }

    private Calendar getWeekStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        return calendar;
    }

    /**
     * Configures this instance.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT, null);
    }

    /**
     * mainly for debug purposes
     * @param args
     */
    public static void main(String[] args) {
        JobOfferManager jobs = new JobOfferManager();
        jobs.run();
        WeeklyEmail instance = new WeeklyEmail();
        instance.run();
        System.out.println("Weekly email sucessfully sent to " + instance.count + " addressses.");
    }
}
