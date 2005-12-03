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
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Used to find the user.
 */
public class SelectUser implements AbcAction {
    public static final String PARAM_ACTION = "sAction";
    public static final String PARAM_URL = "url";
    public static final String PARAM_USER = "uid";
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_FROM = "from";

    public static final String VAR_HIDDEN_FIELDS_PARAMS = "SAVED_PARAMS";
    public static final String VAR_USERS = "USERS";

    public static final String ACTION_SHOW_FORM = "form";
    public static final String ACTION_SEARCH = "search";
    public static final String ACTION_REDIRECT = "redirect";

    public static final String SUBMIT_PREVIOUS_PAGE = "previous";
    public static final String SUBMIT_NEXT_PAGE = "next";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.remove(PARAM_ACTION);

        if (ACTION_SHOW_FORM.equals(action))
            return actionShowForm(request, env, params);

        if (ACTION_SEARCH.equals(action))
            return actionShowFoundUsers(request,env, params);

        if (ACTION_REDIRECT.equals(action)) {
            if (params.containsKey(SUBMIT_PREVIOUS_PAGE) || params.containsKey(SUBMIT_NEXT_PAGE))
                return actionShowFoundUsers(request, env, params);
            else
                return actionRedirect(request, response, env, params);
        }

        return null;
    }

    protected String actionShowForm(HttpServletRequest request, Map env, Map params) throws Exception {
        String saved = Tools.saveParams(params,getListOfProhibitedParams());
        env.put(VAR_HIDDEN_FIELDS_PARAMS,saved);
        return FMTemplateSelector.select("SelectUser", "form", env, request);
    }

    protected String actionShowFoundUsers(HttpServletRequest request, Map env, Map params) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int pageSize = AbcConfig.getViewUserPageSize();
        if ( params.containsKey(SUBMIT_PREVIOUS_PAGE) ) {
            from -= pageSize;
            params.remove(PARAM_USER);
        }
        if ( params.containsKey(SUBMIT_NEXT_PAGE) ) {
            from += pageSize;
            params.remove(PARAM_USER);
        }

        User searched = new User();
        boolean canContinue = true;
        canContinue &= setId(params, searched, env);
        canContinue &= setName(params, searched, env);
        canContinue &= setLogin(params, searched, env);
        canContinue &= setEmail(params, searched, env);

        if ( !canContinue )
            return actionShowForm(request,env,params);

        if (searched.preciseEquals(new User())) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Vyplòte alespoò jedno políèko!",env,null);
            return actionShowForm(request, env, params);
        }

        List list = new ArrayList(1);
        list.add(searched);
        List found = persistance.findByExample(list, null);

        if ( found.size()==0 ) {
            ServletUtils.addMessage("Nenalezen ¾ádný u¾ivatel!", env, null);
            return actionShowForm(request, env, params);
        }

        int i = 0, j = from+pageSize;
        List result = new ArrayList(pageSize);
        for ( Iterator iter = found.iterator(); iter.hasNext(); i++ ) {
            User user = (User) iter.next();
            if (i<from || i>=j) continue;
            result.add(persistance.findById(user));
        }
        Paging paging = new Paging(result, from, pageSize, found.size());
        env.put(VAR_USERS, paging);

        ArrayList prohibited = new ArrayList();
        prohibited.add(PARAM_USER);
        prohibited.add(PARAM_FROM);
        prohibited.add(SUBMIT_NEXT_PAGE);
        prohibited.add(SUBMIT_PREVIOUS_PAGE);
        String saved = Tools.saveParams(params, prohibited);
        env.put(VAR_HIDDEN_FIELDS_PARAMS, saved);

        return FMTemplateSelector.select("SelectUser", "result", env, request);
    }

    protected String actionRedirect(HttpServletRequest request, HttpServletResponse response, Map env, Map params) throws Exception {
        String url = (String) params.remove(PARAM_URL);
        params.remove(PARAM_EMAIL);
        params.remove(PARAM_LOGIN);
        params.remove(PARAM_NAME);
        params.remove(PARAM_FROM);
        params.remove(SUBMIT_PREVIOUS_PAGE);
        params.remove(SUBMIT_NEXT_PAGE);
        request.getSession().setAttribute(Constants.VAR_PARAMS, params);

        ((UrlUtils) env.get(Constants.VAR_URL_UTILS)).redirect(response, url);
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    //                          Setters                                      //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sets name search field from parameters.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, User user, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if (name==null || name.length()==0)
            return true;
        if ( name.length()>0 && name.length()<3 ) {
            ServletUtils.addError(PARAM_NAME, "Jméno musí obsahovat nejménì tøi písmena!", env, null);
            return false;
        }
        user.setName("%"+name+"%");
        return true;
    }

    /**
     * Sets login search field from parameters.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setLogin(Map params, User user, Map env) {
        String login = (String) params.get(PARAM_LOGIN);
        if (login==null || login.length()==0)
            return true;
        if ( login.length()>0 && login.length()<3 ) {
            ServletUtils.addError(PARAM_LOGIN, "Pøihla¹ovací jméno musí obsahovat nejménì tøi písmena!", env, null);
            return false;
        }
        user.setLogin("%"+login+"%");
        return true;
    }

    /**
     * Sets email search field from parameters.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setEmail(Map params, User user, Map env) {
        String email = (String) params.get(PARAM_EMAIL);
        if (email==null || email.length()==0)
            return true;
        if ( email.length()>0 && (email.length()<3 || email.indexOf('@')==-1) ) {
            ServletUtils.addError(PARAM_EMAIL, "Email musí obsahovat nejménì tøi písmena a zavináè!", env, null);
            return false;
        }
        user.setEmail("%"+email+"%");
        return true;
    }

    /**
     * Sets id search field from parameters.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setId(Map params, User user, Map env) {
        String tmp = (String) params.get(PARAM_USER);
        if (tmp==null)
            return true;
        tmp = tmp.trim();
        if (tmp.length()==0)
            return true;
        try {
            int id = Integer.parseInt(tmp);
            user.setId(id);
            return true;
        } catch (NumberFormatException e) {
            ServletUtils.addError(PARAM_USER, "Zadejte èíslo!", env, null);
            return false;
        }
    }

    /**
     * @return list of names of parameters, that must not be saved.
     */
    private List getListOfProhibitedParams() {
        ArrayList prohibited = new ArrayList();
        prohibited.add(PARAM_ACTION);
        prohibited.add(PARAM_USER);
        prohibited.add(PARAM_NAME);
        prohibited.add(PARAM_LOGIN);
        prohibited.add(PARAM_EMAIL);
        prohibited.add(PARAM_FROM);
        prohibited.add(SUBMIT_PREVIOUS_PAGE);
        prohibited.add(SUBMIT_NEXT_PAGE);
        return prohibited;
    }
}
