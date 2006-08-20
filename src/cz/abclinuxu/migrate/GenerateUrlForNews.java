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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.cache.EmptyCache;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.Iterator;
import java.util.List;

/**
 * Generates URL for news, if it was not been set yet.
 */
public class GenerateUrlForNews {
    static Persistence persistence = PersistenceFactory.getPersistance(EmptyCache.class);
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

            item = (Item) persistence.findById(relation.getChild());
            String content = Tools.xpath(item, "data/content");
            String withoutTags = Tools.removeTags(content);
            title = Tools.limitWords(withoutTags, 6, "");

            String url = UrlUtils.PREFIX_NEWS + "/" + URLManager.enforceLastURLPart(title);
            url = URLManager.protectFromDuplicates(url);

            relation.setUrl(url);
            persistence.update(relation);
            count++;
        }
        return count;
    }
}
