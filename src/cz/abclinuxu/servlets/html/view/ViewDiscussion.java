/*
 *  Copyright (C) 2008 Leos Literak
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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.Solution;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.SolutionTool;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class ViewDiscussion implements AbcAction {

    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_THREAD_ID = "threadId";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_VOTERS = "VOTERS";
    public static final String ACTION_SHOW_VOTERS = "showVoters";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr rid je prázdný!");
        env.put(Constants.VAR_CANONICAL_URL, UrlUtils.getCanonicalUrl(relation, env));

        if (ACTION_SHOW_VOTERS.equals(action))
            return actionShowVoters(relation, request, env);

        return processItem(relation, request, env);
    }

    public static String processItem(Relation relation, HttpServletRequest request, Map env) throws Exception {
        Item item = (Item) relation.getChild();

        Tools.sync(item);
        env.put(VAR_ITEM, item);

        if (Tools.isQuestion(relation)) {
            ReadRecorder.log(item, Constants.COUNTER_READ, env);
            env.put(Constants.VAR_RSS, FeedGenerator.getForumFeedUrl(relation.getUpper()));
            return FMTemplateSelector.select("ShowObject", "question", env, request);
        }
        else
            return FMTemplateSelector.select("ShowObject", "discussion", env, request);
    }

    private String actionShowVoters(Relation relation, HttpServletRequest request, Map env) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item diz = (Item) persistence.findById(relation.getChild());
        List<Solution> solutions = SolutionTool.get(diz);
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD_ID), 0);

        List<Integer> voters = null;

        for (Solution s : solutions) {
            if (s.getId() == threadId) {
                voters = s.getVoters();
                break;
            }
        }

        env.put(VAR_VOTERS, voters);
        env.put(ShowObject.VAR_ITEM, diz);

        return FMTemplateSelector.select("ViewDiscussion", "showVoters", env, request);
    }

}
