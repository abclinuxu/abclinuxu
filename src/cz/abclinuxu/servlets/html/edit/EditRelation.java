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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.select.SelectRelation;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.CustomURLCache;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.email.monitor.Decorator;
import cz.abclinuxu.utils.email.monitor.MonitorAction;
import cz.abclinuxu.utils.email.monitor.MonitorPool;
import cz.abclinuxu.utils.email.monitor.ObjectType;
import cz.abclinuxu.utils.email.monitor.UserAction;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

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
    public static final String ACTION_SET_URL = "setURL";
    public static final String ACTION_SET_URL_STEP2 = "setURL2";
    public static final String ACTION_SET_URL_STEP3 = "setURL3";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
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
            ActionProtector.ensureContract(request, EditRelation.class, true, true, true, false);
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
            ActionProtector.ensureContract(request, EditRelation.class, true, false, false, true);
            return actionMoveAll(request, response, env);
        }

        if ( action.equals(ACTION_SET_URL_STEP2) ) {
            if (!canSetUrl(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionSetUrlStep2(request, env);
        }

        if ( action.equals(ACTION_SET_URL_STEP3) ) {
            if (!canSetUrl(user))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            ActionProtector.ensureContract(request, EditRelation.class, true, true, true, false);
            return actionSetUrlStep3(request, response, env);
        }

        if ( action.equals(ACTION_MOVE) ) {
            if ( !canMoveRelation(user, child) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            ActionProtector.ensureContract(request, EditRelation.class, true, false, false, true);
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
            ActionProtector.ensureContract(request, EditRelation.class, true, true, true, false);
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

    protected String actionLinkStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED, Relation.class, params, request);
        if ( relation!=null ) {
            relation = (Relation) persistence.findById(relation);
            env.put(VAR_SELECTED,relation);
        }
        return FMTemplateSelector.select("EditRelation","add",env,request);
    }

    // todo k cemu tohle vlastne slouzi? Zkousel jsem linkovat FAQ do sekce a relace byla obracene, nez bych cekal
    protected String actionLinkStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

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
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(VAR_CURRENT);
        if (relation.getUrl() != null) {
            params.put(PARAM_URL, relation.getUrl());
            return FMTemplateSelector.select("EditRelation", "setUrl2", env, request);
        }

        if (relation.getUpper() != 0) {
            Relation upper = (Relation) persistence.findById(new Relation(relation.getUpper()));
            if (upper.getUrl() != null) {
                String name = Tools.childName(relation);
                String url = upper.getUrl() + "/" + URLManager.enforceRelativeURL(name);
                params.put(PARAM_URL, url);
            }
        }

        return FMTemplateSelector.select("EditRelation", "setUrl2", env, request);
    }

    /**
     * Sets new URL.
     */
    protected String actionSetUrlStep3(HttpServletRequest request, HttpServletResponse response, Map env) throws IOException {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        Relation relation = (Relation) env.get(VAR_CURRENT);
        String originalUrl = relation.getUrl();

        String url = (String) params.get(PARAM_URL);
        if (url == null || url.length() == 0) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL!", env, null);
            return FMTemplateSelector.select("EditRelation", "setUrl2", env, request);
        }

        if (url.charAt(url.length() - 1) == '/')
            url = url.substring(0, url.length() - 1);

        if (relation.getUpper() != 0) {
            Relation upper = (Relation) persistence.findById(new Relation(relation.getUpper()));
            if (url.equals(upper.getUrl())) {
                ServletUtils.addError(PARAM_URL, "Musíte přidat lokální část URL!", env, null);
                return FMTemplateSelector.select("EditRelation", "setUrl2", env, request);
            }
        }

        url = URLManager.enforceAbsoluteURL(url);
        if (url.equals(originalUrl)) {
            urlUtils.redirect(response, url);
            return null;
        }

        if (URLManager.exists(url)) {
            ServletUtils.addError(PARAM_URL, "Toto URL již existuje!", env, null);
            return FMTemplateSelector.select("EditRelation", "setUrl2", env, request);
        }

        relation.setUrl(url);
        persistence.update(relation);

        if (originalUrl != null) {
            CustomURLCache.getInstance().remove(originalUrl);
            sqlTool.insertOldAddress(originalUrl, null, new Integer(relation.getId()));
            ServletUtils.addMessage("Adresa byla změněna. Nyní zkontrolujte, zda není třeba změnit i adresy podstránek.", env, request.getSession());
        }

        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionRemove1(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(VAR_CURRENT);

        Relation[] parents = persistence.findByExample(new Relation(null,relation.getChild(),0));
        env.put(VAR_PARENTS,parents);
        return FMTemplateSelector.select("EditRelation","remove",env,request);
    }

    protected String actionRemove2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
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

        if (child instanceof GenericDataObject) {
            Versioning versioning = VersioningFactory.getVersioning();
            versioning.purge((GenericDataObject)child);
        }

        runMonitor(relation,user);
        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);
        if (relation.getUrl() != null)
            CustomURLCache.getInstance().remove(relation.getUrl());

        AdminLogger.logEvent(user, "remove | relation "+relation.getId()+" | "+objectName);

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix != null && relation.getUpper() > 0 ) {
            url = UrlUtils.getRelationUrl(new Relation(relation.getUpper()), prefix);
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

        Persistence persistence = PersistenceFactory.getPersistence();
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
        Persistence persistence = PersistenceFactory.getPersistence();
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
        if ( prefix != null ) {
            if ( originalUpper == Constants.REL_FORUM && returnBackToForum(user) )
                url = "/diskuse.jsp";
            else
                url = UrlUtils.getRelationUrl(relation, prefix);
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
            ServletUtils.addMessage("Cílová sekce nemá definovanou adresu, URL bylo zrušeno.", env, request.getSession());
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
        Persistence persistence = PersistenceFactory.getPersistence();
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

    ///////////////////////////////////////////////////////////////////////////
    //                          Setters                                      //
    ///////////////////////////////////////////////////////////////////////////

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
        String name = Tools.childName(item);

        if (item.getType() == Item.DRIVER) 
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DRIVER, relation, null);
        else if (item.getType() == Item.HARDWARE)
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.HARDWARE, relation, null);
        else if (item.getType() == Item.SOFTWARE)
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.SOFTWARE, relation, null);
        else if (item.getType() == Item.DICTIONARY)
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DICTIONARY, relation, null);
        else if (item.getType() == Item.PERSONALITY)
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.PERSONALITY, relation, null);
        else if (item.getType() == Item.FAQ)
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.FAQ, relation, null);
        else if (item.getType() == Item.DISCUSSION)
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DISCUSSION, relation, null);

        if (action != null) {
            if (!Misc.empty(name))
                action.setProperty(Decorator.PROPERTY_NAME, name);

            MonitorPool.scheduleMonitorAction(action);
        }
    }
}
