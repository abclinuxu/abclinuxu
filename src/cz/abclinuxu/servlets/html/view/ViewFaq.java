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
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.SectionTreeCache;
import cz.abclinuxu.data.view.SectionNode;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.scheduler.VariableFetcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * User: literakl
 * Date: 17.7.2005
 */
public class ViewFaq implements AbcAction {
    public static final String PARAM_RELATION_ID = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_QUESTIONS = "QUESTIONS";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_SECTION_SIZES = "SIZES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID, Relation.class, params, request);
        if (relation==null)
            throw new NotFoundException("Stránka nebyla nalezena.");
        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);

        if (relation.getChild() instanceof Category) {
            if (relation.getId()==Constants.REL_FAQ)
                return processStart(request, env);
            else
                return processSection(request, relation, env);
        } else
            return processQuestion(request, relation, env);
    }

    /**
     * Processes start page with list of all FAQ sections.
     */
    private String processStart(HttpServletRequest request, Map env) throws Exception {
        // todo use VariableFetcher cache
//        Map sizes = sqlTool.getFaqSectionsSize();
//        env.put(VAR_SECTION_SIZES, sizes);
        return FMTemplateSelector.select("ViewFaq", "start", env, request);
    }

    /**
     * Processes section with FAQ.
     */
    public static String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = getPageSize(params);

        List qualifiers = new ArrayList();
        CompareCondition conditionUpper = new CompareCondition(Field.UPPER, Operation.EQUAL, relation.getId());
        qualifiers.add(conditionUpper);
        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, count));
        Qualifier[] qa = new Qualifier[qualifiers.size()];

        List questions = sqlTool.findItemRelationsWithType(Item.FAQ, (Qualifier[]) qualifiers.toArray(qa));
        Tools.syncList(questions);

        SectionTreeCache faqTree = VariableFetcher.getInstance().getFaqTree();
        SectionNode sectionNode = faqTree.getByRelation(relation.getId());
        int total = -1;
        if (sectionNode != null)
            total = sectionNode.getSize();
        if (total == -1) {
            qa = new Qualifier[] {conditionUpper};
            total = sqlTool.countItemRelationsWithType(Item.FAQ, (Qualifier[]) qualifiers.toArray(qa));
        }

        Paging paging = new Paging(questions, from, count, total);
        env.put(VAR_QUESTIONS, paging);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        env.put(Constants.VAR_RSS, FeedGenerator.getFaqFeedUrl());
        return FMTemplateSelector.select("ViewFaq", "list", env, request);
    }

    /**
     * Processes one frequently asked question.
     * todo zobrazit odkazy na nekolik dalsich otazek
     */
    public static String processQuestion(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        Item item = (Item) relation.getChild();
        env.put(VAR_ITEM, item);

        int revision = Misc.parseInt((String) params.get(ShowRevisions.PARAM_REVISION), -1);
        if (revision != -1)
            Misc.loadRelationRevision(item, relation.getId(), revision);

        env.put(Constants.VAR_RSS, FeedGenerator.getFaqFeedUrl());
        return FMTemplateSelector.select("ViewFaq", "view", env, request);
    }

    /**
     * Gets page size for found questions. Parameters take precendence over user settings.
     * @return page size for found documents.
     */
    private static int getPageSize(Map params) {
        int count = -1;
        String str = (String) params.get(PARAM_COUNT);
        if (str != null && str.length() > 0)
            count = Misc.parseInt(str, -1);
        if (count == -1)
            return AbcConfig.getFaqSectionCount();
        else
            return Misc.limit(count, 5, 50);
    }
}
