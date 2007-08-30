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
package cz.abclinuxu.misc;

import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.cache.EmptyCache;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.freemarker.Tools;

import java.io.*;
import java.util.*;

/**
 * Thjis class creates directory tree, which looks
 * similar to tree of Abc objects. Useful for
 * discussion administrators.
 */
public class DumpCategoryTree {
    Persistence persistence = PersistenceFactory.getPersistence(PersistenceFactory.defaultUrl,EmptyCache.class);
    HashMap map = new HashMap(1000);

    public void dumpTree(File parent, Relation relation) throws Exception {
        Relation i = new Relation(relation.getId());
        map.put(i,i);
        persistence.synchronize(relation);
        if ( ! (relation.getChild() instanceof Category) )
            return;

        Category category = (Category) relation.getChild();
        String name = Tools.xpath(category,"data/name")+" ("+relation.getId()+")";
        File current = new File(parent,name);
        current.mkdir();

        List children = category.getChildren();
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if ( map.get(new Relation(child.getId()))!=null )
                continue;
            dumpTree(current,child);
        }
    }

    public static void main(String[] args) throws Exception {
        DumpCategoryTree dumper = new DumpCategoryTree();
        String home = System.getProperty("user.home");
        File current = new File(home,"abc_tree");
        current.mkdirs();

        dumper.dumpTree(current, new Relation(Constants.REL_ARTICLES));
        dumper.dumpTree(current, new Relation(Constants.REL_HARDWARE));
        dumper.dumpTree(current, new Relation(Constants.REL_SOFTWARE));
        dumper.dumpTree(current, new Relation(Constants.REL_FORUM));

        System.out.println("Strom uložen do adresáře "+current.getAbsolutePath()+".");
    }
}
