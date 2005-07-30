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
import cz.abclinuxu.persistance.cache.EmptyCache;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.Iterator;
import java.util.List;

/**
 * Generates URL for news, if it was not been set yet.
 */
public class GenerateUrlForNews {
    static Persistance persistance = PersistanceFactory.getPersistance(EmptyCache.class);
    static SQLTool sqlTool = SQLTool.getInstance();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.print("Starting to search for News ..");
        int size = sqlTool.countNewsRelations(), total = 0;
        System.out.println("Found "+size+" news");

        for (int i=0, j=50; i<size; i+=j) {
            if (i+j>size) j = size - i;
            List items = sqlTool.findNewsRelations(new Qualifier[]{new LimitQualifier(i, j)});
            total += setUrl(items);
        }
        long end = System.currentTimeMillis();
        System.out.println("Url set for "+total+" news. Total time: " + (end - start) / 1000 + " seconds");
    }

    private static int setUrl(List items) {
        Relation relation;
        Item item;
        String title;
        int count = 0;
        for ( Iterator iter = items.iterator(); iter.hasNext(); ) {
            relation = (Relation) iter.next();
            if (relation.getUrl()!=null)
                continue;

            item = (Item) persistance.findById(relation.getChild());
            String content = Tools.xpath(item, "data/content");
            String withoutTags = Tools.removeTags(content);
            title = Tools.limitWords(withoutTags, 6, "");

            String url = UrlUtils.PREFIX_NEWS + "/" + URLManager.enforceLastURLPart(title);
            url = URLManager.protectFromDuplicates(url);

            relation.setUrl(url);
            persistance.update(relation);
            count++;
        }
        return count;
    }
}
