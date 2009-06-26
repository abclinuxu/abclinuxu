/*
 *  Copyright (C) 2007 Leos Literak
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

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.data.User;
import cz.abclinuxu.servlets.Constants;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author literakl
 * @since 25.3.2007
 */
public class UpdateUserScore extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateUserScore.class);

    private static final String PREF_ARTICLE_RATIO = "ratio.article";
    private static final String PREF_COMMENT_RATIO = "ratio.comment";
    private static final String PREF_WIKI_RATIO = "ratio.wiki";
    private static final String PREF_NEWS_RATIO = "ratio.news";
    private static final String PREF_IGNORED_USERS = "ignored.users";

    private float ratioArticle, ratioComment, ratioWiki, ratioNews;
    private Set<Integer> ignoredLogins = new HashSet<Integer>();

    /**
     * The action to be performed by this timer task.
     */
    public void run() {
        if (log.isDebugEnabled()) log.debug("Starting task " + getJobName());
        Map<Integer, Integer> users = findUsers();
        SQLTool sqlTool = SQLTool.getInstance();
        for (Map.Entry<Integer, Integer> entry : users.entrySet()) {
            User user = new User(entry.getKey());
            if (ignoredLogins.contains(user.getId()))
                continue;
            int score = calculateScore(entry.getValue());
            if (score == 0)
                continue;
            Set properties = Collections.singleton(Integer.toString(score));
            sqlTool.setProperty(user, Constants.PROPERTY_SCORE, properties);
        }

        if (log.isDebugEnabled()) log.debug("Task " + getJobName() + " has finished its job.");
    }

    private Map<Integer, Integer> findUsers() {
        Map<Integer, Integer> users = new HashMap<Integer, Integer>(20000);
        SQLTool sqlTool = SQLTool.getInstance();
        List<Object[]> found = sqlTool.countUsersCommentsInForum();
        incrementUsersCounts(users, found, ratioComment);
        found = sqlTool.countUsersArticles();
        incrementUsersCounts(users, found, ratioArticle);
        found = sqlTool.countUsersModifiedWikiRecords();
        incrementUsersCounts(users, found, ratioWiki);
        found = sqlTool.countUsersNews();
        incrementUsersCounts(users, found, ratioNews);
        return users;
    }

    private void incrementUsersCounts(Map<Integer, Integer> users, List<Object[]> newValues, float ratio) {
        for (Object[] objects : newValues) {
            if (objects[0] == null)
                continue;
            int user;
            if (objects[0] instanceof Number)
                user = ((Number) objects[0]).intValue();
            else
                user = Misc.parseInt(objects[0].toString(), 0);
            int count = ((Number) objects[1]).intValue();
            count = (int) (ratio * count);
            Integer current = users.get(user);
            if (current != null)
                count += current;
            users.put(user, count);
        }
    }

    /**
     * This method calculates user score based on number of points that the user has achieved.
     * The function is non-decreasing and non-linear.
     * @param points non-negative number
     * @return score
     */
    private static int calculateScore(int points) {
        return (int) Math.floor(Math.max(Math.pow(1.03 * Math.log(points + 1), 2) - 2, 0));
    }

    public UpdateUserScore() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        ratioArticle = prefs.getFloat(PREF_ARTICLE_RATIO, 10.0f);
        ratioComment = prefs.getFloat(PREF_COMMENT_RATIO, 1.0f);
        ratioWiki = prefs.getFloat(PREF_WIKI_RATIO, 5.0f);
        ratioNews = prefs.getFloat(PREF_NEWS_RATIO, 4.0f);

        String users = prefs.get(PREF_IGNORED_USERS, "");
        StringTokenizer stk = new StringTokenizer(users, ",");
        while (stk.hasMoreTokens()) {
            int uid = Misc.parseInt(stk.nextToken(), 0);
            if (uid > 0)
                ignoredLogins.add(uid);
        }
    }

    public static void main(String[] args) {
        UpdateUserScore task = new UpdateUserScore();
        task.run();
//        Map<Integer, Integer> users = task.findUsers();
//        for (Map.Entry<Integer, Integer> entry : users.entrySet()) {
//            int points = entry.getValue();
//            int score = calculateScore(points);
//            System.out.println(entry.getKey() + ";" + points + ";" + score);
//        }
//        for (int i=0; i< 12000; i+=100)
//            System.out.println("points = "+i+" = "+calculateScore(i));
    }

    public String getJobName() {
        return "UpdateUserScore";
    }
}
