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
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * User: literakl
 * Date: 3.7.2005
 */
public class ViewPolls implements AbcAction {
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many objects to display */
    public static final String PARAM_COUNT = "count";
    public static final String VAR_POLL = "POLL";
    public static final String VAR_POLLS = "POLLS";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr rid je prázdný!");

        Tools.sync(relation);
        if (relation.getId()==Constants.REL_POLLS)
            return processPolls(env, request);
        else
            return processPoll(env, relation, request);
    }

    /**
     * Displays selected poll.
     */
    public static String processPoll(Map env, Relation relation, HttpServletRequest request) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Poll poll = (Poll) persistance.findById(relation.getChild());
        env.put(VAR_POLL, poll);

        Map children = Tools.groupByType(poll.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP, children);
        return FMTemplateSelector.select("ShowObject", "poll", env, request);
    }

    /**
     * Displays all polls.
     */
    public static String processPolls(Map env, HttpServletRequest request) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.parseInt((String) params.get(PARAM_COUNT), 15);
        count = Misc.limit(count, 1, 50);

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List polls = sqlTool.findStandalonePollRelations(qualifiers);
        int total = sqlTool.countStandalonePollRelations();
        Tools.syncList(polls);

        Paging paging = new Paging(polls, from, count, total);
        env.put(VAR_POLLS, paging);
        return FMTemplateSelector.select("ViewCategory", "ankety", env, request);
    }
}
