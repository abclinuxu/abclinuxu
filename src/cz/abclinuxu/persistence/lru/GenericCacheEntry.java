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
package cz.abclinuxu.persistence.lru;

/**
 * A structure used to store values in a GenericCache.  It
 * is declared with default access to limit it to use only within the
 * package.
 *
 * @author <a href="mailto:oro-dev@jakarta.apache.org">Daniel F. Savarese</a>
 * @version @version@
 * @since 1.0
 */
final class GenericCacheEntry implements java.io.Serializable {
    /** The cache array index of the entry. */
    int _index;
    /** The value stored at this entry. */
    Object _value;
    /** The key used to store the value. */
    Object _key;

    GenericCacheEntry(int index) {
        _index = index;
        _value = null;
        _key   = null;
    }

    public String toString() {
        return _key+"->"+_value;
    }
}
