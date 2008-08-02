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
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
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
	
	public static final String ACTION_MEMBERS = "members";

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
        }
		
		return FMTemplateSelector.select("ViewSubportal", "view", env, request);
	}
	
}
