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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Profile of the user
 */
public class ViewAuthor implements AbcAction {

    public static final String PARAM_RELATION_ID = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ARTICLES = "ARTICLES";
    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_AUTHORS = "AUTHORS";
    public static final String VAR_COUNTS = "COUNTS";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID, Relation.class, params, request);
        if (relation == null)
            throw new NotFoundException("Stránka nebyla nalezena.");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        if (relation.getChild() instanceof Category)
            return processSection(request, env);
        else
            return processAuthor(request, relation, env);
    }

    /**
     * shows profile for selected author
     */
    public static String processAuthor(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item item = (Item) relation.getChild();
        env.put(VAR_AUTHOR, item);

        int from = Misc.parseInt((String)params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(AbcConfig.getAuthorArticlesPageSize(), 50, env, null);

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List<Relation> articles = sqlTool.findArticleRelationsByAuthor(relation.getId(), qualifiers);
        int total = sqlTool.countArticleRelationsByAuthor(relation.getId());
        Tools.syncList(articles);
        Tools.initializeDiscussionsTo(articles);

        Paging paging = new Paging(articles, from, count, total);
        env.put(VAR_ARTICLES, paging);

        return FMTemplateSelector.select("ViewAuthor", "author", env, request);
    }

    /**
     * Processes section with authors.
     */
    private String processSection(HttpServletRequest request, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        
        // sort authors by surname
        List<Relation> authors = sqlTool.findItemRelationsWithType(Item.AUTHOR, new Qualifier[] { Qualifier.SORT_BY_STRING2});
        Tools.syncList(authors);
        env.put(VAR_AUTHORS, authors);

        List counts = sqlTool.countArticleRelationsByAuthors();
        Map byAuthor = new HashMap(counts.size() + 1, 1.0f);
        for (Iterator iter = counts.iterator(); iter.hasNext();) {
            Object[] objects = (Object[]) iter.next();
            byAuthor.put(Misc.parseInt((String)objects[0], -1), objects[1]);
        }
        env.put(VAR_COUNTS, byAuthor);

        return FMTemplateSelector.select("ViewAuthor", "authors_list", env, request);
    }
}
