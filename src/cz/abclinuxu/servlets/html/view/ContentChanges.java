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
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Displays all content documents under selected relation.
 * User can optionally set date, that filters out documents,
 * that were not modified since this date.
 * User: literakl
 * Date: 6.9.2005
 */
public class ContentChanges implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_SINCE = "since";

    /** list of initialized relations with child that is Content */
    public static final String VAR_DATA = "DATA";
    public static final String VAR_NOW = "NOW";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null) {
            throw new MissingArgumentException("Parametr relationId je pr�zdn�!");
        }

        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(VAR_NOW, new Date());

        String s = (String) params.get(PARAM_SINCE);
        if (s==null || s.trim().length()==0)
            s = "2000-01-01 01:01";
        Date modifiedSince = Constants.isoFormat.parse(s);
        List result = new ArrayList();

        List stack = new ArrayList();
        List children = relation.getChild().getChildren();
        Tools.syncList(children);
        stack.addAll(children);

        while (stack.size()>0) {
            Relation childRelation = (Relation) stack.remove(0);
            GenericObject obj = childRelation.getChild();
            if (! (obj instanceof Item))
                continue;
            Item content = (Item) obj;
            if (content.getType()!=Item.CONTENT)
                continue;

            children = content.getChildren();
            Tools.syncList(children);
            stack.addAll(0, children);

            if (content.getUpdated().before(modifiedSince))
                continue;
            result.add(childRelation);
        }

        env.put(VAR_DATA, result);
        return FMTemplateSelector.select("ContentChanges", "show", env, request);
    }
}
