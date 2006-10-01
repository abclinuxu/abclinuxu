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
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Item;

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
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";
    /** specifies attribute, by which data shall be sorted */
    public static final String PARAM_ORDER_BY = "orderBy";
    /** specifies direction of sort order */
    public static final String PARAM_ORDER_DIR = "orderDir";
    /** specifies, which user created the data */
    public static final String PARAM_USER_SHORT = ViewUser.PARAM_USER_SHORT;

    public static final String VALUE_ORDER_BY_CREATED = "create";
    public static final String VALUE_ORDER_BY_UPDATED = "update";
    public static final String VALUE_ORDER_BY_ID = "id";

    public static final String VALUE_ORDER_DIR_ASC = "asc";
    public static final String VALUE_ORDER_DIR_DESC = "desc";

    public static final String VALUE_TYPE_ARTICLES = "articles";
    public static final String VALUE_TYPE_NEWS = "news";
    public static final String VALUE_TYPE_HARDWARE = "hardware";
    public static final String VALUE_TYPE_SOFTWARE = "software";
    public static final String VALUE_TYPE_DISCUSSION = "discussions";
    public static final String VALUE_TYPE_QUESTIONS = "questions";
    public static final String VALUE_TYPE_COMMENTS = "comments";
    public static final String VALUE_TYPE_DICTIONARY = "dictionary";
    public static final String VALUE_TYPE_FAQ = "faq";

    static final Qualifier[] QUALIFIERS_ARRAY = new Qualifier[]{};

    /** list of found relations, that match the conditions */
    public static final String VAR_FOUND = "FOUND";
    /** normalized type */
    public static final String VAR_TYPE = "TYPE";
    /** Starting part of URL, until value of from parameter */
    public static final String VAR_URL_BEFORE_FROM = "URL_BEFORE_FROM";
    /** Final part of URL, after value of from parameter */
    public static final String VAR_URL_AFTER_FROM = "URL_AFTER_FROM";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        String type = (String) params.get(PARAM_TYPE);
        int from = Misc.parseInt((String)params.get(PARAM_FROM),0);
        int count = Misc.parseInt((String)params.get(PARAM_COUNT),20);
        count = Misc.limit(count, 1, 50);
        int uid = Misc.parseInt((String)params.get(PARAM_USER_SHORT),0);

        List data = null;
        int total = 0;
        Paging found = null;
        Qualifier[] qualifiers;

        if ( VALUE_TYPE_ARTICLES.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            if (uid > 0) {
                total = sqlTool.countArticleRelationsByUser(uid);
                data = sqlTool.findArticleRelationsByUser(uid, qualifiers);
            } else {
                total = sqlTool.countArticleRelations(0);
                data = sqlTool.findArticleRelations(qualifiers, 0);
            }
            found = new Paging(data,from,count,total,qualifiers);
            type = VALUE_TYPE_ARTICLES;

        } else if ( VALUE_TYPE_NEWS.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            if ( uid > 0 ) {
                total = sqlTool.countNewsRelationsByUser(uid);
                data = sqlTool.findNewsRelationsByUser(uid, qualifiers);
            } else {
                total = sqlTool.countNewsRelations();
                data = sqlTool.findNewsRelations(qualifiers);
            }
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_NEWS;

        } else if ( VALUE_TYPE_FAQ.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countItemRelationsWithType(Item.FAQ, null);
            data = sqlTool.findItemRelationsWithType(Item.FAQ, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_FAQ;

        } else if ( VALUE_TYPE_HARDWARE.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countItemRelationsWithType(Item.HARDWARE, new Qualifier[]{});
            data = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_HARDWARE;

        } else if ( VALUE_TYPE_SOFTWARE.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            if (uid > 0) {
                CompareCondition userEqualsCondition = new CompareCondition(Field.OWNER, Operation.EQUAL, new Integer(uid));
                total = sqlTool.countItemRelationsWithType(Item.SOFTWARE, new Qualifier[]{userEqualsCondition});
                Qualifier[] tmp = new Qualifier[qualifiers.length + 1];
                tmp[0] = userEqualsCondition;
                System.arraycopy(qualifiers, 0, tmp, 1, qualifiers.length);
                qualifiers = tmp;
            } else
                total = sqlTool.countItemRelationsWithType(Item.SOFTWARE, new Qualifier[]{});
            data = sqlTool.findItemRelationsWithType(Item.SOFTWARE, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_SOFTWARE;

        } else if ( VALUE_TYPE_DISCUSSION.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countDiscussionRelations();
            data = sqlTool.findDiscussionRelations(qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_DISCUSSION;

        } else if ( VALUE_TYPE_QUESTIONS.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countQuestionRelationsByUser(uid);
            data = sqlTool.findQuestionRelationsByUser(uid, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_QUESTIONS;

        } else if ( VALUE_TYPE_COMMENTS.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, null, Qualifier.ORDER_DESCENDING, from, count);
            total = sqlTool.countCommentRelationsByUser(uid);
            data = sqlTool.findCommentRelationsByUser(uid, qualifiers);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_COMMENTS;

        } else if ( VALUE_TYPE_DICTIONARY.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            if ( uid > 0 ) {
                total = sqlTool.countRecordParentRelationsByUserAndType(uid, Record.DICTIONARY);
                data = sqlTool.findRecordParentRelationsByUserAndType(uid, Record.DICTIONARY, qualifiers);
            } else {
                total = sqlTool.countRecordParentRelationsWithType(Record.DICTIONARY);
                data = sqlTool.findRecordParentRelationsWithType(Record.DICTIONARY, qualifiers);
            }
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_DICTIONARY;

        } else
            return ServletUtils.showErrorPage("Chybí parametr type!",env,request);

        Tools.syncList(found.getData());

        StringBuffer sb = new StringBuffer("&amp;count=");
        sb.append(found.getPageSize());
        if (found.isQualifierSet(Qualifier.SORT_BY_CREATED.toString()))
            sb.append("&amp;orderBy=create");
        else if (found.isQualifierSet(Qualifier.SORT_BY_UPDATED.toString()))
            sb.append("&amp;orderBy=update");
        else if (found.isQualifierSet(Qualifier.SORT_BY_ID.toString()))
            sb.append("&amp;orderBy=id");
        if ( found.isQualifierSet(Qualifier.ORDER_DESCENDING.toString()) )
            sb.append("&amp;orderDir=desc");
        else if ( found.isQualifierSet(Qualifier.ORDER_ASCENDING.toString()) )
            sb.append("&amp;orderDir=asc");
        if (uid>0) {
            sb.append("&amp;uid=");
            sb.append(uid);
        }

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
        String sBy = (String) params.get(PARAM_ORDER_BY);
        if (sBy!=null && sortBy != null) {
            if (VALUE_ORDER_BY_CREATED.equals(sBy))
                sortBy = Qualifier.SORT_BY_CREATED;
            else if (VALUE_ORDER_BY_UPDATED.equals(sBy))
                sortBy = Qualifier.SORT_BY_UPDATED;
            else if (VALUE_ORDER_BY_ID.equals(sBy))
                sortBy = Qualifier.SORT_BY_ID;
        }

        String sDir = (String) params.get(PARAM_ORDER_DIR);
        if (sDir!=null) {
            if ( VALUE_ORDER_DIR_ASC.equals(sDir) )
                sortDir = Qualifier.ORDER_ASCENDING;
            else if ( VALUE_ORDER_DIR_DESC.equals(sDir) )
                sortDir = Qualifier.ORDER_DESCENDING;
        }

        LimitQualifier limit = null;
        if (rowCount>0)
            limit = new LimitQualifier(fromRow,rowCount);

        List qualifiers = new ArrayList(3);
        if (sortBy!=null)
            qualifiers.add(sortBy);
        if (sortDir!=null)
            qualifiers.add(sortDir);
        if (limit!=null)
            qualifiers.add(limit);

        return (Qualifier[]) qualifiers.toArray(QUALIFIERS_ARRAY);
    }
}
