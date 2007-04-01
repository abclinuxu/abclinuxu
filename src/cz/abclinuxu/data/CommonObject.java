/*
 *  Copyright (C) 2007 Leos Literak
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
package cz.abclinuxu.data;

import org.dom4j.Document;

import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import cz.abclinuxu.utils.Misc;

/**
 * Common parent for object with properties and XML.
 * @author literakl
 * @since 25.3.2007
 */
public class CommonObject extends GenericObject implements XMLContainer {
    /**
     * XML with data or this object
     */
    protected XMLHandler documentHandler;
    /**
     * Properties of this object *
     */
    protected Map<String, Set<String>> properties;

    public CommonObject() {
    }

    public CommonObject(int id) {
        super(id);
    }

    /**
     * @return XML data of this object
     */
    public Document getData() {
        return (documentHandler != null) ? documentHandler.getData() : null;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        return (documentHandler != null) ? documentHandler.getDataAsString() : null;
    }

    /**
     * sets XML data of this object
     */
    public void setData(Document data) {
        documentHandler = new XMLHandler(data);
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) {
        documentHandler = new XMLHandler(data);
    }

    /**
     * Returns map where key is string identifier of the property
     * and value is Set of its values.
     * @return immutable map of all properties
     */
    public Map<String, Set<String>> getProperties() {
        if (properties == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Finds all string values associated with properties.
     * The returned set is unmodifiable. This method never returns null.
     * @return Set of String values associated with given property
     */
    public Set<String> getProperty(String type) {
        if (properties == null)
            return Collections.emptySet();
        Set<String> result = (Set<String>) properties.get(type);
        if (result == null)
            return Collections.emptySet();
        else
            return Collections.unmodifiableSet(result);
    }

    /**
     * Finds first value for given property and converts it to integer.
     * If there is no such property or it cannot be converted to integer,
     * then null us returned.
     * @param type property key
     * @return property value converted to int or null
     */
    public Integer getIntProperty(String type) {
        if (properties == null)
            return null;
        Set set = (Set) properties.get(type);
        if (set == null || set.size() == 0)
            return null;
        String value = (String) set.iterator().next();
        int result = Misc.parseInt(value, -1);
        return (result != -1) ? Integer.valueOf(result) : null;
    }

    /**
     * Adds specified binding to map of all properties
     * @param property name of key
     * @param value    value to be bound to property
     */
    public void addProperty(String property, String value) {
        if (properties == null)
            properties = new HashMap<String, Set<String>>();
        Set<String> set = (Set<String>) properties.get(property);
        if (set == null) {
            set = new HashSet<String>();
            properties.put(property, set);
        }
        set.add(value);
    }

    /**
     * Set specified binding to map of all properties. Previous bindings will be discarded.
     * @param property name of key
     * @param values   values to be bound to property
     */
    public void setProperty(String property, Set<String> values) {
        if (properties == null)
            properties = new HashMap<String, Set<String>>();
        properties.put(property, values);
    }

    /**
     * Removes all bindings to specified property.
     * @param property name of key
     * @return Set of previous bindings or null, if there were no values associated with given property
     */
    public Set<String> removeProperty(String property) {
        if (properties == null)
            return null;
        return (Set<String>) properties.remove(property);
    }

    /**
     * Removes all properties.
     * @return original properties
     */
    public Map<String, Set<String>> clearProperties() {
        if (properties == null)
            return null;
        Map<String, Set<String>> originalProperties = properties;
        properties = null;
        return originalProperties;
    }
}
