/*
 *  Copyright (C) 2007 Leos Literak
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

import cz.abclinuxu.persistence.Nursery;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Relation;

/**
 * Abstract parent that encapsulates cache logic. The implementing
 * classes must fullfill Cache contract and API. The persistance
 * logic must be kept here, so any changes will apply to all implementations.
 * @author literakl
 * @since 25.2.2007
 */
public abstract class AbstractCache implements TransparentCache {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractCache.class);

    protected abstract void storeObject(GenericObject obj);

    protected abstract GenericObject loadObject(GenericObject obj);

    protected abstract void removeObject(GenericObject obj);

    public void store(GenericObject obj) {
        try {
            GenericObject cached = (GenericObject) ((Class)obj.getClass()).newInstance();
            cached.synchronizeWith(obj);
            if (cached instanceof Relation) {
                Relation rel = (Relation) cached;
                rel.setParent(rel.getParent().makeLightClone());
                rel.setChild(rel.getChild().makeLightClone());
            }
            storeObject(cached);
        } catch (Exception e) {
            log.error("Cloning failed", e);
        }
    }

    public GenericObject load(GenericObject obj) {
        GenericObject result = (GenericObject) loadObject(obj);
        if (result == null)
            return null;

        if (result instanceof Relation) {
            Relation rel = new Relation((Relation) result);
            rel.setParent(rel.getParent().makeLightClone());
            rel.setChild(rel.getChild().makeLightClone());
            result = rel;
        } else {
            try {
                GenericObject o = (GenericObject) ((Class)result.getClass()).newInstance();
                o.synchronizeWith(result);
                result = o;
            } catch (Exception e) {
                log.error("Cannot clone " + result, e);
            }
        }
        return result;
    }

    public void remove(GenericObject obj) {
        removeObject(obj);
        // todo this shall be handled at application level. why it doesn't print anything to logs. really?
        if (obj instanceof Relation)
            Nursery.getInstance().removeChild((Relation) obj);
        else
            Nursery.getInstance().removeParent(obj);
    }
}
