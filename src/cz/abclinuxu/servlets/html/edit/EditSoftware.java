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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.security.ActionCheck;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.email.monitor.MonitorAction;
import cz.abclinuxu.utils.email.monitor.UserAction;
import cz.abclinuxu.utils.email.monitor.ObjectType;
import cz.abclinuxu.utils.email.monitor.MonitorPool;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;
import cz.abclinuxu.servlets.AbcAutoAction;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.prefs.Preferences;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Set;

/**
 * This class is responsible for adding and
 * editing of software items and records.
 */
public class EditSoftware extends AbcAutoAction implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditSoftware.class);

    public static final String PREF_MAX_INTRO_LENGTH = "max.intro.length";
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new EditSoftware());
    }
    static int maxIntroLength;

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

    @ActionCheck(userRequired = true, relationRequired = true, requireCreateRight = true)
    public String actionAdd() throws Exception {
        Relation upper = (Relation) env.get(VAR_RELATION);
        if (upper.getUrl() == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chyba - sekce nemá textové URL. Kontaktujte prosím administrátora.",
                                  env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(upper));
        }

        return FMTemplateSelector.select("EditSoftware", "add", env, request);
    }

    @ActionCheck(userRequired = true, relationRequired = true, requireCreateRight = true, checkPost = true, checkReferer = true)
    public String actionAdd2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

		Tools.sync(relation);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.SOFTWARE);
        item.setData(document);
        item.setOwner(user.getId());

		Category cat = (Category) relation.getChild();
		item.setGroup(cat.getGroup());
		item.setPermissions(cat.getPermissions());

        boolean canContinue = setName(params, item, env);
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

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.create(item);
        versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

        Relation newRelation = new Relation(relation.getChild(), item, relation.getId());
        String name = item.getTitle();
        String url = relation.getUrl() + "/" + URLManager.enforceRelativeURL(name);
        url = URLManager.protectFromDuplicates(url);
        if (url != null)
            newRelation.setUrl(url);

        persistence.create(newRelation);
        newRelation.getParent().addChildRelation(newRelation);
        TagTool.assignDetectedTags(item, user);

        // todo prvni revize nebude obsahovat RSS v tabulce verze
        setRssUrl(params, item, newRelation.getId());
        persistence.update(item);

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz" + newRelation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.SOFTWARE, newRelation, absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        // refresh RSS
        FeedGenerator.updateSoftware();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(newRelation));

        return null;
    }

    @ActionCheck(userRequired = true, itemType = Item.SOFTWARE, requireModifyRight = true)
    public String actionEdit() throws Exception {
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        params.put(PARAM_NAME, item.getTitle());
        Node node = root.element("description");
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

    @ActionCheck(userRequired = true, itemType = Item.SOFTWARE, requireModifyRight = true, checkPost = true, checkReferer = true)
    public String actionEdit2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        env.put(VAR_EDIT_MODE, Boolean.TRUE);

        Item item = (Item) relation.getChild().clone();
        Item origItem = (Item) item.clone();
        item.setOwner(user.getId());
        Element root = item.getData().getRootElement();

        boolean canContinue = setName(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setUserInterface(params, item);
        canContinue &= setApplicationAlternatives(params, item, env);
        canContinue &= setLicenses(params, item);
        canContinue &= setUrl(params, PARAM_HOME_PAGE, root);
        canContinue &= setUrl(params, PARAM_DOWNLOAD_URL, root);
        canContinue &= checkRssUrl(params, env);
        canContinue &= ServletUtils.checkNoChange(item, origItem, env);
        String changesDescription = Misc.getRevisionString(params, env);
        canContinue &= !Constants.ERROR.equals(changesDescription);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditSoftware", "edit", env, request);
        }

        setRssUrl(params, item, relation.getId());

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), changesDescription);

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz" + relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.SOFTWARE, relation, absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        // refresh RSS
        FeedGenerator.updateSoftware();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    @ActionCheck(userRequired = true, itemType = Item.SOFTWARE, checkTicket = true)
    public String actionUser_of() throws Exception {
        Item item = (Item) relation.getChild();
        Persistence persistence = PersistenceFactory.getPersistence();
        Set<String> users = item.getProperty(Constants.PROPERTY_USED_BY);

        // see whether user wants to remove or add himself
        String userid = Integer.toString(user.getId());
        if (! users.contains(userid))
            item.addProperty(Constants.PROPERTY_USED_BY, userid);
        else
            item.removePropertyValue(Constants.PROPERTY_USED_BY, userid);

        Date originalUpdated = item.getUpdated();
        persistence.update(item);
        SQLTool.getInstance().setUpdatedTimestamp(item, originalUpdated);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));

        return null;
    }

    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        if (tmp != null && tmp.length() > 0) {
            tmp = Misc.filterDangerousCharacters(tmp);
            item.setTitle(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte název programu!", env, null);
            return false;
        }
    }

    /**
     * Updates description from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = Misc.filterDangerousCharacters(tmp);
                tmp = HtmlPurifier.clean(tmp);
                HtmlChecker.check(Rules.DEFAULT, tmp);
            } catch (Exception e) {
                ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
                return false;
            }

            Element element = DocumentHelper.makeElement(root, "description");
            tmp = Tools.processLocalLinks(tmp, null);
            element.setText(tmp);

            String noHtml = Tools.removeTags(tmp);
            int position = noHtml.indexOf(". ");
            if (position == -1)
                position = noHtml.indexOf(".");
            if (position == -1)
                position = noHtml.length() - 1;
            String intro = noHtml.substring(0, position + 1);
            intro = Tools.limit(intro, maxIntroLength, "..");
            DocumentHelper.makeElement(root, "intro").setText(intro);
        } else {
            ServletUtils.addError(PARAM_DESCRIPTION, "Zadejte popis programu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates user interface from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @return false, if there is a major error.
     */
    private boolean setUserInterface(Map params, Item item) {
        List entries = Tools.asList(params.get(PARAM_USER_INTERFACE));
        if (entries.size() == 0)
            item.removeProperty(Constants.PROPERTY_USER_INTERFACE);
        item.setProperty(Constants.PROPERTY_USER_INTERFACE, new HashSet<String>(entries));
        return true;
    }

    /**
     * Updates applications, which are replaced by this software, from parameters. Changes are not synchronized with persistence.
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
            item.setProperty(Constants.PROPERTY_ALTERNATIVE_SOFTWARE, new HashSet<String>(entries));
        }
        return true;
    }

    /**
     * Updates license(s) from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @return false, if there is a major error.
     */
    private boolean setLicenses(Map params, Item item) {
        List entries = Tools.asList(params.get(PARAM_LICENSES));
        if (entries.size() == 0)
            item.removeProperty(Constants.PROPERTY_LICENSE);
        else
            item.setProperty(Constants.PROPERTY_LICENSE, new HashSet<String>(entries));
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
        Persistence persistence = PersistenceFactory.getPersistence();
        Item dynamicConfig = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
        dynamicConfig = (Item) dynamicConfig.clone();
        Element configRoot = dynamicConfig.getData().getRootElement();
        Element configRss = (Element) configRoot.selectSingleNode("/data/feeds/feed[@relation='" + relationId + "']");
        Element itemRoot = item.getData().getRootElement();
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

        persistence.update(dynamicConfig);
    }

    /**
     * Updates url associated to software from parameters. Changes are not synchronized with persistence.
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

    public void configure(Preferences prefs) throws ConfigurationException {
        maxIntroLength = prefs.getInt(PREF_MAX_INTRO_LENGTH, 100);
    }
}
