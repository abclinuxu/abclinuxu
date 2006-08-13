/*
 *  Copyright (C) 2006 Leos Literak
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

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.cache.EmptyCache;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.servlets.html.edit.EditRelated;

import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.dom4j.Element;

/**
 * Migrates related links to related documents.
 */
public class UpgradeFaq {
    static Persistance persistance = PersistanceFactory.getPersistance(EmptyCache.class);
    static SQLTool sqlTool = SQLTool.getInstance();

    public static void main(String[] args) {
        System.out.print("Starting to search for FAQ ..");
        int size = sqlTool.countItemsWithType(Item.FAQ);
        System.out.println("Found "+size+" items");

        for (int i=0, j=50; i<size; i+=j) {
            if (i+j>size) j = size - i;
            List items = sqlTool.findItemsWithType(Item.FAQ, i, j);
            upgrade(items);
        }
        System.out.println("Finished");
    }

    private static void upgrade(List items) {
        Item item;
        Element root, links, link;
        Date updated;
        for ( Iterator iter = items.iterator(); iter.hasNext(); ) {
            item = (Item) iter.next();
            updated = item.getUpdated();
            root = item.getData().getRootElement();
            links = root.element("links");
            if (links == null)
                continue;

            for (Iterator iter2 = links.elements("link").iterator(); iter2.hasNext();) {
                link = (Element) iter2.next();
                Map params = new HashMap();
                params.put(EditRelated.PARAM_URL, link.attributeValue("url"));
                params.put(EditRelated.PARAM_TITLE, link.getText());
                EditRelated.insertDocument(params, root, params);
            }
            links.detach();
            persistance.update(item);
            sqlTool.setUpdatedTimestamp(item, updated);
        }
    }
}
