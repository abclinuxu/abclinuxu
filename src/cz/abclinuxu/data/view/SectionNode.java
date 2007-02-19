/*
 *  Copyright (C) 2006 Leos Literak
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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holds information about one section.
 * @author literakl
 * @since 3.9.2006
 */
public class SectionNode implements Comparable {
    private String url;
    private String name;
    private String description;
    private int id;
    private int relationId;
    private int itemsCount;
    private int lastItem;
    private List<SectionNode> children;

    public SectionNode(String url, int id, int relationId) {
        this.id = id;
        this.url = url;
        this.relationId = relationId;
        children = new ArrayList<SectionNode>();
    }

    public SectionNode(String url, String name, int id,  int relationId) {
        this(url, id, relationId);
        this.name = name;
    }

    public SectionNode(String url, String name, String description, int id,  int relationId, int itemsCount) {
        this(url, name, id, relationId);
        this.description = description;
        this.itemsCount = itemsCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public void setLastItem(int lastItem) {
        this.lastItem = lastItem;
    }

    /**
     * @return url for this section
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return name of this section
     */
    public String getName() {
        return name;
    }

    /**
     * @return description of this section. It may contain HTML content.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return id of this section
     */
    public int getId() {
        return id;
    }

    /**
     * @return relation id of this section
     */
    public int getRelationId() {
        return relationId;
    }

    /**
     * @return number of items within this section and its subsections (recursively)
     */
    public int getSize() {
        int size = itemsCount;
        for (Iterator<SectionNode> iter = children.iterator(); iter.hasNext();) {
            SectionNode node = iter.next();
            size += node.getSize();
        }
        return size;
    }

    /**
     * @return relation id of last item
     */
    public int getLastItem() {
        return lastItem;
    }

    /**
     * @return number of levels in this tree. The leaf node returns 0.
     */
    public int getDepth() {
        if (children.size() == 0)
            return 0;
        int depth = 0;
        for (Iterator<SectionNode> iter = children.iterator(); iter.hasNext();) {
            SectionNode node = iter.next();
            int thisDepth = node.getDepth();
            if (thisDepth > depth)
                depth = thisDepth;
        }
        return depth + 1;
    }

    /**
     * @return List of subsections
     */
    public List<SectionNode> getChildren() {
        return children;
    }

    public void addSubsection(SectionNode node) {
        children.add(node);
    }

    public String toString() {
        return name;
    }

    public int compareTo(Object o) {
        if (! (o instanceof SectionNode))
            return 0;
        SectionNode node = (SectionNode) o;
        if (name == null)
            return (node.name == null) ?  0 : 1;
        return name.compareTo(node.name);
    }
}
