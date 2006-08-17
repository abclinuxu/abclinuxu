/*
 *  Copyright (C) 2006 Yin, Leos Literak
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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.security.Roles;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This class is responsible for adding and
 * editing of software items and records.
 */
public class EditSoftware implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditSoftware.class);

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_HOME_PAGE = "homeUrl";
    public static final String PARAM_DOWNLOAD_URL = "downloadUrl";
    public static final String PARAM_RSS_URL = "rssUrl";
    public static final String PARAM_USER_INTERFACE = "ui";
    public static final String PARAM_ALTERNATIVES = "alternative";
    public static final String PARAM_LICENSES = "license";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_EDIT_MODE = "EDIT_MODE";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation != null) {
            Tools.sync(relation);
            env.put(VAR_RELATION, relation);
        } else
            throw new MissingArgumentException("Chybí parametr relationId!");

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        // temporary: check permissions TODO remove when released officially
        if (!user.hasRole(Roles.SOFTWARE_ADMIN))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_ADD))
            return actionAddStep1(request, response, env);

        if (action.equals(ACTION_ADD_STEP2))
            return actionAddStep2(request, response, env, true);

        if (action.equals(ACTION_EDIT))
            return actionEditStep1(request, env);

        if (action.equals(ACTION_EDIT_STEP2))
            return actionEditStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    public String actionAddStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation upper = (Relation) env.get(VAR_RELATION);
        if (upper.getUrl() == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chyba - sekce nemá textové URL. Kontaktujte prosím administrátora.",
                                  env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(upper, true));
        }

        return FMTemplateSelector.select("EditSoftware", "add", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.SOFTWARE);
        item.setData(document);
        item.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setName(params, root, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setUserInterface(params, item);
        canContinue &= setApplicationAlternatives(params, item, env);
        canContinue &= setLicenses(params, item);
        canContinue &= setUrl(params, PARAM_HOME_PAGE, root);
        canContinue &= setUrl(params, PARAM_DOWNLOAD_URL, root);
        canContinue &= checkRssUrl(params, env);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditSoftware", "add", env, request);
        }

        persistance.create(item);
        Relation relation = new Relation(upper.getChild(), item, upper.getId());

        String name = root.elementTextTrim("name");
        String url = upper.getUrl() + "/" + URLManager.enforceLastURLPart(name);
        url = URLManager.protectFromDuplicates(url);
        if (url != null)
            relation.setUrl(url);

        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        setRssUrl(params, item, relation.getId());
        persistance.update(item);

        // commit new version
        Misc.commitRelation(document.getRootElement(), relation, user);

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation, true));
        } else
            env.put(VAR_RELATION, relation);
        return null;
    }

    protected String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        Node node = root.element("name");
        params.put(PARAM_NAME, node.getText());
        node = root.element("description");
        if (node != null)
            params.put(PARAM_DESCRIPTION, node.getText());
        node = root.selectSingleNode("/data/url[@useType='homepage']");
        if (node != null)
            params.put(PARAM_HOME_PAGE, node.getText());
        node = root.selectSingleNode("/data/url[@useType='download']");
        if (node != null)
            params.put(PARAM_DOWNLOAD_URL, node.getText());
        node = root.selectSingleNode("/data/url[@useType='rss']");
        if (node != null)
            params.put(PARAM_RSS_URL, node.getText());

        params.put(PARAM_USER_INTERFACE, item.getProperty(Constants.PROPERTY_USER_INTERFACE));
        params.put(PARAM_ALTERNATIVES, item.getProperty(Constants.PROPERTY_ALTERNATIVE_SOFTWARE));
        params.put(PARAM_LICENSES, item.getProperty(Constants.PROPERTY_LICENSE));

        env.put(VAR_EDIT_MODE, Boolean.TRUE);
        return FMTemplateSelector.select("EditSoftware", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);
        env.put(VAR_EDIT_MODE, Boolean.TRUE);

        Item item = (Item) relation.getChild().clone();
        item.setOwner(user.getId());
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setName(params, root, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setUserInterface(params, item);
        canContinue &= setApplicationAlternatives(params, item, env);
        canContinue &= setLicenses(params, item);
        canContinue &= setUrl(params, PARAM_HOME_PAGE, root);
        canContinue &= setUrl(params, PARAM_DOWNLOAD_URL, root);
        canContinue &= checkRssUrl(params, env);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditSoftware", "edit", env, request);
        }

        setRssUrl(params, item, relation.getId());
        persistance.update(item);

        // commit new version
        Misc.commitRelation(root, relation, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation, true));
        return null;
    }

    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        if (tmp != null && tmp.length() > 0) {
            tmp = Misc.filterDangerousCharacters(tmp);
            DocumentHelper.makeElement(root, "name").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte název programu!", env, null);
            return false;
        }
    }

    /**
     * Updates description from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = Misc.filterDangerousCharacters(tmp);
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '" + tmp + "'", e);
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
        } else {
            ServletUtils.addError(PARAM_DESCRIPTION, "Zadejte popis programu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates user interface from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @return false, if there is a major error.
     */
    private boolean setUserInterface(Map params, Item item) {
        List entries = Tools.asList(params.get(PARAM_USER_INTERFACE));
        if (entries.size() == 0)
            item.removeProperty(Constants.PROPERTY_USER_INTERFACE);
        item.setProperty(Constants.PROPERTY_USER_INTERFACE, new HashSet(entries));
        return true;
    }

    /**
     * Updates applications, which are replaced by this software, from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @return false, if there is a major error.
     */
    private boolean setApplicationAlternatives(Map params, Item item, Map env) {
        List entries = Tools.asList(params.get(PARAM_ALTERNATIVES));
        if (entries.size() == 0)
            item.removeProperty(Constants.PROPERTY_ALTERNATIVE_SOFTWARE);
        else {
            for (Iterator iter = entries.iterator(); iter.hasNext();) {
                String alternative = (String) iter.next();
                if (alternative.indexOf("://") != -1) {
                    ServletUtils.addError(PARAM_ALTERNATIVES, "Do alternativy vkládejte jméno aplikace, URL není povoleno.", env, null);
                    return false;
                }
            }
            item.setProperty(Constants.PROPERTY_ALTERNATIVE_SOFTWARE, new HashSet(entries));
        }
        return true;
    }

    /**
     * Updates license(s) from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @return false, if there is a major error.
     */
    private boolean setLicenses(Map params, Item item) {
        List entries = Tools.asList(params.get(PARAM_LICENSES));
        if (entries.size() == 0)
            item.removeProperty(Constants.PROPERTY_LICENSE);
        else
            item.setProperty(Constants.PROPERTY_LICENSE, new HashSet(entries));
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
     * Sets RSS URL for software item. The URL is copied to dynamic configuration
     * item too, so RSS fetcher is aware of this URL and it can fetch its links.
     * The method must not be called in preview mode and the relationId must not be zero
     * (e.g. the item is already created).
     * @param params map holding request's parameters
     * @param item item to be updated
     * @param relationId id of relation for this item
     */
    private void setRssUrl(Map params, Item item, int relationId) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Item dynamicConfig = (Item) persistance.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
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
                configRss.addAttribute("type", "software");
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

        persistance.update(dynamicConfig);
    }

    /**
     * Updates url associated to software from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param paramName type of the url
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setUrl(Map params, String paramName, Element root) {
        String type;
        if (PARAM_HOME_PAGE.equals(paramName))
            type = "homepage";
        else if (PARAM_DOWNLOAD_URL.equals(paramName))
            type = "download";
        else
            return false;

        Element element = (Element) root.selectSingleNode("url[@useType='"+type+"']");
        String tmp = (String) params.get(paramName);
        if (tmp != null && tmp.length() > 0) {
            if (element == null) {
                element = root.addElement("url");
                element.addAttribute("useType", type);
            }
            element.setText(tmp);
        } else {
            if (element != null)
                element.detach();
        }
        return true;
    }
}
