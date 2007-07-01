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

import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.persistence.versioning.VersionInfo;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersionNotFoundException;
import cz.abclinuxu.persistence.versioning.VersionedDocument;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.html.view.ShowForum;

import java.util.*;
import java.text.ParseException;
import java.text.DateFormat;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;

/**
 * Miscallenous utilities.
 */
public class Misc {

    /**
     * Commits specified relation into version repository.
     * @param obj object to be stored as new revision in versioning
     * @param relationId identifies data
     * @param user user who created this version
     * @param descr description of commited changes
     * @return VersionInfo
     */
    public static VersionInfo commitRelationRevision(GenericDataObject obj, int relationId, User user, String descr) {
        Element copy = obj.getData().getRootElement().createCopy();

        // we do not store monitor element in versioning
        Element monitor = copy.element("monitor");
        if (monitor != null)
            monitor.detach();

        // we store properties in the revision
        Map<String, Set<String>> properties = obj.getProperties();
        if (properties != null) {
            Element propertiesElement = DocumentHelper.makeElement(copy, "/versioning/properties");
            for (String key : properties.keySet()) {
                Element propertyElement = propertiesElement.addElement("property");
                propertyElement.addElement("key").setText(key);
                for (String value : properties.get(key)) {
                    propertyElement.addElement("value").setText(value);
                }
            }
        }

        Versioning versioning = VersioningFactory.getVersioning();
        return versioning.commit(copy.asXML(), relationId, user.getId(), descr);
    }

    /**
     * Loads specified revision of given relation into the item.
     *
     * @param item       GenericDataObject that shall be updated with data from given revision. It shall be already initialized.
     * @param relationId id of relation where item is child
     * @param revision   existing revision number
     * @throws cz.abclinuxu.persistence.versioning.VersionNotFoundException
     *          no such revision for given relation
     */
    public static void loadRelationRevision(GenericDataObject item, int relationId, int revision) throws VersionNotFoundException {
        Versioning versioning = VersioningFactory.getVersioning();
        VersionedDocument version = versioning.load(relationId, revision);
        Document document = item.getData();
        Element monitor = (Element) document.selectSingleNode("/data/monitor");
        item.setData(version.getDocument());
        document = item.getData();
        item.setUpdated(version.getCommited());
        item.setOwner(version.getUser());

        // we do not store monitor element in versioning, let's use it from persistance
        if (monitor != null) {
            monitor = monitor.createCopy();
            Element element = (Element) document.selectSingleNode("/data/monitor");
            if (element != null)
                element.detach();
            document.getRootElement().add(monitor);
        }

        Element versioningData = (Element) document.selectSingleNode("/data/versioning");
        if (versioningData != null) {
            versioningData.detach();
            item.clearProperties();

            List list = versioningData.selectNodes("properties/property");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Element propertyElement = (Element) iter.next();
                String key = propertyElement.elementText("key");
                for (Iterator iterIn = propertyElement.elements("value").iterator(); iterIn.hasNext();) {
                    Element value = (Element) iterIn.next();
                    item.addProperty(key, value.getText());
                }
            }
        }
    }

    /**
     * Purges all revisions of specified relation from version repository. This action cannot be undone!
     * @param relationId identifies data
     */
    public static void purgeRelationRevisions(int relationId) {
        Versioning versioning = VersioningFactory.getVersioning();
        versioning.purge(relationId);
    }

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
    public static void storeToMap(Map map, String key, Object value) {
        List list = (List) map.get(key);
        if (list == null) {
            list = new ArrayList(5);
            map.put(key,list);
        }
        list.add(value);
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
     * Removes trailing spaces from the argument. If s has length 0,
     * null is returned otherwise trimmed version is returned.
     * @param s text to be trimmed
     * @return trimmed s or null, if s has no non-whitespace character
     */
    public static String trimUndefined(String s) {
        if (s==null)
            return s;
        if (s!=null)
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
        int length = input.length();
        if (length == 0)
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
}
