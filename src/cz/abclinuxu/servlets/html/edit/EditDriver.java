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
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.data.view.DriverCategories;

import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.util.List;

/**
 * Cotroller for working drivers manipulation.
 */
public class EditDriver implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDriver.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_VERSION = "version";
    public static final String PARAM_URL = "url";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_CATEGORY = "category";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_CATEGORY = "CATEGORY";
    public static final String VAR_CATEGORIES = "CATEGORIES";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            persistence.synchronize(relation);
            env.put(VAR_RELATION,relation);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_ADD.equals(action) ) {
			if (!Tools.permissionsFor(user, new Relation(Constants.REL_DRIVERS)).canCreate())
				return FMTemplateSelector.select("ViewUser", "login", env, request);

            return actionAddStep1(request, env);
		}

        if ( ACTION_ADD_STEP2.equals(action) ) {
			if (!Tools.permissionsFor(user, new Relation(Constants.REL_DRIVERS)).canCreate())
				return FMTemplateSelector.select("ViewUser", "login", env, request);

            ActionProtector.ensureContract(request, EditDriver.class, true, true, true, false);
            return actionAddStep2(request, response, env, true);
        }

		if (!Tools.permissionsFor(user, relation).canModify())
			return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_EDIT.equals(action) )
            return actionEdit(request, env);

        if ( ACTION_EDIT_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditDriver.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Fills environment with categories data.
     * @return template to be rendered.
     */
    private String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        env.put(VAR_CATEGORIES, DriverCategories.getAllCategories());
        return FMTemplateSelector.select("EditDriver", "add", env, request);
    }

    /**
     * Adds new driver to the database.
     * @return page to be rendered
     */
    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
		Relation parent = new Relation(Constants.REL_DRIVERS);

        Item item = new Item(0, Item.DRIVER);
        Document document = DocumentHelper.createDocument();
        item.setData(document);

		Tools.sync(parent);

        boolean canContinue = true;
        canContinue &= setName(params, item, env);
        canContinue &= setVersion(params, document, env);
        canContinue &= setURL(params, document, env);
        canContinue &= setNote(params, document, env);
        canContinue &= setCategory(params, item, env);

        if ( !canContinue || params.get(PARAM_PREVIEW) != null )
            return redisplayForm("add", request, params, env);

        User user = (User) env.get(Constants.VAR_USER);
        item.setOwner(user.getId());

		Category cat = (Category) parent.getChild();
		item.setGroup(cat.getGroup());
		item.setPermissions(cat.getPermissions());

        item.setCreated(new Date());

        String title = item.getTitle();
        String url = UrlUtils.PREFIX_DRIVERS + "/" + URLManager.enforceRelativeURL(title);
        url = URLManager.protectFromDuplicates(url);

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.create(item);
        versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

        Relation relation = new Relation(parent.getChild(), item, parent.getId());
        relation.setUrl(url);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);
        TagTool.assignDetectedTags(item, user);

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz"+url;
        MonitorAction action = new MonitorAction(user,UserAction.ADD,ObjectType.DRIVER,relation,absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        FeedGenerator.updateDrivers();
        VariableFetcher.getInstance().refreshDrivers();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, url);
        } else
            env.put(VAR_RELATION, relation);

        return null;
    }

    /**
     * Fills environment with existing data of the driver.
     * @return template to be rendered.
     */
    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item driver = (Item) persistence.findById(relation.getChild());

        Document document = driver.getData();
        params.put(PARAM_NAME, driver.getTitle());
        Node node = document.selectSingleNode("data/version");
        if (node != null)
            params.put(PARAM_VERSION, node.getText());
        node = document.selectSingleNode("data/url");
        if (node != null)
            params.put(PARAM_URL, node.getText());
        node = document.selectSingleNode("data/note");
        if (node != null)
            params.put(PARAM_NOTE, node.getText());

        params.put(PARAM_CATEGORY, driver.getSubType());
        env.put(VAR_CATEGORIES, DriverCategories.getAllCategories());

        return FMTemplateSelector.select("EditDriver","edit",env,request);
    }

    /**
     * Validates input values and if it is OK, that it updates the driver and displays it.
     * @return page to be rendered
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) persistence.findById(relation.getChild()).clone();
        Item origItem = (Item) item.clone();
        item.setOwner(user.getId());
        Document document = item.getData();

        boolean canContinue = true;
        canContinue &= setName(params, item, env);
        canContinue &= setVersion(params, document, env);
        canContinue &= setURL(params, document, env);
        canContinue &= setNote(params, document, env);
        canContinue &= setCategory(params, item, env);
        canContinue &= ServletUtils.checkNoChange(item, origItem, env);
        String changesDescription = Misc.getRevisionString(params, env);
        canContinue &= !Constants.ERROR.equals(changesDescription);

        if (! canContinue || params.get(PARAM_PREVIEW) != null)
            return redisplayForm("edit", request, params, env);

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), changesDescription);

        String url = relation.getUrl();
        if (url == null)
            url = UrlUtils.PREFIX_DRIVERS + "/show/" + relation.getId();

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz"+url;
        MonitorAction action = new MonitorAction(user,UserAction.EDIT,ObjectType.DRIVER,relation,absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        FeedGenerator.updateDrivers();
        VariableFetcher.getInstance().refreshDrivers();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    /* ******* setters ********* */

    private String redisplayForm(String action, HttpServletRequest request, Map params, Map env) {
        env.put(VAR_CATEGORY, DriverCategories.get((String) params.get(PARAM_CATEGORY)));
        env.put(VAR_CATEGORIES, DriverCategories.getAllCategories());
        return FMTemplateSelector.select("EditDriver", action, env, request);
    }

    /**
     * Updates name of driver from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item driver to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        tmp = Misc.filterDangerousCharacters(tmp);
        if ( tmp!=null && tmp.length()>0 ) {
            if (tmp.indexOf('<') != -1) {
               ServletUtils.addError(PARAM_NAME, "HTML zde není povoleno.", env, null);
               return false;
            }
            item.setTitle(tmp);
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte název ovladače!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates driver's version from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setVersion(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_VERSION);
        tmp = Misc.filterDangerousCharacters(tmp);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(document, "data/version").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_VERSION, "Zadejte verzi ovladače!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates driver's URL from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setURL(Map params, Document document, Map env) {
        String url = (String) params.get(PARAM_URL);
        if ( url==null || url.length()==0 ) {
            ServletUtils.addError(PARAM_URL, "Zadejte adresu ovladače!", env, null);
            return false;
        } else if ( url.indexOf("://")==-1 || url.length()<12 ) {
            ServletUtils.addError(PARAM_URL, "Neplatná adresa ovladače!", env, null);
            return false;
        }
        DocumentHelper.makeElement(document, "data/url").setText(url);
        return true;
    }

    /**
     * Updates driver's note from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setNote(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_NOTE);
        tmp = Misc.filterDangerousCharacters(tmp);
        if ( tmp!=null && tmp.length()>0 ) {
            try {
                tmp = HtmlPurifier.clean(tmp);
                HtmlChecker.check(Rules.DEFAULT, tmp);
            } catch (Exception e) {
                ServletUtils.addError(PARAM_NOTE, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, "data/note");
            tmp = Tools.processLocalLinks(tmp, null);
            element.setText(tmp);
        } else {
            ServletUtils.addError(PARAM_NOTE, "Zadejte poznámku!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates driver's category from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item Item to be updated
     * @return false, if there is a major error.
     */
    private boolean setCategory(Map params, Item item, Map env) {
        String text = (String) params.get(PARAM_CATEGORY);
        if (text == null || text.length() == 0) {
            ServletUtils.addError(PARAM_CATEGORY, "Vyberte kategorii!", env, null);
            return false;
        }

        List categories = DriverCategories.listKeys();
        if ( categories.contains(text) ) {
            item.setSubType(text);
            return true;
        } else {
            ServletUtils.addError(PARAM_CATEGORY, "Nalezena neznámá kategorie '"+text+"'!", env, null);
            return false;
        }
    }
}
