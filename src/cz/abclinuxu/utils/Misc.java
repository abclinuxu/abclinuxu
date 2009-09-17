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
package cz.abclinuxu.utils;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.view.Discussion;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.RevisionInfo;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowForum;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.comparator.UserNameComparator;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Attribute;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.ParserException;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Miscallenous utilities.
 */
public class Misc {
    /**
     * Retrieves description of changes, that current user made.
     * @return changes description, null if none provided or Constants.ERROR if there is an error.
     */
    public static String getRevisionString(Map params, Map env) {
        String description = (String) params.get(Constants.PARAM_REVISION_DESCRIPTION);
        description = Misc.filterDangerousCharacters(description);
        if (description == null || description.length() == 0)
            return null;

        if (description.indexOf("<") != -1) {
            ServletUtils.addError(Constants.PARAM_REVISION_DESCRIPTION, "Použití HTML značek je zakázáno!", env, null);
            return Constants.ERROR;
        }
        return description;
    }

    /**
     * Record read for wiki object unless current user committed revision to it.
     * @param obj wiki object
     * @param revisionInfo info about committers
     * @param env environment
     */
    public static void recordReadByNonCommitter(GenericDataObject obj, RevisionInfo revisionInfo, Map env) {
        boolean record = true;
        User user = (User) env.get(Constants.VAR_USER);
        if (user != null) {
            if (revisionInfo.getCreator().getId() == user.getId())
                record = false;
            else {
                for (User commiter : revisionInfo.getCommitters()) {
                    if (commiter.getId() == user.getId()) {
                        record = false;
                        break;
                    }
                }
            }
        }

        if (record)
            ReadRecorder.log(obj, Constants.COUNTER_READ, env);
    }

    /**
     * Tests whether given document is wiki-style.
     * @param obj object to be tested
     * @return true if it is wiki
     */
    public static boolean isWiki(GenericDataObject obj) {
        if (! (obj instanceof Item))
            return false;
        switch (obj.getType()) {
            case Item.CONTENT:
            case Item.DICTIONARY:
            case Item.DRIVER:
            case Item.FAQ:
            case Item.HARDWARE:
            case Item.PERSONALITY:
            case Item.SOFTWARE:
                return true;
            default: return false;
        }
    }

    /**
     * Parses string into int. If str cannot be parsed
     * for any reason, it returns the second parameter.
     * @param str String to be parsed, may be null.
     * @param def Default value to be returned, if str is not integer
     */
    public static int parseInt(String str, int def) {
        if ( str==null || str.length()==0 ) return def;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Parse str to integer. Str must start with digit(s). If it encounters a character,
     * that is not decimal digit, it skips the rest of input.
     * @throws InvalidInputException If the str doesn't start with any digit.
     */
    public static int parsePossiblyWrongInt(String str) throws InvalidInputException {
        int last = 0;
        char c;
        for (int i=0; i<str.length(); i++) {
            c = str.charAt(i);
            if (c<'0' || c>'9')
                continue;
            last++;
        }
        if (last==0)
            throw new InvalidInputException("Řetězec '"+str+"' nemůže být převeden na číslo!");
        if (str.length()!=last)
            str = str.substring(0,last);
        return Integer.parseInt(str);
    }

    /**
     * Parses date using given format. If it fails, it returns current date.
     * todo - use this method at all possible places.
     */
    public static Date parseDate(String date, DateFormat format) {
        try {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (format) {
                return format.parse(date);
            }
        } catch (ParseException e) {
            return new Date();
        }
    }

    /**
     * Utility method to fetch string from parameters. It handles invalid situation when parameter
     * is not string (typically Array).
     * @param params params
     * @param key key to be searched
     * @return string or null, if key is not bound
     * @throws InvalidInputException if value is not string
     */
    public static String getString(Map params, String key) throws InvalidInputException {
        Object obj = params.get(key);
        if (obj == null)
            return null;
        if (!(obj instanceof String))
            throw new InvalidInputException("Byl očekáván paramater " + key + " typu string, místo toho přišel " + obj.getClass());
        return (String) obj;
    }

    /**
     * Ensures, that integer fits into specified range.
     * @param x variable to be checked
     * @param min lower limit
     * @param max upper limit
     * @return x, if min &gt;= x &lt;= max<br>
     * min, if x &lt; min<br>
     * max, if x &gt; max
     */
    public static int limit(int x, int min, int max) {
        if ( x<min ) return min;
        if ( x>max ) return max;
        return x;
    }

    /**
     * Associates value with given key in the map. Each key contains
     * list of values. If the list doesn't exist yet, it is created.
     */
    public static void storeToMap(Map<String, List> map, String key, Object value) {
        List list = map.get(key);
        if (list == null) {
            list = new ArrayList(5);
            map.put(key,list);
        }
        list.add(value);
    }

    /**
     * Associates value with given key in the map. Each key contains
     * list of values. If the list doesn't exist yet, it is created.
     */
    public static void storeToMap(Map<String, List<Relation>> map, String key, Relation relation) {
        List list = map.get(key);
        if (list == null) {
            list = new ArrayList(5);
            map.put(key,list);
        }
        list.add(relation);
    }

    /**
     * Finds out, whether string is empty.
     * @return true, if s is null or zero length
     */
    public static boolean empty(String s) {
        return ( s==null || s.length()==0 );
    }

    /**
     * Finds out, whether list is empty.
     * @return true, if list is null or zero length
     */
    public static boolean empty(List list) {
        return ( list==null || list.size()==0 );
    }

    /**
     * Compares two string for equality
     */
    public static boolean same(String a, String b) {
        if ( a==null ) {
            return (b==null);
        }
        return a.equals(b);
    }

    /**
     * Compares two objects.
     * @return true, if both parameters are null or are equal.
     */
    public static boolean same(Object first, Object second) {
        if ( first != null ) {
            if ( second == null )
                return false;
            return first.equals(second);
        }
        return second == null;
    }

    /**
     * Compares two objects.
     * @return true, if both parameters are null or are equal.
     */
    public static boolean sameXml(String first, String second) {
        if ( first != null ) {
            if ( second == null )
                return false;

            if (first.startsWith("<?xml"))
                first = first.substring(first.indexOf('\n') + 1);
            if (second.startsWith("<?xml"))
                second = second.substring(0, second.indexOf('\n') + 1);
            return first.equals(second);
        }
        return second == null;
    }

    /**
     * Removes trailing spaces from the argument. If s has length 0,
     * null is returned otherwise trimmed version is returned.
     * @param s text to be trimmed
     * @return trimmed s or null, if s has no non-whitespace character
     */
    public static String trimUndefined(String s) {
        if (s==null)
            return s;
        s = s.trim();
        if (s.length()==0)
            return null;
        return s;
    }

    /**
     * Removes all characters smaller then 0x20 - space. They can
     * be dangerous for XML processing.
     * @param input
     * @return filtered input
     */
    public static String filterDangerousCharacters(String input) {
        if (input == null)
            return null;
        if (input.length() == 0)
            return input;

        return input.replaceAll("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1f]", "?");
    }

    /**
     * Gets page size from parameters, user preferences or system settings.
     * The paramaters take precendence over user settings.
     * @param defaultSize system settings for page size
     * @param maximum system limit, page size will not exceed this value
     * @param env environment
     * @param userPrefXPath xpath where user preferences are stored, it may be null
     * @return page size to be used
     */
    public static int getPageSize(int defaultSize, int maximum, Map env, String userPrefXPath) {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int count = -1;
        String str = (String) params.get(ShowForum.PARAM_COUNT);
        if ( str != null && str.length() > 0 )
            count = parseInt(str, -1);

        if (userPrefXPath != null && count < 0) {
            User user = (User) env.get(Constants.VAR_USER);
            if (user != null) {
                Node node = user.getData().selectSingleNode(userPrefXPath);
                if (node != null)
                    count = parseInt(node.getText(), -1);
            }
        }

        if ( count == -1 )
            return defaultSize;
        else
            return limit(count, 10, maximum);
    }

    /**
     * Gets default page size from parameters, user preferences or system settings.
     * The paramaters take precendence over user settings.
     * @param env environment
     * @return page size to be used
     */
    public static int getDefaultPageSize(Map env) {
        return getPageSize(AbcConfig.getDefaultPageSize(), 50, env, "/data/settings/page_size");
    }

    /**
     * Creates string in format "(?,?,?)"
     * @param size number of question marks
     * @return string for ids in IN condition
     */
    public static String getInCondition(int size) {
        StringBuffer sb = new StringBuffer();
        sb.append('(');
        for (int i = 0; i < size; i++) {
            sb.append("?,");
        }
        sb.setCharAt(sb.length() - 1, ')');
        return sb.toString();
    }

    /**
     * @return text after last dot in string.
     */
    public static String getFileSuffix(String name) {
        if (name == null)
            return "";
        int i = name.lastIndexOf('.');
        if (i == -1)
            return "";
        else
            return name.substring(i + 1);
    }

    public static String getWebPath(String absolutePath) {
        String deployPath = AbcConfig.getDeployPath();
        return absolutePath.substring(deployPath.length() - 1);
    }

    /**
     * Adds rel=nofollow attribute to all <A> tags in the text.
     * @param text text that shall containt nofollow rel attribute.
     * @return corrected text
     * @throws ParserException
     */
    public static String addRelNofollowToLink(String text) throws ParserException {
        Lexer lexer = new Lexer(text);
        org.htmlparser.Node node;
        TagNode tag;
        String currentTagName;
        StringBuffer sb = new StringBuffer();

        while ((node = lexer.nextNode()) != null) {
            if (!(node instanceof TagNode)) {
                sb.append(node.getText());
                continue;
            }

            tag = (TagNode) node;
            currentTagName = tag.getTagName().toUpperCase();
            if (currentTagName.equals("A") && !tag.isEndTag()) {
                tag.removeAttribute("rel");
                tag.setAttribute("rel", "\"nofollow\"");
            }
            sb.append('<').append(node.getText()).append('>');
        }
        return sb.toString();
    }

    /**
     * Tests if story contains discussion with at least one comment not owned by story author.
     * @param story initialized story
     * @return false if there is discussion with foreign comments.
     */
    public static boolean containsForeignComments(Item story) {
        List<Relation> children = story.getChildren();
        if (children == null)
            return false;

        Persistence persistence = PersistenceFactory.getPersistence();
        for (Relation relation : children) {
            GenericObject child = (relation).getChild();
            if (!(child instanceof Item))
                continue;
            Item item = (Item) child;
            if (!item.isInitialized())
                persistence.synchronize(item);
            if (item.getType() != Item.DISCUSSION)
                continue;

            Discussion diz = Tools.createDiscussionTree(item, null, 0, false);
            if (diz.getSize() == 0)
                return false;

            LinkedList stack = new LinkedList();
            stack.addAll(diz.getThreads());
            Integer owner = story.getOwner();
            while (stack.size() > 0) {
                Comment thread = (Comment) stack.removeFirst();
                if (!owner.equals(thread.getAuthor())) //todo overit funkcnost
                    return true;
                stack.addAll(thread.getChildren());
            }
        }
        return false;
    }

    /**
     * Tests if given user has valid and verified email address.
     * @param user initialized user
     * @return true if email is set, is verified and is not blocked
     */
    public static boolean hasValidEmail(User user) {
        if (user.getEmail() == null)
            return false;

        Element tagEmail = DocumentHelper.makeElement(user.getData(), "/data/communication/email");
        if ("no".equals(tagEmail.attributeValue("valid")))
            return false;
        String value = tagEmail.attributeValue("verified");
        return (value == null || "yes".equals(value));
    }

    /**
     * Opens given file for writing and replaces its content with supplied time rendered using Constants.czFormat.
     * @param file file
     * @param time time to be written into the file
     * @throws IOException some I/O problem
     */
    public static void touchFile(File file, long time) throws IOException {
        String content;
        synchronized (Constants.czFormat) {
            content = Constants.czFormat.format(new Date(time));
        }
        FileWriter fos = new FileWriter(file);
        fos.write(content);
        fos.close();
    }

    /**
     * Sets value to existing or creates new attribute.
     * @param element parent element
     * @param name attribute name
     * @param value desired value
     */
    public static void setAttribute(Element element, String name, String value) {
        Attribute attribute = element.attribute(name);
        if (attribute != null)
            attribute.setText(value);
        else {
            attribute = DocumentHelper.createAttribute(element, name, value);
            element.add(attribute);
        }
    }

    /**
     * Extracts value from element or attribute selected by given xpath starting at element.
     * Purpose of this method is to simplify readability of source code, because it handles
     * situation that xpath matched nothing.
     * @param element starting element
     * @param xpath xpath expression
     * @return value of matched node or null
     */
    public static String getNodeValue(Node element, String xpath) {
        Node node = element.selectSingleNode(xpath);
        if (node != null)
            return node.getText();
        return null;
    }

    /**
     * Extracts boolean value from element or attribute selected by given xpath starting at element.
     * When xpath does not match anything, defaultValue is returned.
     * @param element starting element
     * @param xpath xpath expression
     * @param defaultValue value to be returned when nothing is matched
     * @return value of matched node or defaultValue
     */
    public static Boolean getNodeSetting(Node element, String xpath, Boolean defaultValue) {
        Node node = element.selectSingleNode(xpath);
        if (node != null)
            return "yes".equalsIgnoreCase(node.getText());
        return defaultValue;
    }

    /**
     * Loads list of users for given item whose keys are stored under specified property. Deleted users are skipped.
     * @param item initialized item
     * @param property property to be searched
     * @return list of initialized users
     */
    public static List<User> loadUsersByProperty(Item item, String property) {
        Set<String> strUsers = item.getProperty(property);
        List<User> users = new ArrayList(strUsers.size());
        for (String sId : strUsers) {
            users.add(new User(parseInt(sId, -2)));
        }

        PersistenceFactory.getPersistence().synchronizeList(users, true);
        Collections.sort(users, new UserNameComparator());
        return users;
    }
}
