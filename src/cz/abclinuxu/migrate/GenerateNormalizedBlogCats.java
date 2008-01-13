/*
 *  Copyright (C) 2007 Leos Literak
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
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.data.Category;

import org.dom4j.Element;
import org.dom4j.Document;

import java.util.Iterator;
import java.util.List;

/**
 * Generates normalized names for blog categories.
 */
public class GenerateNormalizedBlogCats {
    static Persistence persistence = PersistenceFactory.getPersistence();
    static SQLTool sqlTool = SQLTool.getInstance();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.print("Starting to search for blogs ..");
        int size = sqlTool.countCategoryRelationsWithType(Category.BLOG), total = 0;
        System.out.println("Found "+size+" blogs");

        for (int i=0, j=50; i<size; i+=j) {
            if (i+j>size) j = size - i;
            List items = sqlTool.findCategoryRelationsWithType(Category.BLOG, new Qualifier[]{new LimitQualifier(i, j)});
            total += generateNormalizedNames(items);
        }
        long end = System.currentTimeMillis();
        System.out.println("Generated "+total+" categories. Total time: " + (end - start) / 1000 + " seconds");
    }

    private static int generateNormalizedNames(List items) {
        Relation relation;
        String url;
        int count = 0;
        for ( Iterator iter = items.iterator(); iter.hasNext(); ) {
            relation = (Relation) iter.next();

            count += processBlog(relation);
        }
        return count;
    }

    private static int processBlog(Relation rel) {
        Category blog = (Category) persistence.findById(rel.getChild());

        int count = 0;
        Document document = blog.getData();
        List nodes = document.selectNodes("//categories/category");

        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Element category = (Element) it.next();
            String name = category.attributeValue("name");
            String normalized = normalizeName(name);

            if (normalized != null) {
                if(category.attribute("url") == null) {
                    category.addAttribute("url", normalized);
                    count++;
                }
            }
        }
        persistence.update(blog);

        return count;
    }

    private static String normalizeName(String name) {
        try {
            String normalized = URLManager.enforceRelativeURL(name);

            if (normalized.equals("export") || normalized.equals("archiv") || normalized.equals("souhrn")) {
                return null;
            }

            return normalized;
        } catch(Exception e) {
            return null;
        }
    }
}
