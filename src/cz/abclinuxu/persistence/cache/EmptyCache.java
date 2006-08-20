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
package cz.abclinuxu.persistence.cache;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.persistence.Cache;

/**
 * Version of cache, which does nothing.
 */
public class EmptyCache implements Cache {
    public EmptyCache() {
    }

    public void store(GenericObject obj) {
    }

    public GenericObject load(GenericObject obj) {
        return null;
    }

    public void remove(GenericObject obj) {
    }

    public void clear() {
    }
}
