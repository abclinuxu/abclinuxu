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
import cz.abclinuxu.data.view.Link;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
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

    private Pattern reTagId = Pattern.compile(UrlUtils.PREFIX_TAGS + "/" + "([^/?]+)");

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String url = (String) env.get(Constants.VAR_REQUEST_URI);

        List parents = new ArrayList();
        parents.add(new Link("Štítky", UrlUtils.PREFIX_TAGS, null));
        env.put(ShowObject.VAR_PARENTS, parents);

        Matcher matcher = reTagId.matcher(url);
        if (matcher.find()) {
            String id = matcher.group(1);
            Tag tag = TagTool.getById(id);
            env.put(VAR_TAG, tag);
            parents.add(new Link(tag.getTitle(), UrlUtils.PREFIX_TAGS + "/" + tag.getId(), null));
            return FMTemplateSelector.select("Tags", "detail", env, request);
        } else {
            int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
            int count = Misc.getPageSize(30, 500, env, null);

            TagTool.ListOrder orderBy = TagTool.ListOrder.BY_TITLE;
            String sBy = (String) params.get(Constants.PARAM_ORDER_BY);
            if (sBy != null) {
                if (Constants.ORDER_BY_CREATED.equals(sBy))
                    orderBy = TagTool.ListOrder.BY_CREATION;
                else if (Constants.ORDER_BY_COUNT.equals(sBy))
                    orderBy = TagTool.ListOrder.BY_USAGE;
            }

            boolean ascendingOrder = true;
            String sDir = (String) params.get(Constants.PARAM_ORDER_DIR);
            if (sDir != null && Constants.ORDER_DIR_DESC.equals(sDir))
                ascendingOrder = false;

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
}
