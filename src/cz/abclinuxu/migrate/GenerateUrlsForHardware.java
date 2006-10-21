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

import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.URLManager;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author literakl
 * @since 21.10.2006
 */
public class GenerateUrlsForHardware {
    static Persistence persistence = PersistenceFactory.getPersistance();
    static SQLTool sqlTool = SQLTool.getInstance();
    static HashMap indexed = new HashMap(10000, 0.99f);

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        int totalItems = 0, totalSections = 0;
        System.out.print("Starting to walk through hardware section ..");

        Relation relation;
        GenericObject child;
        List stackSections = new ArrayList(100);
        List stackItems = new ArrayList(5000);

        Relation hardware = (Relation) Tools.sync(new Relation(Constants.REL_HARDWARE));
        stackSections.add(hardware);
        while (stackSections.size() > 0) {
            relation = (Relation) stackSections.remove(0);
            child = relation.getChild();
//            if (hasBeenIndexed(child))
//                continue;

            if (child instanceof Category) {
                if (relation.getUrl() == null) {
                    if ( ! setURL(relation) ) {
                        stackSections.add(relation);
                        continue;
                    } else
                        totalSections++;
                }

                List children = child.getChildren();
                Tools.syncList(children);
                stackSections.addAll(children);
            } else {
                stackItems.add(relation);
                continue;
            }
        }

        while (stackItems.size() > 0) {
            relation = (Relation) stackItems.remove(0);
            if (!(relation.getChild() instanceof Item)) {
                System.out.println("plete se tu neco jineho nez polozka!"+relation);
                continue;
            }
            if (relation.getUrl() == null) {
                setURL(relation);
                totalItems++;
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Url set for " + totalSections + " sections and "+totalItems+" items. " +
                           "Total time: " + (end - start) / 1000 + " seconds");
    }

    static boolean setURL(Relation relation) throws Exception {
        int upper = relation.getUpper();
        if (upper == 0)
            throw new Exception("Relace predka neni definovana!" + relation);
        Relation parentRelation = (Relation) persistence.findById(new Relation(upper));
        if (parentRelation.getUrl() == null)
            return false;

        String title = Tools.xpath(relation.getChild(), "data/name");
        String url = parentRelation.getUrl() + "/" + URLManager.enforceLastURLPart(title);
        url = URLManager.protectFromDuplicates(url);
        if (url != null) {
            relation.setUrl(url);
            persistence.update(relation);
        }
        return true;
    }

    static boolean hasBeenIndexed(GenericObject child) throws Exception {
        if (indexed.containsKey(child))
            return true;
        else {
            GenericObject key = (GenericObject) child.getClass().newInstance();
            key.setId(child.getId());
            indexed.put(key, Boolean.TRUE);
            return false;
        }
    }
}
