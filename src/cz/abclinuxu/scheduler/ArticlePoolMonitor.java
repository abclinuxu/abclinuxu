/*
 * User: literakl
 * Date: 3.9.2002
 * Time: 10:43:32
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.servlets.Constants;

import java.util.TimerTask;
import java.util.List;
import java.util.Iterator;
import java.util.Date;

/**
 * This class is responsible for monitoring of
 * the article pool, where prepared articles
 * waits for publication.
 */
public class ArticlePoolMonitor extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ArticlePoolMonitor.class);

    Category pool = new Category(Constants.CAT_ARTICLEPOOL);
    Category articles = new Category(Constants.CAT_ACTUAL_ARTICLES);

    public ArticlePoolMonitor() {
    }

    /**
     * Checks, whether there is an article in pool to be published.
     */
    public void run() {
        try {
            Persistance persistance = PersistanceFactory.getPersistance();
            persistance.synchronize(pool);
            Date now = new Date();

            List children = pool.getChildren();
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                if ( relation.getChild() instanceof Item ) {
                    Item item = (Item) relation.getChild();
                    persistance.synchronize(item);
                    if ( item.getType()==Item.ARTICLE && now.after(item.getCreated()) ) {
                        relation.getParent().removeChildRelation(relation);
                        relation.setParent(articles);
                        relation.setUpper(Constants.REL_ACTUAL_ARTICLES);
                        persistance.update(relation);
                        relation.getParent().addChildRelation(relation);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot monitor article pool!", e);
        }
    }
}
