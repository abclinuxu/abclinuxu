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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FixRoyalties {
    private Persistence persistence = PersistenceFactory.getPersistance();
    private SQLTool sqlTool = SQLTool.getInstance();
    private Map authors;
    private User admin = new User(1);

    /**
     * Fixes royalties, author shall be stored in subtype
     */
    void run() throws Exception {
        int total, i;
        Item item;
        authors = initAuthors();
        total = sqlTool.countItemsWithType(Item.ROYALTIES);
        for (i = 0; i < total;) {
            List data = sqlTool.findItemsWithType(Item.ROYALTIES, i, 100);
            i += data.size();

            for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                item = (Item) iter2.next();
                processRoyalty(item);
            }
        }
        System.out.println("Migrated " + total + " royalties");
    }

    Map initAuthors() {
        Map authors = new HashMap();
        List relations = sqlTool.findItemRelationsWithType(Item.AUTHOR, null);
        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            authors.put(relation.getChild().getId(), relation.getId());
        }
        return authors;
    }

    /**
     * Fixes royalty object.
     */
    void processRoyalty(Item royalty) throws Exception {
        int id = royalty.getOwner();
        Number rid = (Number) authors.get(id);
        if (rid == null) {
            System.err.println("Failed to migrate royalty "+royalty.getId()+", author "+id+" was not found!");
        }
        royalty.setSubType(rid.toString());
        persistence.update(royalty);
    }


    public static void main(String[] args) throws Exception {
        FixRoyalties task = new FixRoyalties();
        task.run();
    }
}
