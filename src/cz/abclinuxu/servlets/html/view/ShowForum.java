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
import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.exceptions.MissingArgumentException;

import cz.abclinuxu.persistence.extra.tags.TagExpression;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Used to display content of the selected discussion forum.
 * Attention: each forum must be set in MySQLPersistance as forbidden
 * to load children and its type must be set to SECTION_FORUM!
 * select potomek from relace where predchozi in (49655,49490,49488,49489);
 */
public class ShowForum implements AbcAction {
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";
    public static final String PARAM_TAGS = "tags";

    /** holds forum to be displayed */
    public static final String VAR_CATEGORY = "CATEGORY";
    /** holds list of discussions */
    public static final String VAR_DISCUSSIONS = "DIZS";
    public static final String VAR_CHILDREN_MAP = "CHILDREN";

    static Persistence persistence = PersistenceFactory.getPersistence();

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Parametr rid je prázdný!");
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(Constants.VAR_CANONICAL_URL, UrlUtils.getCanonicalUrl(relation, env));


        env.put(VAR_CATEGORY, relation.getChild());
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        if (relation.getId() == Constants.REL_FORUM)
            return processMain(request, env);
        else
            return processSection(request, relation, env);
    }

    public static String processMain(HttpServletRequest request, Map env) throws Exception {
        
        env.put(Constants.VAR_RSS, FeedGenerator.getForumFeedUrl());
        return FMTemplateSelector.select("ShowForum", "main", env, request);
    }

    public static String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(40, 100, env, "/data/settings/forum_size");
        
        List<String> tags = new ArrayList<String>();
        String expr = (String) params.get(PARAM_TAGS);
        TagExpression tExpr = null;
        
        if (!Misc.empty(expr)) {
            try {
                tExpr = TagExpression.parseString(expr, tags);
            } catch (InvalidInputException e) {
                ServletUtils.addError(PARAM_TAGS, e.getMessage(), env, null);
                tags.clear();
            }
        }

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List discussions;
        discussions = sqlTool.findDiscussionRelationsWithTags(tExpr, tags, relation.getId(), qualifiers);
        Tools.syncList(discussions);

        int total = sqlTool.countDiscussionRelationsWithTags(tExpr, tags, relation.getId());

        Paging paging = new Paging(discussions, from, count, total);
        env.put(VAR_DISCUSSIONS,paging);

        env.put(Constants.VAR_RSS, FeedGenerator.getForumFeedUrl(relation.getId()));
        return FMTemplateSelector.select("ShowForum","show",env,request);
    }
}
