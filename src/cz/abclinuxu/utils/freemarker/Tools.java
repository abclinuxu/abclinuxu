/*
 * User: Leos Literak
 * Date: Jan 4, 2003
 * Time: 3:23:18 PM
 */
package cz.abclinuxu.utils.freemarker;

import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditRating;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.Discussion;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.format.*;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.scheduler.EnsureWatchedDiscussionsLimit;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.Branch;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.*;
import java.util.prefs.Preferences;

import freemarker.template.*;

/**
 * Various utilities available for templates
 */
public class Tools implements Configurable {
    static Logger log = Logger.getLogger(Tools.class);

    public static final String PREF_REGEXP_REMOVE_TAGS = "RE_REMOVE_TAGS";
    public static final String PREF_REGEXP_VLNKA = "RE_VLNKA";
    public static final String PREF_REPLACEMENT_VLNKA = "REPLACEMENT_VLNKA";

    static Persistance persistance = PersistanceFactory.getPersistance();
    static RE reRemoveTags, reVlnka;
    static String vlnkaReplacement;

    static {
        Tools tools = new Tools();
        ConfigurationManager.getConfigurator().configureAndRememberMe(tools);
    }

    private static final Integer MAX_SEEN_COMMENT_ID = new Integer(Integer.MAX_VALUE);


    public void configure(Preferences prefs) throws ConfigurationException {
        try {
            String pref = prefs.get(PREF_REGEXP_REMOVE_TAGS, null);
            reRemoveTags = new RE(pref, RE.MATCH_MULTILINE);
            pref = prefs.get(PREF_REGEXP_VLNKA, null);
            reVlnka = new RE(pref, RE.MATCH_MULTILINE);
            vlnkaReplacement = prefs.get(PREF_REPLACEMENT_VLNKA, null);
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
     * Extracts value of given xpath expression evaluated on element.
     * @param element XML element
     * @param xpath xpath expression
     * @return Strings
     */
    public static String xpath(Node element, String xpath) {
        Node node = element.selectSingleNode(xpath);
        return (node==null)? null : node.getText();
    }

    /**
     * Extracts element found by xpath from element.
     * @param element XML element
     * @param xpath xpath expression
     * @return extracted Element
     */
    public static Element element(Node element, String xpath) {
        return (Element) element.selectSingleNode(xpath);
    }

    /**
     * Extracts value of given xpath expression evaluated on element.
     * @param element XML element
     * @param xpath xpath expression
     * @return object, that is result of xpath expression, or null
     */
    public static Object xpathValue(Node element, String xpath) {
        if ( element==null )
            return null;
        Object result = element.selectObject(xpath);
        if ( result==null )
            return null;
        if ( result instanceof Node )
            return ((Node) result).getText();
        return result.toString();
    }

    /**
     * Extracts values of given xpath expression evaluated on element.
     * @param element XML element
     * @param xpath xpath expression
     * @return list of Strings
     */
    public static List xpaths(Node element, String xpath) {
        List nodes = element.selectNodes(xpath);
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
     * @param relationId object holding integer value of relation id
     * @return name of child
     */
    public static String childName(Number relationId) {
        Relation relation = (Relation) PersistanceFactory.getPersistance().findById(new Relation(relationId.intValue()));
        return childName(relation);
    }

    /**
     * @param relationId object holding integer value of relation id
     * @return name of child
     */
    public static String childName(String relationId) {
        int id = Integer.parseInt(relationId);
        Relation relation = (Relation) PersistanceFactory.getPersistance().findById(new Relation(id));
        return childName(relation);
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
                node = data.selectSingleNode("data/title");
                if ( node!=null )
                    return node.getText();
            }
            if ( (child instanceof Item) && ((Item)child).getType()==Item.DISCUSSION )
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
     * Generates list of Links to parents of this object.
     * @param parents list of relations.
     * @param o it shall be User or undefined value
     * @return List of Links
     */
    public List getParents(List parents, Object o, UrlUtils urlUtils) {
        if (parents.size()==0) return Collections.EMPTY_LIST;
        List result = new ArrayList();
        Relation relation = null;
        for ( Iterator iter = parents.iterator(); iter.hasNext(); ) {
            Link link = new Link();
            relation = (Relation) iter.next();
            GenericObject child = relation.getChild();
            if ( child instanceof Category && (child.getId()==Constants.CAT_ROOT || child.getId()==Constants.CAT_SYSTEM ))
                continue;

            link.setText(childName(relation));
            if (relation.getId()==Constants.REL_FORUM)
                link.setUrl(urlUtils.noPrefix("/diskuse.jsp"));
            else if (child instanceof Category) {
                if (((Category)child).getType()==Category.SECTION_FORUM)
                    link.setUrl(urlUtils.make("/forum/dir/"+relation.getId()));
                else
                    link.setUrl(urlUtils.make("/dir/"+relation.getId()));
            } else
                link.setUrl(urlUtils.make("/show/"+relation.getId()));
            result.add(link);
        }
        return result;
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
     * Gets number of activated monitors in given document.
     * @return integer
     */
    public Integer getMonitorCount(Document document) {
        Object value = document.selectObject("count(//monitor/id)");
        return new Integer(((Double) value).intValue());
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
    public static String limit(String str, int max, String suffix) {
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
     * Synchronizes list of GenericObjects.
     */
    public static List syncList(Collection collection) throws PersistanceException {
        List list = null;
        if (! (collection instanceof List) )
            list = new ArrayList(collection);
        else
            list = (List) collection;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            sync((GenericObject) iter.next());
        }
        return list;
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
     * This method instantiates category and synchronizes it.
     * @return synchronized category.
     */
    public Category createCategory(int id) {
        Category category = new Category(id);
        return (Category) persistance.findById(category);
    }

    /**
     * This method instantiates category and synchronizes it.
     * @return synchronized category.
     */
    public Category createCategory(String id) {
        int i = Integer.parseInt(id);
        Category category = new Category(i);
        return (Category) persistance.findById(category);
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
     * children are same type.
     */
    public static Map groupByType(List relations) throws PersistanceException {
        if (relations==null)
            return Collections.EMPTY_MAP;
        Map map = new HashMap();

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            sync(relation);

            GenericObject child = relation.getChild();
            if ( child instanceof Category )
                Misc.storeToMap(map,Constants.TYPE_CATEGORY,relation);
            else if ( child instanceof Item ) {
                Item item = (Item) child;
                if ( item.getType()==Item.MAKE )
                    Misc.storeToMap(map,Constants.TYPE_MAKE,relation);
                else if ( item.getType()==Item.DISCUSSION )
                    Misc.storeToMap(map,Constants.TYPE_DISCUSSION,relation);
                else if ( item.getType()==Item.ARTICLE )
                    Misc.storeToMap(map,Constants.TYPE_ARTICLE,relation);
                else if ( item.getType()==Item.DRIVER )
                    Misc.storeToMap(map,Constants.TYPE_DRIVER,relation);
                else if ( item.getType()==Item.NEWS )
                    Misc.storeToMap(map,Constants.TYPE_NEWS,relation);
                else if ( item.getType()==Item.REQUEST )
                    Misc.storeToMap(map,Constants.TYPE_REQUEST,relation);
                else if ( item.getType()==Item.ROYALTIES )
                    Misc.storeToMap(map,Constants.TYPE_ROYALTIES,relation);
                else if ( item.getType()==Item.CONTENT )
                    Misc.storeToMap(map,Constants.TYPE_DOCUMENTS,relation);
            } else if ( child instanceof Record )
                Misc.storeToMap(map,Constants.TYPE_RECORD,relation);
            else if ( child instanceof User )
                Misc.storeToMap(map,Constants.TYPE_USER,relation);
        }
        return map;
    }

    /**
     * This method performs visualization enhancements. If string
     * doesn't contain already HTML breaks (&lt;p>, &lt;br>), it inserts them.
     * It also replaces smilies with appropriate images.
     */
    public String render(String str, Object o) {
        if ( Misc.empty(str) ) return "";

        Map params = new HashMap(1,1.0f);
        boolean renderEmoticons = true;
        if ( o!=null && (o instanceof User) ) {
            Node node = ((User)o).getData().selectSingleNode("/data/settings/emoticons");
            if ( node!=null && "no".equals(node.getText()) )
                renderEmoticons = false;
        }
        if (renderEmoticons)
            params.put(Renderer.RENDER_EMOTICONS, Boolean.TRUE);

        Format format = FormatDetector.detect(str);
        if (format.equals(Format.SIMPLE))
            return SimpleFormatRenderer.getInstance().render(str,params);
        else
            return HTMLFormatRenderer.getInstance().render(str,params);
    }

    /**
     * This method renders given Element. It may contain attribute, which specify
     * format of the text.
     */
    public String render(Object el, Object o) {
        if (el==null || !(el instanceof Element))
            return "";

        Map params = new HashMap(1,1.0f);
        boolean renderEmoticons = true;
        if ( o!=null && (o instanceof User) ) {
            Node node = ((User)o).getData().selectSingleNode("/data/settings/emoticons");
            if ( node!=null && "no".equals(node.getText()) )
                renderEmoticons = false;
        }
        if (renderEmoticons)
            params.put(Renderer.RENDER_EMOTICONS, Boolean.TRUE);

        Format format = null;
        Element element = (Element) el;
        String input = element.getText();
        int f = Misc.parseInt(element.attributeValue("format"),-1);
        switch(f) {
            case -1: format = FormatDetector.detect(input); break;
            case 0: format = Format.SIMPLE; break;
            case 1: format = Format.HTML;
        }

        if (format.equals(Format.SIMPLE))
            return SimpleFormatRenderer.getInstance().render(input,params);
        else
            return HTMLFormatRenderer.getInstance().render(input,params);
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
    public static String saveParams(Map params, SimpleSequence prohibited) throws TemplateModelException {
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
     * Encodes all mapping from <code>params</code> except those listed
     * in <<code>prohibited</code> list as form hidden input.
     */
    public static String saveParams(Map params, List prohibited) throws TemplateModelException {
        return saveParams(params, new SimpleSequence(prohibited));
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
        Map children = Tools.groupByType(article.getChildren());
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
        if (text==null || text.length()==0) return "";
        try {
//            return StripTags.process(text);
            return reRemoveTags.subst(text,"");
        } catch (Throwable e) {
            log.warn("Oops, remove tags regexp failed on '"+text+"'!", e);
//            try {
//                System.in.read();
//            } catch (IOException e1) {
//                log.error("", e1);
//            }
            return text;
        }
    }

    /**
     * Converts Item with type==Discussion to tree structure. Tree is consisted from
     * list of Discussion objects.
     */
    public Discussion createDiscussionTree(GenericObject obj) throws PersistanceException {
        if (!obj.isInitialized())
            sync(obj);
        if ( !InstanceUtils.checkType(obj, Item.class, Item.DISCUSSION) )
            throw new IllegalArgumentException("Not an discussion: "+obj);

        if (obj.getChildren().size()==0)
            return new Discussion();

        Relation child = (Relation) obj.getChildren().get(0);
        Record record = (Record) child.getChild();
        sync(record);

        List nodes = record.getData().getRootElement().elements("comment");
        Discussion diz = new Discussion(nodes.size());
        Map map = new HashMap(nodes.size()+1, 1.0f);
        Comment current, upper = null;
        List alone = new ArrayList();
        int upperId;
        Element element;

        for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
            element = (Element) iter.next();
            current = new Comment(element); upper = null; upperId = 0;
            if (current.getParent()!=null)
                upperId = current.getParent().intValue();

            if ( upperId!=0 ) {
                upper = (Comment) map.get(current.getParent());
                if (upper!=null)
                    upper.addChild(current);
                else
                    alone.add(current);
            } else
                diz.add(current);

            map.put(current.getId(), current);
        }
        if (alone.size()>0)
            for ( Iterator iter = alone.iterator(); iter.hasNext(); ) {
                current = (Comment) iter.next();
                upper = (Comment) map.get(current.getParent());
                upper.addChild(current);
            }

        return diz;
    }

    /**
     * Gathers statistics on given discussion.
     */
    public static DiscussionHeader analyzeDiscussion(Relation relation) {
        if ( !InstanceUtils.checkType(relation.getChild(), Item.class, Item.DISCUSSION) ) {
            log.error("Relation "+relation.getId()+" doesn't contain item!");
            return null;
        }
        Item item = (Item) relation.getChild();
        DiscussionHeader discussion = new DiscussionHeader(item);
        discussion.relationId = relation.getId();
        discussion.updated = item.getUpdated();
        discussion.created = item.getCreated();

        Document data = item.getData();
        discussion.responseCount = Misc.parseInt(data.selectSingleNode("/data/comments").getText(),0);
        Node node = data.selectSingleNode("data/title");
        if (node==null) {
            GenericObject parent = relation.getParent();
            if (!parent.isInitialized()) persistance.synchronize(parent);
            if (parent instanceof Item) {
                item = (Item) parent;
                if (item.getType()==Item.ARTICLE) {
                    node = item.getData().selectSingleNode("data/name");
                    discussion.title = node.getText();
                } else if ( item.getType()==Item.NEWS )
                    discussion.title = "Zprávièka";
            }
        } else
            discussion.title = node.getText();

        return discussion;
    }

    /**
     * Prepares discussions for displaying.
     * @param content List of Relations containing Items with type=Item.Discussion
     * @return list of PreparedDiscussions.
     */
    public List analyzeDiscussions(List content) {
        List list = new ArrayList(content.size());
        for ( Iterator iter = content.iterator(); iter.hasNext(); ) {
            DiscussionHeader preparedDiscussion = analyzeDiscussion((Relation) iter.next());
            if ( preparedDiscussion!=null )
                list.add(preparedDiscussion);
        }
        return list;
    }

    /**
     * Finds, how many comments is in discussion associated with this
     * GenericObject. If there is more than one comment, it sets its time too.
     * @return Map holding info about comments of this object.
     */
    public static DiscussionHeader findComments(GenericObject object) {
        List children = object.getChildren();
        if (children==null)
            return new DiscussionHeader(null);
        for ( Iterator iter = children.iterator(); iter.hasNext(); ) {
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
        return new DiscussionHeader(null);
    }

    /**
     * Creates facade around Item.
     * @param item question
     * @return Comment instance
     */
    public Comment createComment(Item item) {
        return new Comment(item);
    }

    /**
     * This method is responsible for finding greatest id of comment, that user has read already.
     * @param discussion It may be unitialized
     */
    public static Integer getLastSeenComment(Item discussion, User user, boolean saveInfo) {
        if (user==null)
            return MAX_SEEN_COMMENT_ID;

        Node node = user.getData().selectSingleNode("//new_comments");
        if (node!=null && node.getText().equals("no"))
            return MAX_SEEN_COMMENT_ID;

        List children = discussion.getChildren();
        if (children.size()==0)
            return MAX_SEEN_COMMENT_ID;

        SQLTool sqlTool = SQLTool.getInstance();
        Integer lastSeen = sqlTool.getLastSeenComment(user.getId(), discussion.getId());

        if (saveInfo) {
            Relation childRelation = (Relation) children.get(0);
            GenericObject child = persistance.findById(childRelation.getChild());
            if ( !(child instanceof Record) || ((Record) child).getType()!=Record.DISCUSSION ) {
                log.warn(childRelation+" shall be Record holding discussion!");
                return MAX_SEEN_COMMENT_ID;
            }

            Document data = ((Record) child).getData();
            List comments = data.getRootElement().elements("comment");
            if ( comments!=null && comments.size()>0 ) {
                Element lastComment = (Element) comments.get(comments.size()-1);
                String tmp = lastComment.attributeValue("id");
                int total = Misc.parseInt(tmp, 0);
                if (lastSeen==null || total>lastSeen.intValue()) {
                    sqlTool.insertLastSeenComment(user.getId(), discussion.getId(), total);
                    EnsureWatchedDiscussionsLimit.checkLimits(user.getId());
                }
            }
        }

        return (lastSeen==null) ? MAX_SEEN_COMMENT_ID : lastSeen;
    }

    /**
     * Finds rating of specified type for given object.
     * @param object object that might have rating
     * @param type type of rating we are interested in
     * @return current value or null, if such rating doesn't exist
     */
    public Float ratingFor(Branch object, String type) {
        Element rating = (Element) object.selectSingleNode("//rating[type/text()=\""+type+"\"]");
        if ( rating==null )
            return null;

        int sum = Misc.parseInt(rating.elementText("sum"), EditRating.VALUE_MIN);
        int count = Misc.parseInt(rating.elementText("count"), EditRating.VALUE_MIN);
        float result = sum/(float)count;

        return new Float(result);
    }

    /**
     * replaces spaces with html nonbreaking spaces in string
     */
    public String nonBreakingSpaces(String s) {
        int length = s.length();
        if (s==null || length==0) return "";
        int i = s.indexOf(' '), j=0;
        if (i==-1) return s;
        StringBuffer sb = new StringBuffer();
        while(i<length && i>-1) {
            sb.append(s.substring(j, i));
            sb.append("&nbsp;");
            j = i+1;
            i = s.indexOf(j, ' ');
        }
        if (i==-1 && j<length)
            sb.append(s.substring(j));
        return sb.toString();
    }

    /**
     * @return string where spaces after one or two characters log words
     * are replaced by non breaking spaces.
     */
    public String vlnka(String s) {
        String modified = reVlnka.subst(s,vlnkaReplacement,RE.REPLACE_ALL+RE.REPLACE_BACKREFERENCES);
        return modified;
    }
}
