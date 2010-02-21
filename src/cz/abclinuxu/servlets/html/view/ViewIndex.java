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
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.BlogStory;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.scheduler.VariableFetcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.dom4j.Node;

/**
 * This servlet renders index page of AbcLinuxu.
 */
public class ViewIndex implements AbcAction {
    private static final String VAR_COMPLETE_ARTICLES = "COMPLETE_ARTICLES";
    private static final String VAR_ARTICLES = "ARTICLES";
    private static final String VAR_STORIES = "STORIES";

    /**
     * Evaluate the request.
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        int count = -1;
        User user = (User) env.get(Constants.VAR_USER);
        if (user != null) {
            Node node = user.getData().selectSingleNode("/data/settings/index_complete_articles");
            if (node != null)
                count = Misc.parseInt(node.getText(), -1);
        }
        if (count == -1)
            count = AbcConfig.getIndexCompleteArticles();
        env.put(VAR_COMPLETE_ARTICLES, count);

        VariableFetcher variables = VariableFetcher.getInstance();
        List<Relation> articles = variables.getFreshArticles(user);
        env.put(VAR_ARTICLES, articles);
        env.put(Constants.VAR_READ_COUNTERS, Tools.getRelationCountersValue(articles, Constants.COUNTER_READ));

        List<Relation> stories = variables.getFreshDigestStories(user);
        List<BlogStory> blogStories = new ArrayList<BlogStory>(stories.size());
        for (Relation relation : stories) {
            BlogStory blogStory = Tools.analyzeBlogStory(relation, false, false);
            blogStories.add(blogStory);
        }
        env.put(VAR_STORIES, blogStories);
        env.put(Constants.VAR_CANONICAL_URL, UrlUtils.getCanonicalUrl("/"));

        return FMTemplateSelector.select("ViewIndex","show",env, request);
    }
}
