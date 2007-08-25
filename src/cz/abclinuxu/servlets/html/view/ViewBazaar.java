/*
 *  Copyright (C) 2006 Leos Literak
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

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * User: literakl
 * Date: 8.10.2006
 */
public class ViewBazaar implements AbcAction {
    public static final String PARAM_RELATION_ID = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_ADS = "ADS";
    public static final String VAR_ITEM = "ITEM";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID, Relation.class, params, request);
        if (relation==null)
            throw new NotFoundException("Stránka nebyla nalezena.");
        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);

        if (relation.getChild() instanceof Category) {
            return processSection(request, relation, env);
        } else
            return processAd(request, relation, env);
    }

    /**
     * Processes section with advertisements.
     */
    public static String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = AbcConfig.getBazaarPageSize();
        int total = sqlTool.countItemRelationsWithType(Item.BAZAAR, null);

        List qualifiers = new ArrayList();
        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, count));
        Qualifier[] qa = new Qualifier[qualifiers.size()];

        List ads = sqlTool.findItemRelationsWithType(Item.BAZAAR, (Qualifier[]) qualifiers.toArray(qa));
        Tools.syncList(ads);

        Paging paging = new Paging(ads, from, count, total);
        env.put(VAR_ADS, paging);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        env.put(Constants.VAR_RSS, FeedGenerator.getBazaarFeedUrl());
        return FMTemplateSelector.select("ViewBazaar", "bazaar", env, request);
    }

    /**
     * Processes section with advertisements.
     */
    public static String processAd(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Item item = (Item) relation.getChild();
        Map children = Tools.groupByType(item.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP, children);
        env.put(ShowObject.VAR_ITEM, item);
        env.put(ShowObject.VAR_RELATION, relation);

        User user = (User) env.get(Constants.VAR_USER);
        if (user == null || user.getId() != item.getOwner())
            ReadRecorder.log(item, Constants.COUNTER_READ, env);

        env.put(Constants.VAR_RSS, FeedGenerator.getBazaarFeedUrl());
        return FMTemplateSelector.select("ViewBazaar", "ad", env, request);
    }
}
