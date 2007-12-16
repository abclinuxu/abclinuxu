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

import java.util.Date;

/**
 * Class to hold tag information.
 * @author literakl
 * @since 14.9.2007
 */
public class Tag {
    private String id;
    private String title;
    private Date created;
    private int usage;

    /**
     * Creates new instance of Tag.
     * @param id id constructed from the title
     * @param title tag title
     */
    public Tag(String id, String title) {
        this.title = title;
        this.id = id;
    }

    /**
     * Gets unique identifier of this tag constructed from the title
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
     * Time when this tag was created
     * @return creation date
     */
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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
        return "tag " + title;
    }
}
