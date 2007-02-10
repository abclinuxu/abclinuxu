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
package cz.abclinuxu.servlets.html.select;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.exceptions.PersistenceException;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.Misc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Servlet for interactive selection of relation. When user chooses the relation, flow is forwarded
 * to <code>PARAM_URL</code> with all parameters propagated to new location plus
 * <code>SelectRelation.PARAM_SELECTED</code> set.<p>
 */
public class SelectRelation implements AbcAction {
    public static final String PARAM_SELECTED = "selectedId";
    public static final String PARAM_CURRENT = "currentId";
    public static final String PARAM_ENTERED = "enteredId";
    public static final String PARAM_URL = "url";
    public static final String PARAM_FINISH = "finish";
    public static final String PARAM_CONFIRM = "confirm";

    public static final String VAR_FORUM = "FORUM";
    public static final String VAR_CLANKY = "CLANKY";
    public static final String VAR_386 = "H386";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_CURRENT = "CURRENT";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String confirm = request.getParameter(PARAM_CONFIRM);
        String finish = request.getParameter(PARAM_FINISH);

        if ( finish!=null && finish.length()>0 ) {
            return actionFinish(request,response,env);
        }

        if ( confirm!=null && confirm.length()>0 ) {
            return actionConfirm(request,env);
        }
        return actionNext(request,env);
    }

    /**
     * Called, when we shall descend to another relation
     */
    protected String actionNext(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        String manual = request.getParameter(PARAM_ENTERED);
        String tmp = request.getParameter(PARAM_CURRENT);

        if ( tmp!=null ) {
            try {
                int currentId = Integer.parseInt( (Misc.empty(manual))? tmp:manual);
                Relation current = (Relation) persistence.findById(new Relation(currentId));
                env.put(VAR_CURRENT,current);
                return FMTemplateSelector.select("SelectRelation","step1",env,request);
            } catch (NumberFormatException e) {
                ServletUtils.addError(PARAM_ENTERED,"Číslo větší než nula!",env, null);
            } catch (PersistenceException e) {
                ServletUtils.addError(Constants.ERROR_GENERIC,"Nebyla zvolena platná relace!",env, null);
            }
        }

        Category clanky = (Category) persistence.findById(new Category(Constants.CAT_ARTICLES));
        List content = clanky.getChildren();
        env.put(VAR_CLANKY,content);

        Category forum = (Category) persistence.findById(new Category(Constants.CAT_FORUM));
        content = forum.getChildren();
        env.put(VAR_FORUM,content);

        Category hw386 = (Category) persistence.findById(new Category(Constants.CAT_386));
        content = hw386.getChildren();
        env.put(VAR_386,content);

        Category sw = (Category) persistence.findById(new Category(Constants.CAT_SOFTWARE));
        content = sw.getChildren();
        env.put(VAR_SOFTWARE, content);

        return FMTemplateSelector.select("SelectRelation","step1",env,request);
    }

    /**
     * Called, when user select relation.
     */
    protected String actionConfirm(HttpServletRequest request, Map env) throws Exception {
        int result = 0;
        String manual = request.getParameter(PARAM_ENTERED);
        String tmp = request.getParameter(PARAM_CURRENT);

        if ( manual!=null && manual.length()>0 ) {
            try {
                result = Integer.parseInt(manual);
            } catch (NumberFormatException e) {
                ServletUtils.addError(PARAM_ENTERED,"Císlo větší než nula!",env, null);
            }
        } else {
            try {
                result = Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                ServletUtils.addError(Constants.ERROR_GENERIC,"Nebyla zvolena platná relace!",env, null);
            }
        }

        Relation current = (Relation) PersistenceFactory.getPersistance().findById(new Relation(result));
        env.put(VAR_CURRENT,current);
        return FMTemplateSelector.select("SelectRelation","step2",env,request);
    }

    /**
     * Called, when user confirms his choice. It redirects flow to PARAM_URL and puts all parameters
     * to session map AbcVelocityServlet.ATTRIB_PARAMS. There will be also result under name PARAM_SELECTED.
     * todo replace usage of ServletUtils with env.
     */
    protected String actionFinish(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map map = ServletUtils.putParamsToMap(request);
        map.put(PARAM_SELECTED,map.get(PARAM_CURRENT));
        String url = (String) map.remove(PARAM_URL);
        map.remove(PARAM_CURRENT);
        request.getSession().setAttribute(Constants.VAR_PARAMS,map);

        ((UrlUtils) env.get(Constants.VAR_URL_UTILS)).redirect(response, url);
        return null;
    }
}
