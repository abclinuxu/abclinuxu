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

package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class EditServers implements AbcAction {
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_SERVER = "server";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_RSS_URL = "rssUrl";
    public static final String PARAM_URL = "url";
    public static final String PARAM_CONTACT = "contact";
    
    public static final String ACTION_LIST = "list";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_REMOVE = "remove";
    
    public static final String VAR_SERVERS = "SERVERS";
    
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);
        
        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }
        
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation != null) {
            Tools.sync(relation);
            env.put(ShowObject.VAR_RELATION, relation);
        } else
            throw new MissingArgumentException("Chybí číslo relace!");
        
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);        
        
        if (ACTION_EDIT.equals(action)) {
            Relation parentRelation = new Relation(relation.getUpper());
            Tools.sync(parentRelation);

            if (!Tools.permissionsFor(user, parentRelation).canModify())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionEditStep1(request, response, env);
        }
        if (ACTION_EDIT_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditServers.class, true, true, true, false);
            
            Relation parentRelation = new Relation(relation.getUpper());
            Tools.sync(parentRelation);

            if (!Tools.permissionsFor(user, parentRelation).canModify())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionEditStep2(request, response, env, parentRelation);
        }
        
        if (Misc.empty(action) || ACTION_LIST.equals(action)) {
            if (!Tools.permissionsFor(user, relation).canModify())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return processSection(request, response, env, relation);
        }
        
        if (ACTION_ADD.equals(action)) {
            if (!Tools.permissionsFor(user, relation).canCreate())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            
            return FMTemplateSelector.select("EditServers", "add", env, request);
        }
        
        if (ACTION_ADD_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditServers.class, true, true, true, false);
            
            if (!Tools.permissionsFor(user, relation).canCreate())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            
            return actionAddStep2(request, response, env);
        }
        
        if (ACTION_REMOVE.equals(action)) {
            if (!Tools.permissionsFor(user, relation).canDelete())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            
            return actionRemove(request, response, env);
        }
        
        return null;
    }
    
    public static String processSection(HttpServletRequest request, HttpServletResponse response, Map env, Relation relation) {
        SQLTool sqlTool = SQLTool.getInstance();
        
        List servers = sqlTool.findServerRelationsInCategory(relation.getChild().getId());
        Tools.syncList(servers);
        env.put(VAR_SERVERS, servers);
        
        return FMTemplateSelector.select("EditServers", "list", env, request);
    }
    
    public String actionRemove(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        List servers = Tools.asList(params.get(PARAM_SERVER));
        
        for (Iterator it = servers.iterator(); it.hasNext(); ) {
            String srid = (String) it.next();
            Relation rel = new Relation(Integer.parseInt(srid));
            
            Tools.sync(rel);
            persistence.remove(rel);
        }
        
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditServers/"+relation.getId());
        return null;
    }
    
    public String actionEditStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Server server = (Server) relation.getChild();
        
        params.put(PARAM_NAME, server.getName());
        params.put(PARAM_RSS_URL, server.getRssUrl());
        params.put(PARAM_URL, server.getUrl());
        params.put(PARAM_CONTACT, server.getContact());
        
        return FMTemplateSelector.select("EditServers", "edit", env, request);
    }
    
    public String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env, Relation parentRelation) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Server server = (Server) relation.getChild().clone();
        Persistence persistence = PersistenceFactory.getPersistence();
        
        boolean canContinue;
        canContinue = setName(params, server, env);
        canContinue &= setRssUrl(params, server, env);
        canContinue &= setUrl(params, server, env);
        canContinue &= setContact(params, server, env);
        
        if (!canContinue)
            return FMTemplateSelector.select("EditServers", "edit", env, request);
        
        persistence.update(server);
        
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditServers/"+parentRelation.getId());
        return null;
    }
    
    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation parentRelation = (Relation) env.get(ShowObject.VAR_RELATION);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Server server = new Server();
        Persistence persistence = PersistenceFactory.getPersistence();
        
        boolean canContinue;
        canContinue = setName(params, server, env);
        canContinue &= setRssUrl(params, server, env);
        canContinue &= setUrl(params, server, env);
        canContinue &= setContact(params, server, env);
        
        if (!canContinue)
            return FMTemplateSelector.select("EditServers", "edit", env, request);
        
        Relation relation = new Relation(parentRelation.getChild(), server, parentRelation.getId());
        
        persistence.create(server);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);
        
        ServletUtils.addMessage("Server přidán, bude načten při dalším cyklu.", env, request.getSession());
        
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditServers/"+parentRelation.getId());
        return null;
    }
    
    private boolean setName(Map params, Server server, Map env) {
        String name = (String) params.get(PARAM_NAME);
        
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_NAME, "Zadejte jméno serveru!", env, null);
            return false;
        }
        
        if (name.indexOf('<') != -1) {
            ServletUtils.addError(PARAM_NAME, "HTML zde není povoleno!", env, null);
            return false;
        }
        
        server.setName(name);
        return true;
    }
    
    private boolean setRssUrl(Map params, Server server, Map env) {
        String url = (String) params.get(PARAM_RSS_URL);
        
        if (Misc.empty(url)) {
            ServletUtils.addError(PARAM_RSS_URL, "Zadejte URL RSS kanálu!", env, null);
            return false;
        }
        
        if (!url.startsWith("http://")) {
            ServletUtils.addError(PARAM_RSS_URL, "Zadejte platné URL RSS kanálu!", env, null);
            return false;
        }
        
        server.setRssUrl(url);
        return true;
    }
    
    private boolean setUrl(Map params, Server server, Map env) {
        String url = (String) params.get(PARAM_URL);
        
        if (Misc.empty(url)) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL webu!", env, null);
            return false;
        }
        
        if (!url.startsWith("http://")) {
            ServletUtils.addError(PARAM_URL, "Zadejte platné URL webu!", env, null);
            return false;
        }
        
        server.setUrl(url);
        return true;
    }
    
    private boolean setContact(Map params, Server server, Map env) {
        String contact = (String) params.get(PARAM_CONTACT);
        server.setContact(contact);
        return true;
    }
}
