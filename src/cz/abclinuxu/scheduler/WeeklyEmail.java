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
            int week = getCurrentWeek(), year = getCurrentYear();

            Map params = new HashMap();
            params.put(EmailSender.KEY_FROM,sender);
            params.put(EmailSender.KEY_SUBJECT,subject+" "+week+"/"+year);
            params.put(EmailSender.KEY_TEMPLATE,template);
            params.put(Constants.VAR_TOOL,new Tools());
            params.put(Constants.VAR_DATE_TOOL,new DateTool());
            params.put(VAR_WEEK,new Integer(week));
            params.put(VAR_YEAR,new Integer(year));

            log.info("Time to send weekly emails. Let's find subscribed users first.");
            List users = SQLTool.getInstance().findUsersWithWeeklyEmail(null);
            log.info("Weekly emails have subscribed "+users.size()+" users.");

            if (!setArticles(params)) {
                log.warn("No articles were found!");
                return;
            }

            int count = EmailSender.sendEmailToUsers(params,users);
            log.info("Weekly email sucessfully sent to "+count+" addressses.");
        } catch (Exception e) {
            log.warn("Cannot sent weekly emails!",e);
        }
    }

    /**
     * Finds articles, that shall be sent and initializes them.
     * Then it puts them into params with other information.
     * @return true, if there was at least one article
     */
    private boolean setArticles(Map params) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Tools tools = new Tools();
        Item item;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List relations = SQLTool.getInstance().findArticleRelationsWithinPeriod(calendar.getTime(), new Date(), qualifiers);
        List articles = new ArrayList(relations.size());

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            item = (Item) persistance.findById(relation.getChild());
            String tmp = tools.xpath(item,"/data/author");
            Article article = new Article(tools.xpath(item, "data/name"),item.getCreated(),relation.getId());
            article.setAuthor(tools.createUser(tmp).getName());
            article.setPerex(tools.xpath(item, "data/perex"));
            articles.add(article);
        }

        relations = SQLTool.getInstance().findNewsRelationsWithinPeriod(calendar.getTime(), new Date(), qualifiers);
        List news = new ArrayList(relations.size());

        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            item = (Item) persistance.findById(relation.getChild());
            News newz = new News(tools.xpath(item, "data/content"), item.getCreated(), relation.getId());
            newz.setAuthor(tools.createUser(item.getOwner()).getName());
            newz.setComments(tools.findComments(item).getResponseCount());
            news.add(newz);
        }

        params.put(VAR_ARTICLES, articles);
        params.put(VAR_NEWS, news);
        return articles.size()>0;
    }

    /**
     * @return current week of year
     */
    private int getCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * @return current year
     */
    private int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
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

    public static class Article {
        private String title, perex, author;
        private Date published;
        private int relationId;

        public Article(String title, Date published, int relationId) {
            this.title = title;
            this.published = published;
            this.relationId = relationId;
        }

        public void setPerex(String perex) {
            this.perex = perex;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public String getPerex() {
            return perex;
        }

        public String getAuthor() {
            return author;
        }

        public Date getPublished() {
            return published;
        }

        public int getRelationId() {
            return relationId;
        }
    }

    public static class News {
        String content, author;
        Date published;
        int relationId, comments;

        public News(String content, Date published, int relationId) {
            this.content = content;
            this.published = published;
            this.relationId = relationId;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setComments(int comments) {
            this.comments = comments;
        }

        public String getContent() {
            return content;
        }

        public String getAuthor() {
            return author;
        }

        public Date getPublished() {
            return published;
        }

        public int getRelationId() {
            return relationId;
        }

        public int getComments() {
            return comments;
        }
    }
}
