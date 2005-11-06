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
package cz.abclinuxu.utils.format;

/**
 * Holds text formats, that Render can understand.
 */
public class Format {
    /** Empty line is replaced with P tag, it may contain HTML tags except P, DIV and PRE. */
    public static final Format SIMPLE = new Format("SIMPLE",0);
    /** HTML-formatted text */
    public static final Format HTML = new Format("HTML",1);
    /** new proposed format similar to wiki */
    public static final Format WIKI = new Format("WIKI",2);

    private final String myName;
    private final int id;

    private Format(String name, int id) {
        myName = name;
        this.id = id;
    }

    public String toString() {
        return myName;
    }

    public int getId() {
        return id;
    }
}
