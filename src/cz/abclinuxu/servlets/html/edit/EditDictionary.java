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
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.ActionProtector;
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

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        if ( action==null)
            throw new MissingArgumentException("Chybí parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            Tools.sync(relation);
            env.put(VAR_RELATION,relation);
        } else
			relation = new Relation(Constants.REL_DICTIONARY); // for perm. checks only

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_ADD) ) {
			if (!Tools.permissionsFor(user, relation).canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
			
            return FMTemplateSelector.select("Dictionary", "add", env, request);
		}

        if ( action.equals(ACTION_ADD_STEP2) ) {
			if (!Tools.permissionsFor(user, relation).canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
			
            ActionProtector.ensureContract(request, EditDictionary.class, true, false, true, false);
            return actionAddStep2(request, response, env, true);
        }
		
		if (!Tools.permissionsFor(user, relation).canModify())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_EDIT) )
            return actionEdit(request, env);

        if ( action.equals(ACTION_EDIT_STEP2) ) {
            ActionProtector.ensureContract(request, EditDictionary.class, true, true, true, false);
            return actionEdit2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
		Relation parent = new Relation(Constants.REL_DICTIONARY);
		
		Tools.sync(parent);

        Document documentItem = DocumentHelper.createDocument();
        Element root = documentItem.addElement("data");
        Item item = new Item(0, Item.DICTIONARY);
        item.setData(documentItem);
        item.setOwner(user.getId());
		item.setGroup( ((Category) parent.getChild()).getGroup() );
		
        Relation relation = new Relation(parent.getChild(), item, parent.getId());

        boolean canContinue = true;
        canContinue &= setName(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setURL(relation, item, env);
        if (!canContinue || params.get(PARAM_PREVIEW)!=null)
            return FMTemplateSelector.select("Dictionary", "add", env, request);

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.create(item);
        versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

        persistence.create(relation);
        relation.getParent().addChildRelation(relation);
        TagTool.assignDetectedTags(item, user);

        // run monitor
        String url = "http://www.abclinuxu.cz"+relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.DICTIONARY, relation, url);
        MonitorPool.scheduleMonitorAction(action);

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
        params.put(PARAM_NAME, item.getTitle());
        Node desc = document.selectSingleNode("data/description");
        params.put(PARAM_DESCRIPTION, desc.getText());

        return FMTemplateSelector.select("Dictionary","edit",env,request);
    }

    protected String actionEdit2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Item origItem = (Item) item.clone();
        item.setOwner(user.getId());
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setName(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= ServletUtils.checkNoChange(item, origItem, env);
        String changesDescription = Misc.getRevisionString(params, env);
        canContinue &= !Constants.ERROR.equals(changesDescription);

        if ( !canContinue || params.get(PARAM_PREVIEW) != null)
            return FMTemplateSelector.select("Dictionary", "edit", env, request);

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), changesDescription);

        // run monitor
        String url = "http://www.abclinuxu.cz"+relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.DICTIONARY, relation, url);
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
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Map env) {
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

        item.setTitle(name);

        String normalizedName = DiacriticRemover.getInstance().removeDiacritics(name);
        normalizedName = normalizedName.toLowerCase();
        item.setSubType(normalizedName); // used for SQL queries
        return true;
    }

    /**
     * Updates URL, checks for duplicates.
     * Changes are not synchronized with persistence.
     * @param item item to be updated
     * @param relation relation
     * @return false, if there is a major error.
     */
    private boolean setURL(Relation relation,  Item item, Map env) {
        String name = item.getTitle();
        name = Misc.filterDangerousCharacters(name);
        if (name == null)
            return false;

        // TODO dava tato kontrola smysl? Spise by se melo kontrolovat url /slovnik/jmeno-pojmu , ne?
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
        tmp = Tools.processLocalLinks(tmp, null);
        element.setText(tmp);
        Format format = FormatDetector.detect(tmp);
        element.addAttribute("format", Integer.toString(format.getId()));

        return true;
    }
}
