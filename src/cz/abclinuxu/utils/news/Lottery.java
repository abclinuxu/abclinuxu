/*
 * User: literakl
 * Date: 5.11.2003
 * Time: 20:07:04
 */
package cz.abclinuxu.utils.news;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;

import java.util.prefs.Preferences;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Serves to draw lots of news within given time.
 * Each month we select the user (not administrator),
 * who wins the t-shirt. See relation 46426 for details.
 */
public class Lottery implements Configurable {
    public static final String PREF_ADMINS = "admins";

    static List admins;

    public Lottery() {
        ConfigurationManager.getConfigurator().configureMe(this);
    }

    /**
     * entry method
     */
    public static void main(String[] args) {
        if (args.length!=2) {
            System.err.println("Chybny pocet parametru! Prvni argument je datum prvniho dne,");
            System.err.println("druhy posledniho dne casoveho useku, po ktery se ma provest");
            System.err.println("loterie. Datum ma byt v ISO formatu: 2003-11-01");
            System.exit(1);
        }

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = null, end = null;
        try {
            start = isoFormat.parse(args[0]);
            end = isoFormat.parse(args[1]);
        } catch (ParseException e) {
            System.err.println("Chyba! Datum musi byt v ISO formatu: 2003-11-01");
            System.exit(1);
        }

        Lottery lottery = new Lottery();
        List relations = lottery.findNews(start,end);
        lottery.filterProhibited(relations,admins);
        Map grouped = lottery.groupNewsByUser(relations);
        lottery.printNewsByUser(grouped);
        lottery.drawLotsOfNews(relations);
    }

    /**
     * Finds news in given time range.
     * @param start first day of time range
     * @param end last day of time range
     * @return list of initialized relations
     */
    private List findNews(Date start, Date end) {
        return SQLTool.getInstance().findNewsRelationsWithinPeriod(start,end);
    }

    /**
     * Removes all news, that are owned by people in prohibited list
     */
    private void filterProhibited(List relations, List prohibited) {
        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Persistance persistance = PersistanceFactory.getPersistance();
            Relation relation = (Relation) iter.next();
            Item news = (Item) persistance.findById(relation.getChild());
            Integer owner = new Integer(news.getOwner());
            if (prohibited.contains(owner))
                iter.remove();
        }
    }

    /**
     * Groups relations with news by user, who submitted them. The key is
     * id of the user and the value is list of relations, which he owns.
     */
    private Map groupNewsByUser(List relations) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map map = new HashMap(relations.size()+1,1.0f);
        for ( Iterator iter = relations.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            Item news = (Item) persistance.findById(relation.getChild());
            Integer owner = new Integer(news.getOwner());
            if (map.get(owner)!=null) {
                ((List)map.get(owner)).add(relation);
            } else {
                List list = new ArrayList();
                list.add(relation);
                map.put(owner,list);
            }
        }
        return map;
    }

    /**
     * Prints HTML table of news
     */
    private void printNewsByUser(Map grouped) {
        Persistance persistance = PersistanceFactory.getPersistance();
        System.out.println("<table border=\"0\">");
        for ( Iterator iter = grouped.keySet().iterator(); iter.hasNext(); ) {
            Integer key = (Integer) iter.next();
            List list = (List) grouped.get(key);
            User user = (User) persistance.findById(new User(key.intValue()));

            System.out.println("<tr>\n<td><a href=\"/Profile?uid="+key+"\">"+user.getName()+"</a></td>");
            System.out.println("<td>");
            for ( Iterator iterNews = list.iterator(); iterNews.hasNext(); ) {
                Relation relation = (Relation) iterNews.next();
                System.out.print("<a href=\"/news/ViewRelation?rid="+relation.getId()+"\">"+relation.getId()+"</a>");
                if (iterNews.hasNext()) System.out.println(", ");
            }
            System.out.println("</td>\n</tr>");
        }
        System.out.println("</table>");
    }

    /**
     * Randomly selects one news and prints the winner.
     */
    private void drawLotsOfNews(List listOfNews) {
        Persistance persistance = PersistanceFactory.getPersistance();
        int i = new Random().nextInt(listOfNews.size());
        Relation relation = (Relation) listOfNews.get(i);
        Item news = (Item) persistance.findById(relation.getChild());
        User user = (User) persistance.findById(new User(news.getOwner()));

        String text = news.getData().selectSingleNode("//content").getText();
        System.out.println("Vítìznou zprávièkou se stává");
        System.out.println("<a href=\"/Profile?uid="+user.getId()+"\">"+user.getName()+"</a>");
        System.out.println(text);
        System.out.println("<a href=\"/news/ViewRelation?rid="+relation.getId()+"\">Ukázat</a>");
    }

    /**
     * Finds administrators, that cannot attend the lottery.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_ADMINS,"");
        StringTokenizer stk = new StringTokenizer(tmp,", ");
        admins = new ArrayList();
        while (stk.hasMoreTokens()) {
            int key = Misc.parseInt(stk.nextToken(),0);
            if (key>0)
                admins.add(new Integer(key));
        }
    }
}
