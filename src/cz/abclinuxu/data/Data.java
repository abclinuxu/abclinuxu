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
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

/**
 * Class for storage various data objects like images, logs, configuration files ..
 * Subtype shall contain content type, if known.
 */
public class Data extends GenericDataObject {
    public static final int IMAGE = 1;

    public Data() {
        super();
    }

    public Data(int id) {
        super(id);
    }

    public Data(int id, int type) {
        super(id);
        this.type = type;
    }

    public String toString() {
        return "Data: id=" + id;
    }

    public boolean preciseEquals(Object o) {
        if (!(o instanceof Data)) return false;
        if (id != ((GenericObject) o).getId()) return false;
        if (type != ((GenericDataObject) o).type) return false;
        if (owner != ((GenericDataObject) o).owner) return false;
        return Misc.same(getDataAsString(), ((GenericDataObject) o).getDataAsString());
    }

    public int hashCode() {
        String tmp = "Data"+id;
        return tmp.hashCode();
    }
}
