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
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class ViewVideo implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_FROM = "from";
    
    public static final String VAR_ITEMS = "ITEMS";
    public static final String VAR_ITEM = "ITEM";
    
    public static final String ACTION_USERS = "users";
    
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        relation = (Relation) persistence.findById(relation);
        env.put(ShowObject.VAR_RELATION,relation);
        
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);
        
        if (relation.getId() == Constants.REL_VIDEOS)
            return processSection(request, response, relation, env);
        else
            return processItem(request, relation, env);
    }
    
    public static String processSection(HttpServletRequest request, HttpServletResponse response, Relation relation, Map env) {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getDefaultPageSize(env);

        List qualifiers = new ArrayList();
        Qualifier[] qa;
        
        qualifiers.add(new CompareCondition(Field.UPPER, Operation.EQUAL, relation.getId()));
        
        qa = new Qualifier[qualifiers.size()];
        int total = sqlTool.countItemRelationsWithType(Item.VIDEO, (Qualifier[]) qualifiers.toArray(qa));
        
        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, count));
        qa = new Qualifier[qualifiers.size()];

        List videos = sqlTool.findItemRelationsWithType(Item.VIDEO, (Qualifier[]) qualifiers.toArray(qa));
        Tools.syncList(videos);

        Paging paging = new Paging(videos, from, count, total);
        env.put(VAR_ITEMS, paging);
        
        return FMTemplateSelector.select("ViewVideo", "list", env, request);
    }
    
    public static String processItem(HttpServletRequest request, Relation relation, Map env) {
        Item item = (Item) relation.getChild();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        
        env.put(VAR_ITEM, item);
        
        if (ACTION_USERS.equals(action)) {
            return FMTemplateSelector.select("ViewVideo", "users", env, request);
        } else {
            User user = (User) env.get(Constants.VAR_USER);
            if (user == null || user.getId() != item.getOwner())
                ReadRecorder.log(item, Constants.COUNTER_READ, env);

            return FMTemplateSelector.select("ViewVideo", "item", env, request);
        }
    }
}
