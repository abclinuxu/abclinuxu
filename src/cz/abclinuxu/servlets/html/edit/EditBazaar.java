/*
 *  Copyright (C) 2006 Leos Literak
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
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.ActionProtector;

import cz.abclinuxu.security.Permissions;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.htmlparser.util.ParserException;

/**
 * This class is used to add new advertisements, edit or delete them
 * within the bazaar.
 */
public class EditBazaar implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditBazaar.class);

    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_PRICE = "price";
    public static final String PARAM_CONTACT = "contact";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PREVIEW = "PREVIEW";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_REMOVE = "rm";
    public static final String ACTION_REMOVE_STEP2 = "rm2";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation != null) {
            Tools.sync(relation);
            env.put(VAR_RELATION, relation);
        }

        // check permissions
		Permissions permissions = Tools.permissionsFor(user, new Relation(Constants.REL_BAZAAR));
        if (user == null || !permissions.canCreate())
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (ACTION_ADD.equals(action))
            return FMTemplateSelector.select("EditBazaar", "add", env, request);

        if (ACTION_ADD_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditBazaar.class, true, true, true, false);
            return actionAddStep2(request, response, env, true);
        }

        Item ad = (Item) relation.getChild();

		permissions = Tools.permissionsFor(user, relation);

        if (ACTION_EDIT.equals(action)) {
			if (user.getId() != ad.getOwner() && !permissions.canModify())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            return actionEdit(request, env);
		}

        if (ACTION_EDIT_STEP2.equals(action)) {
			if (user.getId() != ad.getOwner() && !permissions.canModify())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            ActionProtector.ensureContract(request, EditBazaar.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

		if (user.getId() != ad.getOwner() && !permissions.canDelete())
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_REMOVE.equals(action))
            return FMTemplateSelector.select("EditBazaar", "remove", env, request);

        if (ACTION_REMOVE_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditBazaar.class, true, true, true, false);
            return actionRemoveStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Adds new advertisement to the database.
     * @return page to be rendered
     */
    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
		Relation parent = new Relation(Constants.REL_BAZAAR);

        Item item = new Item(0, Item.BAZAAR);
        Document document = DocumentHelper.createDocument();
        item.setData(document);

        boolean canContinue = setTitle(params, item, env);
        canContinue &= setContent(params, document, env);
        canContinue &= setType(params, item, env);
        canContinue &= setPrice(params, document, env);
        canContinue &= setContact(params, document, env);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            item.setCreated(new Date());
            item.setUpdated(new Date());
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditBazaar", "add", env, request);
        }

        User user = (User) env.get(Constants.VAR_USER);
        item.setOwner(user.getId());
        item.setCreated(new Date());

		Tools.sync(parent);

		Category cat = ((Category) parent.getChild());
		item.setGroup(cat.getGroup());
		item.setPermissions(cat.getPermissions());

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.create(item);
        versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

        Relation relation = new Relation(parent.getChild(), item, parent.getId());
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        TagTool.assignDetectedTags(item, user);
        EditDiscussion.createEmptyDiscussion(relation, user, persistence);

        FeedGenerator.updateBazaar();
        VariableFetcher.getInstance().refreshBazaar();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        } else
            env.put(VAR_RELATION, relation);

        return null;
    }

    /**
     * Fills environment with existing data of the advertisement.
     * @return template to be rendered.
     */
    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item ad = (Item) persistence.findById(relation.getChild());

        Document document = ad.getData();
        params.put(PARAM_TITLE, ad.getTitle());
        Node node = document.selectSingleNode("data/text");
        if (node != null)
            params.put(PARAM_TEXT, node.getText());
        node = document.selectSingleNode("data/price");
        if (node != null)
            params.put(PARAM_PRICE, node.getText());
        node = document.selectSingleNode("data/contact");
        if (node != null)
            params.put(PARAM_CONTACT, node.getText());
        params.put(PARAM_TYPE, ad.getSubType());

        return FMTemplateSelector.select("EditBazaar", "edit", env, request);
    }

    /**
     * Validates input values and if it is OK, that it updates the advertisement and displays it.
     * @return page to be rendered
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) persistence.findById(relation.getChild()).clone();
        Document document = item.getData();

        boolean canContinue = setTitle(params, item, env);
        canContinue &= setContent(params, document, env);
        canContinue &= setType(params, item, env);
        canContinue &= setPrice(params, document, env);
        canContinue &= setContact(params, document, env);
        String changesDescription = Misc.getRevisionString(params, env);
        canContinue &= !Constants.ERROR.equals(changesDescription);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditBazaar", "edit", env, request);
        }

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), changesDescription);

        FeedGenerator.updateBazaar();
        VariableFetcher.getInstance().refreshBazaar();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    protected String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);
        Item item = (Item) relation.getChild();

        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);

        if (item.getOwner() != user.getId())
            AdminLogger.logEvent(user, "  remove | bazar " + relation.getId());

        FeedGenerator.updateBazaar();
        VariableFetcher.getInstance().refreshBazaar();

        response.sendRedirect(response.encodeRedirectURL(UrlUtils.PREFIX_BAZAAR));
        return null;
    }

    /* ******* setters ********* */

    /**
     * Updates name of advertisement from parameters. Changes are not synchronized with persistence.
     * @param params   map holding request's parameters
     * @param item item to be updated
     * @param env      environment
     * @return false, if there is a major error.
     */
    private boolean setTitle(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_TITLE);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            if (tmp.indexOf("<") != -1) {
                params.put(PARAM_TITLE, "");
                ServletUtils.addError(PARAM_TITLE, "Použití HTML značek je zakázáno!", env, null);
                return false;
            }
            if (tmp.indexOf('\n') != -1)
                tmp = tmp.replace('\n', ' ');

            item.setTitle(tmp);
        } else {
            ServletUtils.addError(PARAM_TITLE, "Zadejte titulek inzerátu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates advertisement's content from parameters. Changes are not synchronized with persistence.
     * @param params   map holding request's parameters
     * @param document Document to be updated
     * @param env      environment
     * @return false, if there is a major error.
     */
    private boolean setContent(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_TEXT);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = HtmlPurifier.clean(tmp);
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '" + tmp + "'", e);
                ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, "data/text");
            tmp = Tools.processLocalLinks(tmp, null);
            element.setText(tmp);
        } else {
            ServletUtils.addError(PARAM_TEXT, "Zadejte obsah inzerátu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates advertisement's price from parameters. Changes are not synchronized with persistence.
     * @param params   map holding request's parameters
     * @param document Document to be updated
     * @param env      environment
     * @return false, if there is a major error.
     */
    private boolean setPrice(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_PRICE);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = HtmlPurifier.clean(tmp);
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '" + tmp + "'", e);
                ServletUtils.addError(PARAM_PRICE, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_PRICE, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, "data/price");
            element.setText(tmp);
        } else {
            Element element = (Element) document.selectSingleNode("/data/price");
            if (element != null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates advertisement's contact from parameters. Changes are not synchronized with persistence.
     * @param params   map holding request's parameters
     * @param document Document to be updated
     * @param env      environment
     * @return false, if there is a major error.
     */
    private boolean setContact(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_CONTACT);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = HtmlPurifier.clean(tmp);
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '" + tmp + "'", e);
                ServletUtils.addError(PARAM_CONTACT, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_CONTACT, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, "data/contact");
            element.setText(tmp);
        } else {
            Element element = (Element) document.selectSingleNode("/data/contact");
            if (element != null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates name of advertisement from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param ad item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setType(Map params, Item ad, Map env) {
        String tmp = (String) params.get(PARAM_TYPE), type = null;
        if (Constants.BAZAAR_BUY.equals(tmp))
            type = Constants.BAZAAR_BUY;
        if (Constants.BAZAAR_GIVE.equals(tmp))
            type = Constants.BAZAAR_GIVE;
        if (Constants.BAZAAR_SELL.equals(tmp))
            type = Constants.BAZAAR_SELL;
        if (type == null) {
            ServletUtils.addError(PARAM_TYPE, "Vyberte typ inzerátu!", env, null);
            return false;
        }
        ad.setSubType(type);
        return true;
    }
}
