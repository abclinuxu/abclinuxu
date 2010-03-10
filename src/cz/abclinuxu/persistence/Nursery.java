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
package cz.abclinuxu.persistence;

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistence.cache.ChildrenCache;

import java.util.*;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * This class is responsible for loading children of the GenericObjects.
 * It also preserves its cache.
 */
public class Nursery implements Configurable {
    static Logger log = Logger.getLogger(Nursery.class);

    public static final String PREF_CACHE_CLASS = "cache.class";
    public static final String PREF_IGNORE_SECTION = "ignore.section";

    protected static Nursery singleton;
    static {
        singleton = new Nursery();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    protected ChildrenCache cache;
    protected Map noChildren;
    protected Persistence persistence;


    /**
     * Limit access to new instances.
     */
    protected Nursery() {
    }

    /**
     * Public access to instance of this class.
     * @return singleton instance
     */
    public static Nursery getInstance() {
        return singleton;
    }

    /**
     * If relation.getParent() is stored in cache, then relation.getChild()
     * is inserted in its list of children.
     * @param relation relation to store.
     */
    public synchronized void addChild(Relation relation) {
        GenericObject parent = relation.getParent();
        if (isChildrenLoadingForbidden(parent))
            return;

        List<Integer> list = cache.get(parent);
        if ( list == null ) {
            List<Relation> found = persistence.findChildren(parent);
            storeChildren(parent, found);
            list = cache.get(parent);
        }

        if (list == null) {
            list = new ArrayList<Integer>(2);
            cache.put(parent.makeLightClone(), list);
        }

        if (! list.contains(relation.getId()))
            list.add(relation.getId());
    }

    /**
     * If relation.getParent() is stored in cache, the relation.getChild()
     * is removed from its list of children.
     * @param relation relation to be removed.
     */
    public synchronized void removeChild(Relation relation) {
        List<Integer> list = cache.get(relation.getParent());
        if (list == null)
            return;
        list.remove(new Integer(relation.getId()));
    }

    /**
     * Removes object and its children from the cache. This method is not recursive
     * but children of object's children will never be used again and they will be
     * removed automatically soon.
     * @param obj object to be removed
     */
    public synchronized void removeParent(GenericObject obj) {
        cache.remove(obj);
    }

    /**
     * Loads list of children for given object. If the list is not
     * cached, it is retrieved from database first (and cached).
     * The children are always uninitialized.
     * @param object objects, whose children caller wish to retrieve
     * @return List of children
     * @throws cz.abclinuxu.exceptions.PersistenceException if there is database related error.
     */
    public synchronized List<Relation> getChildren(GenericObject object) {
        if (isChildrenLoadingForbidden(object))
            return Collections.emptyList();

        List<Integer> list = cache.get(object);
        if (list != null) { // children of this object are already cached
            if (list.size() == 0)
                return Collections.emptyList();

            List<Relation> copy = new ArrayList<Relation>(list.size());
            for (Iterator<Integer> iter = list.iterator(); iter.hasNext();)
                copy.add(new Relation(iter.next()));

            persistence.synchronizeList(copy);

            return copy;
        }

        // children of this object must be seeked in persistence first
        List<Relation> dbList = persistence.findChildren(object);
        storeChildren(object, dbList);

        List<Relation> copy = new ArrayList<Relation>(dbList.size());
        for (Iterator iter = dbList.iterator(); iter.hasNext();)
            copy.add(cloneRelation((Relation) iter.next()));

        return copy;
    }

    /**
     * Loads children of specified objects into cache. Calling getChildren()
     * for each object will be then much faster.
     * @param objects list of GenericObjects
     */
    public synchronized void initChildren(List objects) {
        objects = new ArrayList(objects);
        GenericObject object;
        List<Integer> list;
        for (Iterator iter = objects.iterator(); iter.hasNext();) {
            object = (GenericObject) iter.next();
            if (isChildrenLoadingForbidden(object)) { // shall not be cached
                iter.remove();
                continue;
            }

            list = cache.get(object); // is already cached
            if (list != null)
                iter.remove();
        }

        Map<GenericObject, List<Relation>> fetchedChildren = persistence.findChildren(objects);
        for (Iterator iter = fetchedChildren.keySet().iterator(); iter.hasNext();) {
            GenericObject obj = (GenericObject) iter.next();
            storeChildren(obj, fetchedChildren.get(obj));
        }
    }

    /**
     * Flushes the content of the cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Stores children of specified GenericObject into cache.
     * @param object object
     * @param list list of object's child relations
     */
    private void storeChildren(GenericObject object, List<Relation> list) {
        List<Integer> ids = new ArrayList<Integer>(list.size() + 1);
        for (Iterator<Relation> iter = list.iterator(); iter.hasNext();) {
            Relation relation = iter.next();
            ids.add(relation.getId());
        }
        cache.put(object.makeLightClone(), ids);
    }

    /**
     * @return true if object's children must not be loaded from database or stored in cache.
     */
    protected boolean isChildrenLoadingForbidden(GenericObject object) {
        if (object instanceof Category) {
            Category category = (Category) object;
            switch (category.getType()) {
                case Category.FORUM:
                case Category.BLOG:
                case Category.SECTION:
                case Category.FAQ:
                    return true;
            }
        }
        return noChildren.get(object) != null;
    }

    /**
     * Creates deep clone of the relation. Its parent and child
     * properties are cloned too (light).
     */
    private Relation cloneRelation(Relation relation) {
        Relation clone = (Relation) relation.makeLightClone();
        if (relation.isInitialized()) {
            clone.synchronizeWith(relation);
            clone.setParent(relation.getParent().makeLightClone());
            clone.setChild(relation.getChild().makeLightClone());
        }
        return clone;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public synchronized void configure(Preferences prefs) throws ConfigurationException {
        String className = prefs.get(PREF_CACHE_CLASS, null);
        if (className == null)
            throw new ConfigurationException("You must provide valid cache class name!");
        try {
            Class aClass = Class.forName(className);
            cache = (ChildrenCache) aClass.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Cannot instantiate cache " + className + "!", e);
        }

        persistence = PersistenceFactory.getPersistence();

        // content of these sections shall not be loaded!
        noChildren = new HashMap();
        String tmp = prefs.get(PREF_IGNORE_SECTION, "");
        StringTokenizer stk = new StringTokenizer(tmp, ",");
        while ( stk.hasMoreTokens() ) {
            String key = PREF_IGNORE_SECTION+"."+stk.nextToken();
            String values = prefs.get(key, "");

            StringTokenizer stk2 = new StringTokenizer(values, ",");
            while ( stk2.hasMoreTokens() ) {
                Category category = new Category(Misc.parseInt(stk2.nextToken(), 0));
                noChildren.put(category, Boolean.TRUE);
            }
        }
    }
}
