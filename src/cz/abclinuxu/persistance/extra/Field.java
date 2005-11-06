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
package cz.abclinuxu.persistance.extra;

/**
 * Constants for database columns.
 */
public class Field {
    public static final Field CREATED = new Field("CREATED");
    public static final Field UPDATED = new Field("UPDATED");
    public static final Field ID = new Field("ID");
    public static final Field DATA = new Field("DATA");
    public static final Field TYPE = new Field("TYPE");
    public static final Field SUBTYPE = new Field("SUBTYPE");
    public static final Field OWNER = new Field("OWNER");
    public static final Field UPPER = new Field("UPPER");

    private final String myName; // for debug only

    private Field(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
