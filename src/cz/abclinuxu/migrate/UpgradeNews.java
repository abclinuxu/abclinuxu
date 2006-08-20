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
package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.cache.EmptyCache;
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
    static Persistence persistence = PersistenceFactory.getPersistance(EmptyCache.class);
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
            persistence.update(item);
            sqlTool.setUpdatedTimestamp(item, updated);
        }
    }
}
