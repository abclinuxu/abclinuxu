/*
 * User: literakl
 * Date: 8.5.2004
 * Time: 8:23:28
 */
package cz.abclinuxu.migrate;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.cache.EmptyCache;
import cz.abclinuxu.data.Item;

import java.util.List;
import java.util.Iterator;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Move category from XML to subType of Item
 * (from performance reasons, it will enable filters on this parameter)
 */
public class UpgradeNews {
    static Persistance persistance = PersistanceFactory.getPersistance(EmptyCache.class);
    static SQLTool sqlTool = SQLTool.getInstance();

    public static void main(String[] args) {
        System.out.print("Starting to search for News ..");
        int size = sqlTool.countItemsWithType(Item.NEWS);
        System.out.println("Found "+size+" items");

        for (int i=0, j=50; i<size; i+=j) {
            if (i+j>size) j = size - i;
            List items = sqlTool.findItemsWithType(Item.NEWS, i, j);
            upgrade(items);
        }
        System.out.println("Finished");   
    }

    private static void upgrade(List items) {
        Item item;
        Document doc;
        Node category;
        String value;
        Date updated;
        for ( Iterator iter = items.iterator(); iter.hasNext(); ) {
            item = (Item) iter.next();
            updated = item.getUpdated();
            doc = item.getData();
            category = doc.selectSingleNode("//category");
            if (category!=null) {
                value = category.getText();
                category.detach();
            } else {
                value = "ARTICLE";
            }
            item.setSubType(value);
            persistance.update(item);
            sqlTool.setUpdatedTimestamp(item, updated);
        }
    }
}
