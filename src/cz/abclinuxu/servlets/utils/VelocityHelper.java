/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 7:39:39 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Sorters;
import org.dom4j.Document;
import org.dom4j.Node;
import org.apache.velocity.context.Context;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;

/**
 * This class provides several methods, that
 * make velocity developer's life easier.
 */
public class VelocityHelper {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VelocityHelper.class);

    /**
     * Get name of this child in this relation context. Default name
     * from getChild may be overriden by relation.
     * If child doesn't have any name, it returns class name.
     */
    public String getChildName(Relation relation) throws PersistanceException {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = getXPath(relation,"data/name");
        if ( name!=null && name.length()>0) return name;

        GenericObject child = relation.getChild();
        if ( !child.isInitialized() ) PersistanceFactory.getPersistance().synchronize(child);

        if ( child instanceof GenericDataObject ) {
            Document data = ((GenericDataObject)child).getData();
            if ( data!=null ) {
                Node node = data.selectSingleNode("data/name");
                if ( node!=null ) name = node.getText();
            }

        } else if ( child instanceof Link ) {
            name = ((Link)child).getText();

        } else if ( child instanceof Poll ) {
            name = ((Poll)child).getText();

        } else if ( child instanceof User ) {
            name = ((User)child).getName();

        } else if ( child instanceof Server ) {
            name = ((Server)child).getName();
        }

        if ( name==null || name.length()==0) {
            name = child.getClass().getName();
            name = name.substring(name.lastIndexOf('.')+1);
            name = name.concat(" "+child.getId());
        }
        return name;
    }

    /**
     * Retrieves icon for child of this relation. If relation declares icon,
     * it will be used. Otherwise relation's child's icon will be returned.
     * If there is no icon at all, null will be returned.
     */
    public String getChildIcon(Relation relation) throws PersistanceException {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = getXPath(relation,"data/icon");
        if ( name!=null && name.length()>0) return name;

        GenericObject child = relation.getChild();
        name = getXPath(child,"data/icon");
        return name;
    }

    /**
     * Gets text value of node selected by xpath expression for GenericObject.
     * If <code>obj</code> doesn't contain <code>Document data</code> field
     * or xpath element doesn't exist, null is returned.
     */
    public static String getXPath(GenericObject obj, String xpath) throws PersistanceException {
        if ( obj==null ) return null;
        if ( !obj.isInitialized() ) PersistanceFactory.getPersistance().synchronize(obj);
        Document doc = null;
        String value = null;

        if ( obj instanceof GenericDataObject ) {
            doc = ((GenericDataObject)obj).getData();
        } else if ( obj instanceof Relation ) {
            doc = ((Relation)obj).getData();
        } else if ( obj instanceof User ) {
            doc = ((User)obj).getData();
        }
        if ( doc==null ) return null;

        Node node = doc.selectSingleNode(xpath);
        if ( node!=null ) value = node.getText();

        return value;
    }

    /**
     * Adds all parameters from request to specified map and returns it back.
     * If map is null, new HashMap is created. <p>
     * If there is only one value for a parameter, it will be stored directly
     * associated with parameter's name. But if there are at least two values,
     * they will be stored in list associated with parameter's name.
     */
    public static Map putParamsToMap(HttpServletRequest request, Map map) {
        if ( map==null ) map = new HashMap();
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String[] values = request.getParameterValues(name);

            if ( values.length==1 ) {
                String value = request.getParameter(name);
                try { value = new String(value.getBytes("ISO-8859-1")); } catch (UnsupportedEncodingException e) {}
                map.put(name,value.trim());

            } else {
                List list = new ArrayList(values.length);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    try { value = new String(value.getBytes("ISO-8859-1")); } catch (UnsupportedEncodingException e) {}
                    list.add(value.trim());
                }
                map.put(name,list);
            }
        }
        return map;
    }

    /**
     * Merges template specified in <code>templateName</code> with context
     * and returns it as String. If an exception is caught, empty string
     * is returned.
     * @param templateName name of template
     * @param context context holding parameters
     * @return String holding the result og merge.
     */
    public static String mergeTemplate(String templateName, Context context) {
        try {
            Template template = Velocity.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.merge(context,writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Cannot merge template "+templateName,e);
            return "";
        }
    }

    /**
     * Creates string, which contains every item in <code>VAR_PARAMS</code> as hidden field
     * except that listed in <code>prohibited</code> list.
     */
    public String saveParams(Map params, List prohibited) {
        if ( params==null || params.size()==0 ) return "";
        if ( prohibited==null ) prohibited = new ArrayList(1);

        StringBuffer sb = new StringBuffer();
        Set entries = params.keySet();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            if ( prohibited.contains(key) ) continue;

            List values = null;
            if ( params.get(key) instanceof List ) {
                values = (List) params.get(key);
            } else {
                values = new ArrayList(1);
                values.add(params.get(key));
            }

            for (Iterator iter2 = values.iterator(); iter2.hasNext();) {
                String value = (String) iter2.next();
                sb.append("<input type=\"hidden\" name=\"");
                sb.append(key);
                sb.append("\" value=\"");
                sb.append(encodeSpecial(value));
                sb.append("\">\n");
            }

        }
        return sb.toString();
    }

    /**
     * Takes list of relations in argument and sorts them by
     * relations.get(0).getChild()'s name in ascendant order.
     * If generic object doesn't contain property name,
     * it will be appended to the end of list.
     * @return new list, where relations are sorted by date
     */
    public List sortByName(List relations) throws PersistanceException {
        if ( relations==null ) return null;
        List sorted = new ArrayList(relations.size());
        int size = 0, i = 0;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();

            for (i=size;i>0;) {
                String current = getChildName((Relation) sorted.get(i-1));
                String used = getChildName(relation);
                if ( current.compareTo(used)<0 ) break;
                i--;
            }
            sorted.add(i,relation);
            size++;
        }

        return sorted;
    }

    /**
     * Sorts list of generic objects by updated or created field. More info
     * in class Sorters. Ascending order.
     */
    public List sortByDateAscending(List list) throws PersistanceException {
        if ( list==null ) return null;
        sync(list);
        Sorters.sortByDate(list,true);
        return list;
    }

    /**
     * Sorts list of generic objects by updated or created field. More info
     * in class Sorters. Descending order.
     */
    public List sortByDateDescending(List list) throws PersistanceException {
        if ( list==null ) return null;
        sync(list);
        Sorters.sortByDate(list,false);
        return list;
    }

    /**
     * Sorts list of generic objects by id in ascending order. Details in class Sorters.
     */
    public List sortByIdAscending(List list) {
        if ( list==null ) return null;
        Sorters.sortById(list,true);
        return list;
    }

    /**
     * Sorts list of generic objects by id in descending order. Details in class Sorters.
     */
    public List sortByIdDescending(List list) {
        if ( list==null ) return null;
        Sorters.sortById(list,false);
        return list;
    }

    /**
     * Sorts list of generic objects by id in ascending order. Details in class Sorters.
     */
    public List sortByIdAscending(Set set) {
        if ( set==null ) return null;
        List list = new ArrayList(set);
        Sorters.sortById(list,true);
        return list;
    }

    /**
     * Sorts list of generic objects by id in descending order. Details in class Sorters.
     */
    public List sortByIdDescending(Set set) {
        if ( set==null ) return null;
        List list = new ArrayList(set);
        Sorters.sortById(list,false);
        return list;
    }

    /**
     * Filters out every relation, which's child is not Discussion.
     * @param relations List of relations, they shall be initialized.
     * @return New list of relations, where each child is Discussion.
     */
    public List filterDiscussions(List relations) throws PersistanceException {
        List list = new ArrayList(5);

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            GenericObject child = rel.getChild();
            if ( child instanceof Item ) {
                sync(child);
                if ( ((Item)child).getType()==Item.DISCUSSION ) list.add(rel);
            }
        }

        return list;
    }

    /**
     * @return sublist of list, starting at <code>start</code> position and
     * having maximum <code>count</code> items. List can be shortened, if
     * IndexOutOfBounds would be thrown. It may even return null, if result
     * is empty.
     */
    public List sublist(List list, int start, int count) {
        if ( list==null ) return null;
        if ( start>=list.size() ) return null;

        int end = start+count;
        if ( end>=list.size() ) end = list.size();
        return list.subList(start,end);
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
     * Does standard HTML conversions like & to &amp;amp; or &lt; to &amp;lt;.
     * @return Modified String, which may be inserted into html page without affecting its structure.
     */
    public String encodeSpecial(String in) {
        if ( in==null ) return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < in.length(); i++) {
            int c = in.charAt(i);
            switch (c) {
                case '"': sb.append("&quot;");break;
                case '<': sb.append("&lt;");break;
                case '>': sb.append("&gt;");break;
                case '&': sb.append("&amp;");break;
                default: sb.append((char)c);
            }
        }
        return sb.toString();
    }

    /**
     * @return int value of <code>str</code> or 0
     */
    public int parseInt(String str) {
        if ( str!=null && str.length()>0 ) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {}
        }
        return 0;
    }

    /**
     * Returns formatted String according to current locale.
     */
    public String showDate(Date date) {
        if ( date==null ) return null;
        return Constants.defaultFormat.format(date);
    }

    /**
     * Returns formatted String according to current locale.
     */
    public String showDizDate(Date date) {
        if ( date==null ) return null;
        return Constants.discussionFormat.format(date);
    }

    /**
     * Returns formatted String according to current locale. Uses current time.
     */
    public String showDate() {
        return Constants.defaultFormat.format(new Date());
    }

    /**
     * Returns formatted String according to current locale. isoDate must be in iso format (2002-01-03 12:32)
     */
    public String showDate(String isoDate) {
        if ( isoDate==null ) return null;
        try {
            Date date = Constants.isoFormat.parse(isoDate);
            return Constants.defaultFormat.format(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Synchronizes object
     */
    public void sync(GenericObject obj) throws PersistanceException {
        if ( ! obj.isInitialized() ) PersistanceFactory.getPersistance().synchronize(obj);
    }

    /**
     * Synchronizes objects in list.
     */
    public void sync(Collection list) throws PersistanceException {
        Persistance persistance = PersistanceFactory.getPersistance();

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if ( !(o instanceof GenericObject) ) continue;
            GenericObject obj = (GenericObject)o;
            if ( obj instanceof Relation ) {
                persistance.synchronize(obj);
                GenericObject child = ((Relation)obj).getChild();
                persistance.synchronize(child);
            } else {
                persistance.synchronize(obj);
            }
        }
    }

    /**
     * It is not possible directly instantiate object in Velocity. This method
     * is a workaround for this. Pass classname and id and it will return you new instance
     * of selected class or null, if class name was not recognized.
     */
    public GenericObject instantiate(String className, int id) {
        if ( className.equalsIgnoreCase("Category") ) return new Category(id);
        if ( className.equalsIgnoreCase("Server") ) return new Server(id);
        if ( className.equalsIgnoreCase("User") ) return new User(id);
        if ( className.equalsIgnoreCase("Poll") ) return new Poll(id);
        return null;
    }

    /**
     * It is not possible directly instantiate object in Velocity. This method
     * is a workaround for this. Pass classname and id and it will return you new instance
     * of selected class or null, if class name was not recognized.
     */
    public GenericObject instantiate(String className, String  id) {
        try {
            int i = Integer.parseInt(id);
            return instantiate(className,i);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts Item with type==Discussion to tree structure. Tree is consisted from
     * list of Discussion objects.
     */
    public List createDiscussionTree(GenericObject obj) throws PersistanceException {
        if ( ! (obj instanceof Item) ) throw new IllegalArgumentException("Not an discussion: "+obj);
        Item item = (Item) obj;
        if ( item.getType()!=Item.DISCUSSION ) throw new IllegalArgumentException("Not an discussion: "+obj);

        List top = new ArrayList(5);
        Map map = new HashMap();

        sync(obj.getContent());
        List records = sortByIdAscending(obj.getContent());

        for (Iterator iter = records.iterator(); iter.hasNext();) {
            Record record = (Record) ((Relation)iter.next()).getChild();
            Node node = record.getData().selectSingleNode("data/thread");
            int upperId = (node==null)? 0:Integer.parseInt(node.getText());

            Discussion upper = null;
            if ( upperId!=0 ) upper = (Discussion) map.get(new Record(upperId,Record.DISCUSSION));
            Discussion created = null;

            if ( upper!=null ) {
                created = upper.add(record);
            } else {
                created = new Discussion(record);
                top.add(created);
            }

            map.put(record,created);
        }

        return top;
    }

    /**
     * Tests, whether specified object <code>obj</code> is instance of selected
     * class <code>clazz</code> and if even if it belongs to certain <code>type</code>
     * (for GenericDataObject subclasses). GenericObject <code>obj</code> shall be
     * initialized.
     */
    public boolean is(GenericObject obj, String clazz, String type) {
        if ( "Item".equalsIgnoreCase(clazz) ) {
            if ( ! (obj instanceof Item) ) return false;
            if ( type==null ) return true;
            switch (((Item)obj).getType()) {
                case 1: return "Make".equalsIgnoreCase(type);
                case 2: return "Article".equalsIgnoreCase(type);
                case 3: return "Discussion".equalsIgnoreCase(type);
                case 4: return "Request".equalsIgnoreCase(type);
                case 5: return "Driver".equalsIgnoreCase(type);
                default: return false;
            }
        }
        return false;
    }

    /**
     * This method groups relations by their children's type. Each type represents
     * one of Constants.TYPE_* strings. List of relations can be looked up for
     * key.
     */
    public Map groupByType(List relations) throws PersistanceException {
        Map map = new HashMap();
        sync(relations);

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            GenericObject child = relation.getChild();

            if ( child instanceof Category ) {
                storeToMap(map,Constants.TYPE_CATEGORY,relation);
            } else if ( child instanceof Item ) {
                Item item = (Item) child;
                if ( item.getType()==Item.MAKE ) {
                    storeToMap(map,Constants.TYPE_MAKE,relation);
                } else if ( item.getType()==Item.DISCUSSION ) {
                    storeToMap(map,Constants.TYPE_DISCUSSION,relation);
                } else if ( item.getType()==Item.ARTICLE ) {
                    storeToMap(map,Constants.TYPE_ARTICLE,relation);
                } else if ( item.getType()==Item.DRIVER ) {
                    storeToMap(map,Constants.TYPE_DRIVER,relation);
                } else if ( item.getType()==Item.REQUEST ) {
                    storeToMap(map,Constants.TYPE_REQUEST,relation);
                }
            }
        }
        return map;
    }

    /**
     * Associates value with given key in the map. Each key contains
     * list of values. If the list doesn't exist yet, it is created.
     */
    private void storeToMap(Map map, String key, GenericObject value) {
        List list = (List) map.get(key);
        if ( list==null ) {
            list = new ArrayList(5);
            map.put(key,list);
        }
        list.add(value);
    }

    /**
     * Gathers statistics on discussion.
     * @param item discussion, which must be initialized.
     */
    public PreparedDiscussion prepareDiscussion(Relation relation) throws PersistanceException {
        Item item = (Item) relation.getChild();
        if ( item.getType()!=Item.DISCUSSION ) return null;
        PreparedDiscussion discussion = new PreparedDiscussion(item);
        sync(item.getContent());

        Date lastUpdate = item.getUpdated();
        int count = 0;

        for (Iterator iter = item.getContent().iterator(); iter.hasNext(); ) {
            Relation rel = (Relation) iter.next();
            Record record = (Record) rel.getChild();
            if ( record.getType()==Record.DISCUSSION ) {
                count++;
                if ( lastUpdate.before(record.getUpdated()) ) lastUpdate = record.getUpdated();
            }
        }

        discussion.lastUpdate = lastUpdate;
        discussion.responseCount = count;
        discussion.relationId = relation.getId();
        return discussion;
    }

    /**
     * @param content List of Relations containing Items with type=Item.Discussion
     * @return list of PreparedDiscussions.
     */
    public List getDiscussions(List content) throws PersistanceException {
        List list = new ArrayList(content.size());

        for (Iterator iter = content.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            sync(relation.getChild());
            PreparedDiscussion p = prepareDiscussion(relation);
            list.add(p);
        }

        return list;
    }
}
