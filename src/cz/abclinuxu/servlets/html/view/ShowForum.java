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
import cz.abclinuxu.data.view.SectionTreeCache;
import cz.abclinuxu.data.view.SectionNode;
import cz.abclinuxu.data.view.Forum;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.scheduler.VariableFetcher;

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

    /** holds forum to be displayed */
    public static final String VAR_CATEGORY = "CATEGORY";
    /** holds list of discussions */
    public static final String VAR_DISCUSSIONS = "DIZS";
    public static final String VAR_FORUM_APPLICATIONS = "APPS";
    public static final String VAR_FORUM_DISTRIBUTIONS = "DISTROS";
    public static final String VAR_FORUM_HARDWARE = "HARDWARE";
    public static final String VAR_FORUM_SETTINGS = "SETTINGS";
    public static final String VAR_FORUM_VARIOUS = "VARIOUS";

    static Persistence persistence = PersistenceFactory.getPersistance();

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr rid je prázdný!");
        }

        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(VAR_CATEGORY, relation.getChild());
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        if (relation.getId() == Constants.REL_FORUM)
            return processMain(request, env);
        else
            return processSection(request, relation, env);
    }

    public static String processMain(HttpServletRequest request, Map env) throws Exception {
        SectionTreeCache forumTree = VariableFetcher.getInstance().getForumTree();
        SectionNode sectionApps = forumTree.getByRelation(Constants.REL_FORUM_APPLICATIONS);
        SectionNode sectionDistros = forumTree.getByRelation(Constants.REL_FORUM_DISTRIBUTIONS);
        SectionNode sectionHardware = forumTree.getByRelation(Constants.REL_FORUM_HARDWARE);
        SectionNode sectionSettings = forumTree.getByRelation(Constants.REL_FORUM_SETTINGS);
        SectionNode sectionVarious = forumTree.getByRelation(Constants.REL_FORUM_VARIOUS);

        Map<Integer,Relation> lastQuestions = new HashMap();
        List<Forum> forumApps = initForum(sectionApps, lastQuestions);
        List<Forum> forumDistros = initForum(sectionDistros, lastQuestions);
        List<Forum> forumHardware = initForum(sectionHardware, lastQuestions);
        List<Forum> forumSettings = initForum(sectionSettings, lastQuestions);
        List<Forum> forumVarious = initForum(sectionVarious, lastQuestions);

        Tools.syncList(lastQuestions.values());
        setLastQuestion(forumApps, lastQuestions);
        setLastQuestion(forumDistros, lastQuestions);
        setLastQuestion(forumHardware, lastQuestions);
        setLastQuestion(forumSettings, lastQuestions);
        setLastQuestion(forumVarious, lastQuestions);

        env.put(VAR_FORUM_APPLICATIONS, forumApps);
        env.put(VAR_FORUM_DISTRIBUTIONS, forumDistros);
        env.put(VAR_FORUM_HARDWARE, forumHardware);
        env.put(VAR_FORUM_SETTINGS, forumSettings);
        env.put(VAR_FORUM_VARIOUS, forumVarious);
        return FMTemplateSelector.select("ShowForum", "main", env, request);
    }

    private static List initForum(SectionNode section, Map<Integer,Relation> lastQuestions) {
        List<Forum> list = new ArrayList(section.getSize());
        for (SectionNode node : section.getChildren()) {
            int lastId = node.getLastItem();
            if (lastId > 0) {
                Relation last = new Relation(lastId);
                lastQuestions.put(lastId, last);
            }
            list.add(new Forum(node));
        }

        return list;
    }

    private static void setLastQuestion(List<Forum> forums, Map<Integer,Relation> lastQuestions) {
        for (Forum forum : forums) {
            Relation last = lastQuestions.get(forum.getSection().getLastItem());
            if (last == null)
                continue;
            DiscussionHeader header = Tools.analyzeDiscussion(last);
            forum.setDiscussion(header);
        }
    }

    public static String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(40, 100, env, "/data/settings/forum_size");

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List discussions = sqlTool.findDiscussionRelationsWithParent(relation.getId(),qualifiers);
        Tools.syncList(discussions);

        SectionTreeCache forumTree = VariableFetcher.getInstance().getForumTree();
        SectionNode sectionNode = forumTree.getByRelation(relation.getId());
        int total = -1;
        if (sectionNode != null)
            total = sectionNode.getSize();
        if (total == -1)
            total = sqlTool.countDiscussionRelationsWithParent(relation.getId());

        Paging paging = new Paging(discussions, from, count, total);
        env.put(VAR_DISCUSSIONS,paging);

        return FMTemplateSelector.select("ShowForum","show",env,request);
    }
}
