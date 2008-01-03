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
package cz.abclinuxu.utils;

import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;

import java.util.List;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Main class to work with tags. The application programmer shall use these tags instead of Persistence interface.
 * @author literakl
 * @since 16.12.2007
 */
public class TagTool {
    // cache, probably sorted by usage property

    /**
     * Creates new tag in persistent storage.
     * @param tag tag to be persisted, its key must be unique
     */
    public void create(Tag tag, User user, String ipAddress) {
        PersistenceFactory.getPersistence().create(tag);
        SQLTool.getInstance().logTagAction(tag, Action.ADD, user, ipAddress, null);
    }

    /**
     * Update tag's title and keywords.
     * @param tag a tag
     */
    public void update(Tag tag, User user, String ipAddress) {
        PersistenceFactory.getPersistence().update(tag);
        SQLTool.getInstance().logTagAction(tag, Action.UPDATE, user, ipAddress, null);
    }

    /**
     * Removes tag from persistent storage and unassign all its usages.
     * @param tag a tag to be removed
     */
    public void remove(Tag tag, User user, String ipAddress) {
        PersistenceFactory.getPersistence().create(tag);
        SQLTool.getInstance().logTagAction(tag, Action.REMOVE, user, ipAddress, null);
    }

    /**
     * Assign tags to given object. Duplicates are slightly ignored.
     * @param obj object to which tags shall be assigned
     * @param tags tags to be assigned
     */
    public void assignTags(GenericDataObject obj, List<String> tags, User user, String ipAddress) {
        if (tags == null)
            return;

        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        for (Iterator<String> iter = tags.iterator(); iter.hasNext();) {
            String id = iter.next();
            Tag tag = getById(id);
            if (tag == null) {
                iter.remove();
                continue;
            }

            sqlTool.logTagAction(tag, Action.ASSIGN, user, ipAddress, obj);
        }
        persistence.assignTags(obj, tags);
        // TODO update cache
    }

    /**
     * Unassigns tags from given object. Already unassigned tags are slightly ignored.
     * @param obj object from which tags shall be unassigned
     * @param tags tags to be unassigned
     */
    public void unassignTags(GenericDataObject obj, List<String> tags, User user, String ipAddress) {
        if (tags == null)
            return;

        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        for (Iterator<String> iter = tags.iterator(); iter.hasNext();) {
            String id = iter.next();
            Tag tag = getById(id);
            if (tag == null) {
                iter.remove();
                continue;
            }

            sqlTool.logTagAction(tag, Action.UNASSIGN, user, ipAddress, obj);
        }
        persistence.unassignTags(obj, tags);
        // TODO update cache
    }

    /**
     * Finds tag with specified identifier or return null, if there is no such tag.
     * The search is case insensitive.
     * @param id identifier
     * @return the tag
     */
    public Tag getById(String id) {
        return null;
    }

    /**
     * List tags in specified order.
     * @param from offset
     * @param count number of returned tags
     * @param order specified sort field - title, usage, creation time
     * @param ascending true when ascending order is requested
     * @return tags according to criteria
     */
    public List<Tag> list(int from, int count, String order, boolean ascending) {
        return null;
    }

    /**
     * Finds most frequently used tags. The returned tags will be sorted by their title in ascending order.
     * @param count number of returned tags
     * @return found tags
     */
    public List<Tag> getMostUsedTags(int count) {
        return null;
    }

    /**
     * Finds tags assigned to given object.
     * @param obj object to be searched
     * @return list of tags (empty in case that no tags have been assigned to given object)
     */
    public List<Tag> getAssignedTags(GenericDataObject obj) {
        return null;
    }

    /**
     * Compares two Tags by their usage property in descending order.
     */
    private static class UsageComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((Tag)o2).getUsage() - ((Tag)o1).getUsage();
        }
    }

    /**
     * Constants for logging the action.
     */
    public static class Action {
        public static final Action ADD = new Action("add");
        public static final Action UPDATE = new Action("update");
        public static final Action REMOVE = new Action("remove");
        public static final Action ASSIGN = new Action("assign");
        public static final Action UNASSIGN = new Action("unassign");

        String id;
        private Action(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }

        public boolean equals(Object obj) {
            return ((Action)obj).id.equals(id);
        }
    }
}
