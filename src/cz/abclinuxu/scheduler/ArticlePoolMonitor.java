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
                        Element element = (Element) item.getData().selectSingleNode("/data/section_rid");
                        if (element==null)
                            continue;
                        int section_rid = Misc.parseInt(element.getText(), 0);
                        if (section_rid==0)
                            continue;
                        Relation section = (Relation) persistance.findById(new Relation(section_rid));

                        element.detach();
                        persistance.update(item);

                        if (relation.getUrl() == null) {
                            String url = EditArticle.getUrl(item, section.getId(), persistance);
                            if (url != null) {
                                relation.setUrl(url);
                                persistance.update(relation);
                            }
                        }

                        relation.getParent().removeChildRelation(relation);
                        relation.setParent(section.getChild());
                        relation.setUpper(section.getId());
                        persistance.update(relation);
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
