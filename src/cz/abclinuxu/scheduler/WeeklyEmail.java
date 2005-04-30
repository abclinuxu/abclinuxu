/*
 * User: Leos Literak
 * Date: Jul 23, 2003
 * Time: 9:08:13 PM
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.extra.Qualifier;
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
        Persistance persistance = PersistanceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();
        Item item;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List relations = sqlTool.findArticleRelationsWithinPeriod(calendar.getTime(), new Date(), qualifiers);
        List articles = new ArrayList(relations.size());

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            item = (Item) persistance.findById(relation.getChild());
            String tmp = Tools.xpath(item,"/data/author");
            Article article = new Article(Tools.xpath(item, "data/name"),item.getCreated(),relation.getId());
            article.setAuthor(Tools.createUser(tmp).getName());
            article.setPerex(Tools.xpath(item, "data/perex"));
            articles.add(article);
        }

        relations = sqlTool.findNewsRelationsWithinPeriod(calendar.getTime(), new Date(), qualifiers);
        List news = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            item = (Item) persistance.findById(relation.getChild());
            News newz = new News(Tools.xpath(item, "data/content"), item.getCreated(), relation.getId());
            newz.setAuthor(Tools.createUser(item.getOwner()).getName());
            newz.setComments(Tools.findComments(item).getResponseCount());
            news.add(newz);
        }

        params.put(VAR_ARTICLES, articles);
        params.put(VAR_NEWS, news);
    }

    /**
     * Configures this instance.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT, null);
        sender = prefs.get(PREF_SENDER, null);
        template = prefs.get(PREF_TEMPLATE, null);
    }

    public static void main(String[] args) {
        WeeklyEmail weeklyEmail = new WeeklyEmail();
        weeklyEmail.run();
    }
}
