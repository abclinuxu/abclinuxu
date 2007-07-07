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
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.servlets.Constants;

import java.util.LinkedList;

import org.dom4j.DocumentHelper;

/**
 * Open hardware categories are now stored differently.
 * @author literakl
 * @since 20.3.2006
 */
public class UpgradeHardwareCategories {

    public static void main(String[] args) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = new Relation(Constants.REL_HARDWARE);
        Category category;
        GenericObject obj;

        long start = System.currentTimeMillis();
        int i = 0, j = 0;
        LinkedList stack = new LinkedList();
        stack.add(relation);
        while (stack.size() > 0) {
            relation = (Relation) stack.removeFirst();
            relation = (Relation) persistence.findById(relation);
            obj = relation.getChild();
            if ( ! (obj instanceof Category) )
                continue;

            category = (Category) persistence.findById(obj);
            if (category.getType() == 0) {
                category.setType(Category.HARDWARE_SECTION);
                i++;
            } else {
                DocumentHelper.makeElement(category.getData(), "/data/writeable").setText("true");
                j++;
            }
            persistence.update(category);
            stack.addAll(category.getChildren());
        }
        long end = System.currentTimeMillis();
        System.out.println("Conversion took "+(end-start)/1000+" seconds, "+j+" open sections, "+i+" closed sections");
    }
}
