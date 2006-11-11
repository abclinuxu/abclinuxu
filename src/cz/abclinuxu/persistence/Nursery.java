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

import java.util.*;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * This class is responsible for loading children of the GenericObjects.
 * It also preserves its cache.
 */
public class Nursery implements Configurable {
    static Logger log = Logger.getLogger(Nursery.class);
    public static final String PREF_CACHE_SIZE = "cache.size";
    public static final String PREF_NO_CHILDREN_FOR_SECTION = "no.children.for.section";

    protected static Nursery singleton;
    static {
        singleton = new Nursery();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    protected Map cache;
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
        if (isNotCached(parent))
            return;
        List list = (List) cache.get(parent);
        if ( list==null )
            list = getChildrenInternal(parent);
        if (list.size()==0) { // Collections.EMPTY_LIST is read-only
            list = new ArrayList(2);
            cache.put(parent, list);
        }
        if (list.contains(relation))
            return;
        Relation clone = cloneRelation(relation);
        list.add(clone);
    }

    /**
     * If relation.getParent() is stored in cache, the relation.getChild()
     * is removed from its list of children.
     * @param relation relation to be removed.
     */
    public synchronized void removeChild(Relation relation) {
        List list = (List) cache.get(relation.getParent());
        if (list==null)
            return;
        list.remove(relation);
//        boolean removed = list.remove(relation);
//        if (!removed) // stane se pri asynchronnim pristupu. Proc to neni cele synchronizovane?
//            log.warn("Failed to delete child relation "+relation);
    }

    /**
     * Recursively removes object and its children from the cache.
     * @param obj object to be removed
     */
    public synchronized void removeParent(GenericObject obj) {
        List stack = new ArrayList();
        stack.add(obj);
        do {
            List list = (List) cache.remove(stack.remove(0));
            if ( list==null )
                continue;
            for ( Iterator iter = list.iterator(); iter.hasNext(); ) {
                Relation relation = (Relation) iter.next();
                stack.add(relation.getChild());
            }
        } while (stack.size()>0);
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
        return getChildrenInternal(object);
    }

    /**
     * Loads children of specified objects into cache. Calling getChildren()
     * for each object will be then much faster.
     * @param objects list of GenericObjects
     */
    public synchronized void initChildren(List objects) {
        objects = new ArrayList(objects);
        GenericObject object;
        List list;
        for (Iterator iter = objects.iterator(); iter.hasNext();) {
            object = (GenericObject) iter.next();
            if (isNotCached(object))  // shall not be cached
                iter.remove();
            list = (List) cache.get(object); // is already cached
            if (list!=null)
                iter.remove();
        }

        Map fetchedChildren = persistence.findChildren(objects);
        for (Iterator iter = fetchedChildren.keySet().iterator(); iter.hasNext();) {
            GenericObject obj = (GenericObject) iter.next();
            cache.put(obj.makeLightClone(), fetchedChildren.get(obj));
        }
    }

    protected List<Relation> getChildrenInternal(GenericObject object) {
        if (isNotCached(object))
            return Collections.emptyList();

        List list = (List) cache.get(object);
        if (list==null) {
            list = persistence.findChildren(object);
            cache.put(object.makeLightClone(), list);
        }

        if (list.size()==0)
            return Collections.emptyList();

        List<Relation> copy = new ArrayList<Relation>(list.size());
        for ( Iterator iter = list.iterator(); iter.hasNext(); )
            copy.add(cloneRelation((Relation) iter.next()));

        return copy;
    }

    protected boolean isNotCached(GenericObject object) {
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
        if (noChildren.get(object) != null)
            return true;
        return false;
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
        int size = prefs.getInt(PREF_CACHE_SIZE, 100);
        log.info("Initializing with cache size "+size);
//        cache = Collections.synchronizedMap(new LinkedHashMap(size, 1.0f, true));
        cache = new ChildrenCache(size, 1.0f, true);

        persistence = PersistenceFactory.getPersistance();

        // content of these sections shall not be loaded!
        noChildren = new HashMap();
        Category category = null;
        String tmp = prefs.get(PREF_NO_CHILDREN_FOR_SECTION, "");
        StringTokenizer stk = new StringTokenizer(tmp, ",");
        while ( stk.hasMoreTokens() ) {
            String key = PREF_NO_CHILDREN_FOR_SECTION+"."+stk.nextToken();
            String values = prefs.get(key, "");

            StringTokenizer stk2 = new StringTokenizer(values, ",");
            while ( stk2.hasMoreTokens() ) {
                category = new Category(Misc.parseInt(stk2.nextToken(), 0));
                noChildren.put(category, Boolean.TRUE);
            }
        }
    }

    /**
     * LRU Cache. Removes oldest entries.
     */
    class ChildrenCache extends LinkedHashMap {
        int MAX_SIZE;

        public ChildrenCache(int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
            MAX_SIZE = initialCapacity;
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size()>MAX_SIZE;
        }
    }
}
