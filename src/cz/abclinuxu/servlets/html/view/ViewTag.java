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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.exceptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Displays tags.
 * @author literakl
 * @since 5.1.2008
 */
public class ViewTag implements AbcAction {
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    /** Starting part of URL, until value of from parameter */
    public static final String VAR_URL_BEFORE_FROM = "URL_BEFORE_FROM";
    /** Final part of URL, after value of from parameter */
    public static final String VAR_URL_AFTER_FROM = "URL_AFTER_FROM";
    public static final String VAR_TAG = "TAG";
    public static final String VAR_TAGS = "TAGS";
    public static final String VAR_DOCUMENTS = "DOCUMENTS";

    private Pattern reTagId = Pattern.compile(UrlUtils.PREFIX_TAGS + "/" + "([^/?]+)");
    static final Qualifier[] QUALIFIERS_ARRAY = new Qualifier[]{};

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String url = (String) env.get(Constants.VAR_REQUEST_URI);

        List parents = new ArrayList();
        parents.add(new Link("Štítky", UrlUtils.PREFIX_TAGS, null));
        env.put(ShowObject.VAR_PARENTS, parents);

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(50, 150, env, null);
        boolean ascendingOrder = true;
        String sDir = (String) params.get(Constants.PARAM_ORDER_DIR);
        if (sDir != null && Constants.ORDER_DIR_DESC.equals(sDir))
            ascendingOrder = false;

        Matcher matcher = reTagId.matcher(url);
        if (matcher.find()) { // tag detail
            String id = matcher.group(1);
            Tag tag = TagTool.getById(id);
            if (tag == null)
                throw new NotFoundException("Štítek '" + id + "' nebyl nalezen");

            env.put(VAR_TAG, tag);
            parents.add(new Link(tag.getTitle(), UrlUtils.PREFIX_TAGS + "/" + tag.getId(), null));

            int total = tag.getUsage();
            List<Relation> relations;
            if (total != 0) {
                Qualifier[] qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
                relations = sqlTool.findRelationsWithTag(id, qualifiers);
            } else
                relations = Collections.emptyList();
            Paging found = new Paging(relations, from, count, total);
            env.put(VAR_DOCUMENTS, found);

            StringBuffer sb = new StringBuffer("&amp;count=").append(found.getPageSize());
            if (found.isQualifierSet(Qualifier.SORT_BY_CREATED.toString()))
                sb.append("&amp;").append(Constants.PARAM_ORDER_BY).append("=").append(Constants.ORDER_BY_CREATED);
            else if (found.isQualifierSet(Qualifier.SORT_BY_UPDATED.toString()))
                sb.append("&amp;").append(Constants.PARAM_ORDER_BY).append("=").append(Constants.ORDER_BY_UPDATED);
            else if (found.isQualifierSet(Qualifier.SORT_BY_TITLE.toString()))
                sb.append("&amp;").append(Constants.PARAM_ORDER_BY).append("=").append(Constants.ORDER_BY_TITLE);

            if (found.isQualifierSet(Qualifier.ORDER_DESCENDING.toString()))
                sb.append("&amp;").append(Constants.PARAM_ORDER_DIR).append("=").append(Constants.ORDER_DIR_DESC);
            else if (found.isQualifierSet(Qualifier.ORDER_ASCENDING.toString()))
                sb.append("&amp;").append(Constants.PARAM_ORDER_DIR).append("=").append(Constants.ORDER_DIR_ASC);

            env.put(VAR_URL_BEFORE_FROM, "/stitky/" + tag.getId() + "?from=");
            env.put(VAR_URL_AFTER_FROM, sb.toString());

            return FMTemplateSelector.select("Tags", "detail", env, request);
        } else { // list of tags
            TagTool.ListOrder orderBy = TagTool.ListOrder.BY_TITLE;
            String sBy = (String) params.get(Constants.PARAM_ORDER_BY);
            if (sBy != null) {
                if (Constants.ORDER_BY_CREATED.equals(sBy))
                    orderBy = TagTool.ListOrder.BY_CREATION;
                else if (Constants.ORDER_BY_COUNT.equals(sBy))
                    orderBy = TagTool.ListOrder.BY_USAGE;
            }

            List<Tag> tags = TagTool.list(from, count, orderBy, ascendingOrder);
            Paging paging = new Paging(tags, from, count, TagTool.getTagsCount());
            env.put(VAR_TAGS, paging);

            StringBuffer sb = new StringBuffer("&amp;count=");
            sb.append(paging.getPageSize());
            if (TagTool.ListOrder.BY_TITLE.equals(orderBy))
                sb.append("&amp;" + Constants.PARAM_ORDER_BY + "=" + Constants.ORDER_BY_TITLE);
            else if (TagTool.ListOrder.BY_CREATION.equals(orderBy))
                sb.append("&amp;" + Constants.PARAM_ORDER_BY + "=" + Constants.ORDER_BY_CREATED);
            else if (TagTool.ListOrder.BY_USAGE.equals(orderBy))
                sb.append("&amp;" + Constants.PARAM_ORDER_BY + "=" + Constants.ORDER_BY_COUNT);

            if (ascendingOrder)
                sb.append("&amp;" + Constants.PARAM_ORDER_DIR + "=" + Constants.ORDER_DIR_ASC);
            else
                sb.append("&amp;" + Constants.PARAM_ORDER_DIR + "=" + Constants.ORDER_DIR_DESC);

            env.put(VAR_URL_BEFORE_FROM, "/stitky?from=");
            env.put(VAR_URL_AFTER_FROM, sb.toString());

            return FMTemplateSelector.select("Tags", "list", env, request);
        }
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
            else if (Constants.ORDER_BY_TITLE.equals(sBy))
                sortBy = Qualifier.SORT_BY_TITLE;
        }

        String sDir = (String) params.get(Constants.PARAM_ORDER_DIR);
        if (sDir != null) {
            if (Constants.ORDER_DIR_ASC.equals(sDir))
                sortDir = Qualifier.ORDER_ASCENDING;
            else if (Constants.ORDER_DIR_DESC.equals(sDir))
                sortDir = Qualifier.ORDER_DESCENDING;
        }

        LimitQualifier limit = null;
        if (rowCount > 0)
            limit = new LimitQualifier(fromRow, rowCount);

        List qualifiers = new ArrayList(3);
        if (sortBy != null)
            qualifiers.add(sortBy);
        if (sortDir != null)
            qualifiers.add(sortDir);
        if (limit != null)
            qualifiers.add(limit);

        return (Qualifier[]) qualifiers.toArray(QUALIFIERS_ARRAY);
    }
}
