/*
 * User: Leos Literak
 * Date: Jan 4, 2003
 * Time: 3:23:18 PM
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.PreparedDiscussion;
import cz.abclinuxu.servlets.utils.Discussion;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.Element;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.*;
import java.io.StringReader;
import java.io.IOException;

import freemarker.template.*;

/**
 * Various utilities available for templates
 */
public class Tools {
    static Logger log = Logger.getLogger(Tools.class);

    static Persistance persistance = PersistanceFactory.getPersistance();
    static RE lineBreaks, reRemoveTags, emptyLine;
    static {
        try {
            // todo move it to systemPrefs.xml
            lineBreaks = new RE("(<br>)|(<p>)|(<div>)",RE.MATCH_CASEINDEPENDENT);
            emptyLine = new RE("(\r\n){2}|(\n){2}", RE.MATCH_MULTILINE);
            reRemoveTags = new RE("<[\\w\\s\\d/=:.~?\"]+>", RE.MATCH_SINGLELINE);
        } catch (RESyntaxException e) {
            log.error("Cannot create regexp to find line breaks!", e);
        }
    }

    /**
     * Returns text value of node selected by xpath expression for GenericObject.
     * @throws cz.abclinuxu.exceptions.PersistanceException if object cannot be synchronized
     */
    public static String xpath(GenericObject obj, String xpath) {
        if ( obj==null || !(obj instanceof XMLContainer) )
            return null;
        if ( !obj.isInitialized() )
            persistance.synchronize(obj);
        Document doc = ((XMLContainer)obj).getData();
        if ( doc==null )
            return null;
        Node node = doc.selectSingleNode(xpath);
        return (node!=null)? node.getText() : null;
    }

    /**
     * Extracts values of given xpath expression evaluated on document.
     * @param doc XML tree
     * @param xpath xpath expression
     * @return list of Strings
     */
    public static List xpaths(Document doc, String xpath) {
        List nodes = doc.selectNodes(xpath);
        List result = new ArrayList(nodes.size());
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Node node = (Node) iter.next();
            String value = node.getText();
            if ( !Misc.empty(value) )
                result.add(value);
        }
        return result;
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
    public static PreparedDiscussion analyzeDiscussion(Relation relation) {
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
            PreparedDiscussion preparedDiscussion = analyzeDiscussion((Relation) iter.next());
            if ( preparedDiscussion!=null )
                list.add(preparedDiscussion);
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
                } else if ( item.getType()==Item.NEWS ) {
                    Misc.storeToMap(map,Constants.TYPE_NEWS,relation);
                } else if ( item.getType()==Item.REQUEST ) {
                    Misc.storeToMap(map,Constants.TYPE_REQUEST,relation);
                }
            } else if ( child instanceof Record ) {
                Misc.storeToMap(map,Constants.TYPE_RECORD,relation);
            } else if ( child instanceof User ) {
                Misc.storeToMap(map,Constants.TYPE_USER,relation);
            }
        }
        return map;
    }

    /**
     * @deprecated use method below
     * @param str
     * @return
     */
    public String render(String str) {
        log.warn("render with one argument called!");
        return render(str,null);
    }

    /**
     * This method performs visualization enhancements. If string
     * doesn't contain already HTML breaks (&lt;p>, &lt;br>), it inserts them.
     * It also replaces smilies with appropriate images.
     */
    public String render(String str, Object o) {
        if ( Misc.empty(str) ) return "";

        boolean renderEmoticons = true;
        if ( o!=null && (o instanceof User) ) {
            Node node = ((User)o).getData().selectSingleNode("/data/settings/emoticons");
            if ( "no".equals(node.getText()) )
                renderEmoticons = false;
        }

        String tmp = str;

        if (renderEmoticons) {
            StringReader reader = new StringReader(str);
            StringBuffer sb = new StringBuffer((int)(1.1*str.length()));

            try {
                int c = reader.read(), d, e;
                while (c!=-1) {
                    if (c==':' || c==';') {
                        d = reader.read();
                        if ( d=='-' ) {
                            e = reader.read();
                            switch (e) {
                                case -1 :
                                    sb.append((char) c);
                                    sb.append((char) d);
                                    break;
                                case ')':
                                    if (c==':')
                                        sb.append("<img src=\"/images/smile/usmev.gif\" alt=\":-)\" class=\"emo\">");
                                    else
                                        sb.append("<img src=\"/images/smile/mrk.gif\" alt=\";-)\" class=\"emo\">");
                                    break;
                                case '(':
                                    sb.append("<img src=\"/images/smile/smutek.gif\" alt=\":-(\" class=\"emo\">");
                                    break;
                                case 'D':
                                    sb.append("<img src=\"/images/smile/smich.gif\" alt=\":-D\" class=\"emo\">");
                                    break;
                                default:
                                    sb.append((char) c);
                                    sb.append((char) d);
                                    sb.append((char) e);
                            }
                        } else {
                            sb.append((char) c);
                            if (d!=-1)
                                sb.append((char) d);
                        }
                    } else {
                        sb.append((char)c);
                    }
                    c = reader.read();
                }
            } catch (IOException e) {
                log.error("Error while rendering emoticons!", e);
            }

            tmp = sb.toString();
        }

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

    /**
     * Concatenates all pages of the article.
     * @return complete text of the article
     */
    public static String getCompleteArticleText(Item article) {
        Map children = Tools.groupByType(article.getContent());
        List records = (List) children.get(Constants.TYPE_RECORD);
        Record record = (Record) ((Relation) records.get(0)).getChild();
        if ( !record.isInitialized() )
            persistance.synchronize(record);

        StringBuffer sb = new StringBuffer(5000);
        List nodes = record.getData().selectNodes("/data/content");
        if ( nodes.size()==0 )
            throw new InvalidDataException("Záznam "+record.getId()+" má ¹patný obsah!");
        else {
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); )
                sb.append(((Element) iter.next()).getText());
        }
        return sb.toString();
    }

    /**
     * This method removes all tags from text.
     */
    public static String removeTags(String text) {
        try {
            return reRemoveTags.subst(text,"");
        } catch (Throwable e) {
            log.warn("Oops, remove tags regexp failed on '"+text+"'!", e);
            return text;
        }
    }

    /**
     * Finds, how many comments is in discussion associated with this
     * GenericObject. If there is more than one, it finds time of last comment too.
     * @return Map holding info about comments of this object.
     */
    public static PreparedDiscussion findComments(GenericObject object) {
        if ( !(object instanceof Item) )
            return null;
        for ( Iterator iter = object.getContent().iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            GenericObject child = (relation).getChild();
            if ( !(child instanceof Item) )
                continue;
            Item item = (Item) child;
            if ( !item.isInitialized() )
                persistance.synchronize(item);
            if ( item.getType()!=Item.DISCUSSION )
                continue;
            return analyzeDiscussion(relation);
        }
        return new PreparedDiscussion(null);
    }
}
