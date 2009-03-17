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
    public static final Field CREATED = new Field(Id.CREATED, "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.DATA, PersistenceMapping.Table.COMMENT});
    public static final Field UPDATED = new Field(Id.UPDATED, "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.DATA});
    public static final Field ID = new Field(Id.ID, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.COMMENT, PersistenceMapping.Table.RELATION,
                                           PersistenceMapping.Table.SERVER, PersistenceMapping.Table.LINK,
                                           PersistenceMapping.Table.USER, PersistenceMapping.Table.DATA});
    public static final Field TITLE = new Field(Id.TITLE, "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field DATA = new Field(Id.DATA, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.COMMENT, PersistenceMapping.Table.RELATION,
                                           PersistenceMapping.Table.USER, PersistenceMapping.Table.DATA});
    public static final Field TYPE = new Field(Id.TYPE, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.STATISTICS,
                                           PersistenceMapping.Table.ACTION, PersistenceMapping.Table.DATA} );
    public static final Field SUBTYPE = new Field(Id.SUBTYPE, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field NUMERIC1 = new Field(Id.NUMERIC1, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field NUMERIC2 = new Field(Id.NUMERIC2, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field STRING1 = new Field(Id.STRING1, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field STRING2 = new Field(Id.STRING2, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field DATE1 = new Field(Id.DATE1, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field DATE2 = new Field(Id.DATE2, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.DATA});
    public static final Field OWNER = new Field(Id.OWNER, "S",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.CATEGORY, PersistenceMapping.Table.ITEM,
                                           PersistenceMapping.Table.RECORD, PersistenceMapping.Table.POLL,
                                           PersistenceMapping.Table.LINK, PersistenceMapping.Table.DATA});
    public static final Field UPPER = new Field(Id.UPPER, "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field PARENT_TYPE = new Field(Id.PARENT_TYPE, "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field PARENT = new Field(Id.PARENT, "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field CHILD_TYPE = new Field(Id.CHILD_TYPE, "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field CHILD = new Field(Id.CHILD, "R",
            new PersistenceMapping.Table[]{PersistenceMapping.Table.RELATION});
    public static final Field DAY = new Field(Id.DAY, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.STATISTICS});
    public static final Field WHEN = new Field(Id.WHEN, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.LAST_SEEN_COMMENT});
    public static final Field LOGIN = new Field(Id.LOGIN, null,
            new PersistenceMapping.Table[]{PersistenceMapping.Table.USER});
    public static final Field COUNTER = new Field(Id.COUNTER, null,
    		null);

    private final Id id;
    private Set<PersistenceMapping.Table> compatibility;
    private String defaultTableNick;

    private Field(Id id, String defaultMapping, PersistenceMapping.Table[] tables) {
        this.id = id;
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

    public Id getId() {
        return id;
    }

    public String toString() {
        return id.toString();
    }

    public enum Id {
        CHILD,
        CHILD_TYPE,
        COUNTER,
        CREATED,
        DATA,
        DATE1,
        DATE2,
        DAY,
        ID,
        LOGIN,
        NUMERIC1,
        NUMERIC2,
        OWNER,
        PARENT,
        PARENT_TYPE,
        STRING1,
        STRING2,
        SUBTYPE,
        TITLE,
        TYPE,
        UPDATED,
        UPPER,
        WHEN
    }
}
