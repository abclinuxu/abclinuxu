/*
 * User: literakl
 * Date: 29.2.2004
 * Time: 8:22:19
 */
package cz.abclinuxu.persistance;

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
    protected Persistance persistance;


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
     * If relation.getParent() is stored in cache, the relation.getChild()
     * is inserted in its list of children.
     * @param relation relation to store.
     */
    public synchronized void addChild(Relation relation) {
        List list = (List) cache.get(relation.getParent());
        if ( list==null )
            list = getChildrenInternal(relation.getParent());
        if (list.size()==0) { // Collections.EMPTY_LIST is read-only
            list = new ArrayList(2);
            cache.put(relation.getParent(), list);
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
        boolean removed = list.remove(relation);
        if (!removed)
            log.warn("Failed to delete child relation "+relation);
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
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is database related error.
     */
    public synchronized List getChildren(GenericObject object) {
        return getChildrenInternal(object);
    }

    protected List getChildrenInternal(GenericObject object) {
        if ( noChildren.get(object)!=null )
            return Collections.EMPTY_LIST; // content of this object might be too big

        List list = (List) cache.get(object);
        if (list==null) {
            list = persistance.findChildren(object);
            cache.put(object, list);
        }

        if (list.size()==0)
            return Collections.EMPTY_LIST;

        List copy = new ArrayList(list.size());
        for ( Iterator iter = list.iterator(); iter.hasNext(); )
            copy.add(cloneRelation((Relation) iter.next()));

        return copy;
    }

    /**
     * Creates deep clone of the relation. Its parent and child
     * properties are cloned too (light).
     */
    private Relation cloneRelation(Relation relation) {
        Relation clone = (Relation) relation.makeLightClone();
        if ( relation.isInitialized() ) {
            clone.setUpper(relation.getUpper());
            clone.setData(relation.getData());
            clone.setInitialized(true);
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

        persistance = PersistanceFactory.getPersistance();

        // content of these sections shall not be loaded!
        noChildren = new HashMap(100, 0.95f);
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
