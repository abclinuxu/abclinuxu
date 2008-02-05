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
package cz.abclinuxu.persistence.extra;

import cz.abclinuxu.persistence.PersistenceMapping;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Constants for database columns.
 */
public class Field {
    public static final Field CREATED = new Field("CREATED",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.COMMENT});
    public static final Field UPDATED = new Field("UPDATED",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL});
    public static final Field ID = new Field("ID",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.COMMENT, PersistenceMapping.Table.RELATION,
                                           PersistenceMapping.Table.SERVER, PersistenceMapping.Table.LINK,
                                           PersistenceMapping.Table.USER});
    public static final Field DATA = new Field("DATA",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.COMMENT, PersistenceMapping.Table.RELATION,
                                           PersistenceMapping.Table.USER});
    public static final Field TYPE = new Field("TYPE",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.STATISTICS,
                                           PersistenceMapping.Table.ACTION} );
    public static final Field SUBTYPE = new Field("SUBTYPE",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD});
    public static final Field OWNER = new Field("OWNER",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.LINK});
    public static final Field UPPER = new Field("UPPER", new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field PARENT_TYPE = new Field("PARENT_TYPE", new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field PARENT = new Field("PARENT", new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field CHILD_TYPE = new Field("CHILD_TYPE", new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field CHILD = new Field("CHILD", new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field DAY = new Field("DAY", new PersistenceMapping.Table[]{PersistenceMapping.Table.STATISTICS});
    public static final Field WHEN = new Field("WHEN", new PersistenceMapping.Table[]{PersistenceMapping.Table.LAST_SEEN_COMMENT});

    private final String myName; // for debug only
    private Set compatibility;

    private Field(String name, PersistenceMapping.Table[] tables) {
        myName = name;
        if (tables == null) {
            compatibility = Collections.EMPTY_SET;
            return;
        }
        compatibility = new HashSet(tables.length, 1.0f);
        for (int i = 0; i < tables.length; i++) {
            PersistenceMapping.Table table = tables[i];
            compatibility.add(table);
        }
    }

    /**
     * Test if this field is compatible with given table
     * @param table
     * @return true of field is compatible
     */
    public boolean isCompatible(PersistenceMapping.Table table) {
        return compatibility.contains(table);
    }

    public String toString() {
        return myName;
    }
}
