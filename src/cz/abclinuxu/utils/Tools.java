/*
 * User: Leos Literak
 * Date: Jan 4, 2003
 * Time: 3:23:18 PM
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.PreparedDiscussion;
import org.dom4j.Document;
import org.dom4j.Node;
import org.apache.log4j.Logger;

import java.util.*;

import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * Various utilities available for templates
 */
public class Tools {
    Logger log = Logger.getLogger(Tools.class);

    static Persistance persistance = PersistanceFactory.getPersistance();

    /**
     * Returns text value of node selected by xpath expression for GenericObject.
     * @throws PersistanceException if object cannot be synchronized
     */
    public static String xpath(GenericObject obj, String xpath) {
        if ( obj==null || !(obj instanceof XMLContainer) ) return null;
        if ( !obj.isInitialized() ) persistance.synchronize(obj);
        Document doc = ((XMLContainer)obj).getData();
        if ( doc==null ) return null;
        Node node = doc.selectSingleNode(xpath);
        return (node!=null)? node.getText() : null;
    }

    /**
     * @returns name of child in this relation.
     */
    public static String childName(Relation relation) {
        if ( relation==null || relation.getChild()==null ) return null;
        Document data = relation.getData();
        if ( data!=null ) {
            Node node = data.selectSingleNode("data/name");
            if ( node!=null )
                return node.getText();
        }

        GenericObject child = relation.getChild();
        if ( ! child.isInitialized() )
            persistance.synchronize(child);

        if ( child instanceof GenericDataObject ) {
            data = ((GenericDataObject)child).getData();
            if ( data!=null ) {
                Node node = data.selectSingleNode("data/name");
                if ( node!=null )
                    return node.getText();
                node = data.selectSingleNode("data/question");
                if ( node!=null )
                    return node.getText();
            }
        }

        if ( child instanceof Link )
            return ((Link)child).getText();

        if ( child instanceof Poll )
            return ((Poll)child).getText();

        if ( child instanceof User )
            return ((User)child).getName();

        if ( child instanceof Server )
            return ((Server)child).getName();

        String name = child.getClass().getName();
        name = name.substring(name.lastIndexOf('.')+1);
        name = name.concat(" "+child.getId());
        return name;
    }

    /**
     * Retrieves icon for child of this relation. If relation declares icon,
     * it will be used. Otherwise relation's child's icon will be returned.
     * If there is no icon at all, null will be returned.
     */
    public String childIcon(Relation relation) {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = xpath(relation,"data/icon");
        if ( ! Misc.empty(name) ) return name;
        return xpath(relation.getChild(),"data/icon");
    }

    /**
     * Calculates percentage with correct rounding. 0<=count<=base.
     * @return Number between 0 and 100
     */
    public int percent(int count, int base) {
        if ( base<=0 ) return 0;
        double percent = 100*count/(double)base;
        return (int)(percent+0.5);
    }

    /**
     * @return String containing one asterisk for each ten percents.
     */
    public String percentBar(int percent) {
        if ( percent<5 )       return "";
        else if ( percent<15 ) return "*";
        else if ( percent<25 ) return "**";
        else if ( percent<35 ) return "***";
        else if ( percent<45 ) return "****";
        else if ( percent<55 ) return "*****";
        else if ( percent<65 ) return "******";
        else if ( percent<75 ) return "*******";
        else if ( percent<85 ) return "********";
        else if ( percent<95 ) return "*********";
        else                   return "**********";
    }

    /**
     * New operator for servers. It parses list of ids
     * and returns list of servers having same id.
     * They are initialized already.
     */
    public List createServers(SimpleSequence seq) throws TemplateModelException {
        int size = seq.size();
        List list = new ArrayList();
        for (int i=0; i<size; i++) {
            TemplateNumberModel a = (TemplateNumberModel) seq.get(i);
            int id = a.getAsNumber().intValue();
            Server server = (Server) persistance.findById(new Server(id));
            list.add(server);
        }
        return list;
    }

    /**
     * If <code>str</code> is longer than <code>max</code>, it
     * is shortened to length of <code>max-suffix.length()</code>
     * and <code>suffix</code> is appended.
     */
    public String limit(String str, int max, String suffix) {
        if ( str==null || str.length()==0 ) return "";
        if ( str.length()<=max ) return str;

        if ( suffix==null ) suffix = "";
        StringBuffer sb = new StringBuffer(str);
        int suffixLength = suffix.length();
        if ( suffixLength>0 ) {
            sb.insert(max-suffixLength,suffix);
        }
        sb.setLength(max);
        return sb.toString();
    }

    /**
     * Extracts selected part of given list. Implementation
     * is aware of list's limits, so IndexOutOfBounds is never
     * thrown.
     */
    public List sublist(List list, int start, int count) {
        if ( list==null ) return null;
        if ( start>=list.size() ) return null;

        int end = start+count;
        if ( end>=list.size() ) end = list.size();
        return list.subList(start,end);
    }

    /**
     * Gathers statistics on given discussion.
     */
    public PreparedDiscussion analyzeDiscussion(Relation relation) {
        if ( !InstanceUtils.checkType(relation.getChild(),Item.class,Item.DISCUSSION) ) {
            log.error("Relation "+relation+" doesn't contain item!");
            return null;
        }
        Item item = (Item) relation.getChild();
        Date lastUpdate = item.getUpdated();
        int count = 0;

        for (Iterator iter = item.getContent().iterator(); iter.hasNext(); ) {
            Relation rel = (Relation) iter.next();
            sync(rel);
            if ( !InstanceUtils.checkType(rel.getChild(),Record.class,Record.DISCUSSION) ) {
                log.error("Relation "+rel+" isn't discussion record!");
                continue;
            }
            Record record = (Record) rel.getChild();
            if ( lastUpdate.before(record.getUpdated()) )
                lastUpdate = record.getUpdated();
            count++;
        }

        PreparedDiscussion discussion = new PreparedDiscussion(item);
        discussion.lastUpdate = lastUpdate;
        discussion.responseCount = count;
        discussion.relationId = relation.getId();
        return discussion;
    }

    /**
     * Prepares discussions for displaying.
     * @param content List of Relations containing Items with type=Item.Discussion
     * @return list of PreparedDiscussions.
     */
    public List analyzeDiscussions(List content) {
        List list = new ArrayList(content.size());

        for (Iterator iter = content.iterator(); iter.hasNext();) {
            PreparedDiscussion p = analyzeDiscussion((Relation)iter.next());
            list.add(p);
        }

        return list;
    }

    /**
     * Synchronizes object with persistance if it is not initialized.
     * For relation, its child is synchronized too.
     */
    public static GenericObject sync(GenericObject obj) {
        if ( ! obj.isInitialized() )
            persistance.synchronize(obj);
        if ( obj instanceof Relation ) {
            GenericObject child = ((Relation)obj).getChild();
            if ( ! child.isInitialized() )
                persistance.synchronize(child);
        }
        return obj;
    }

    /**
     * Synchronizes GenericOjects in list.
     */
    public static void sync(Collection list) throws PersistanceException {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            sync((GenericObject) iter.next());
        }
    }

    /**
     * This method instantiates user and synchronizes it.
     * @return synchronized User.
     */
    public User createUser(String id) {
        int i = Integer.parseInt(id);
        User user = new User(i);
        return (User) persistance.findById(user);
    }

    /**
     * @return counter value for selected GenericObject
     */
    public int getCounterValue(GenericObject obj) {
        return persistance.getCounterValue(obj);
    }

    /**
     * This method groups relations by their children's type. Each type represents
     * one of Constants.TYPE_* strings. The key represents list of relations, where
     * children are same type. As side effect, the relations and their objects
     * are initialized.
     */
    public Map groupByType(List relations) throws PersistanceException {
        Map map = new HashMap();

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            sync(relation);

            GenericObject child = relation.getChild();
            if ( child instanceof Category ) {
                Misc.storeToMap(map,Constants.TYPE_CATEGORY,relation);
            } else if ( child instanceof Item ) {
                Item item = (Item) child;
                if ( item.getType()==Item.MAKE ) {
                    Misc.storeToMap(map,Constants.TYPE_MAKE,relation);
                } else if ( item.getType()==Item.DISCUSSION ) {
                    Misc.storeToMap(map,Constants.TYPE_DISCUSSION,relation);
                } else if ( item.getType()==Item.ARTICLE ) {
                    Misc.storeToMap(map,Constants.TYPE_ARTICLE,relation);
                } else if ( item.getType()==Item.DRIVER ) {
                    Misc.storeToMap(map,Constants.TYPE_DRIVER,relation);
                } else if ( item.getType()==Item.REQUEST ) {
                    Misc.storeToMap(map,Constants.TYPE_REQUEST,relation);
                }
            } else if ( child instanceof Record ) {
                Misc.storeToMap(map,Constants.TYPE_RECORD,relation);
            }
        }
        return map;
    }
}
