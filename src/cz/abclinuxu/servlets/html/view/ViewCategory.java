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

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.SectionTreeCache;
import cz.abclinuxu.data.view.SectionNode;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.data.view.DriverCategories;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.html.edit.EditRequest;
import cz.abclinuxu.servlets.html.edit.EditDiscussion;
import cz.abclinuxu.servlets.html.edit.EditDriver;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.scheduler.VariableFetcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

/**
 * Servlet, which loads Category specified by parameter <code>categoryId</code> (or
 * by relation.getChild() from Context) and displays the result.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
 * <dl>
 * <dt><code>VAR_CATEGORY</code></dt>
 * <dd>instance of Category.</dd>
 * </dl>
 * <u>Parameters used by ShowObject</u>
 * <dl>
 * <dt>PARAM_CATEGORY_ID</dt>
 * <dd>PK of asked Category, number.</dd>
 * <dt>PARAM_FROM</dt>
 * <dd>used by clanky.vm. Defines range of shown objects.</dd>
 * </dl>
 */
public class ViewCategory implements AbcAction {
    /** if set, it indicates to display parent in the relation of two categories */
    public static final String PARAM_PARENT = "parent";
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    /** n-th oldest object, where to display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";
    /** plain category will be displayed, not high-level representation */
    public static final String PARAM_RAW = "raw";

    /** holds category to be displayed */
    public static final String VAR_CATEGORY = "CATEGORY";
    public static final String VAR_CHILDREN = "CHILDREN";
	public static final String VAR_GROUP = "GROUP";
    /** holds list of articles */
    public static final String VAR_ARTICLES = "ARTICLES";
    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_TREE_DEPTH = "DEPTH";

    static Persistence persistence = PersistenceFactory.getPersistence();

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        relation = (Relation) persistence.findById(relation);
        env.put(ShowObject.VAR_RELATION,relation);
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS,parents);
        env.put(ShowObject.VAR_SUBPORTAL, Tools.getParentSubportal(parents));

        return processCategory(request,response,env,relation);
    }

    /**
     * processes given category
     * @return template to be rendered
     */
    public static String processCategory(HttpServletRequest request, HttpServletResponse response, Map env, Relation relation) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        String tmp = (String) ((Map)env.get(Constants.VAR_PARAMS)).get(PARAM_PARENT);
        GenericObject obj;
        if ( Misc.same(tmp,"yes") )
            obj = relation.getParent();
        else
            obj = relation.getChild();

        Category category;
        if ( !(obj instanceof Category) ) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/show/"+relation.getId());
            return null;
        } else
            category = (Category) obj;

        Tools.sync(category);
        env.put(VAR_CATEGORY, category);
		
		if (category.getGroup() != 0) {
			Item group = new Item(category.getGroup());
			Tools.sync(group);
			env.put(VAR_GROUP, group);
		}

        List children = Tools.syncList(category.getChildren());
        env.put(VAR_CHILDREN, children);

        if (params.get(PARAM_RAW) != null)
            return FMTemplateSelector.select("ViewCategory", "sekce", env, request);

        int type = category.getType();
        switch (type) {
            case Category.SECTION:
                return processArticleSection(request, relation, env);
            case Category.HARDWARE_SECTION:
                return processHardwareSection(relation, env, request);
            case Category.FAQ:
                return ViewFaq.processSection(request, relation, env);
            case Category.FORUM:
                return ShowForum.processSection(request, relation, env);
            case Category.SOFTWARE_SECTION:
                return ViewSoftware.processSection(request, relation, env);
			case Category.SUBPORTAL:
				return ViewSubportal.processSection(request, relation, env);
            case Category.EVENT:
                return ViewEvent.processSection(request, response, relation, env);
        }

        switch ( relation.getId() ) {
            case Constants.REL_FORUM:
                return ShowForum.processMain(request, env);
            case Constants.REL_POLLS:
                return ViewPolls.processPolls(env, request);
            case Constants.REL_DRIVERS: {
                env.put(Constants.VAR_RSS, FeedGenerator.getDriversFeedUrl());
                env.put(EditDriver.VAR_CATEGORIES, DriverCategories.getAllCategories());
                return FMTemplateSelector.select("ViewCategory", "drivers", env, request);
            }
            case Constants.REL_NEWS_POOL:
                return FMTemplateSelector.select("ViewCategory", "waiting_news", env, request);
            case Constants.REL_REQUESTS: {
                EditDiscussion.detectSpambotCookie(request, env, user);
                env.put(EditRequest.VAR_CATEGORIES, EditRequest.categories);
                return FMTemplateSelector.select("EditRequest", "view", env, request);
            }
            case Constants.REL_DOCUMENTS:
                return FMTemplateSelector.select("ViewCategory", "documents", env, request);
            case Constants.REL_BAZAAR:
                return ViewBazaar.processSection(request, relation, env);
            case Constants.REL_SCREENSHOTS:
                return ViewScreenshot.processSection(request, env);
			case Constants.REL_SUBPORTALS:
				return FMTemplateSelector.select("ViewCategory", "subportals", env, request);
			case Constants.REL_EVENTS:
				return ViewEvent.processSection(request, response, relation, env);
        }

        if ( category.getId()==Constants.CAT_ARTICLES ) {
            env.put(Constants.VAR_RSS, FeedGenerator.getArticlesFeedUrl());
            return FMTemplateSelector.select("ViewCategory","rubriky",env, request);
        }

        // todo smazat? Zda se mi to duplikaci processArticleSection
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        tmp = urlUtils.getPrefix();
        if ( Misc.same(tmp,UrlUtils.PREFIX_CLANKY) ) {
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Relation relation1 = (Relation) iter.next();
                Tools.sync(relation1);
                if (!(relation1.getChild() instanceof Item)) {
                    iter.remove();
                    continue;
                }
                if (((Item)relation1.getChild()).getType()!=Item.ARTICLE)
                    iter.remove();
            }
            Paging paging = new Paging(children, 0, children.size(), children.size());
            env.put(VAR_ARTICLES, paging);
            return FMTemplateSelector.select("ViewCategory","rubrika",env, request);
        }
        return FMTemplateSelector.select("ViewCategory","sekce",env, request);
    }

    private static String processHardwareSection(Relation relation, Map env, HttpServletRequest request) {
        SectionTreeCache sectionCache = VariableFetcher.getInstance().getHardwareTree();
        List<SectionNode> categories = null;
        int depth = 0;
        if (relation.getId() == Constants.REL_HARDWARE || relation.getId() == Constants.REL_HARDWARE_386) {
            categories = sectionCache.getChildren();
            depth = 999;
        } else {
            SectionNode sectionNode = sectionCache.getByRelation(relation.getId());
            if (sectionNode != null) {
                categories = sectionNode.getChildren();
                depth = sectionNode.getDepth();
            }
        }
        env.put(VAR_CATEGORIES, categories);
        env.put(VAR_TREE_DEPTH, depth);

        env.put(Constants.VAR_RSS, FeedGenerator.getHardwareFeedUrl());
        return FMTemplateSelector.select("ViewCategory", "hwsekce", env, request);
    }

    public static String processArticleSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Category section = (Category) relation.getChild();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = AbcConfig.getSectionArticleCount();

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List<Relation> articles = sqlTool.findArticleRelations(qualifiers, section.getId());

        SectionTreeCache faqTree = VariableFetcher.getInstance().getArticleTree();
        SectionNode sectionNode = faqTree.getByRelation(relation.getId());
        int total = -1;
        if (sectionNode != null)
            total = sectionNode.getSize();
        if (total == -1)
            total = sqlTool.countArticleRelations(section.getId());

        Tools.syncList(articles);
        Tools.initializeDiscussionsTo(articles);
//        Tools.initializeAuthors(articles);

        Paging paging = new Paging(articles, from, count, total);
        env.put(VAR_ARTICLES, paging);


        String feedUrl = FeedGenerator.getSeriesFeedUrl(relation.getId());
        if (feedUrl == null)
            feedUrl = FeedGenerator.getArticlesFeedUrl();
        env.put(Constants.VAR_RSS, feedUrl);
        return FMTemplateSelector.select("ViewCategory", "rubrika", env, request);
    }

}
