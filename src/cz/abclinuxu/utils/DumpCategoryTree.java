/*
 * User: literakl
 * Date: Oct 26, 2002
 * Time: 6:08:18 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.servlets.Constants;

import java.io.*;
import java.util.*;

/**
 * Thjis class creates directory tree, which looks
 * similar to tree of Abc objects. Useful for
 * discussion administrators.
 */
public class DumpCategoryTree {
    Persistance persistance = PersistanceFactory.getPersistance(PersistanceFactory.defaultUrl,EmptyCache.class);
    HashMap map = new HashMap(1000);

    public void dumpTree(File parent, Relation relation) throws Exception {
        Relation i = new Relation(relation.getId());
        map.put(i,i);
        persistance.synchronize(relation);
        if ( ! (relation.getChild() instanceof Category) )
            return;

        Category category = (Category) relation.getChild();
        String name = Tools.xpath(category,"data/name")+" ("+relation.getId()+")";
        File current = new File(parent,name);
        current.mkdir();

        List children = category.getContent();
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
        dumper.dumpTree(current, new Relation(Constants.REL_ABC));
        dumper.dumpTree(current, new Relation(Constants.REL_HARDWARE));
        dumper.dumpTree(current, new Relation(Constants.REL_SOFTWARE));
        dumper.dumpTree(current, new Relation(Constants.REL_FORUM));

        System.out.println("Strom ulo¾en do adresáøe "+current.getAbsolutePath()+".");
    }
}
