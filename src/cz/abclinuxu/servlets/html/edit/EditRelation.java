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
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.servlets.html.select.SelectRelation;
import cz.abclinuxu.servlets.html.select.SelectUser;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.CustomURLCache;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.ACL;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.exceptions.MissingArgumentException;

import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Class for removing relations or creating links.
 */
public class EditRelation implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRelation.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_SELECTED = SelectRelation.PARAM_SELECTED;
    public static final String PARAM_ACL_ID = "id";
    public static final String PARAM_ACL_TYPE = "right";
    public static final String PARAM_ACL_VALUE = "value";
    public static final String PARAM_ACL_WHO = "who";
    public static final String PARAM_USER = ViewUser.PARAM_USER_SHORT;
    public static final String PARAM_GROUP = EditGroup.PARAM_GROUP;
    public static final String PARAM_URL = "url";

    public static final String VAR_CURRENT = "CURRENT";
    public static final String VAR_SELECTED = "SELECTED";
    public static final String VAR_PARENTS = "PARENTS";
    public static final String VAR_PARENT = "PARENT";
    public static final String VAR_ACL = "ACL";
    public static final String VAR_GROUPS = EditGroup.VAR_GROUPS;

    public static final String VALUE_DISCUSSIONS = "discussions";
    public static final String VALUE_MAKES = "makes";
    public static final String VALUE_ARTICLES = "articles";
    public static final String VALUE_CATEGORIES = "categories";

    public static final String ACTION_LINK = "add";
    public static final String ACTION_LINK_STEP2 = "add2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";
    public static final String ACTION_MOVE = "move";
    public static final String ACTION_MOVE_ALL = "moveAll";
    public static final String ACTION_MOVE_ALL_STEP2 = "moveAll2";
    public static final String ACTION_SHOW_ACL = "showACL";
    public static final String ACTION_ADD_ACL = "addACL";
    public static final String ACTION_ADD_ACL_STEP2 = "addACL2";
    public static final String ACTION_ADD_ACL_STEP3 = "addACL3";
    public static final String ACTION_REMOVE_ACL = "removeACL";
    public static final String ACTION_SET_URL = "setURL";
    public static final String ACTION_SET_URL_STEP2 = "setURL2";
    public static final String ACTION_SET_URL_STEP3 = "setURL3";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        if (ACTION_SET_URL.equals(action))
            return actionSetUrlStep1(request, env);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        relation = (Relation) persistence.findById(relation);
        env.put(VAR_CURRENT, relation);
        GenericObject child = relation.getChild();
        persistence.synchronize(child);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_LINK) ) {
            if ( !canCreateLink(user) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionLinkStep1(request,env);
        }

        if ( action.equals(ACTION_LINK_STEP2) ) {
            if ( !canCreateLink(user) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionLinkStep2(request, response, env);
        }

        if ( action.equals(ACTION_MOVE_ALL) ) {
            if ( !canMoveAll(user) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return FMTemplateSelector.select("EditRelation", "moveAll", env, request);
        }

        if ( action.equals(ACTION_MOVE_ALL_STEP2) ) {
            if ( !canMoveAll(user) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionMoveAll(request, response, env);
        }

        if ( action.equals(ACTION_SHOW_ACL) ) {
            if (!canManageACL(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionShowACL(request,env);
        }

        if ( action.equals(ACTION_ADD_ACL) ) {
            if (!canManageACL(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionAddACLStep1(request,env);
        }

        if ( action.equals(ACTION_ADD_ACL_STEP2) ) {
            if (!canManageACL(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionAddACLStep2(request,response,env);
        }

        if ( action.equals(ACTION_ADD_ACL_STEP3) ) {
            if (!canManageACL(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionAddACLStep3(request,env);
        }

        if ( action.equals(ACTION_REMOVE_ACL) ) {
            if (!canManageACL(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionRemoveACL(request, env);
        }

        if ( action.equals(ACTION_SET_URL_STEP2) ) {
            if (!canSetUrl(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionSetUrlStep2(request, env);
        }

        if ( action.equals(ACTION_SET_URL_STEP3) ) {
            if (!canSetUrl(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionSetUrlStep3(request, response, env);
        }

        if ( action.equals(ACTION_MOVE) ) {
            if ( !canMoveRelation(user, child) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionMove(request, response, env);
        }

        if ( action.equals(ACTION_REMOVE) ) {
            if (!canRemoveRelation(user, child))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionRemove1(request, env);
        }

        if ( action.equals(ACTION_REMOVE_STEP2) ) {
            if (!canRemoveRelation(user, child))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionRemove2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    private boolean canMoveAll(User user) {
        return user.hasRole(Roles.CATEGORY_ADMIN);
    }

    private boolean canMoveRelation(User user, GenericObject obj) {
        boolean canMove = false;
        canMove |= user.hasRole(Roles.CAN_MOVE_RELATION);
        if (obj instanceof Category)
            canMove |= user.hasRole(Roles.CATEGORY_ADMIN);
        if (obj instanceof Item) {
            switch (((Item) obj).getType()) {
                case Item.DISCUSSION:
                    canMove |= user.hasRole(Roles.DISCUSSION_ADMIN);
                    break;
                case Item.ARTICLE:
                    canMove |= user.hasRole(Roles.ARTICLE_ADMIN);
                    break;
            }
        }
        return canMove;
    }

    private boolean canRemoveRelation(User user, GenericObject obj) {
        boolean canRemove = false;
        canRemove |= user.hasRole(Roles.CAN_REMOVE_RELATION);
        if (obj instanceof Category)
            canRemove |= user.hasRole(Roles.CATEGORY_ADMIN);
        if (obj instanceof Item) {
            switch (((Item) obj).getType()) {
                case Item.DISCUSSION:
                    canRemove |= user.hasRole(Roles.DISCUSSION_ADMIN);
                    break;
                case Item.ARTICLE:
                    canRemove |= user.hasRole(Roles.ARTICLE_ADMIN);
                    break;
                case Item.SURVEY:
                    canRemove |= user.hasRole(Roles.SURVEY_ADMIN);
                    break;
            }
        }
        if (obj instanceof Poll)
            canRemove |= user.hasRole(Roles.POLL_ADMIN);
        // todo check ownership
        return canRemove;
    }

    private boolean canCreateLink(User user) {
        return user.hasRole(Roles.CATEGORY_ADMIN);
    }

    private boolean canSetUrl(User user) {
        return user.hasRole(Roles.ROOT);
    }

    private boolean canManageACL(User user) {
        return user.hasRole(Roles.ROOT);
    }

    protected String actionLinkStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED, Relation.class, params, request);
        if ( relation!=null ) {
            relation = (Relation) persistence.findById(relation);
            env.put(VAR_SELECTED,relation);
        }
        return FMTemplateSelector.select("EditRelation","add",env,request);
    }

    protected String actionLinkStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();

        Relation parent = (Relation) env.get(VAR_CURRENT);
        Relation child = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED, Relation.class, params, request);
        persistence.synchronize(child);

        Relation relation = new Relation();
        relation.setParent(parent.getChild());
        relation.setChild(child.getChild());
        relation.setUpper(parent.getId());

        String tmp = (String) params.get(PARAM_NAME);
        if ( tmp!=null && tmp.length()>0 ) {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            root.addElement("name").addText(tmp);
            relation.setData(document);
        }

        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        String prefix = (String)params.get(PARAM_PREFIX);
        UrlUtils urlUtils = new UrlUtils(prefix, response);
        urlUtils.redirect(response, "/show/"+parent.getId());
        return null;
    }

    /**
     * Displays form to set new URL.
     */
    protected String actionSetUrlStep1(HttpServletRequest request, Map env) {
        return FMTemplateSelector.select("EditRelation", "setUrl", env, request);
    }

    /**
     * Shows the relation for which the user wishes to set new URL.
     */
    protected String actionSetUrlStep2(HttpServletRequest request, Map env) {
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_CURRENT);
        Relation upper = null;
        if (relation.getUpper()!=0)
            upper = (Relation) persistence.findById(new Relation(relation.getUpper()));
        else {
            upper = new Relation();
            upper.setUrl("");
        }
        if (upper.getUrl() == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nadøazená relace ("+relation.getUpper()+") nemá definované URL!", env, null);
            return FMTemplateSelector.select("EditRelation", "setUrl", env, request);
        }
        env.put(VAR_PARENT, upper);

        return FMTemplateSelector.select("EditRelation", "setUrl2", env, request);
    }

    /**
     * Sets new URL.
     */
    protected String actionSetUrlStep3(HttpServletRequest request, HttpServletResponse response, Map env) throws IOException {
        Persistence persistence = PersistenceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();

        Relation relation = (Relation) env.get(VAR_CURRENT);
        String originalUrl = relation.getUrl();

        Relation upper = null;
        if (relation.getUpper() != 0)
            upper = (Relation) persistence.findById(new Relation(relation.getUpper()));
        else {
            upper = new Relation();
            upper.setUrl("");
        }

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String url = (String) params.get(PARAM_URL);
        if (url==null || url.length()==0) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL!", env, null);
            return actionSetUrlStep2(request, env);
        }

        if (url.charAt(url.length()-1)=='/')
            url = url.substring(0, url.length()-1);

        if (url.indexOf('/')!=-1) {
            ServletUtils.addError(PARAM_URL, "Zadáváte jen poslední èást URL, lomítko je zakázáno!", env, null);
            return actionSetUrlStep2(request, env);
        }

        url = URLManager.enforceRelativeURL(url);
        url = upper.getUrl() + '/' + url;
        if (URLManager.exists(url)) {
            ServletUtils.addError(PARAM_URL, "Toto URL ji¾ existuje!", env, null);
            return actionSetUrlStep2(request, env);
        }

        relation.setUrl(url);
        persistence.update(relation);

        if (originalUrl != null) {
            CustomURLCache.getInstance().remove(originalUrl);
            sqlTool.insertOldAddress(originalUrl, null, new Integer(relation.getId()));
            ServletUtils.addMessage("Adresa byla zmìnìna. Nyní zkontrolujte, zda není tøeba zmìnit i adresy podstránek.", env, request.getSession());
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionRemove1(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_CURRENT);

        Relation[] parents = persistence.findByExample(new Relation(null,relation.getChild(),0));
        env.put(VAR_PARENTS,parents);
        return FMTemplateSelector.select("EditRelation","remove",env,request);
    }

    protected String actionRemove2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_CURRENT);
        GenericObject child = relation.getChild();
        String objectName = Tools.childName(relation);

        if (child instanceof Item) {
            Item item = (Item) child;
            switch (item.getType()) {
                case Item.ARTICLE:
                    removeArticleFromSeries(item, relation.getId());
                    break;
            }

            Element inset = (Element) item.getData().selectSingleNode("/data/inset");
            if (inset != null)
                EditAttachment.removeAllAttachments(inset, env, user, request);
        }

        runMonitor(relation,user);
        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);
        AdminLogger.logEvent(user, "remove | relation "+relation.getId()+" | "+objectName);

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix != null && relation.getUpper() > 0 ) {
            url = prefix.concat("/dir/"+relation.getUpper());
        } else
            url = "/";

        UrlUtils urlUtils = new UrlUtils(prefix, response);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Removes link to this article from the series (if set).
     */
    private void removeArticleFromSeries(Item article, int articleRelationId) {
        Element element = article.getData().getRootElement().element("series_rid");
        if (element == null)
            return;

        int seriesRid = Misc.parseInt(element.getText(), 0);
        if (seriesRid == 0)
            return;

        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) persistence.findById(new Relation(seriesRid));
        Item series = (Item) persistence.findById(relation.getChild()).clone();

        String xpath = "/data/article[text()='" + articleRelationId + "']";
        element = (Element) series.getData().selectSingleNode(xpath);
        if (element != null) {
            element.detach();
            persistence.update(series);
        }
    }

    /**
     * Called, when user selects destination in SelectRelation. It replaces parent in relation with child
     * in destination.
     */
    protected String actionMove(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_CURRENT);
        int originalUpper = relation.getUpper();
        Relation destination = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED, Relation.class, params, request);
        persistence.synchronize(destination);

        String originalUrl = relation.getUrl();
        if (originalUrl != null)
            updateRelationUri(originalUrl, relation, destination, env, request);

        relation.getParent().removeChildRelation(relation);
        relation.setParent(destination.getChild());
        relation.setUpper(destination.getId());
        persistence.update(relation);
        relation.getParent().addChildRelation(relation);

        String from = (originalUrl == null) ? Integer.toString(originalUpper) : originalUrl;
        String to = (relation.getUrl() == null) ? Integer.toString(destination.getId()) : relation.getUrl();
        AdminLogger.logEvent(user, "  move | relation "+relation.getId()+" | from "+from+" | to "+to);

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            if ( originalUpper==Constants.REL_FORUM && returnBackToForum(user) )
                url = "/diskuse.jsp";
            else
                url = prefix.concat("/show/"+relation.getUpper());
        } else url = "/";

        UrlUtils urlUtils = new UrlUtils("", response);
        urlUtils.redirect(response, url);
        return null;
    }

    private void updateRelationUri(String originalUrl, Relation relation, Relation destination, Map env, HttpServletRequest request) {
        CustomURLCache.getInstance().remove(originalUrl);
        SQLTool sqlTool = SQLTool.getInstance();
        sqlTool.insertOldAddress(originalUrl, null, new Integer(relation.getId()));

        String dirUri = destination.getUrl();
        if (dirUri == null) {
            relation.setUrl(null);
            ServletUtils.addMessage("Cílová sekce nemá definovanou adresu, URL bylo zru¹eno.", env, request.getSession());
        }

        int position = originalUrl.lastIndexOf('/');
        String localPart = originalUrl.substring(position + 1);
        String url = URLManager.enforceRelativeURL(localPart);
        url = dirUri + '/' + localPart;
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);
    }

    /**
     * Called, when user selects destination in SelectRelation. It replaces parent in relation with child
     * in destination.
     */
    protected String actionMoveAll(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String type = (String) params.get(PARAM_TYPE);

        Relation relation = (Relation) env.get(VAR_CURRENT);
        persistence.synchronize(relation.getChild());
        Relation destination = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED, Relation.class, params, request);
        persistence.synchronize(destination);

        for ( Iterator iter = relation.getChild().getChildren().iterator(); iter.hasNext(); ) {
            Relation childRelation = (Relation) iter.next();
            GenericObject child = childRelation.getChild();
            persistence.synchronize(child);
            boolean move = false;
            if (child instanceof Item) {
                int itemType = ((Item)child).getType();
                if (VALUE_ARTICLES.equals(type) && itemType==Item.ARTICLE)
                    move = true;
                if (VALUE_DISCUSSIONS.equals(type) && (itemType==Item.DISCUSSION || itemType == Item.FAQ))
                    move = true;
                if (VALUE_MAKES.equals(type) && (itemType==Item.HARDWARE || itemType==Item.SOFTWARE))
                    move = true;
            } else if ( VALUE_CATEGORIES.equals(type) && child instanceof Category)
                move = true;

            if (move) {
                String originalUrl = childRelation.getUrl();
                if (originalUrl != null)
                    updateRelationUri(originalUrl, childRelation, destination, env, request);

                childRelation.getParent().removeChildRelation(childRelation);
                childRelation.setParent(destination.getChild());
                childRelation.setUpper(destination.getId());
                persistence.update(childRelation);
                childRelation.getParent().addChildRelation(childRelation);

                String from = (originalUrl == null) ? Integer.toString(relation.getId()) : originalUrl;
                String to = (childRelation.getUrl() == null) ? Integer.toString(destination.getId()) : childRelation.getUrl();
                AdminLogger.logEvent(user, "  move | relation " + relation.getId() + " | from " + from + " | to " + to);
            }
        }

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        url = (prefix!=null) ? prefix.concat("/show/"+relation.getId()) : "/";

        UrlUtils urlUtils = new UrlUtils("", response);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Shows ACL for given relation.
     */
    private String actionShowACL(HttpServletRequest request, Map env) throws Exception {
        Relation relation = (Relation) env.get(VAR_CURRENT);
        if (relation.getData()!=null) {
            List nodes = relation.getData().selectNodes("/data/acl");
            List acls = new ArrayList(nodes.size());
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                ACL acl = getACL(element);
                acls.add(acl);
            }
            if (acls.size()>0)
                env.put(VAR_ACL,acls);
        }

        return FMTemplateSelector.select("EditRelation", "showACL", env, request);
    }

    /**
     * Adds new ACL for given relation.
     */
    private String actionAddACLStep1(HttpServletRequest request, Map env) throws Exception {
        List items = SQLTool.getInstance().findItemsWithType(Item.GROUP, 0, EditGroup.DEFAULT_MAX_NUMBER_OF_GROUPS);
        env.put(VAR_GROUPS, items);

        return FMTemplateSelector.select("EditRelation", "addACL", env, request);
    }

    /**
     * Adds new ACL for given relation.
     */
    private String actionAddACLStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String who = (String) params.remove(PARAM_ACL_WHO);
        if ( "user".equals(who) ) {
            String url = "/SelectUser";
            params.put(SelectUser.PARAM_ACTION,SelectUser.ACTION_SHOW_FORM);
            params.put(SelectUser.PARAM_URL, "/EditRelation");
            params.put(PARAM_ACTION, ACTION_ADD_ACL_STEP3);
            request.getSession().setAttribute(Constants.VAR_PARAMS, params);

            ((UrlUtils) env.get(Constants.VAR_URL_UTILS)).redirect(response, url);
            return null;
        } else
            return actionAddACLStep3(request,env);
    }

    /**
     * Adds new ACL for given relation.
     */
    private String actionAddACLStep3(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_CURRENT);
        Persistence persistence = PersistenceFactory.getPersistance();

        boolean canContinue = setACL(params,relation,env);
        if (canContinue) {
            persistence.update(relation);
            AdminLogger.logEvent(user, "vytvoril nove ACL pro relaci "+relation.getId());
        }

        return actionShowACL(request,env);
    }

    /**
     * Removes selected ACLs.
     */
    protected String actionRemoveACL(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_CURRENT);
        Persistence persistence = PersistenceFactory.getPersistance();
        Document document = relation.getData();

        List acls = null;
        Object tmp = params.get(PARAM_ACL_ID);
        if ( tmp instanceof String ) {
            acls = new ArrayList(1);
            acls.add(tmp);
        } else
            acls = (List) tmp;

        for ( Iterator iter = acls.iterator(); iter.hasNext(); ) {
            String id = (String) iter.next();
            Element element = (Element) document.selectSingleNode("/data/acl[@id='"+id+"']");
            if ( element==null )
                continue;
            element.detach();
            AdminLogger.logEvent(user, "zrusil ACL "+id+" z relace "+relation.getId());
        }
        persistence.update(relation);

        return actionShowACL(request, env);
    }

    ///////////////////////////////////////////////////////////////////////////
    //                          Setters                                      //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates ACL from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param relation relation to be updated
     * @return false, if there is a major error.
     */
    private boolean setACL(Map params, Relation relation, Map env) {
        String right = (String) params.get(PARAM_ACL_TYPE);
        String value = (String) params.get(PARAM_ACL_VALUE);
        int gid = Misc.parseInt((String) params.get(PARAM_GROUP),0);
        int uid = Misc.parseInt((String) params.get(PARAM_USER),0);

        Document document = relation.getData();
        if ( document==null ) {
            document = DocumentHelper.createDocument();
            relation.setData(document);
        }

        int id = 0;
        List acls = document.selectNodes("/data/acl");
        for ( Iterator iter = acls.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            int tmp = Misc.parseInt(element.attributeValue("id"),0);
            if (tmp>=id) id = tmp+1;
        }

        Element data = document.getRootElement();
        if (data==null) {
            data = DocumentHelper.createElement("data");
            document.setRootElement(data);
        }

        Element acl = data.addElement("acl");
        acl.addAttribute("id",new Integer(id).toString());
        acl.addAttribute("right",right);
        acl.addAttribute("value",value);
        if (uid>0)
            acl.addAttribute("uid", new Integer(uid).toString());
        else if (gid>0)
            acl.addAttribute("gid", new Integer(gid).toString());
        else {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Musíte zadat èíslo skupiny nebo u¾ivatele!",env,null);
            return false;
        }

        return true;
    }

    /**
     * Converts DOM4J Element to ACL.
     */
    public static ACL getACL(Element element) {
        Persistence persistence = PersistenceFactory.getPersistance();

        int id = Misc.parseInt(element.attributeValue("id"),0);
        int gid = Misc.parseInt(element.attributeValue("gid"),0);
        int uid = Misc.parseInt(element.attributeValue("uid"),0);

        ACL acl;
        if (uid!=0)
            acl = new ACL(id,(User) persistence.findById(new User(uid)));
        else
            acl = new ACL(id, (Item) persistence.findById(new Item(gid)));

        boolean value = "yes".equals(element.attributeValue("value"));
        acl.setRight(element.attributeValue("right"),value);

        return acl;
    }

    /**
     * Whether user wishes to be redirected after move of discussions
     * from discussion forum back to the forum.
     * @param user
     * @return
     */
    private static boolean returnBackToForum(User user) {
        Node node = user.getData().selectSingleNode("/data/settings/return_to_forum");
        if ( node!=null )
            return "yes".equals(node.getText());
        return false;
    }

    /**
     * Runs monitor, if deleted object was driver, make or discussion (question).
     */
    private void runMonitor(Relation relation, User user) {
        MonitorAction action = null;

        GenericObject child = relation.getChild();
        if ( ! (child instanceof Item) )
            return;

        Item item = (Item) child;
        if (item.getType()==Item.DRIVER) {
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DRIVER, item, null);
            String name = item.getData().selectSingleNode("/data/name").getText();
            action.setProperty(Decorator.PROPERTY_NAME, name);
        } else if (item.getType()==Item.HARDWARE) {
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.ITEM, item, null);
            String name = item.getData().selectSingleNode("/data/name").getText();
            action.setProperty(Decorator.PROPERTY_NAME, name);
        } else if (item.getType()==Item.DISCUSSION) {
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DISCUSSION, item, null);
            Element title = (Element) item.getData().selectSingleNode("/data/title");
            if (title==null)
                return;
            action.setProperty(Decorator.PROPERTY_NAME, title.getText());
        }

        if (action!=null)
            MonitorPool.scheduleMonitorAction(action);
    }
}
