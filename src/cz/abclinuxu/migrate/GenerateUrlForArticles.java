/*
 * User: literakl
 * Date: 8.5.2004
 * Time: 8:23:28
 */
package cz.abclinuxu.migrate;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.Iterator;
import java.util.List;

/**
 * Generates URL for articles, if it was not been set yet.
 */
public class GenerateUrlForArticles {
    static Persistance persistance = PersistanceFactory.getPersistance();
    static SQLTool sqlTool = SQLTool.getInstance();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.print("Starting to search for articles ..");
        int size = sqlTool.countItemRelationsWithType(Item.ARTICLE, null), total = 0;
        System.out.println("Found "+size+" articles");

        for (int i=0, j=50; i<size; i+=j) {
            if (i+j>size) j = size - i;
            List items = sqlTool.findItemRelationsWithType(Item.ARTICLE, new Qualifier[]{new LimitQualifier(i, j)});
            total += setUrl(items);
        }
        long end = System.currentTimeMillis();
        System.out.println("Url set for "+total+" articles. Total time: " + (end - start) / 1000 + " seconds");
    }

    private static int setUrl(List items) {
        Relation relation, parentRelation;
        Item item;
        String title;
        int count = 0, upper;
        for ( Iterator iter = items.iterator(); iter.hasNext(); ) {
            relation = (Relation) iter.next();
            if (relation.getUrl()!=null)
                continue;

            upper = relation.getUpper();
            if (upper==0) {
//                System.out.println("clanek "+relation.getId()+" nema definovaneho predka");
                continue;
            }
            parentRelation = (Relation) persistance.findById(new Relation(upper));
            if (parentRelation.getUrl()==null) {
//                System.out.println("clanek " + relation.getId() + " ma predka "+upper+" bez relace s url");
                continue;
            }

            item = (Item) persistance.findById(relation.getChild());
            title = Tools.xpath(item, "data/name");
            String url = parentRelation.getUrl() + "/" + URLManager.enforceLastURLPart(title);
            url = URLManager.protectFromDuplicates(url);

            relation.setUrl(url);
            persistance.update(relation);
            count++;
        }
        return count;
    }
}
