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

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.*;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.safehtml.ContentGuard;
import cz.abclinuxu.utils.email.monitor.*;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;

/**
 * Used to add/edit static content
 */
public class EditContent implements AbcAction {
    static Logger log = Logger.getLogger(EditContent.class);

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_URL = "url";
    public static final String PARAM_CLASS = "java_class";
    public static final String PARAM_EXECUTE_AS_TEMPLATE = "execute";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_START_TIME = "startTime";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_START_TIME = "START_TIME";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_ADD_DERIVED_PAGE = "addDerivedPage";
    public static final String ACTION_ADD_DERIVED_PAGE_STEP2 = "addDerivedPage2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_EDIT_PUBLIC_CONTENT = "editPublicContent";
    public static final String ACTION_EDIT_PUBLIC_CONTENT_STEP2 = "editPublicContent2";
    public static final String ACTION_ALTER_PUBLIC = "alterPublic";

    /** item subtype that means that content can be edited by any logged user */
    public static final String TYPE_PUBLIC_CONTENT = "public";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation != null) {
            Persistence persistence = PersistenceFactory.getPersistance();
            relation = (Relation) persistence.findById(relation);
            persistence.synchronize(relation.getChild());
            env.put(VAR_RELATION, relation);
        }

        boolean manager = user.hasRole(Roles.CONTENT_ADMIN);
        boolean canDerive = user.hasRole(Roles.CAN_DERIVE_CONTENT);
        boolean publicContent = false;
        if (relation.getChild() instanceof Item)
            publicContent = TYPE_PUBLIC_CONTENT.equals(((Item) relation.getChild()).getSubType());

        if (ACTION_ADD_DERIVED_PAGE.equals(action)) {
            if (manager || (publicContent && canDerive))
                return actionAddDerivedPage(request, env);
            else
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        }

        if (ACTION_ADD_DERIVED_PAGE_STEP2.equals(action)) {
            if (manager || (publicContent && canDerive))
                return actionAddDerivedPageStep2(request, response, env);
            else
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        }

        if (action.equals(ACTION_EDIT_PUBLIC_CONTENT)) {
            if (manager || publicContent)
                return actionEditPublicContent(request, env);
            else
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        }

        if (action.equals(ACTION_EDIT_PUBLIC_CONTENT_STEP2)) {
            if (manager || publicContent)
                return actionEditPublicContent2(request, response, env);
            else
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        }

        if ( !manager )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD.equals(action) )
            return FMTemplateSelector.select("EditContent", "add", env, request);

        if ( action.equals(ACTION_ADD_STEP2) )
            return actionAddStep2(request, response, env);

        if ( action.equals(ACTION_EDIT) )
            return actionEditItem(request, env);

        if ( action.equals(ACTION_EDIT_STEP2) )
            return actionEditItem2(request, response, env);

        if ( action.equals(ACTION_ALTER_PUBLIC) )
            return actionAlterPublic(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation parentRelation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item(0, Item.CONTENT);
        Document document = DocumentHelper.createDocument();
        item.setData(document);
        item.setOwner(user.getId());

        Relation relation = new Relation();
        relation.setParent(parentRelation.getChild());
        relation.setUpper(parentRelation.getId());

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setContent(params, item, env);
        canContinue &= setURL(params, relation, env);
        canContinue &= setClass(params, item);

        if ( !canContinue  || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditContent", "add", env, request);
        }

        persistence.create(item);
        relation.setChild(item);
        persistence.create(relation);

        // commit new version
        Misc.commitRelation(document.getRootElement(), relation, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    protected String actionAddDerivedPage(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("EditContent", "addDerived", env, request);
    }

    protected String actionAddDerivedPageStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation parentRelation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item(0, Item.CONTENT);
        Document document = DocumentHelper.createDocument();
        item.setData(document);
        item.setOwner(user.getId());

        GenericObject parent = parentRelation.getChild();
        Relation relation = new Relation();
        relation.setParent(parent);
        relation.setUpper(parentRelation.getId());
        Item toc = null;

        if (parent instanceof Item) {
            Item parentItem = ((Item)parent);
            if (parentItem.getType()==Item.CONTENT) {
                if (TYPE_PUBLIC_CONTENT.equals(parentItem.getSubType()))
                    item.setSubType(TYPE_PUBLIC_CONTENT);

                Element element = (Element) parentItem.getData().selectSingleNode("/data/toc");
                if (element!=null) {
                    int id = Misc.parseInt(element.getText(), -1);
                    toc = (Item) persistence.findById(new Item(id));
                    DocumentHelper.makeElement(item.getData(), "/data/toc").setText(element.getText());
                }
            }
        }

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setContent(params, item, env);
        canContinue &= setDerivedURL(item, relation, parentRelation);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditContent", "addDerived", env, request);
        }

        persistence.create(item);
        relation.setChild(item);
        persistence.create(relation);

        // commit new version
        Misc.commitRelation(document.getRootElement(), relation, user);

        if (toc!=null) {
            Element element = (Element) toc.getData().selectSingleNode("//node[@rid="+parentRelation.getId()+"]");
            if (element!=null) {
                element.addElement("node").addAttribute("rid", Integer.toString(relation.getId()));
                persistence.update(toc);
            }
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    protected String actionEditPublicContent(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        Document document = item.getData();
        Element element = (Element) document.selectSingleNode("/data/name");
        params.put(PARAM_TITLE, element.getText());
        element = (Element) document.selectSingleNode("/data/content");
        params.put(PARAM_CONTENT, element.getText());

        env.put(VAR_START_TIME, new Long(System.currentTimeMillis()));
        return FMTemplateSelector.select("EditContent", "editPublic", env, request);
    }

    protected String actionEditPublicContent2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setContent(params, item, env);
        canContinue &= checkStartTime(params, item, env);
        item.setOwner(user.getId());

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditContent", "editPublic", env, request);
        }

        persistence.update(item);
        persistence.update(relation);

        // commit new version
        Misc.commitRelation(item.getData().getRootElement(), relation, user);

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz" + relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.CONTENT, item, absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    protected String actionEditItem(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        Document document = item.getData();
        Element element = (Element) document.selectSingleNode("/data/name");
        params.put(PARAM_TITLE, element.getText());
        element = (Element) document.selectSingleNode("/data/content");
    	if (element != null) {
            params.put(PARAM_CONTENT, element.getText());
            params.put(PARAM_EXECUTE_AS_TEMPLATE, element.attributeValue("execute"));
        }
        element = (Element) document.selectSingleNode("/data/java_class");
        if (element!=null)
            params.put(PARAM_CLASS, element.getText());
        params.put(PARAM_URL, relation.getUrl());

        env.put(VAR_START_TIME, System.currentTimeMillis());
        return FMTemplateSelector.select("EditContent", "edit", env, request);
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setContent(params, item, env);
        canContinue &= setURL(params, relation, env);
        canContinue &= setClass(params, item);
        canContinue &= checkStartTime(params, item, env);
        item.setOwner(user.getId());

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditContent", "edit", env, request);
        }

        persistence.update(item);
        persistence.update(relation);

        // commit new version
        Misc.commitRelation(item.getData().getRootElement(), relation, user);

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz" + relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.CONTENT, item, absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    /**
     * Reverts public flag state for this document.
     */
    protected String actionAlterPublic(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item content = (Item) persistence.findById(relation.getChild());

        String subType = content.getSubType();
        if (TYPE_PUBLIC_CONTENT.equals(subType))
            content.setSubType(null);
        else if (subType==null)
            content.setSubType(TYPE_PUBLIC_CONTENT);

        persistence.update(content);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    // setters


    /**
     * Updates title from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setTitle(Map params, Item item, Map env) {
        String name = (String) params.get(PARAM_TITLE);
        name = Misc.filterDangerousCharacters(name);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_TITLE, "Vyplňte titulek stránky!", env, null);
            return false;
        }
        Element element = DocumentHelper.makeElement(item.getData(), "/data/name");
        element.setText(name);
        return true;
    }

    /**
     * Updates content from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item article  to be updated
     * @return false, if there is a major error.
     */
    private boolean setContent(Map params, Item item, Map env) {
        String content = (String) params.get(PARAM_CONTENT);
        content = Misc.filterDangerousCharacters(content);
        String exec = (String) params.get(PARAM_EXECUTE_AS_TEMPLATE);

        if ( content==null || content.length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT, "Vyplňte obsah stránky!", env, null);
            return false;
        }

        try {
            ContentGuard.check(content);
        } catch (ParserException e) {
            log.error("ParseException on '" + content + "'", e);
            ServletUtils.addError(PARAM_CONTENT, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_CONTENT, e.getMessage(), env, null);
            return false;
        }

        Element element = DocumentHelper.makeElement(item.getData(), "/data/content");
        element.setText(content);
        if (! "yes".equals(exec))
            exec = "no";
        element.addAttribute("execute", exec);
        return true;
    }

    private boolean setDerivedURL(Item item, Relation relation, Relation parentRelation) {
        Element element = (Element) item.getData().selectSingleNode("/data/name");
        String title = element.getTextTrim();
        String url = parentRelation.getUrl() + "/" + URLManager.enforceRelativeURL(title);
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);
        return true;
    }

    /**
     * Updates url from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param relation relation to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setURL(Map params, Relation relation, Map env) {
        String url = (String) params.get(PARAM_URL);
        try {
            url = URLManager.enforceAbsoluteURL(url);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_URL, e.getMessage(), env, null);
            return false;
        }
        relation.setUrl(url);

        if (!URLManager.isURLUnique(relation.getUrl(), relation.getId())) {
            ServletUtils.addError(PARAM_URL, "Tato adresa je již použita!", env, null);
            return false;
        }

        return true;
    }

    /**
     * Verifies that none has modified document since start of editing.
     * @return true if document was not modified
     */
    private boolean checkStartTime(Map params, Item item, Map env) {
        String s = (String) params.get(PARAM_START_TIME);
        long startTime = Long.parseLong(s);
        long lastModified = item.getUpdated().getTime();
        if (lastModified>startTime) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Systém detekoval souběžnou editaci tohoto dokumentu. " +
                    "Někdo upravil dokument poté, co jste jej začal(a) editovat. Není možné pokračovat. " +
                    "Prosím vraťte se zpět na dokument, znovu jej načtěte a pak teprve pokračujte ve vašich úpravách.", env, null);
            return false;
        }

        return true;
    }

    /**
     * Updates title from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @return false, if there is a major error.
     */
    private boolean setClass(Map params, Item item) {
        Element element = (Element) item.getData().selectSingleNode("/data/java_class");
        if ( element!=null )
            element.detach();

        String clazz = (String) params.get(PARAM_CLASS);
        if ( clazz==null || clazz.length()==0 )
            return true;

        element = DocumentHelper.makeElement(item.getData(), "/data/java_class");
        element.setText(clazz);
        return true;
    }
}
