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

import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.finesoft.socd.analyzer.DiacriticRemover;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class is responsible for adding and
 * editing of dictionary items and records.
 */
public class EditDictionary implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDictionary.class);

    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_RECORD = "RECORD";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        if ( action==null)
            throw new MissingArgumentException("Chybí parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            Tools.sync(relation);
            env.put(VAR_RELATION,relation);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_ADD) )
            return FMTemplateSelector.select("Dictionary", "add", env, request);

        if ( action.equals(ACTION_ADD_STEP2) )
            return actionAddStep2(request, response, env, true);

        if ( action.equals(ACTION_EDIT) )
            return actionEdit(request, env);

        if ( action.equals(ACTION_EDIT_STEP2) )
            return actionEdit2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Document documentItem = DocumentHelper.createDocument();
        Element root = documentItem.addElement("data");
        Item item = new Item(0, Item.DICTIONARY);
        item.setData(documentItem);
        item.setOwner(user.getId());
        Relation relation = new Relation(new Category(Constants.CAT_DICTIONARY), item, Constants.REL_DICTIONARY);

        boolean canContinue = true;
        canContinue &= setName(params, item, root, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setURL(relation, root, env);
        if (!canContinue || params.get(PARAM_PREVIEW)!=null)
            return FMTemplateSelector.select("Dictionary", "add", env, request);

        persistence.create(item);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        // commit new version
        Misc.commitRelation(root, relation, user);

        FeedGenerator.updateDictionary();
        VariableFetcher.getInstance().refreshDictionary();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, relation.getUrl());
        } else
            env.put(VAR_RELATION, relation);
        return null;
    }

    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild();
        Document document = item.getData();
        Node node = document.selectSingleNode("data/name");
        params.put(PARAM_NAME, node.getText());
        Node desc = document.selectSingleNode("data/description");
        params.put(PARAM_DESCRIPTION, desc.getText());

        return FMTemplateSelector.select("Dictionary","edit",env,request);
    }

    protected String actionEdit2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setName(params, item, root, env);
        canContinue &= setDescription(params, root, env);
        if ( !canContinue || params.get(PARAM_PREVIEW) != null)
            return FMTemplateSelector.select("Dictionary", "edit", env, request);

        item.setOwner(user.getId());
        persistence.update(item);

        // commit new version
        Misc.commitRelation(root, relation, user);

        // run monitor
        String url = "http://www.abclinuxu.cz"+relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.DICTIONARY, item, url);
        MonitorPool.scheduleMonitorAction(action);

        FeedGenerator.updateDictionary();
        VariableFetcher.getInstance().refreshDictionary();

        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Element root, Map env) {
        String name = (String) params.get(PARAM_NAME);
        name = Misc.filterDangerousCharacters(name);
        if ( name == null || name.length() == 0 ) {
            ServletUtils.addError(PARAM_NAME, "Nezadali jste jméno pojmu.", env, null);
            return false;
        }

        char first = Character.toLowerCase(name.charAt(0));
        if (first < 'a' || first > 'z') {
            ServletUtils.addError(PARAM_NAME, "Pojem musí začínat písmenem (a-z).", env, null);
            return false;
        }

        DocumentHelper.makeElement(root, "name").setText(name);

        String normalizedName = DiacriticRemover.getInstance().removeDiacritics(name);
        normalizedName = normalizedName.toLowerCase();
        item.setSubType(normalizedName); // used for SQL queries
        return true;
    }

    /**
     * Updates URL, checks for duplicates.
     * Changes are not synchronized with persistence.
     * @param root root element of item
     * @param relation relation
     * @return false, if there is a major error.
     */
    private boolean setURL(Relation relation,  Element root, Map env) {
        String name = root.elementText("name");
        name = Misc.filterDangerousCharacters(name);
        if (name == null)
            return false;

        String url = URLManager.enforceRelativeURL(name);
        Relation relation2 = SQLTool.getInstance().findRelationByURL(url);
        if (relation2 != null) {
            ServletUtils.addError(PARAM_NAME, "Tento pojem již byl <a href=\"/slovnik/"+url+"\">vysvětlen</a> " +
                    "nebo došlo v důsledku normalizace ke konfliktu URL (pak zvolte jiné jméno a kontaktuje adminy).", env, null);
            return false;
        }

        relation.setUrl(UrlUtils.PREFIX_DICTIONARY + "/" + url);
        return true;
    }

    /**
     * Updates explaination from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        tmp = Misc.filterDangerousCharacters(tmp);
        if ( tmp==null || tmp.length()==0 ) {
            ServletUtils.addError(PARAM_DESCRIPTION, "Nezadali jste popis tohoto pojmu.", env, null);
            return false;
        }

        try {
            SafeHTMLGuard.check(tmp);
        } catch (ParserException e) {
            log.error("ParseException on '"+tmp+"'", e);
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        }

        Element element = DocumentHelper.makeElement(root, "description");
        element.setText(tmp);
        Format format = FormatDetector.detect(tmp);
        element.addAttribute("format", Integer.toString(format.getId()));

        return true;
    }
}
