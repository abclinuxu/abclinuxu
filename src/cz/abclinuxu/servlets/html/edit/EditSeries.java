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

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * This class is responsible for managing article series.
 * Date: 5.1.2007
 */
public class EditSeries implements AbcAction {
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_ARTICLE_RELATION = "articleRid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_URL = "url";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_EDIT_MODE = "EDIT_MODE";
    public static final String VAR_SERIES_LIST = "SERIES";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_REMOVE = "rm";
    public static final String ACTION_ADD_ARTICLE = "addArticle";
    public static final String ACTION_ADD_ARTICLE_STEP2 = "addArticle2";
    public static final String ACTION_ADD_ARTICLES_URLS = "addArticlesUrls";
    public static final String ACTION_ADD_ARTICLES_URLS_STEP2 = "addArticlesUrls2";
    public static final String ACTION_REMOVE_ARTICLE = "rmArticle";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (!user.hasRole(Roles.ARTICLE_ADMIN))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_ADD_ARTICLE))
            return actionAttachArticleStep1(request, env);

        if (action.equals(ACTION_ADD))
            return actionAddStep1(request, env);

        if (action.equals(ACTION_ADD_STEP2)) {
            ActionProtector.ensureContract(request, EditSeries.class, true, true, true, false);
            return actionAddStep2(request, response, env, true);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relationId!");

        persistence.synchronize(relation);
        persistence.synchronize(relation.getChild());
        env.put(VAR_RELATION,relation);

        if (action.equals(ACTION_EDIT))
            return actionEditStep1(request, env);

        if (action.equals(ACTION_EDIT_STEP2)) {
            ActionProtector.ensureContract(request, EditSeries.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        if (action.equals(ACTION_REMOVE)) {
            ActionProtector.ensureContract(request, EditSeries.class, true, false, false, true);
            return actionRemove(response, env);
        }

        if (action.equals(ACTION_ADD_ARTICLE_STEP2)) {
            ActionProtector.ensureContract(request, EditSeries.class, true, true, true, false);
            return actionAttachArticleStep2(response, env, false);
        }

        if (action.equals(ACTION_ADD_ARTICLES_URLS))
            return actionAttachArticlesUrlsStep1(request, env);

        if (action.equals(ACTION_ADD_ARTICLES_URLS_STEP2)) {
            ActionProtector.ensureContract(request, EditSeries.class, true, true, true, false);
            return actionAttachArticlesUrlsStep2(request, response, env);
        }

        if (action.equals(ACTION_REMOVE_ARTICLE)) {
            ActionProtector.ensureContract(request, EditSeries.class, true, false, false, true);
            return actionRemoveArticle(response, env);
        }

        return null;
    }

    public String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("EditSeries", "add", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.SERIES);
        item.setData(document);
        item.setOwner(user.getId());
        Relation relation = new Relation(new Category(Constants.CAT_SERIES), null, Constants.REL_SERIES);

        boolean canContinue = setName(params, root, env);
        canContinue &= setDescription(params, root);
        canContinue &= setIcon(params, root);
        canContinue &= setUrl(params, relation, env);

        if (!canContinue )
            return FMTemplateSelector.select("EditSeries", "add", env, request);

        persistence.create(item);

        relation.setChild(item);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
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
        node = root.element("icon");
        if (node != null)
            params.put(PARAM_ICON, node.getText());

        env.put(VAR_EDIT_MODE, Boolean.TRUE);
        return FMTemplateSelector.select("EditSeries", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = setName(params, root, env);
        canContinue &= setDescription(params, root);
        canContinue &= setIcon(params, root);

        if (!canContinue ) {
            env.put(VAR_EDIT_MODE, Boolean.TRUE);
            return FMTemplateSelector.select("EditSeries", "edit", env, request);
        }
        persistence.update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    protected String actionRemove(HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation seriesRelation = (Relation) env.get(VAR_RELATION);

        Item seriesItem = (Item) seriesRelation.getChild().clone();
        Element seriesRoot = seriesItem.getData().getRootElement();
        List articles = seriesRoot.elements("article");

        for (Iterator iter = articles.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            int articleRid = Misc.parseInt(element.getText(), 0);
            Relation articleRelation = (Relation) persistence.findById(new Relation(articleRid));
            persistence.synchronize(articleRelation.getChild());

            Item articleItem = (Item) articleRelation.getChild().clone();
            Element articleRoot = articleItem.getData().getRootElement();
            Element seriesElement = articleRoot.element("series_rid");
            seriesElement.detach();
            persistence.update(articleItem);
        }

        persistence.remove(seriesRelation);
        seriesRelation.getParent().removeChildRelation(seriesRelation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, UrlUtils.PREFIX_SERIES);
        return null;
    }

    private String actionAttachArticleStep1(HttpServletRequest request, Map env) {
        Persistence persistence = PersistenceFactory.getPersistance();
        Category category = (Category) persistence.findById(new Category(Constants.CAT_SERIES));
        List<Relation> series = category.getChildren();
        Sorters2.byName(series);
        env.put(VAR_SERIES_LIST, series);
        return FMTemplateSelector.select("EditSeries", "addArticle", env, request);
    }

    public static String actionAttachArticleStep2(HttpServletResponse response, Map env, boolean noRedirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation seriesRelation = (Relation) env.get(VAR_RELATION);

        Item seriesItem = (Item) seriesRelation.getChild().clone();
        Element seriesRoot = seriesItem.getData().getRootElement();
        List articles = seriesRoot.elements("article");

        List articleRelations = Tools.asList(params.get(PARAM_ARTICLE_RELATION));
        if ( articleRelations.size() == 0 )
            throw new MissingArgumentException("Chybí parametr "+PARAM_ARTICLE_RELATION+"!");

        for (Iterator iter = articleRelations.iterator(); iter.hasNext();) {
            String ridString = (String) iter.next();
            int rid = Misc.parseInt(ridString, 0);
            Relation articleRelation = (Relation) persistence.findById(new Relation(rid));
            persistence.synchronize(articleRelation.getChild());
            Item articleItem = (Item) articleRelation.getChild().clone();
            Element articleRoot = articleItem.getData().getRootElement();
            addArticleToSeries(articleItem, articleRelation, articles);
            articleRoot.addElement("series_rid").setText(Integer.toString(seriesRelation.getId()));
            persistence.update(articleItem);
        }

        persistence.update(seriesItem);

        if (! noRedirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(seriesRelation));
        }
        return null;
    }

    /**
     * Adds element article to the list of article elements.
     * @param articleItem
     * @param articleRelation
     * @param articles list of article elements
     */
    public static void addArticleToSeries(Item articleItem, Relation articleRelation, List articles) {
        String published;
        synchronized (Constants.isoFormatShort) {
            published = Constants.isoFormatShort.format(articleItem.getCreated());
        }

        Element seriesArticle = DocumentHelper.createElement("article");
        seriesArticle.setText(Integer.toString(articleRelation.getId()));
        seriesArticle.addAttribute("published", published);

        int position = articles.size();
        while (position > 0) {
            Element element = (Element) articles.get(position - 1);
            if (published.compareTo(element.attributeValue("published")) > 0) // 2007-01-02 > 2006-12-24
                break;
            position--;
        }

        if (position < articles.size())
            articles.add(position, seriesArticle);
        else
            articles.add(seriesArticle);
    }

    private String actionAttachArticlesUrlsStep1(HttpServletRequest request, Map env) {
        return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
    }

    public String actionAttachArticlesUrlsStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        List articleRelations = new ArrayList();

        String domain = AbcConfig.getDomain();
        String urls = (String) params.get(PARAM_URL);
        StringTokenizer stk = new StringTokenizer(urls, "\n");
        while (stk.hasMoreTokens()) {
            String url = stk.nextToken().trim();
            int domainPosition = url.indexOf(domain);
            if (domainPosition != -1) {
                int position = url.indexOf("/", domainPosition + domain.length());
                url = url.substring(position);
            }

            if (url.indexOf("://") != -1) {
                ServletUtils.addError(PARAM_URL, "Neznáme URL: "+url+". Zadejte buď relativní URL nebo absolutní URL patřící do serveru "+domain, env, null);
                return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
            }

            Relation relation = URLMapper.loadRelationFromUrl(url);
            if (relation == null) {
                ServletUtils.addError(PARAM_URL, "URL "+url+" nebylo nalezeno!", env, null);
                return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
            }

            GenericObject obj = persistence.findById(relation.getChild());
            if ( !(obj instanceof Item) || ((Item)obj).getType() != Item.ARTICLE) {
                ServletUtils.addError(PARAM_URL, "URL "+url+" nepatří článku!", env, null);
                return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
            }

            articleRelations.add(Integer.toString(relation.getId()));
        }

        if (articleRelations.size() == 0) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL adresu nejméně jednoho článku!", env, null);
            return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
        }

        params.put(PARAM_ARTICLE_RELATION, articleRelations);
        return actionAttachArticleStep2(response, env, false);
    }

    private String actionRemoveArticle(HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation seriesRelation = (Relation) env.get(VAR_RELATION);

        Item seriesItem = (Item) seriesRelation.getChild().clone();
        Element seriesRoot = seriesItem.getData().getRootElement();

        String ridString = (String) params.get(PARAM_ARTICLE_RELATION);
        if ( ridString == null )
            throw new MissingArgumentException("Chybí parametr "+PARAM_ARTICLE_RELATION+"!");

        int articleRid = Misc.parseInt(ridString, 0);
        Relation articleRelation = (Relation) persistence.findById(new Relation(articleRid));
        persistence.synchronize(articleRelation.getChild());
        Item articleItem = (Item) articleRelation.getChild().clone();
        Element articleRoot = articleItem.getData().getRootElement();

        Element articleElement = (Element) seriesRoot.selectObject("article[text()='"+articleRid+"']");
        if (articleElement == null)
            throw new MissingArgumentException("Seriál neobsahuje článek "+articleRid+"!");

        Element seriesElement = articleRoot.element("series_rid");
        seriesElement.detach();
        persistence.update(articleItem);

        articleElement.detach();
        persistence.update(seriesItem);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(seriesRelation));
        return null;
    }


    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        if (tmp != null && tmp.length() > 0) {
            tmp = Misc.filterDangerousCharacters(tmp);
            DocumentHelper.makeElement(root, "name").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte jméno seriálu!", env, null);
            return false;
        }
    }

    /**
     * Updates description from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        Element element = root.element("description");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "description");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates icon from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setIcon(Map params, Element root) {
        String tmp = (String) params.get(PARAM_ICON);
        Element element = root.element("icon");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "icon");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Sets url for series from parameters.
     * @param params map holding request's parameters
     * @param relation series relation
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setUrl(Map params, Relation relation, Map env) {
        String url = (String) params.get(PARAM_URL);
        if (url == null) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL seriálu!", env, null);
            return false;
        }
        if (! url.startsWith(UrlUtils.PREFIX_SERIES)) {
            ServletUtils.addError(PARAM_URL, "URL seriálu musí začínat prefixem "+UrlUtils.PREFIX_SERIES+"!", env, null);
            return false;
        }

        url = URLManager.enforceAbsoluteURL(url);
        if (url.equals(UrlUtils.PREFIX_SERIES)) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL seriálu!", env, null);
            return false;
        }

        if (URLManager.exists(url)) {
            ServletUtils.addError(PARAM_URL, "Zadejte unikátní URL seriálu!", env, null);
            return false;
        }

        relation.setUrl(url);

        return true;
    }
}
