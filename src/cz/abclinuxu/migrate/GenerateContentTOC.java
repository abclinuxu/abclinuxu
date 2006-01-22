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

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

/**
 * Creates TOC for content with hierarchy.
 * @author literakl
 * @since 21.1.2006
 */
public class GenerateContentTOC {

    public static void main(String[] args) throws IOException {
        if (args.length!=1)
            exitWithHelp("Usage: GenerateContentTOC 1234\n where 1234 is relation id of toplevel document");
        int rid = Misc.parseInt(args[0], -1);
        if (rid==-1)
            exitWithHelp("Usage: GenerateContentTOC 1234\n where 1234 is relation id of toplevel document");

        Persistance persistance = PersistanceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();

        Relation top = (Relation) persistance.findById(new Relation(rid));
        if (!(top.getChild() instanceof Item))
            exitWithHelp("Relation must point to content!");
        Item item = (Item) persistance.findById(top.getChild());
        if (item.getType()!=Item.CONTENT)
            exitWithHelp("Relation must point to content!");

        System.out.print("Enter yes to generate TOC for '"+Tools.childName(top)+"': ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String response = reader.readLine();
        if (!"yes".equals(response))
            exitWithHelp("Expected yes but got "+response);

        Item tocItem = new Item(0, Item.TOC);
        tocItem.setData("<data><name>Obsah</name></data>");
        persistance.create(tocItem);
        Relation tocRelation = new Relation(top.getChild(), tocItem, top.getId());
        persistance.create(tocRelation);
        String tocId = Integer.toString(tocRelation.getId());

        Element parentElement, element;
        Relation current;
        Date updated;
        StackItem stackItem;

        List stack = new ArrayList(), children;
        stack.add(new StackItem(top, tocItem.getData().getRootElement()));
        while (stack.size() > 0) {
            stackItem = (StackItem) stack.remove(0);
            current = stackItem.relation;
            parentElement = stackItem.parent;
            item = (Item) current.getChild();
            element = parentElement.addElement("node").addAttribute("rid", Integer.toString(current.getId()));

            DocumentHelper.makeElement(item.getData(), "//data/toc").setText(tocId);
            updated = item.getUpdated();
            persistance.update(item);
            sqlTool.setUpdatedTimestamp(item, updated);

            children = item.getChildren();
            Tools.syncList(children);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                current = (Relation) iter.next();
                if (!(current.getChild() instanceof Item))
                    continue;
                if (((Item)current.getChild()).getType() != Item.CONTENT)
                    continue;
                stack.add(new StackItem(current, element));
            }
        }

        persistance.update(tocItem);
    }

    static class StackItem {
        Relation relation;
        Element parent;

        public StackItem(Relation relation, Element parent) {
            this.relation = relation;
            this.parent = parent;
        }
    }

    private static void exitWithHelp(String help) {
        System.out.println(help);
        System.exit(1);
    }
}
