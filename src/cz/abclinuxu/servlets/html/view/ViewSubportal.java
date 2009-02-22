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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class ViewSubportal implements AbcAction {
	public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_ORDER_BY = "orderBy";
    public static final String PARAM_ORDER_DIR = "orderDir";

	public static final String ACTION_MEMBERS = "members";
    public static final String ACTION_ADMINS = "admins";

    public static final String VAR_SUBPORTALS = "SUBPORTALS";
    public static final String VAR_ADMINS = "ADMINS";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		Persistence persistence = PersistenceFactory.getPersistence();

		Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        relation = (Relation) persistence.findById(relation);
        env.put(ShowObject.VAR_RELATION,relation);

		return processSection(request, relation, env);
	}

	public static String processSection(HttpServletRequest request, Relation relation, Map env) {
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		String action = (String) params.get(PARAM_ACTION);

        Category cat = (Category) relation.getChild();
        env.put(ShowObject.VAR_ITEM, cat);

		if (ACTION_MEMBERS.equals(action)) {
            List parents = (List) env.get(ShowObject.VAR_PARENTS);
            Link link = new Link("Členové", relation.getUrl()+"?action="+ACTION_MEMBERS, null);
            parents.add(link);

			return FMTemplateSelector.select("ViewSubportal", "members", env, request);
        } else if (ACTION_ADMINS.equals(action)) {
            Item group = new Item(cat.getGroup());
            Tools.sync(group);

            SQLTool sqlTool = SQLTool.getInstance();
            List keys = sqlTool.findUsersInGroup(group.getId(), null);
            List users = new ArrayList(keys.size());
            for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
                Integer key = (Integer) iter.next();
                users.add(Tools.sync(new User(key)));
            }

            env.put(VAR_ADMINS, users);

            List parents = (List) env.get(ShowObject.VAR_PARENTS);
            Link link = new Link("Admini", relation.getUrl()+"?action="+ACTION_ADMINS, null);
            parents.add(link);

            return FMTemplateSelector.select("ViewSubportal", "admins", env, request);
        }

        return FMTemplateSelector.select("ViewSubportal", "view", env, request);
	}

    public static String processSectionList(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getDefaultPageSize(env);
        int total;
        List list;
        String orderBy = (String) params.get(PARAM_ORDER_BY);
        String orderDir = (String) params.get(PARAM_ORDER_DIR);

        if (Misc.empty(orderBy))
            orderBy = "score";
        if (Misc.empty(orderDir))
            orderDir = "desc";

        if ("updated".equals(orderBy)) {
            List<Map> portals = VariableFetcher.getInstance().getAllSubportalChanges();

            list = new ArrayList(portals.size());
            if ("asc".equals(orderDir)) {
                for (int i = portals.size()-1; i >= 0; i--)
                    list.add(portals.get(i).get("subportal"));
            } else {
                for (Map map : portals)
                    list.add(map.get("subportal"));
            }

            total = list.size();
        } else {
            List<Qualifier> qualifiers = new ArrayList<Qualifier>(3);
            Qualifier[] qa;

            if ("created".equals(orderBy))
                qualifiers.add(Qualifier.SORT_BY_CREATED);
            else if ("title".equals(orderBy))
                qualifiers.add(Qualifier.SORT_BY_TITLE);

            if ("desc".equals(orderDir))
                qualifiers.add(Qualifier.ORDER_DESCENDING);
            else
                qualifiers.add(Qualifier.ORDER_ASCENDING);

            qualifiers.add(new LimitQualifier(from, count));

            qa = new Qualifier[qualifiers.size()];
            qualifiers.toArray(qa);

            total = sqlTool.countCategoryRelationsWithType(Category.SUBPORTAL);

            if ("score".equals(orderBy))
                list = sqlTool.findSubportalsOrderedByScore(qa);
            else if ("members".equals(orderBy))
                list = sqlTool.findSubportalsOrderedByMemberCount(qa);
            else
                list = sqlTool.findCategoryRelationsWithType(Category.SUBPORTAL, qa);
            Tools.syncList(list);
        }

        Paging paging = new Paging(list, from, count, total);
        env.put(VAR_SUBPORTALS, paging);

        return FMTemplateSelector.select("ViewSubportal", "list", env, request);
    }
}
