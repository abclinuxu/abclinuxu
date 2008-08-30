/*
 *  Copyright (C) 2007 Leos Literak
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
import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class is responsible for adding and
 * editing of personality items and records.
 */
public class EditPersonality implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditPersonality.class);

    public static final String PARAM_RELATION_ID = "rid";
    public static final String PARAM_FIRSTNAME = "firstname";
    public static final String PARAM_SURNAME = "surname";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_BIRTH_DATE = "birthDate";
    public static final String PARAM_DEATH_DATE = "deathDate";
    public static final String PARAM_WEB_URL = "webUrl";
    public static final String PARAM_RSS_URL = "rssUrl";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_EDIT_MODE = "EDIT_MODE";
    public static final String VAR_PREVIEW = "PREVIEW";

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

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID, Relation.class, params, request);
        if ( relation!=null ) {
            Tools.sync(relation);
            env.put(VAR_RELATION,relation);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_ADD) ) {
			if (!Tools.permissionsFor(user, new Relation(Constants.REL_PERSONALITIES)).canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
			
            return FMTemplateSelector.select("EditPersonality", "add", env, request);
		}

        if ( action.equals(ACTION_ADD_STEP2) ) {
			if (!Tools.permissionsFor(user, new Relation(Constants.REL_PERSONALITIES)).canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
			
            ActionProtector.ensureContract(request, EditPersonality.class, true, false, true, false);
            return actionAddStep2(request, response, env, true);
        }
		
		if (!Tools.permissionsFor(user, relation).canModify())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_EDIT) )
            return actionEdit(request, env);

        if ( action.equals(ACTION_EDIT_STEP2) ) {
            ActionProtector.ensureContract(request, EditPersonality.class, true, true, true, false);
            return actionEdit2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
		Relation parent = new Relation(Constants.REL_PERSONALITIES);
		
		Tools.sync(parent);

        Document documentItem = DocumentHelper.createDocument();
        Element root = documentItem.addElement("data");
        Item item = new Item(0, Item.PERSONALITY);
        item.setData(documentItem);
        item.setOwner(user.getId());
		
		Category cat = (Category) parent.getChild();
		item.setGroup(cat.getGroup());
		item.setPermissions(cat.getPermissions());
		
        Relation relation = new Relation(parent.getChild(), item, parent.getId());

        boolean canContinue = true;
        canContinue &= setFirstname(params, root, env);
        canContinue &= setSurname(params, item, root, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setURL(relation, root, env); // local URL
        canContinue &= setWebUrl(params, root, env); // "more information" website (e.g. Wikipedia)
        canContinue &= setBirthDate(params, root);
        canContinue &= setDeathDate(params, root);
        canContinue &= checkRssUrl(params, env);
        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            env.put(VAR_PREVIEW, item);
            item.setInitialized(true);
            return FMTemplateSelector.select("EditPersonality", "add", env, request);
        }

        item.setTitle(Tools.getPersonName(item));

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.create(item);
        versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

        persistence.create(relation);
        relation.getParent().addChildRelation(relation);
        TagTool.assignDetectedTags(item, user);

        // todo prvni revize nebude obsahovat RSS v tabulce verze
        setRssUrl(params, item, relation.getId());
        persistence.update(item);

        // run monitor
        String url = "http://www.abclinuxu.cz"+relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.PERSONALITY, relation, url);
        MonitorPool.scheduleMonitorAction(action);

        FeedGenerator.updatePersonalities();
        VariableFetcher.getInstance().refreshPersonalities();

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
        Node node = document.selectSingleNode("data/firstname");

        if (node != null)
            params.put(PARAM_FIRSTNAME, node.getText());

        node = document.selectSingleNode("data/surname");
        if (node != null)
            params.put(PARAM_SURNAME, node.getText());

        node = document.selectSingleNode("data/description");
        if (node != null)
            params.put(PARAM_DESCRIPTION, node.getText());

        node = document.selectSingleNode("/data/url[@useType='info']");
        if (node != null)
            params.put(PARAM_WEB_URL, node.getText());
        node = document.selectSingleNode("/data/url[@useType='rss']");
        if (node != null)
            params.put(PARAM_RSS_URL, node.getText());

        node = document.selectSingleNode("/data/date[@type='birth']");
        if (node != null)
            params.put(PARAM_BIRTH_DATE, node.getText());
        node = document.selectSingleNode("/data/date[@type='death']");
        if (node != null)
            params.put(PARAM_DEATH_DATE, node.getText());

        env.put(VAR_EDIT_MODE, Boolean.TRUE);
        return FMTemplateSelector.select("EditPersonality","edit",env,request);
    }

    protected String actionEdit2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        env.put(VAR_EDIT_MODE, Boolean.TRUE);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Item origItem = (Item) item.clone();
        item.setOwner(user.getId());
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setFirstname(params, root, env);
        canContinue &= setSurname(params, item, root, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setWebUrl(params, root, env); // "more information" website (e.g. Wikipedia)
        canContinue &= setBirthDate(params, root);
        canContinue &= setDeathDate(params, root);
        canContinue &= checkRssUrl(params, env);
        setRssUrl(params, item, relation.getId());
        canContinue &= ServletUtils.checkNoChange(item, origItem, env);
        String changesDescription = Misc.getRevisionString(params, env);
        canContinue &= !Constants.ERROR.equals(changesDescription);

        if ( !canContinue || params.get(PARAM_PREVIEW) != null) {
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditPersonality", "edit", env, request);
        }

        item.setTitle(Tools.getPersonName(item));

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), changesDescription);

        // run monitor
        String url = "http://www.abclinuxu.cz"+relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.PERSONALITY, relation, url);
        MonitorPool.scheduleMonitorAction(action);

        FeedGenerator.updatePersonalities();
        VariableFetcher.getInstance().refreshPersonalities();

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
    private boolean setFirstname(Map params, Element root, Map env) {
        String name = (String) params.get(PARAM_FIRSTNAME);

        name = Misc.filterDangerousCharacters(name);
        if ( name == null || name.length() == 0 ) {
            ServletUtils.addError(PARAM_FIRSTNAME, "Nezadali jste jméno osobnosti.", env, null);
            return false;
        }

        String normalizedName = DiacriticRemover.getInstance().removeDiacritics(name);

        char first = Character.toLowerCase(normalizedName.charAt(0));
        if (first < 'a' || first > 'z') {
            ServletUtils.addError(PARAM_FIRSTNAME, "Jméno musí začínat písmenem.", env, null);
            return false;
        }

        DocumentHelper.makeElement(root, "firstname").setText(name);
        return true;
    }

    /**
     * Updates surname from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item Item to be updated
     * @param root root element of item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setSurname(Map params, Item item, Element root, Map env) {
        String surname = (String) params.get(PARAM_SURNAME);

        surname = Misc.filterDangerousCharacters(surname);
        if ( surname == null || surname.length() == 0 ) {
            ServletUtils.addError(PARAM_SURNAME, "Nezadali jste příjmení osobnosti.", env, null);
            return false;
        }

        String normalizedSurname = DiacriticRemover.getInstance().removeDiacritics(surname);
        if (normalizedSurname.length() == 0) {
            ServletUtils.addError(PARAM_SURNAME, "Příjmení musí začínat písmenem.", env, null);
            return false;
        }

        char first = Character.toLowerCase(normalizedSurname.charAt(0));
        if (first < 'a' || first > 'z') {
            ServletUtils.addError(PARAM_SURNAME, "Příjmení musí začínat písmenem.", env, null);
            return false;
        }

        DocumentHelper.makeElement(root, "surname").setText(surname);

        normalizedSurname = normalizedSurname.toLowerCase();
        item.setSubType(normalizedSurname); // used for SQL queries
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
        String name = root.elementText("firstname") + " " + root.elementText("surname");
        name = Misc.filterDangerousCharacters(name);
        if (name == null)
            return false;

        String url = URLManager.enforceRelativeURL(name);
        Relation relation2 = SQLTool.getInstance().findRelationByURL(url);
        if (relation2 != null) {
            ServletUtils.addError(PARAM_FIRSTNAME, "Tato osobnost <a href=\"/kdo-je/"+url+"\">již existuje</a>.", env, null);
            return false;
        }

        relation.setUrl(UrlUtils.PREFIX_PERSONALITIES + "/" + url);
        return true;
    }

    /**
     * Updates personality's profile from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp == null || tmp.length() == 0) {
            ServletUtils.addError(PARAM_DESCRIPTION, "Zadejte popis!", env, null);
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

    /**
     * Performs check, that RSS URL is valid.
     * @param params map holding request's parameters
     * @return false, if there is a major error
     */
    private boolean checkRssUrl(Map params, Map env) {
        String url = (String) params.get(PARAM_RSS_URL);
        if (Misc.empty(url))
            return true;

        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            ServletUtils.addError(PARAM_RSS_URL, "Zadejte prosím platné URL.", env, null);
            return false;
        }
    }

    /**
     * Sets RSS URL for a personality item. The URL is copied to dynamic configuration
     * item too, so RSS fetcher is aware of this URL and it can fetch its links.
     * The method must not be called in preview mode and the relationId must not be zero
     * (e.g. the item is already created).
     * @param params map holding request's parameters
     * @param item item to be updated
     * @param relationId id of relation for this item
     */
    private void setRssUrl(Map params, Item item, int relationId) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Item dynamicConfig = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
        dynamicConfig = (Item) dynamicConfig.clone();
        Element configRoot = dynamicConfig.getData().getRootElement();
        Element configRss = (Element) configRoot.selectSingleNode("/data/feeds/feed[@relation='" + relationId + "']");
        Element itemRoot = (Element) item.getData().getRootElement();
        Element rssElement = (Element) itemRoot.selectSingleNode("/data/url[@useType='rss']");

        String url = (String) params.get(PARAM_RSS_URL);
        if (! Misc.empty(url)) {
            if (configRss == null) {
                Element configFeeds = DocumentHelper.makeElement(configRoot, "feeds");
                configRss = configFeeds.addElement("feed");
                configRss.addAttribute("type", "personality");
                configRss.addAttribute("relation", String.valueOf(relationId));
            }
            configRss.setText(url);

            if (rssElement == null) {
                rssElement = itemRoot.addElement("url");
                rssElement.addAttribute("useType", "rss");
            }
            rssElement.setText(url);
        } else {
            if (configRss != null)
                configRss.detach();
            if (rssElement != null)
                rssElement.detach();
        }

        persistence.update(dynamicConfig);
    }

    /**
     * Sets info URL for a personality item.
     * @param params map holding request's parameters
     * @param env environment
     */
    private boolean setWebUrl(Map params, Element itemRoot, Map env) {
        String url = (String) params.get(PARAM_WEB_URL);
        Element urlElement = (Element) itemRoot.selectSingleNode("/data/url[@useType='info']");

        if (!Misc.empty(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                ServletUtils.addError(PARAM_WEB_URL, "Zadejte prosím platné URL.", env, null);
                return false;
            }

            if (urlElement == null) {
                urlElement = itemRoot.addElement("url");
                urlElement.addAttribute("useType", "info");
            }
            urlElement.setText(url);
        } else if (urlElement != null)
                urlElement.detach();

        return true;
    }

    /**
     * Sets birth date for a personality item.
     * @param params map holding request's parameters
     */
    private boolean setBirthDate(Map params, Element itemRoot) {
        String date = (String) params.get(PARAM_BIRTH_DATE);
        Element dateElement = (Element) itemRoot.selectSingleNode("/data/date[@type='birth']");

        if (!Misc.empty(date)) {
            if (dateElement == null) {
                dateElement = itemRoot.addElement("date");
                dateElement.addAttribute("type", "birth");
            }
            dateElement.setText(date);
        } else if (dateElement != null)
            dateElement.detach();

        return true;
    }

    /**
     * Sets death date for a personality item.
     * @param params map holding request's parameters
     */
    private boolean setDeathDate(Map params, Element itemRoot) {
        String date = (String) params.get(PARAM_DEATH_DATE);
        Element dateElement = (Element) itemRoot.selectSingleNode("/data/date[@type='death']");

        if (!Misc.empty(date)) {
            if (dateElement == null) {
                dateElement = itemRoot.addElement("date");
                dateElement.addAttribute("type", "death");
            }
            dateElement.setText(date);
        } else if (dateElement != null)
            dateElement.detach();

        return true;
    }
}
