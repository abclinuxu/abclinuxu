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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Collections;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.cache.TagCache;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.comparator.TagTitleComparator;
import cz.finesoft.socd.analyzer.DiacriticRemover;

/**
 * Main class to work with tags to be used by the application developer.
 * @author literakl
 * @since 16.12.2007
 */
public class TagTool implements Configurable {
    private static Logger log = Logger.getLogger(TagTool.class);

    private static TagCache cache = TagCache.getInstance();
    private static Persistence persistence = PersistenceFactory.getPersistence();

    public static final String PREF_INVALID_TITLE_REGEXP = "regexp.invalid.title";
    private static Pattern reInvalidTitle;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new TagTool());
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_INVALID_TITLE_REGEXP, null);
        reInvalidTitle = Pattern.compile(tmp);
    }

    public static void init() {
        persistence.getTags(); // rereads the tag cache
    }

    /**
     * Creates new tag in persistent storage.
     * @param tag tag to be persisted, its key must be unique
     */
    public static void create(Tag tag, User user, String ipAddress) {
        persistence.create(tag);
        SQLTool.getInstance().logTagAction(tag, Action.ADD, user, ipAddress, null);
    }

    /**
     * Update tag's title and keywords.
     * @param tag a tag
     */
    public static void update(Tag tag, User user, String ipAddress) {
        persistence.update(tag);
        SQLTool.getInstance().logTagAction(tag, Action.UPDATE, user, ipAddress, null);
    }

    /**
     * Removes tag from persistent storage and unassign all its usages.
     * @param tag a tag to be removed
     */
    public static void remove(Tag tag, User user, String ipAddress) {
        persistence.remove(tag);
        SQLTool.getInstance().logTagAction(tag, Action.REMOVE, user, ipAddress, null);
    }

    /**
     * Returns total number of tags.
     * @return number of tags
     */
    public static int getTagsCount() {
        return cache.size();
    }

    /**
     * Assign tags to given object. Duplicates are slightly ignored.
     * @param obj object to which tags shall be assigned
     * @param tags tags to be assigned
     */
    public static void assignTags(GenericDataObject obj, List<String> tags, User user, String ipAddress) {
        if (tags == null)
            return;
        else
            tags = new ArrayList<String>(tags);

        Persistence persistence = TagTool.persistence;
        SQLTool sqlTool = SQLTool.getInstance();
        for (ListIterator<String> iter = tags.listIterator(); iter.hasNext();) {
            String id = iter.next();
            Tag tag = getById(id);
            if (tag == null)
                continue;
            if (tag.getParent() != null)
            	iter.add(tag.getParent());

            sqlTool.logTagAction(tag, Action.ASSIGN, user, ipAddress, obj);
        }
        persistence.assignTags(obj, tags);
    }

    /**
     * Unassigns tags from given object. Already unassigned tags are slightly ignored.
     * @param obj object from which tags shall be unassigned
     * @param tags tags to be unassigned
     */
    public static void unassignTags(GenericDataObject obj, List<String> tags, User user, String ipAddress) {
        if (tags == null)
            return;

        Persistence persistence = TagTool.persistence;
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
    }

    /**
     * Finds tag with specified identifier or return null, if there is no such tag.
     * The search is case insensitive.
     * @param id identifier
     * @return the tag
     */
    public static Tag getById(String id) {
        return cache.get(id);
    }

    /**
     * List tags in specified order.
     * @param from offset
     * @param count number of returned tags or -1 for all
     * @param order specified sort field - title, usage, creation time
     * @param ascending true when ascending order is requested
     * @return list of tags according to criteria
     */
    public static List<Tag> list(int from, int count, ListOrder order, boolean ascending) {
        return cache.list(from, count, order, ascending);
    }

    /**
     * Finds tags assigned to given object.
     * @param obj object to be searched
     * @return list of tags (empty in case that no tags have been assigned to given object)
     */
    public static List<Tag> getAssignedTags(GenericDataObject obj) {
        List<String> ids = persistence.getAssignedTags(obj);
        List<Tag> tags = new ArrayList<Tag>(ids.size());
        for (String id : ids) {
            Tag tag = getById(id);
            if (tag == null) {
                log.warn("persistence.getAssignedTags(" + obj + ") returned unknown tag '" + id + "'!");
                continue;
            }
            tags.add(tag);
        }
        Collections.sort(tags, new TagTitleComparator(true));
        return tags;
    }

    /**
     * Generates id from tag title. Use this method to receive consistent behavior.
     * @param title title, it must have at least one character
     * @return id generated from title
     * @throws InvalidInputException if title contains illegal characters
     */
    public static String getNormalizedId(String title) throws InvalidInputException {
        String id = DiacriticRemover.getInstance().removeDiacritics(title.toLowerCase());
        Matcher matcher = reInvalidTitle.matcher(id);
        if (matcher.find())
            throw new InvalidInputException("Název štítku obsahuje nepovolené znaky!");

        id = URLManager.enforceRelativeURL(id);
        if ("edit".equals(id))
            throw new InvalidInputException("Zakázaný název štítku!");

        return id;
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

    /**
     * Constants for list order.
     */
    public static class ListOrder {
        /** order by tag title */
        public static final ListOrder BY_TITLE = new ListOrder("title");
        /** order by tag usage */
        public static final ListOrder BY_USAGE = new ListOrder("usage");
        /** order by time when tag was created */
        public static final ListOrder BY_CREATION = new ListOrder("creation");

        String id;
        private ListOrder(String id) {
            this.id = id;
        }

        public String toString() {
            return id;
        }

        public boolean equals(Object obj) {
            return ((ListOrder)obj).id.equals(id);
        }
    }
}
