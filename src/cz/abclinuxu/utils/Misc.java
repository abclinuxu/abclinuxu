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
import cz.abclinuxu.persistance.versioning.VersionInfo;
import cz.abclinuxu.persistance.versioning.VersioningFactory;
import cz.abclinuxu.persistance.versioning.Versioning;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.dom4j.Element;

/**
 * Miscallenous utilities.
 */
public class Misc {

    /**
     * Commits specified relation into version repository.
     * @param element XML to be stored
     * @param relation identifies data
     * @param user user who created this version
     * @return VersionInfo
     */
    public static VersionInfo commitRelation(Element element, Relation relation, User user) {
        String path = Integer.toString(relation.getId());
        String userId = Integer.toString(user.getId());
        Element copy = element.createCopy();
        Element monitor = copy.element("monitor");
        if (monitor != null)
            monitor.detach();
        Versioning versioning = VersioningFactory.getVersioning();
        return versioning.commit(copy.asXML(), path, userId);
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
            throw new InvalidInputException("Øetìzec '"+str+"' nemù¾e být pøeveden na èíslo!");
        if (str.length()!=last)
            str = str.substring(0,last);
        return Integer.parseInt(str);
    }

    /**
     * Parses date using given format. If it fails, it returns current date.
     * todo - use this method at all possible places.
     */
    public static Date parseDate(String date, SimpleDateFormat format) {
        try {
            synchronized (format) {
                return format.parse(date);
            }
        } catch (ParseException e) {
            return new Date();
        }
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
        if ( list==null ) {
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
        if ( first!=null ) {
            if ( second==null ) return false;
            return first.equals(second);
        }
        return second==null;
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
}
