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
import cz.abclinuxu.servlets.utils.VelocityHelper;

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

    public void dumpTree(Relation relation, Writer writer) throws Exception {
        Relation i = new Relation(relation.getId());
        map.put(i,i);
        persistance.synchronize(relation);
        if ( ! (relation.getChild() instanceof Category) )
            return;

        Category category = (Category) relation.getChild();
        String name = VelocityHelper.getXPath(category,"data/name")+" ("+relation.getId()+")";
        writer.write("mkdir \""+name+"\"\n");
        writer.write("cd \""+name+"\"\n");

        List children = category.getContent();
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if ( map.get(new Relation(child.getId()))!=null )
                continue;
            dumpTree(child,writer);
        }
        writer.write("cd ..\n");
    }

    public static void main(String[] args) throws Exception {
        DumpCategoryTree dumper = new DumpCategoryTree();
        BufferedWriter writer = new BufferedWriter(new FileWriter("create_tree.sh"));
        writer.write("#!/bin/sh\n");

        dumper.dumpTree(new Relation(Constants.REL_ARTICLES), writer);
        dumper.dumpTree(new Relation(Constants.REL_ABC), writer);
        dumper.dumpTree(new Relation(Constants.REL_HARDWARE), writer);
        dumper.dumpTree(new Relation(Constants.REL_SOFTWARE), writer);

        writer.close();
    }
}
