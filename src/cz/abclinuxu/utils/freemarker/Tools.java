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
package cz.abclinuxu.utils.freemarker;

import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.PersistenceException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Nursery;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditRating;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.data.view.*;
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
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;
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
    public static final String PREF_REGEXP_AMPERSAND = "RE_AMPERSAND";

    static Persistence persistence = PersistenceFactory.getPersistance();
    static REProgram reRemoveTags, reVlnka, lineBreak;
    static Pattern reAmpersand;
    static String vlnkaReplacement;

    static {
        Tools tools = new Tools();
        ConfigurationManager.getConfigurator().configureAndRememberMe(tools);
    }

    private static final Integer MAX_SEEN_COMMENT_ID = Integer.MAX_VALUE;


    public void configure(Preferences prefs) throws ConfigurationException {
        try {
            String pref = prefs.get(PREF_REGEXP_REMOVE_TAGS, null);
            RECompiler reCompiler = new RECompiler();
            reRemoveTags = reCompiler.compile(pref);
            pref = prefs.get(PREF_REGEXP_VLNKA, null);
            reVlnka = reCompiler.compile(pref);
            vlnkaReplacement = prefs.get(PREF_REPLACEMENT_VLNKA, null);
            lineBreak = reCompiler.compile("[\r\n$]+");
            pref = prefs.get(PREF_REGEXP_AMPERSAND, null);
            reAmpersand = Pattern.compile(pref);
        } catch (RESyntaxException e) {
            log.error("Chyba pri kompilaci regexpu!", e);
        } catch (PatternSyntaxException e) {
            log.error("Chyba pri kompilaci regexpu!", e);
        }
    }

    /**
     * Returns text value of node selected by xpath expression for GenericObject.
     * @throws cz.abclinuxu.exceptions.PersistenceException if object cannot be synchronized
     */
    public static String xpath(GenericObject obj, String xpath) {
        if ( obj==null || !(obj instanceof XMLContainer) )
            return null;
        if ( !obj.isInitialized() )
            persistence.synchronize(obj);
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
        Relation relation = (Relation) PersistenceFactory.getPersistance().findById(new Relation(relationId.intValue()));
        return childName(relation);
    }

    /**
     * @param relationId object holding integer value of relation id
     * @return name of child
     */
    public static String childName(String relationId) {
        int id = Integer.parseInt(relationId);
        Relation relation = (Relation) PersistenceFactory.getPersistance().findById(new Relation(id));
        return childName(relation);
    }

    /**
     * @return name of child in this relation.
     */
    public static String childName(Relation relation) {
        if ( relation == null || relation.getChild() == null )
            return null;

        Document data = relation.getData();
        if ( data != null ) {
            Node node = data.selectSingleNode("data/name");
            if ( node != null )
                return node.getText();
        }

        return childName(relation.getChild());
    }

    /**
     * @return name of child in this relation.
     */
    public static String childName(GenericObject obj) {
        if ( ! obj.isInitialized() )
            persistence.synchronize(obj);

        if ( obj instanceof GenericDataObject ) {
            Document data = ((GenericDataObject)obj).getData();
            if ( data!=null ) {
                Node node = data.selectSingleNode("data/name");
                if ( node != null )
                    return node.getText();
                node = data.selectSingleNode("data/title");
                if ( node != null )
                    return node.getText();
                node = data.selectSingleNode("data/custom/title");
                if ( node != null )
                    return node.getText();
            }
        }

        if ((obj instanceof Item)) {
            int type = ((Item) obj).getType();
            if (type == Item.DISCUSSION)
                return "Diskuse";
            if (type == Item.NEWS)
                return "Zprávièka";
        }

        if ( obj instanceof Link )
            return ((Link)obj).getText();

        if ( obj instanceof Poll )
            return removeTags(((Poll)obj).getText());

        if ( obj instanceof User )
            return ((User)obj).getName();

        if ( obj instanceof Server )
            return ((Server)obj).getName();

        String name = obj.getClass().getName();
        name = name.substring(name.lastIndexOf('.')+1);
        name = name.concat(" "+obj.getId());
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
     * Returns URL for selected relation.
     * @param relation initialized relation
     * @param urlUtils initialized UrlUtils instance
     * @return URL for this relation
     */
    public static String childUrl(Relation relation, UrlUtils urlUtils) {
        if (relation.getUrl() != null)
            return urlUtils.noPrefix(relation.getUrl());

        if (relation.getId() == Constants.REL_FORUM)
            return urlUtils.noPrefix("/diskuse.jsp");

        if (relation.getId() == Constants.REL_BLOGS)
            return urlUtils.noPrefix("/blog");

        if (relation.getId() == Constants.REL_NEWS)
            return urlUtils.noPrefix("/zpravicky");

        GenericObject child = relation.getChild();
        if (child instanceof Category) {
            Category category = (Category) child;
            if (category.getType() == Category.FORUM)
                return urlUtils.make("/forum/dir/" + relation.getId());

            if (category.getType() == Category.BLOG)
                return urlUtils.noPrefix("/blog/" + category.getSubType());

            return urlUtils.make("/dir/" + relation.getId());
        }

        if (child instanceof Item) {
            Item item = (Item) child;
            if (item.getType() == Item.BLOG) {
                Category blog = (Category) relation.getParent();
                sync(blog);
                return urlUtils.noPrefix(getUrlForBlogStory(blog.getSubType(), item.getCreated(), relation.getId()));
            } else
                return urlUtils.make("/show/" + relation.getId());
        }

        return urlUtils.make("/show/" + relation.getId());
    }

    /**
     * Finds related documents for given Item.
     * @param item
     * @return list with RelatedDocument instances, if there are any.
     */
    public List getRelatedDocuments(Item item) {
        if (! item.isInitialized())
            item = (Item) persistence.findById(item);

        List elements = item.getData().selectNodes("/data/related/document");
        if (elements.size() == 0)
            return Collections.EMPTY_LIST;

        List related = new ArrayList(elements.size());
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            RelatedDocument document = new RelatedDocument(element.elementText("url"), element.attributeValue("type"));
            document.setTitle(element.elementText("title"));
            document.setDescription(element.elementText("description"));
            related.add(document);
        }
        return related;
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
            link.setUrl(childUrl(relation, urlUtils));
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
     *
     * @return integer
     */
    public Integer getMonitorCount(Document document) {
        Object value = document.selectObject("count(//monitor/id)");
        return ((Double) value).intValue();
    }

    /**
     * If number of votes for yes is higher than for no, than
     * this question is considered to be solved.
     * @return whether the question is solved
     */
    public static boolean isQuestionSolved(Document document) {
        Element solved = (Element) document.selectSingleNode("/data/solved");
        if (solved==null)
            return false;
        int yes = Misc.parseInt(solved.attributeValue("yes"), 0);
        int no = Misc.parseInt(solved.attributeValue("no"), 0);
        return yes>no;
    }

    /**
     * Finds out, whether the guidepost shall be displayed or not.
     * @deprecated todo remove
     */
    public boolean isGuidePostEnabled(Object user) {
        if (!(user instanceof User))
            return true;
        User userA = (User) user;
        if (!userA.isInitialized())
            sync(userA);
        Element element = (Element) userA.getData().selectSingleNode("/data/settings/guidepost");
        if (element == null)
            return true;
        return "yes".equals(element.getText());
    }

    /**
     * Finds user's signature and display it, unless visitor forbids it.
     * If user or visitor is registered, these objects will be instance of User
     * class. If signature is for any reason not available, null will be returned.
     * @param user user, whose signature shall be displayed.
     * @param visitor visitor, that is viewing this comment.
     * @return user's signature or null.
     */
    public String getUserSignature(Object user, Object visitor) {
        if (user==null || !(user instanceof User))
            return null;
        User userA = (User) user;
        if (!userA.isInitialized())
            sync(userA);
        Element element = (Element) userA.getData().selectSingleNode("/data/personal/signature");
        if (element==null)
            return null;
        if (visitor != null && (visitor instanceof User)) {
            User userVisitor = (User) visitor;
            if (!userVisitor.isInitialized())
                sync(userVisitor);
            Element element2 = (Element) userVisitor.getData().selectSingleNode("//settings/signatures");
            if (element2!=null && !element2.getTextTrim().equalsIgnoreCase("yes"))
                return null;
        }
        return element.getText();
    }

    /**
     * Creates anchor for the blog of specified user.
     * @param user existing user
     * @param defaultTitle if blog doesn't have set its name, this will be used as anchor text
     * @return anchor to the user's blog
     */
    public String getUserBlogAnchor(User user, String defaultTitle) {
        if (!user.isInitialized())
            sync(user);
        Element element = (Element) user.getData().selectSingleNode("//settings/blog");
        if (element==null)
            return null;
        Category blog = createCategory(element.getTextTrim());
        String title = defaultTitle;
        element = (Element) blog.getData().selectSingleNode("//custom/title");
        if (element!=null)
            title = element.getText();
        String anchor = "<a href=\"/blog/"+blog.getSubType()+"\">"+title+"</a>";
        return anchor;
    }

    /**
     * Creates a List of users, which are in user's blacklist
     * @param user
     * @return list of Users
     */
    public List getUsersBlacklist(User user) {
        List nodes = xpaths(user.getData(), "/data/settings/blacklist/uid");
        List blacklist = new ArrayList(nodes.size());
        for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
            String value = (String) iter.next();
            blacklist.add(new User(new Integer(value)));
        }
        return blacklist;
    }

    /**
     * Creates a List of ids (integer) of users, that are in user's blacklist
     * @param user
     * @return set of integers
     */
    public static Set getUsersBlacklistOnlyIds(User user) {
        List nodes = xpaths(user.getData(), "/data/settings/blacklist/uid");
        Set blacklist = new HashSet(nodes.size());
        for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
            String value = (String) iter.next();
            blacklist.add(new Integer(value));
        }
        return blacklist;
    }

    /**
     * Returns all item relations from parameter that were not created by users
     * blocked by passed user.
     * @param relations initialized Item relations
     * @param aUser typically User instance
     * @return relations not created by blocked users
     */
    public static List filterRelationsOfBlockedUsers(List relations, Object aUser) {
        if (aUser == null || ! (aUser instanceof User))
            return relations;

        Set blocked = getUsersBlacklistOnlyIds((User)aUser);
        List result = new ArrayList(relations.size());
        Relation relation; Item story;
        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            story = (Item) relation.getChild();
            if (! blocked.contains(new Integer(story.getOwner())))
                result.add(relation);
        }
        return result;
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
            Server server = (Server) persistence.findById(new Server(id));
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
     * If <code>str</code> is longer than <code>max</code>, it
     * is shortened to length of <code>max-prefix.length()</code>
     * and <code>prefix</code> is added to start.
     */
    public static String reverseLimit(String str, int max, String prefix) {
        if (str == null || str.length() == 0) return "";
        if (str.length() <= max) return str;

        if (prefix == null) prefix = "";
        StringBuffer sb = new StringBuffer(max);
        sb.append(prefix);
        int index = str.length() + prefix.length() - max;
        sb.append(str.substring(index));
        return sb.toString();
    }

    /**
     * If <code>str</code> is longer than <code>max</code> words, it
     * is shortened to contain only first max words and <code>suffix</code>.
     */
    public static String limitWords(String str, int max, String suffix) {
        if ( str==null || str.length()==0 ) return "";
        StringTokenizer stk = new StringTokenizer(str, " \t\n\r\f", true);
        if (stk.countTokens()<=max)
            return str;

        if ( suffix==null ) suffix = "";
        int i = 0;
        max = 2*max;
        StringBuffer sb = new StringBuffer();
        while (stk.hasMoreTokens() && i<max) {
            sb.append(stk.nextToken());
            i++;
        }
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * Returns string containing count times what.
     * @param what
     * @param count
     * @return multiple concatenation of argument what.
     */
    public static String repeatString(String what, int count) {
        if (count<=0)
            return "";
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<count; i++)
            sb.append(what);
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
     * Creates list in order split to columns. For example if original list contained
     * elements 1,2,3,4,5,6 and number of columns is 2, output will hold 1,4,2,5,3,6.
     * You can the sequentially iterate the list and easily create two columns (1,2,3)
     * and (4,5,6).
     * @param list original list
     * @param columns number of columns
     * @return new list ordered by columns
     */
    public List columnize(List list, int columns) {
        int size = list.size();
        int rows = size/columns;
        if (size%columns!=0)
            rows ++;

        List result = new ArrayList(size);
        for (int i=0; i<rows; i++) {
            for (int j = 0; j<columns; j++) {
                int index = i + j * rows;
                if (index<size)
                    result.add(list.get(index));
            }
        }

        return result;
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
     * Synchronizes object with persistence if it is not initialized.
     * For relation, its child is synchronized too.
     */
    public static GenericObject sync(GenericObject obj) {
        if ( ! obj.isInitialized() )
            persistence.synchronize(obj);
        if ( obj instanceof Relation ) {
            GenericObject child = ((Relation)obj).getChild();
            if ( ! child.isInitialized() )
                persistence.synchronize(child);
        }
        return obj;
    }

    /**
     * Synchronizes list of GenericObjects.
     */
    public static List syncList(Collection collection) throws PersistenceException {
        if (collection.size() == 0)
            return Collections.EMPTY_LIST;

        List list;
        if (! (collection instanceof List) )
            list = new ArrayList(collection);
        else
            list = (List) collection;

        persistence.synchronizeList(list);
        if (list.get(0) instanceof Relation) {
            List children = new ArrayList(list.size());
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                GenericObject obj = (GenericObject) iter.next();
                if (obj instanceof Relation)
                    children.add(((Relation)obj).getChild());
            }
            persistence.synchronizeList(children);
        }
        return list;
    }

    /**
     * Initialize children of relations children.
     * Consequent genericObject.getChildren() runs much faster.
     * @param relations list of initialized relations
     */
    public static void initializeChildren(List relations) {
        List children = new ArrayList(relations.size());
        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            children.add(relation.getChild());
        }
        Nursery.getInstance().initChildren(children);
    }

    /**
     * This method instantiates user and synchronizes it.
     * @return synchronized User.
     */
    public static User createUser(String id) {
        int i = Integer.parseInt(id);
        User user = new User(i);
        return (User) persistence.findById(user);
    }

    /**
     * This method instantiates user and synchronizes it.
     * @return synchronized User.
     */
    public static User createUser(int id) {
        User user = new User(id);
        return (User) persistence.findById(user);
    }

    /**
     * This method instantiates category and synchronizes it.
     * @return synchronized category.
     */
    public static Category createCategory(int id) {
        Category category = new Category(id);
        return (Category) persistence.findById(category);
    }

    /**
     * This method instantiates category and synchronizes it.
     * @return synchronized category.
     */
    public static Category createCategory(String id) {
        int i = Integer.parseInt(id);
        Category category = new Category(i);
        return (Category) persistence.findById(category);
    }

    /**
     * @param type type of counter to be fetched
     * @return counter value for selected GenericObject
     */
    public static int getCounterValue(GenericObject obj, String type) {
        return persistence.getCounterValue(obj, type);
    }

    /**
     * Fetches values of counter for children in list of relations.
     * @param relations initialized list of relations
     * @param type type of counter to be fetched
     * @return map where key is GenericObject and value is Number with its counter.
     */
    public static Map getRelationCountersValue(List relations, String type) {
        if (relations==null || relations.size()==0)
            return Collections.EMPTY_MAP;
        List list = new ArrayList(relations.size());
        for (Object relation1 : relations) {
            Relation relation = (Relation) relation1;
            list.add(relation.getChild());
        }
        return persistence.getCountersValue(list, type);
    }

    /**
     * This method groups relations by their children's type. Each type represents
     * one of Constants.TYPE_* strings. The key represents list of relations, where
     * children are same type.
     */
    public static Map groupByType(List relations) throws PersistenceException {
        return groupByType(relations, null);
    }

    /**
     * This method groups relations by their children's type. Each type represents
     * one of Constants.TYPE_* strings. The key represents list of relations, where
     * all children have same type. The classFilter gives possibility to include
     * only children of certain type. If it is empty or null, all classes are considered,
     * otherwise only specified classes will be included in result. Possible value is
     * class name (not FQCN).
     * @param classFilter comma separated list of classes, that may be included in the list
     */
    public static Map groupByType(List relations, String classFilter) throws PersistenceException {
        if (relations==null)
            return Collections.EMPTY_MAP;
        else
            if (!Misc.empty(classFilter))
                relations = new ArrayList(relations);

        boolean itemYes, recordYes, categoryYes, userYes, pollYes, linkYes;
        itemYes = recordYes = categoryYes = userYes = pollYes = linkYes = false;
        if (Misc.empty(classFilter))
            itemYes = recordYes = categoryYes = userYes = pollYes = linkYes = true;
        else {
            StringTokenizer stk = new StringTokenizer(classFilter);
            while (stk.hasMoreTokens()) {
                String className = stk.nextToken();
                if ("Item".equalsIgnoreCase(className))
                    itemYes = true;
                else if ("Record".equalsIgnoreCase(className))
                    recordYes = true;
                else if ("Category".equalsIgnoreCase(className))
                    categoryYes = true;
                else if ("User".equalsIgnoreCase(className))
                    userYes = true;
                else if ("Poll".equalsIgnoreCase(className))
                    pollYes = true;
                else if ("Link".equalsIgnoreCase(className))
                    linkYes = true;
            }
        }

        boolean needsSync = false;
        GenericObject child;
        for (Iterator iterator = relations.iterator(); iterator.hasNext();) {
            Relation relation = (Relation) iterator.next();
            if (!relation.isInitialized()) {
                needsSync = true;
                continue;
            }

            child = relation.getChild();
            if (child instanceof Item && !itemYes) {
                iterator.remove();
                continue;
            } else if (child instanceof Record && !recordYes) {
                iterator.remove();
                continue;
            } else if (child instanceof Category && !categoryYes) {
                iterator.remove();
                continue;
            } else if (child instanceof Poll && !pollYes) {
                iterator.remove();
                continue;
            } else if (child instanceof User && !userYes) {
                iterator.remove();
                continue;
            } else if (child instanceof Link && !linkYes) {
                iterator.remove();
                continue;
            }

            if (!child.isInitialized())
                needsSync = true;
        }
        if (needsSync)
            syncList(relations);

        Map map = new HashMap();

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();

            child = relation.getChild();
            if ( child instanceof Category )
                Misc.storeToMap(map,Constants.TYPE_CATEGORY,relation);
            else if ( child instanceof Item ) {
                Item item = (Item) child;
                if ( item.getType()==Item.HARDWARE )
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
            else if ( child instanceof Link )
                Misc.storeToMap(map,Constants.TYPE_LINK, relation);
            else if ( child instanceof Poll )
                Misc.storeToMap(map,Constants.TYPE_POLL, relation);
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

        if (Format.SIMPLE.equals(format))
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
                case Item.HARDWARE: return Constants.TYPE_MAKE.equalsIgnoreCase(type);
                case Item.ARTICLE: return Constants.TYPE_ARTICLE.equalsIgnoreCase(type);
                case Item.DISCUSSION: return Constants.TYPE_DISCUSSION.equalsIgnoreCase(type);
                case Item.REQUEST: return Constants.TYPE_REQUEST.equalsIgnoreCase(type);
                case Item.DRIVER: return Constants.TYPE_DRIVER.equalsIgnoreCase(type);
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
        List exceptions = null;
        if (prohibited.size() > 0)
            exceptions = prohibited.toList();
        return saveParams(params, exceptions);
    }

    /**
     * Encodes all mapping from <code>params</code> except those listed
     * in <<code>prohibited</code> list as form hidden input.
     */
    public static String saveParams(Map params, List exceptions) {
        if (params == null || params.size() == 0)
            return "";
        if (exceptions == null)
            exceptions = Collections.EMPTY_LIST;

        StringBuffer sb = new StringBuffer(params.size() * 50);
        Set entries = params.keySet();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            if (exceptions.contains(key))
                continue;

            List values = null;
            if (params.get(key) instanceof List)
                values = (List) params.get(key);
            else
                values = Collections.singletonList(params.get(key));

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
                default: sb.append((char)c);
            }
        }

        String out = sb.toString();
        Matcher matcher = reAmpersand.matcher(out);
        return matcher.replaceAll("&amp;");
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
            persistence.synchronize(record);

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
        if (text==null || text.length()==0)
            return "";
        if (text.indexOf('<')==-1)
            return text;
        try {
//            return StripTags.process(text);
            return new RE(reRemoveTags, RE.MATCH_MULTILINE).subst(text,"");
        } catch (Throwable e) {
            log.warn("Oops, remove tags regexp failed on '"+text+"'!", e);
            return text;
        }
    }

    /**
     * Converts Item with type Discussion to object Discussion.
     * @param obj Item
     * @param maybeUser this may be instance of User that is viewing this discussion
     * @param rid relation id of this discussion
     * @param saveLast if true and maybeUser is instance of User, then latest comment id will be saved
     */
    public Discussion createDiscussionTree(GenericObject obj, Object maybeUser, int rid, boolean saveLast) throws PersistenceException {
        if (!obj.isInitialized())
            obj = persistence.findById(obj);
        if ( !InstanceUtils.checkType(obj, Item.class, Item.DISCUSSION) )
            throw new IllegalArgumentException("Not a discussion: "+obj);

        Item item = (Item) obj;
        Document document = item.getData();
        User user = null;
        if (maybeUser instanceof User)
            user = (User) maybeUser;

        Discussion discussion = new Discussion();
        discussion.setId(obj.getId());
        discussion.setRelationId(rid);
        discussion.setFrozen(document.selectSingleNode("/data/frozen") != null);
        Integer monitorCount = getMonitorCount(document);
        discussion.setMonitorSize(monitorCount);
        if (user != null) {
            String xpath = "//monitor/id[text()='"+user.getId()+"']";
            discussion.setMonitored(document.selectSingleNode(xpath) != null);
        }

        if (item.getChildren().size()==0)
            return discussion;

        Relation child = (Relation) item.getChildren().get(0);
        Record record = (Record) child.getChild();
        if (!record.isInitialized())
            record = (Record) persistence.findById(record);

        DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
        if (dizRecord.getThreads().size() == 0)
            return discussion;

        discussion.init(dizRecord);
        if (user != null) {
            SQLTool sqlTool = SQLTool.getInstance();
            discussion.setBlacklist(getUsersBlacklistOnlyIds(user));
            Integer lastSeen = user.getLastSeenComment(obj.getId());
            if (lastSeen != null)
                discussion.setUnreadComments(lastSeen);
            sqlTool.insertLastSeenComment(user.getId(), obj.getId(), discussion.getGreatestId());
            user.storeLastSeenComment(obj.getId(), discussion.getGreatestId());
            EnsureWatchedDiscussionsLimit.checkLimits(user.getId());
        }

        return discussion;
    }

    /**
     * @return empty discussion
     */
    public Discussion createEmptyDiscussion() {
        return new Discussion();
    }

    /**
     * This method is responsible for finding greatest id of comment, that user has read already.
     *
     * @param discussion It may be unitialized
     */
    public static Integer getLastSeenComment(Item discussion, User user) {
        if (user == null)
            return MAX_SEEN_COMMENT_ID;

        List children = discussion.getChildren();
        if (children.size() == 0)
            return MAX_SEEN_COMMENT_ID;

        SQLTool sqlTool = SQLTool.getInstance();
        Integer lastSeen = sqlTool.getLastSeenComment(user.getId(), discussion.getId());
        return (lastSeen == null) ? MAX_SEEN_COMMENT_ID : lastSeen;
    }

    /**
     * @return uninitialized DiscussionHeader.
     */
    public static DiscussionHeader analyzeDiscussion(String s) {
        return new DiscussionHeader(null);
    }

    /**
     * Gathers statistics on given discussion.
     */
    public static DiscussionHeader analyzeDiscussion(Relation relation) {
        if (relation==null)
            return new DiscussionHeader(null);

        if ( ! InstanceUtils.checkType(relation.getChild(), Item.class, Item.DISCUSSION) ) {
            log.error("Relation "+relation.getId()+" doesn't contain item!");
            return null;
        }

        Item item = (Item) relation.getChild();
        DiscussionHeader discussion = new DiscussionHeader(item);
        discussion.relationId = relation.getId();
        discussion.updated = item.getUpdated();
        discussion.created = item.getCreated();
        discussion.url = relation.getUrl();

        Document data = item.getData();
        Element element = (Element) data.selectSingleNode("/data/comments");
        discussion.responseCount = Misc.parseInt(element.getText(), 0);
        element = (Element) data.selectSingleNode("/data/last_id");
        if (element != null)
            discussion.lastCommentId = Misc.parseInt(element.getText(), discussion.responseCount);
        else
            discussion.lastCommentId = discussion.responseCount;

        Node node = data.selectSingleNode("data/title");
        if (node == null) {
            GenericObject parent = relation.getParent();
            if (!parent.isInitialized())
                persistence.synchronize(parent);
            if (parent instanceof Item) {
                item = (Item) parent;
                data = item.getData();
                int type = item.getType();
                if (type==Item.ARTICLE || type==Item.BLOG)
                    node = data.selectSingleNode("data/name");
                else if ( type==Item.NEWS || type==Item.BAZAAR ) {
                    discussion.title = "Zprávièka";
                    node = data.selectSingleNode("data/title");
                }
                if (node != null)
                    discussion.title = node.getText();
            } else if ( parent instanceof Poll) {
                discussion.title = ((Poll)parent).getText();
            }
        } else
            discussion.title = node.getText();

        discussion.title = removeTags(discussion.title);
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
     * Tests whether user has seen all comments in given discussion.
     * @param maybeUser instance of User or some undefined value
     * @param diz initialized discussion header
     * @return true if maybeUser is user, which has seen this discussion and there is new comment
     */
    public boolean hasNewComments(Object maybeUser, DiscussionHeader diz) {
        if (maybeUser == null || ! (maybeUser instanceof User))
            return false;
        if (diz.getDiscussion() == null)
            return false;
        int dizId = diz.getDiscussion().getId();
        Integer lastSeen = ((User)maybeUser).getLastSeenComment(dizId);
        if (lastSeen == null)
            return false;
        return lastSeen < diz.getLastCommentId();
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
                persistence.synchronize(item);
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
        return new ItemComment(item);
    }

    public Map ratingFor(Branch object, String s) {
        return ratingFor(object);
    }
    /**
     * Finds rating of specified type for given object.
     * @param object object that might have rating
     * @return null, if such rating doesn't exist. Otherwise map will contain
     * keys "sum", "count" and "result".
     */
    public Map ratingFor(Branch object) {
        Element rating = (Element) object.selectSingleNode("//rating");
        if ( rating==null )
            return null;

        int sum = Misc.parseInt(rating.elementText("sum"), EditRating.VALUE_MIN);
        int count = Misc.parseInt(rating.elementText("count"), EditRating.VALUE_MIN);
        float percent = (100 * sum) / (float)(count * EditRating.VALUE_MAX);

        Map map = new HashMap();
        map.put("sum", sum);
        map.put("count", count);
        map.put("percent", percent);

        return map;
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
        String modified = new RE(reVlnka, RE.MATCH_MULTILINE).subst(s,vlnkaReplacement,RE.REPLACE_ALL+RE.REPLACE_BACKREFERENCES);
        return modified;
    }

    /**
     * Gets absolute url for story.
     * @param blogName Name of blog.
     * @param date date when story was published
     * @param relation id of relation of story
     * @return formated unique URL. E.g. /blog/leos/archive/2004/12/12/12345
     */
    public static String getUrlForBlogStory(String blogName, Date date, int relation) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        StringBuffer sb = new StringBuffer("/blog/");
        sb.append(blogName);
        sb.append('/');
        sb.append(calendar.get(Calendar.YEAR));
        sb.append('/');
        sb.append(calendar.get(Calendar.MONTH)+1);
        sb.append('/');
        sb.append(calendar.get(Calendar.DAY_OF_MONTH));
        sb.append('/');
        sb.append(relation);
        return sb.toString();
    }

    /**
     * Replaces new line characters with space.
     */
    public static String removeNewLines(String text) {
        return new RE(lineBreak, RE.MATCH_MULTILINE).subst(text, " ");
    }

    /**
     * Returns true if object <code>o</code> is a List containing, or a String equal to <code>s</code>.
     */
    public static boolean isWithin(Object o, String s) {
        if ( o==null || s==null )
            return false;

        if ( o instanceof Collection )
            return ((Collection) o).contains(s);

        if ( o instanceof String )
            return ((String) o).equals(s);

        return false;
    }

    /**
     * Returns obj as list. If obj is List, then it is casted
     * to List. If it is collection, then new ArrayList initialized
     * with its entries is returned. If obj is null, empty list
     * is returned. In all other cases list with obj is returned.
     * Warning - returned list may be read-only.
     *
     * @param obj value that can be list
     * @return obj casted to List or List containing obj
     */
    public static List asList(Object obj) {
        if (obj == null)
            return Collections.EMPTY_LIST;
        if (obj instanceof List)
            return (List) obj;
        if (obj instanceof Collection)
            return new ArrayList((Collection) obj);
        return Collections.singletonList(obj);
    }

    /**
     * Returns obj as set. If obj is Set, then it is casted
     * to Set. If it is collection, then new HashSet initialized
     * with its entries is returned. If obj is null, empty set
     * is returned. In all other cases Set with obj is returned.
     * Warning - returned list may be read-only.
     *
     * @param obj value that can be list
     * @return obj casted to Set or Set containing obj
     */
    public static Set asSet(Object obj) {
        if (obj == null)
            return Collections.EMPTY_SET;
        if (obj instanceof Set)
            return (Set) obj;
        if (obj instanceof Collection)
            return new HashSet((Collection) obj);
        return Collections.singleton(obj);
    }

    /**
     * Finds all screenshots for given object.
     * @return list of Maps with two keys: path and optional thumbnailPath.
     */
    public List screenshotsFor(GenericDataObject obj) {
        if (obj == null)
            return Collections.EMPTY_LIST;

        Element images = (Element) obj.getData().selectSingleNode("/data/inset/images");
        if (images == null)
            return Collections.EMPTY_LIST;

        List result = new ArrayList();
        for (Iterator iter = images.elementIterator("image"); iter.hasNext();) {
            Element element = (Element) iter.next();
            Map map = new HashMap(2, 1.0f);
            map.put("path", element.getText());
            map.put("thumbnailPath", element.attributeValue("thumbnail"));
            result.add(map);
        }
        return result;
    }
}
