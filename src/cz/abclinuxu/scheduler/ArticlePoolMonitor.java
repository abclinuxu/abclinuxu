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
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditArticle;
import cz.abclinuxu.utils.Misc;

import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.TimerTask;

import org.dom4j.Element;

/**
 * This class is responsible for monitoring of
 * the article pool, where prepared articles
 * waits for publication.
 */
public class ArticlePoolMonitor extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ArticlePoolMonitor.class);

    Category pool = new Category(Constants.CAT_ARTICLES_POOL);

    public ArticlePoolMonitor() {
    }

    /**
     * Checks, whether there is an article in pool to be published.
     */
    public void run() {
        try {
            Persistence persistence = PersistenceFactory.getPersistance();
            persistence.synchronize(pool);
            Date now = new Date();

            List children = pool.getChildren();
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                if ( relation.getChild() instanceof Item ) {
                    Item item = (Item) relation.getChild();
                    persistence.synchronize(item);
                    if ( item.getType()==Item.ARTICLE && now.after(item.getCreated()) ) {
                        Element element = (Element) item.getData().selectSingleNode("/data/section_rid");
                        if (element==null)
                            continue;
                        int section_rid = Misc.parseInt(element.getText(), 0);
                        if (section_rid==0)
                            continue;
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

                        relation.getParent().removeChildRelation(relation);
                        relation.setParent(section.getChild());
                        relation.setUpper(section.getId());
                        persistence.update(relation);
                        relation.getParent().addChildRelation(relation);

                        VariableFetcher.getInstance().refreshArticles();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot monitor article pool!", e);
        }
    }
}
