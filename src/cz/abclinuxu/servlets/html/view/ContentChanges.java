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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.ChangedContent;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.OpaqueComparator;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Displays all content documents under selected relation.
 * User can optionally set date, that filters out documents,
 * that were not modified since this date.
 * User: literakl
 * Date: 6.9.2005
 */
public class ContentChanges implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_SORT_BY = "sortBy";
    public static final String PARAM_ORDER = "order";

    public static final String COLUMN_URL = "url";
    public static final String COLUMN_USERNAME = "user";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_SIZE = "size";
    public static final String ORDER_ASCENDING = "asc";
    public static final String ORDER_DESCENDING = "desc";

    /** list of ChangedContent instances */
    public static final String VAR_DATA = "DATA";
    public static final String VAR_SORT_COLUMN = "COLUMN";
    public static final String VAR_ORDER_DESCENDING = "ORDER_DESC";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null) {
            throw new MissingArgumentException("Parametr rid je prázdný!");
        }

        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);
        List result = new ArrayList(), stack = new ArrayList(), children;
        stack.add(relation);

        while (stack.size()>0) {
            Relation childRelation = (Relation) stack.remove(0);
            GenericObject obj = childRelation.getChild();
            if (! (obj instanceof Item))
                continue;
            Item content = (Item) obj;
            if (content.getType()!=Item.CONTENT)
                continue;

            children = content.getChildren();
            Tools.syncList(children);
            stack.addAll(0, children);

            result.add(createChangedContent(childRelation, persistence));
        }

        String column = (String) params.get(PARAM_SORT_BY);
        if (column==null)
            column = COLUMN_DATE;

        boolean orderDesc = true;
        String order = (String) params.get(PARAM_ORDER);
        if (ORDER_ASCENDING.equalsIgnoreCase(order))
            orderDesc = false;

        Comparator comparator = new ChangesComparator(column);
        if (orderDesc)
            comparator = new OpaqueComparator(comparator);
        Collections.sort(result, comparator);

        env.put(VAR_DATA, result);
        env.put(VAR_SORT_COLUMN, column);
        env.put(VAR_ORDER_DESCENDING, Boolean.valueOf(orderDesc));
        return FMTemplateSelector.select("ContentChanges", "show", env, request);
    }

    /**
     * Creates new instance of ChangedContent.
     * @param relation initialized relation
     * @return ChangedContent
     */
    private ChangedContent createChangedContent(Relation relation, Persistence persistence) {
        Item item = (Item) relation.getChild();
        User user = (User) persistence.findById(new User(item.getOwner()));
        String userName = user.getNick();
        if (userName==null)
            userName = user.getName();
        String content = Tools.xpath(item, "/data/content");
        content = Tools.removeTags(content);

        ChangedContent changedContent = new ChangedContent(relation);
        changedContent.setUpdated(item.getUpdated());
        changedContent.setUrl(relation.getUrl());
        changedContent.setSize(content.length());
        changedContent.setUserName(userName);
        changedContent.setUser(user);
        return changedContent;
    }

    /**
     * Compares two valid instances of ChangedContent by selected column, default is COLUMN_DATE.
     */
    static class ChangesComparator implements Comparator {
        String column;

        public ChangesComparator(String column) {
            this.column = column;
        }

        public int compare(Object o1, Object o2) {
            ChangedContent c1 = (ChangedContent) o1, c2 = (ChangedContent) o2;
            if (COLUMN_USERNAME.equals(column)) {
                return c1.getUserName().compareTo(c2.getUserName());
            } else if (COLUMN_URL.equals(column)) {
                return c1.getUrl().compareTo(c2.getUrl());
            } else if (COLUMN_SIZE.equals(column)) {
                return c1.getSize()-c2.getSize();
            } else { // Date column is default
                return c1.getUpdated().compareTo(c2.getUpdated());
            }
        }
    }
}
