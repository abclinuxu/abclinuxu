/*
 *  Copyright (C) 2008 Leos Literak
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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.prefs.Preferences;

/**
 *
 * @author lubos
 */
public class UpdateSubportalScore extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateSubportalScore.class);
    
    private static final String PREF_ARTICLE_RATIO = "ratio.article";
    private static final String PREF_QUESTION_RATIO = "ratio.question";
    private static final String PREF_WIKI_RATIO = "ratio.wiki";
    private static final String PREF_EVENT_RATIO = "ratio.event";
    
    private float ratioArticle, ratioQuestion, ratioWiki, ratioEvent;
    
    public void run() {
        if (log.isDebugEnabled()) log.debug("Starting task " + getJobName());
        Map<Integer, Integer> portals = findSubportals();
        SQLTool sqlTool = SQLTool.getInstance();
        for (Map.Entry<Integer, Integer> entry : portals.entrySet()) {
            Relation rel = new Relation(entry.getKey());
            int score = calculateScore(entry.getValue());
            if (score == 0)
                continue;
            
            Tools.sync(rel);
            Set properties = Collections.singleton(Integer.toString(score));
            sqlTool.setProperty((Category) rel.getChild(), Constants.PROPERTY_SCORE, properties);
        }

        if (log.isDebugEnabled()) log.debug("Task " + getJobName() + " has finished its job.");
    }
    
    private Map<Integer, Integer> findSubportals() {
        Map<Integer, Integer> portals = new HashMap<Integer, Integer>(20);
        SQLTool sqlTool = SQLTool.getInstance();
        List<Object[]> found = sqlTool.countSubportalForumQuestions();
        incrementSubportalCounts(portals, found, ratioQuestion);
        found = sqlTool.countSubportalArticles();
        incrementSubportalCounts(portals, found, ratioArticle);
        found = countSubportalWikiPages();
        incrementSubportalCounts(portals, found, ratioWiki);
        found = sqlTool.countSubportalArticles();
        incrementSubportalCounts(portals, found, ratioEvent);
        return portals;
    }
    
    private List countSubportalWikiPages() {
        Persistence persistence = PersistenceFactory.getPersistence();
        Category subportals = new Category(Constants.CAT_SUBPORTALS);
        List<Relation> children = Tools.syncList(subportals.getChildren());
        List<Integer[]> result = new ArrayList(children.size());
        
        for (Relation rel : children) {
            Relation wiki = Tools.createRelation(Tools.xpath(rel.getChild(), "//wiki"));
            int wikiPages = 0;
            
            LinkedList<Relation> stack = new LinkedList(persistence.findChildren(wiki.getChild()));
            while (stack.size() > 0) {
                Relation current = stack.removeFirst();
                wikiPages++;
                stack.addAll(0, persistence.findChildren(current.getChild()));
            }
            
            result.add(new Integer[] { rel.getId(), wikiPages });
        }
        
        return result;
    }

    private void incrementSubportalCounts(Map<Integer, Integer> portals, List<Object[]> newValues, float ratio) {
        for (Object[] objects : newValues) {
            if (objects[0] == null)
                continue;
            int rid = 0;
            if (objects[0] instanceof Number)
                rid = ((Number) objects[0]).intValue();
            else
                rid = Misc.parseInt(objects[0].toString(), 0);
            int count = ((Number) objects[1]).intValue();
            count = (int) (ratio * count);
            Integer current = portals.get(rid);
            if (current != null)
                count += current;
            portals.put(rid, count);
        }
    }
    
    /**
     * This method calculates subportal score based on number of points that the subportal has achieved.
     * The function is non-decreasing and non-linear.
     * @param points non-negative number
     * @return score
     */
    private static int calculateScore(int points) {
        return (int) Math.floor(Math.max(Math.pow(1.03 * Math.log(points + 1), 2) - 2, 0));
    }

    public UpdateSubportalScore() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }
    
    public static void main(String[] args) {
        UpdateSubportalScore task = new UpdateSubportalScore();
        task.run();
    }
    
    public void configure(Preferences prefs) throws ConfigurationException {
        ratioArticle = prefs.getFloat(PREF_ARTICLE_RATIO, 8.0f);
        ratioQuestion = prefs.getFloat(PREF_QUESTION_RATIO, 2.0f);
        ratioWiki = prefs.getFloat(PREF_WIKI_RATIO, 5.0f);
        ratioEvent = prefs.getFloat(PREF_EVENT_RATIO, 15.0f);
    }
    
    public String getJobName() {
        return "UpdateSubportalScore";
    }
}
