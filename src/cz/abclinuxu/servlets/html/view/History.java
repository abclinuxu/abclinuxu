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

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.exceptions.SecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * This servlet is responsible for displaying
 * the range of objects in specified time interval.
 * todo odstranit duplicitu u linkovanych objektu u SQL_ARTICLES
 */
public class History implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(History.class);

    /** type of object to display */
    public static final String PARAM_TYPE = "type";
    /** specifies, which user created the data */
    public static final String PARAM_USER_SHORT = ViewUser.PARAM_USER_SHORT;
    public static final String PARAM_FILTER = "filter";

    public static final String VALUE_TYPE_ARTICLES = "articles";
    public static final String VALUE_TYPE_NEWS = "news";
    public static final String VALUE_TYPE_HARDWARE = "hardware";
    public static final String VALUE_TYPE_SOFTWARE = "software";
    public static final String VALUE_TYPE_DISCUSSION = "discussions";
    public static final String VALUE_TYPE_QUESTIONS = "questions";
    public static final String VALUE_TYPE_COMMENTS = "comments";
    public static final String VALUE_TYPE_DICTIONARY = "dictionary";
    public static final String VALUE_TYPE_PERSONALITIES = "personalities";
    public static final String VALUE_TYPE_WIKI = "wiki";
    public static final String VALUE_TYPE_FAQ = "faq";

    public static final String VALUE_FILTER_LAST = "last";

    /** list of found relations, that match the conditions */
    public static final String VAR_FOUND = "FOUND";
    /** normalized type */
    public static final String VAR_TYPE = "TYPE";
    /** Starting part of URL, until value of from parameter */
    public static final String VAR_URL_BEFORE_FROM = "URL_BEFORE_FROM";
    /** Final part of URL, after value of from parameter */
    public static final String VAR_URL_AFTER_FROM = "URL_AFTER_FROM";
    /** user used as filter */
    public static final String VAR_PAGE_TITLE = "PAGE_TITLE";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();

        String type = (String) params.get(PARAM_TYPE);
        int from = Misc.parseInt((String)params.get(Constants.PARAM_FROM),0);
        int count = Misc.getDefaultPageSize(env);
        int uid = Misc.parseInt((String)params.get(PARAM_USER_SHORT),0);
        String filter = (String) params.get(PARAM_FILTER);

        User selectedUser = null;
        if (uid > 0)
            selectedUser = (User) persistence.findById(new User(uid));

        List<Relation> data;
        List parents = new ArrayList();
        int total;
        Paging found;
        Qualifier[] qualifiers;

        if ( VALUE_TYPE_ARTICLES.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countArticleRelations(0);
            data = sqlTool.findArticleRelations(qualifiers, 0);
            found = new Paging(data,from,count,total,qualifiers);
            type = VALUE_TYPE_ARTICLES;
            parents.add(persistence.findById(new Relation(Constants.REL_ARTICLES)));
            env.put(VAR_PAGE_TITLE, "Archiv článků");

        } else if ( VALUE_TYPE_NEWS.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            if ( selectedUser != null ) {
                total = sqlTool.countNewsRelationsByUser(uid);
                data = sqlTool.findNewsRelationsByUser(uid, qualifiers);
                env.put(VAR_PAGE_TITLE, selectedUser.getName() + " - archiv zpráviček");
            } else {
                total = sqlTool.countNewsRelations();
                data = sqlTool.findNewsRelations(qualifiers);
                env.put(VAR_PAGE_TITLE, "Archiv zpráviček");
            }
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_NEWS;
            parents.add(persistence.findById(new Relation(Constants.REL_NEWS)));

        } else if ( VALUE_TYPE_FAQ.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countItemRelationsWithType(Item.FAQ, null);
            data = sqlTool.findItemRelationsWithType(Item.FAQ, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_FAQ;
            parents.add(persistence.findById(new Relation(Constants.REL_FAQ)));
            env.put(VAR_PAGE_TITLE, "Archiv často kladených otázek");

        } else if ( VALUE_TYPE_HARDWARE.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countItemRelationsWithType(Item.HARDWARE, new Qualifier[]{});
            data = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_HARDWARE;
            parents.add(persistence.findById(new Relation(Constants.REL_HARDWARE)));
            env.put(VAR_PAGE_TITLE, "Archiv hardwarových záznamů");

        } else if ( VALUE_TYPE_WIKI.equalsIgnoreCase(type) && selectedUser != null ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_WHEN, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countWikiRelationsByUser(uid);
            data = sqlTool.findWikiRelationsByUser(uid, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_WIKI;
            env.put(VAR_PAGE_TITLE, selectedUser.getName() + " - archiv wiki záznamů");

        } else if ( VALUE_TYPE_SOFTWARE.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countItemRelationsWithType(Item.SOFTWARE, new Qualifier[]{});
            data = sqlTool.findItemRelationsWithType(Item.SOFTWARE, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_SOFTWARE;
            parents.add(persistence.findById(new Relation(Constants.REL_SOFTWARE)));
            env.put(VAR_PAGE_TITLE, "Archiv softwarových záznamů");

        } else if ( VALUE_TYPE_DISCUSSION.equalsIgnoreCase(type) ) {
            if (selectedUser != null && VALUE_FILTER_LAST.equals(filter)) {
                User user = (User) env.get(Constants.VAR_USER);
                if (user == null)
                    return FMTemplateSelector.select("ViewUser", "login", env, request);
                if (user.getId() != uid)
                    throw new SecurityException("Není povoleno šmírovat jiné uživatele!");

                qualifiers = getQualifiers(params, Qualifier.SORT_BY_WHEN, Qualifier.ORDER_DESCENDING, from, count);
                total = sqlTool.countLastSeenDiscussionRelationsBy(uid);
                data = sqlTool.findLastSeenDiscussionRelationsBy(uid, qualifiers);
                env.put(VAR_PAGE_TITLE, selectedUser.getName() + " - archiv navštívených diskusí");
            } else {
                qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
                total = sqlTool.countDiscussionRelations();
                data = sqlTool.findDiscussionRelations(qualifiers);
                env.put(VAR_PAGE_TITLE, "Archiv diskusí");
            }
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_DISCUSSION;

        } else if ( VALUE_TYPE_QUESTIONS.equalsIgnoreCase(type) && selectedUser != null ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countQuestionRelationsByUser(uid);
            data = sqlTool.findQuestionRelationsByUser(uid, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_QUESTIONS;
            env.put(VAR_PAGE_TITLE, selectedUser.getName() + " - archiv dotazů");

        } else if ( VALUE_TYPE_COMMENTS.equalsIgnoreCase(type) && selectedUser != null ) {
            qualifiers = getQualifiers(params, null, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countCommentRelationsByUser(uid);
            data = sqlTool.findCommentRelationsByUser(uid, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_COMMENTS;
            env.put(VAR_PAGE_TITLE, selectedUser.getName() + " - archiv komentovaných diskusí");

        } else if ( VALUE_TYPE_DICTIONARY.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countItemRelationsWithType(Item.DICTIONARY, null);
            data = sqlTool.findItemRelationsWithType(Item.DICTIONARY, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_DICTIONARY;
            parents.add(persistence.findById(new Relation(Constants.REL_DICTIONARY)));
            env.put(VAR_PAGE_TITLE, "Archiv záznamů ve slovníků");

        } else if ( VALUE_TYPE_PERSONALITIES.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countItemRelationsWithType(Item.PERSONALITY, null);
            data = sqlTool.findItemRelationsWithType(Item.PERSONALITY, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_PERSONALITIES;
            parents.add(persistence.findById(new Relation(Constants.REL_PERSONALITIES)));
            env.put(VAR_PAGE_TITLE, "Archiv záznamů v Kdo je");

        } else
            return ServletUtils.showErrorPage("Chybí parametr type!",env,request);

        Tools.syncList(found.getData());

        StringBuffer sb = new StringBuffer("&amp;count=").append(found.getPageSize());
        if (found.isQualifierSet(Qualifier.SORT_BY_CREATED.toString()))
            sb.append("&amp;").append(Constants.PARAM_ORDER_BY).append("=").append(Constants.ORDER_BY_CREATED);
        else if (found.isQualifierSet(Qualifier.SORT_BY_UPDATED.toString()))
            sb.append("&amp;").append(Constants.PARAM_ORDER_BY).append("=").append(Constants.ORDER_BY_UPDATED);
        else if (found.isQualifierSet(Qualifier.SORT_BY_WHEN.toString()))
            sb.append("&amp;").append(Constants.PARAM_ORDER_BY).append("=").append(Constants.ORDER_BY_WHEN);
        else if (found.isQualifierSet(Qualifier.SORT_BY_ID.toString()))
            sb.append("&amp;").append(Constants.PARAM_ORDER_BY ).append("=").append(Constants.ORDER_BY_ID);
        else if (found.isQualifierSet(Qualifier.SORT_BY_TITLE.toString()))
            sb.append("&amp;").append(Constants.PARAM_ORDER_BY ).append("=").append(Constants.ORDER_BY_TITLE);

        if ( found.isQualifierSet(Qualifier.ORDER_DESCENDING.toString()) )
            sb.append("&amp;").append(Constants.PARAM_ORDER_DIR).append("=").append(Constants.ORDER_DIR_DESC);
        else if ( found.isQualifierSet(Qualifier.ORDER_ASCENDING.toString()) )
            sb.append("&amp;").append(Constants.PARAM_ORDER_DIR).append("=").append(Constants.ORDER_DIR_ASC);

        if (uid > 0)
            sb.append("&amp;").append(PARAM_USER_SHORT).append("=").append(uid);

        if (filter != null)
            sb.append("&amp;").append(PARAM_FILTER).append("=").append(filter);

        if (! parents.isEmpty())
            env.put(Constants.VAR_PARENTS, parents);

        env.put(VAR_URL_BEFORE_FROM, "/History?type="+type+"&amp;from=");
        env.put(VAR_URL_AFTER_FROM, sb.toString());
        env.put(VAR_FOUND,found);
        env.put(VAR_TYPE, type);
        return FMTemplateSelector.select("History",type,env,request);
    }

    /**
     * Gets qualifiers, which user might overwrote in params.
     * @param params Map of parameters.
     * @param sortBy Optional sortBy Qualifier.
     * @param sortDir Optional sort direction Qualifier.
     * @param fromRow Optional first row of data to be fetched.
     * @param rowCount 0 means do not set LimiQualifier. Otherwise it sets size of page to be fetched.
     * @return Qualifiers.
     */
    public static Qualifier[] getQualifiers(Map params, Qualifier sortBy, Qualifier sortDir, int fromRow, int rowCount) {
        String sBy = (String) params.get(Constants.PARAM_ORDER_BY);
        if (sBy != null && sortBy != null) {
            if (Constants.ORDER_BY_CREATED.equals(sBy))
                sortBy = Qualifier.SORT_BY_CREATED;
            else if (Constants.ORDER_BY_UPDATED.equals(sBy))
                sortBy = Qualifier.SORT_BY_UPDATED;
            else if (Constants.ORDER_BY_WHEN.equals(sBy))
                sortBy = Qualifier.SORT_BY_WHEN;
            else if (Constants.ORDER_BY_ID.equals(sBy))
                sortBy = Qualifier.SORT_BY_ID;
            else if (Constants.ORDER_BY_TITLE.equals(sBy))
                sortBy = Qualifier.SORT_BY_TITLE;
        }

        String sDir = (String) params.get(Constants.PARAM_ORDER_DIR);
        if (sDir != null) {
            if ( Constants.ORDER_DIR_ASC.equals(sDir) )
                sortDir = Qualifier.ORDER_ASCENDING;
            else if ( Constants.ORDER_DIR_DESC.equals(sDir) )
                sortDir = Qualifier.ORDER_DESCENDING;
        }

        LimitQualifier limit = null;
        if (rowCount > 0)
            limit = new LimitQualifier(fromRow,rowCount);

        List qualifiers = new ArrayList(3);
        if (sortBy != null)
            qualifiers.add(sortBy);
        if (sortDir != null)
            qualifiers.add(sortDir);
        if (limit != null)
            qualifiers.add(limit);

        return (Qualifier[]) qualifiers.toArray(Qualifier.ARRAY_TYPE);
    }
}
