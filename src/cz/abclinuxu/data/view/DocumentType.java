/*
 *  Copyright (C) 2008 Karel Piwko
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
package cz.abclinuxu.data.view;

/**
 * Holder of document types.
 * @author kapy
 */
public class DocumentType {

    /**
     * Type of document is now known
     */
    public static final int TYPE_UNKNOWN = -1;
    public static final String SUBTYPE_NULL = "NULL";

    /** Unique identification of type */
    private String key;
    /** Human readable name, used for example in HTML forms */
    private String label;
    /**
     * Constant from class Item
     */
    private int type;
    private String subtype;

    /**
     * Creates new DocumentType holder using provided values
     * @param key Unique identification
     * @param label Name of type, readable by user
     * @param type Numeric identification of type in SQL relations
     * @param subtype optional additional qualifier. "NULL" is special value meaning that subtype must be null,
     * java null means that this proeprty is undefined and it will be ignored in search
     */
    public DocumentType(String key, String label, int type, String subtype) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.subtype = subtype;
    }

    /**
     * Creates new DocumentType holder using provided values
     * @param key Unique identification
     * @param label Name of type, readable by user
     * @param type Numeric identification of type in SQL relations
     */
    public DocumentType(String key, String label, int type) {
        this(key, label, type, null);
    }

    /**
     * Key getter.
     * @return Value of key
     */
    public String getKey() {
        return key;
    }

    /**
     * Label getter.
     * @return Value of label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Type getter
     * @return Value of numeric identification
     */
    public int getType() {
        return type;
    }

    /**
     * Subtype getter
     * @return optional additional qualifier
     */
    public String getSubtype() {
        return subtype;
    }

    public String toString() {
        return label;
    }
}
