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
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.PreparedDiscussion;
import cz.abclinuxu.servlets.utils.Discussion;
import org.dom4j.Document;
import org.dom4j.Node;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.*;

import freemarker.template.*;

/**
 * Various utilities available for templates
 */
public class Tools {
    static Logger log = Logger.getLogger(Tools.class);

    static Persistance persistance = PersistanceFactory.getPersistance();
    static RE lineBreaks,emptyLine,usmev,smich,mrk,smutek;
    static {
        try {
            lineBreaks = new RE("(<br>)|(<p>)|(<div>)",RE.MATCH_CASEINDEPENDENT);
            emptyLine = new RE("(\r\n){2}|(\n){2}", RE.MATCH_MULTILINE);
            usmev = new RE("([\\:][-][)]+)");
            smich = new RE("([\\:][-][D]([^a-zA-Z]|$))");
            mrk = new RE("([;][-][)])");
            smutek = new RE("([\\:][-][(])");
        } catch (RESyntaxException e) {
            log.error("Cannot create regexp to find line breaks!", e);
        }
    }

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
     * @return name of child in this relation.
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
            if ( (child instanceof Item) && child.getId()==Item.DISCUSSION )
                return "Diskuse";
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
     * Extracts new string from str. Workaround for freemarker,
     * which uses BigDecimal as integer holder, so String.substring(int)
     * is not recognized by beans introspection.
     */
    public String substring(String str, java.math.BigDecimal from) {
        return str.substring(from.intValue());
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
        PreparedDiscussion discussion = new PreparedDiscussion(item);
        discussion.lastUpdate = SQLTool.getInstance().getMaxCreatedDateOfRecordForItem(item);
        discussion.responseCount = item.getContent().size();
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
            list.add(analyzeDiscussion((Relation)iter.next()));
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
     * This method instantiates user and synchronizes it.
     * @return synchronized User.
     */
    public User createUser(int id) {
        User user = new User(id);
        return (User) persistance.findById(user);
    }

    /**
     * Increments usage counter of GenericObject.
     */
    public void incrementCounter(GenericObject obj) {
        persistance.incrementCounter(obj);
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
    public static Map groupByType(List relations) throws PersistanceException {
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

    /**
     * This method performs visualization enhancements. If string
     * doesn't contain already HTML breaks (<p>, <br>), it inserts them.
     * It also replaces smilies with appropriate images.
     */
    public String render(String str) {
        if ( Misc.empty(str) ) return "";

        String tmp = smich.subst(str,"<img src=\"/images/smile/smich.gif\" alt=\":-D\" class=\"emo\">");
        tmp = usmev.subst(tmp,"<img src=\"/images/smile/usmev.gif\" alt=\":-)\" class=\"emo\">");
        tmp = mrk.subst(tmp,"<img src=\"/images/smile/mrk.gif\" alt=\";-)\" class=\"emo\">");
        tmp = smutek.subst(tmp,"<img src=\"/images/smile/smutek.gif\" alt=\":-(\" class=\"emo\">");

        if ( lineBreaks.match(tmp) ) return tmp;
        return emptyLine.subst(tmp,"<p>\n");
    }

    /**
     * Converts Item with type==Discussion to tree structure. Tree is consisted from
     * list of Discussion objects.
     */
    public List createDiscussionTree(GenericObject obj) throws PersistanceException {
        if ( ! InstanceUtils.checkType(obj,Item.class,Item.DISCUSSION) )
            throw new IllegalArgumentException("Not an discussion: "+obj);

        List top = new ArrayList(5);
        Map map = new HashMap();
        sync(obj.getContent());
        List records = Sorters2.byId(obj.getContent());

        for (Iterator iter = records.iterator(); iter.hasNext();) {
            Record record = (Record) ((Relation)iter.next()).getChild();
            Node node = record.getData().selectSingleNode("data/thread");
            int upperId = (node==null)? 0:Integer.parseInt(node.getText());

            Discussion created = null, upper = null;
            if ( upperId!=0 )
                upper = (Discussion) map.get(new Record(upperId,Record.DISCUSSION));

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
     * @deprecated
     */
    public boolean is(GenericObject obj, String clazz, String type) {
        if ( "Item".equalsIgnoreCase(clazz) ) {
            if ( ! (obj instanceof Item) ) return false;
            if ( type==null ) return true;
            switch (((Item)obj).getType()) {
                case Item.MAKE: return "Make".equalsIgnoreCase(type);
                case Item.ARTICLE: return "Article".equalsIgnoreCase(type);
                case Item.DISCUSSION: return "Discussion".equalsIgnoreCase(type);
                case Item.REQUEST: return "Request".equalsIgnoreCase(type);
                case Item.DRIVER: return "Driver".equalsIgnoreCase(type);
                default: return false;
            }
        }
        if ( "Record".equalsIgnoreCase(clazz) ) {
            if ( ! (obj instanceof Record) ) return false;
            if ( type==null ) return true;
            switch (((Record)obj).getType()) {
                case Record.HARDWARE: return "Hardware".equalsIgnoreCase(type);
                case Record.SOFTWARE: return "Software".equalsIgnoreCase(type);
                case Record.ARTICLE: return "Article".equalsIgnoreCase(type);
                case Record.DISCUSSION: return "Discussion".equalsIgnoreCase(type);
                default: return false;
            }
        }
        if ( "Category".equalsIgnoreCase(clazz) ) {
            return (obj instanceof Category);
        }
        return false;
    }

    /**
     * @return integer value of <code>str</code> or 0
     */
    public int parseInt(String str) {
        if ( ! Misc.empty(str) ) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {}
        }
        return 0;
    }

    /**
     * Encodes all mapping from <code>params</code> except those listed
     * in <<code>prohibited</code> list as form hidden input.
     */
    public String saveParams(Map params, SimpleSequence prohibited) throws TemplateModelException {
        List exceptions;
        if ( params==null || params.size()==0 )
            return "";
        if ( prohibited==null )
            exceptions = new ArrayList(1);
        else {
            exceptions = new ArrayList(prohibited.size());
            for ( int i = 0, j = prohibited.size(); i<j; i++ ) {
                exceptions.add(((TemplateScalarModel)prohibited.get(i)).getAsString());
            }
        }

        StringBuffer sb = new StringBuffer(params.size()*50);
        Set entries = params.keySet();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            if ( exceptions.contains(key) )
                continue;

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
     * Does standard HTML conversions like & to &amp;amp; or &lt; to &amp;lt;. Use ?html
     * in freemarker templates!
     * @return Modified String, which may be inserted into html page without affecting its structure.
     */
    public static String encodeSpecial(String in) {
        if ( in==null || in.length()==0 )
            return "";
        StringBuffer sb = new StringBuffer(in.length());
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
}
