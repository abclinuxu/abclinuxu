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
import cz.abclinuxu.servlets.html.edit.EditArticle;

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
        Relation relation;
        String url;
        int count = 0;
        for ( Iterator iter = items.iterator(); iter.hasNext(); ) {
            relation = (Relation) iter.next();
            if (relation.getUrl()!=null)
                continue;

            url = EditArticle.getUrl((Item) relation.getChild(), relation.getUpper(), persistance);
            if (url==null)
                continue;
            
            relation.setUrl(url);
            persistance.update(relation);
            count++;
        }
        return count;
    }
}