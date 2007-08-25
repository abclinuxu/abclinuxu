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
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.feeds.FeedGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Default page for news.
 * User: literakl
 * Date: 17.4.2005
 */
public class ShowNewsPage implements AbcAction {
    public static final String VAR_CATEGORIES = "CATEGORIES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 10)};
        List news = SQLTool.getInstance().findNewsRelations(qualifiers);
        Tools.syncList(news);
        env.put("NEWS", news);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        env.put(VAR_CATEGORIES, Search.getNewsCategories(params));
        env.put(Constants.VAR_RSS, FeedGenerator.getNewsFeedUrl());

        return FMTemplateSelector.select("ViewCategory", "news", env, request);
    }
}
