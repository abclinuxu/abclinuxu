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

import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditArticle;
import cz.abclinuxu.servlets.html.edit.EditSeries;
import cz.abclinuxu.servlets.html.edit.EditDiscussion;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.email.monitor.MonitorAction;
import cz.abclinuxu.utils.email.monitor.MonitorPool;
import cz.abclinuxu.utils.email.monitor.ObjectType;
import cz.abclinuxu.utils.email.monitor.UserAction;
import cz.abclinuxu.utils.feeds.FeedGenerator;

import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.TimerTask;

import org.dom4j.Element;
import org.dom4j.Document;

/**
 * This class is responsible for monitoring of
 * the object pools and publishing objects waiting
 * for publication.
 */
public class PoolMonitor extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PoolMonitor.class);

    Category newsPool = new Category(Constants.CAT_NEWS_POOL);
	List<Category> articlePools;

    public PoolMonitor() {
		gatherArticlePools();
    }

    /**
     * Checks, whether there are articles or news to be published.
     */
    public void run() {
        try {
            log.debug(getJobName()+" starts");
            Persistence persistence = PersistenceFactory.getPersistence();
            Date now = new Date();
            boolean articlesUpdated = false, newsUpdated = false;

			for(Category articlePool : articlePools) {
				List children = articlePool.getChildren();
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					Relation relation = (Relation) iter.next();
					if ( ! (relation.getChild() instanceof Item) )
						continue;
					Item item = (Item) relation.getChild();
					persistence.synchronize(item);
					if ( item.getType() != Item.ARTICLE)
						continue;

					if ( now.after(item.getCreated()) ) {
						Document document = item.getData();
						// move article to selected article section
						Element element = (Element) document.selectSingleNode("/data/section_rid");
						if (element == null)
							continue;
						int section_rid = Misc.parseInt(element.getText(), 0);
						if (section_rid == 0) {
							log.error("bug", new Exception());
							continue;
						}

						Relation section = (Relation) persistence.findById(new Relation(section_rid));
						element.detach();
						persistence.update(item);

						if (relation.getUrl() == null) {
							String url = EditArticle.getUrl(item, section.getId(), persistence);
							if (url != null) {
								relation.setUrl(url);
								persistence.update(relation);
							}
						}

						// link article to article series, if it is set
						element = (Element) document.selectSingleNode("/data/series_rid");
						if (element != null) {
							int series_rid = Misc.parseInt(element.getText(), 0);
							if (series_rid == 0) {
								log.error("bug", new Exception());
								continue;
							}

							Relation seriesRelation = (Relation) persistence.findById(new Relation(series_rid));
							Item series = (Item) persistence.findById(seriesRelation.getChild());
							List articles = series.getData().getRootElement().elements("article");
							EditSeries.addArticleToSeries(item, relation, articles);
							persistence.update(series);
						}

						relation.getParent().removeChildRelation(relation);
						relation.setParent(section.getChild());
						relation.setUpper(section.getId());
						persistence.update(relation);
						relation.getParent().addChildRelation(relation);

						if (item.getData().selectSingleNode("/data/forbid_discussions") == null)
							EditDiscussion.createEmptyDiscussion(relation, new User(Constants.USER_REDAKCE), persistence);
                        
                        String absoluteUrl = "http://www.abclinuxu.cz" + relation.getUrl();
                        MonitorAction action = new MonitorAction("", UserAction.ADD, ObjectType.ARTICLE, relation, absoluteUrl);
                        MonitorPool.scheduleMonitorAction(action);

						articlesUpdated = true;
					}
				}
			}

            List children = newsPool.getChildren();
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                if ( ! (relation.getChild() instanceof Item) )
                    continue;
                Item item = (Item) persistence.findById(relation.getChild());
                if ( item.getType() != Item.NEWS)
                    continue;
                Element element = (Element) item.getData().selectSingleNode("/data/approved_by");
                if (element == null)
                    continue;

                if ( now.after(item.getCreated()) ) {
                    relation.getParent().removeChildRelation(relation);
                    relation.getParent().setId(Constants.CAT_NEWS);
                    relation.setUpper(Constants.REL_NEWS);
                    persistence.update(relation);
                    relation.getParent().addChildRelation(relation);

                    newsUpdated = true;
                }
            }

            if (articlesUpdated) {
                VariableFetcher.getInstance().refreshArticles();
                FeedGenerator.updateArticles();
            }
            if (newsUpdated) {
                VariableFetcher.getInstance().refreshNews();
                FeedGenerator.updateNews();
            }
            log.debug(getJobName() + " finished");
        } catch (Exception e) {
            log.error("Object pool monitor failed!", e);
        }
    }
	
	public void gatherArticlePools() {
		Category subportals = new Category(Constants.CAT_SUBPORTALS);
		List<Relation> portals = subportals.getChildren();
		
		articlePools = new ArrayList(portals.size()+1);
		
		Tools.syncList(portals);
		
		for (Relation portal : portals) {
			Category cat = (Category) portal.getChild();
			int poolid = Misc.parseInt(Tools.xpath(cat, "/data/article_pool"), 0);
			
			if (poolid == 0)
				continue;
			
			Relation r = new Relation(poolid);
			Tools.sync(r);
			
			articlePools.add((Category) r.getChild());
		}
		
		articlePools.add(new Category(Constants.CAT_ARTICLES_POOL));
	}

    private String getJobName() {
        return "PoolMonitor";
    }
}
