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

import java.util.List;
import java.util.ArrayList;

/**
 * Class to hold tag information.
 * @author literakl
 * @since 14.9.2007
 */
public class Tag {
    private String id;
    private String title;
    private List<String> keywords = new ArrayList<String>();
    private int usage;

    public Tag(String id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * Gets unique identifier of this tag
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Title for this tag
     * @return title for this tag
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets list of keywords that instructs automatic assignment of document to this tag.
     * @return
     */
    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        if (keywords == null)
            throw new NullPointerException();
        this.keywords = keywords;
    }

    public void addKeyword(String keyword) {
        keywords.add(keyword);
    }

    /**
     * Gets number of objects to which this tag is assigned.
     * @return number of objects to which this tag is assigned
     */
    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;

        final Tag tag = (Tag) o;

        if (!id.equals(tag.id)) return false;

        return true;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return "tag " + id;
    }
}
