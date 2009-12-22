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

/**
 * Constants for operations.
 */
public class Operation {
    public static final Operation SMALLER = new Operation("SMALLER");
    public static final Operation SMALLER_OR_EQUAL = new Operation("SMALLER_OR_EQUAL");
    public static final Operation GREATER = new Operation("GREATER");
    public static final Operation GREATER_OR_EQUAL = new Operation("GREATER_OR_EQUAL");
    public static final Operation EQUAL = new Operation("EQUAL");
    public static final Operation NOT_EQUAL = new Operation("NOT_EQUAL");
    public static final Operation LIKE = new Operation("LIKE");
    public static final Operation NOT_LIKE = new Operation("NOT LIKE");
    public static final Operation IS_NULL = new Operation("IS NULL");
    public static final Operation IS_NOT_NULL = new Operation("IS NOT NULL");

    private final String myName; // for debug only

    protected Operation(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
