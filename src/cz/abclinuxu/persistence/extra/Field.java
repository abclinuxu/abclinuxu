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
    public static final Field CREATED = new Field("CREATED", "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.DATA, PersistenceMapping.Table.COMMENT});
    public static final Field UPDATED = new Field("UPDATED", "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.DATA});
    public static final Field ID = new Field("ID", null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.COMMENT, PersistenceMapping.Table.RELATION,
                                           PersistenceMapping.Table.SERVER, PersistenceMapping.Table.LINK,
                                           PersistenceMapping.Table.USER, PersistenceMapping.Table.DATA});
    public static final Field TITLE = new Field("TITLE", "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field DATA = new Field("DATA", null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.COMMENT, PersistenceMapping.Table.RELATION,
                                           PersistenceMapping.Table.USER, PersistenceMapping.Table.DATA});
    public static final Field TYPE = new Field("TYPE", null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.STATISTICS,
                                           PersistenceMapping.Table.ACTION, PersistenceMapping.Table.DATA} );
    public static final Field SUBTYPE = new Field("SUBTYPE", null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field OWNER = new Field("OWNER", "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.LINK, PersistenceMapping.Table.DATA});
    public static final Field UPPER = new Field("UPPER", "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field PARENT_TYPE = new Field("PARENT_TYPE", "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field PARENT = new Field("PARENT", "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field CHILD_TYPE = new Field("CHILD_TYPE", "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field CHILD = new Field("CHILD", "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field DAY = new Field("DAY", null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.STATISTICS});
    public static final Field WHEN = new Field("WHEN", null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.LAST_SEEN_COMMENT});

    private final String myName; // for debug only
    private Set<PersistenceMapping.Table> compatibility;
    private String defaultTableNick;

    private Field(String name, String defaultMapping, PersistenceMapping.Table[] tables) {
        myName = name;
        if (tables == null) {
            compatibility = Collections.emptySet();
            return;
        }
        compatibility = new HashSet<PersistenceMapping.Table>(tables.length, 1.0f);
        for (int i = 0; i < tables.length; i++) {
            PersistenceMapping.Table table = tables[i];
            compatibility.add(table);
        }
        this.defaultTableNick = defaultMapping;
    }

    /**
     * Test if this field is compatible with given table
     * @param table
     * @return true of field is compatible
     */
    public boolean isCompatible(PersistenceMapping.Table table) {
        return compatibility.contains(table);
    }

    /**
     * @return default table nick name
     */
    public String getDefaultTableNick() {
        return defaultTableNick;
    }

    public String toString() {
        return myName;
    }
}
