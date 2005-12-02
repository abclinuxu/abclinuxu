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

import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;

/**
 * Tool that migrates from original system of one item with many records to single item.
 * If there are multiple records, they are stored as revisions and merged together.
 * @author literakl
 * @since 27.11.2005
 */
public class UpgradeHardware {
    Persistance persistance = PersistanceFactory.getPersistance();
    SQLTool sqlTool = SQLTool.getInstance();

    void run() throws Exception {
        int count = sqlTool.countItemRelationsWithType(Item.HARDWARE, new Qualifier[]{});
        int i = 0;
        while (i<count) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 50)};
            List items = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(items);
            i += items.size();
        }
    }

    void migrateItem(Relation relation) throws Exception {

    }

    public static void main(String[] args) throws Exception {
        UpgradeHardware task = new UpgradeHardware();
        task.run();
    }
}
