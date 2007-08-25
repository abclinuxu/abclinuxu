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
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.dom4j.Element;

/**
 * This action renders list of series and selected series.
 * Date: 7.1.2007
 */
public class ViewSeries implements AbcAction {
    public static final String PARAM_RELATION = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_SERIES_LIST = "SERIES";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_ARTICLES = "ARTICLES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new NotFoundException("Stránka nebyla nalezena.");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        if (relation.getChild() instanceof Category)
            return processSection(request, relation, env);
        else
            return processSeries(request, relation, env);
    }

    private String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Category category = (Category) relation.getChild();
        List<Relation> series = category.getChildren();
        Tools.syncList(series);
        Sorters2.byName(series);

        env.put(VAR_SERIES_LIST, series);
        env.put(ShowObject.VAR_PARENTS, Collections.singletonList(relation));

        env.put(Constants.VAR_RSS, FeedGenerator.getArticlesFeedUrl());
        return FMTemplateSelector.select("ViewSeries", "main", env, request);
    }

    private String processSeries(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int pageSize = Misc.getPageSize(AbcConfig.getSeriesArticleCount(), 100, env, null);
        int count = pageSize;

        Item series = (Item) relation.getChild();
        List articlesElements = series.getData().getRootElement().elements("article");
        articlesElements = new ArrayList(articlesElements);
        Collections.reverse(articlesElements);

        int size = articlesElements.size();
        if (from > 0 || count < size) {
            if (from > size)
                from = size - 1;
            if (from + count > size)
                count = size - from;
            articlesElements = articlesElements.subList(from, from + count);
        }

        List<Relation> articles = new ArrayList<Relation>(count);
        for (Iterator iter = articlesElements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            int rid = Misc.parseInt(element.getText(), 0);
            articles.add(new Relation(rid));
        }

        Tools.syncList(articles);
        Tools.initializeDiscussionsTo(articles);

        Paging paging = new Paging(articles, from, pageSize, size);
        env.put(VAR_ARTICLES, paging);
        env.put(VAR_ITEM, series);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        String feedUrl = FeedGenerator.getSeriesFeedUrl(relation.getId());
        if (feedUrl == null)
            feedUrl = FeedGenerator.getArticlesFeedUrl();
        env.put(Constants.VAR_RSS, feedUrl);

        return FMTemplateSelector.select("ViewSeries", "series", env, request);
    }
}
