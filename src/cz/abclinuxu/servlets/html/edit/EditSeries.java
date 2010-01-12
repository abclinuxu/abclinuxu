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

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.security.ActionCheck;
import cz.abclinuxu.servlets.AbcAutoAction;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.TagTool;
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
public class EditSeries extends AbcAutoAction {
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_ARTICLE_RELATION = "articleRid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_URL = "url";

    public static final String VAR_EDIT_MODE = "EDIT_MODE";
    public static final String VAR_SERIES_LIST = "SERIES";

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        init(request, response, env);

        if (relation == null) {
            relation = new Relation(Constants.REL_SERIES);
            Tools.sync(relation);
        }

        return invokeAction();
    }

    @ActionCheck(requireCreateRight = true)
    public String actionAdd() throws Exception {
        return FMTemplateSelector.select("EditSeries", "add", env, request);
    }

    @ActionCheck(requireCreateRight = true, checkPost = true, checkReferer = true)
    public String actionAdd2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.SERIES);
        item.setData(document);
        item.setOwner(user.getId());
		item.setGroup( ((Category) relation.getChild()).getGroup() );
		
        Relation newRelation = new Relation(relation.getChild(), null, relation.getId());

        boolean canContinue = setName(params, item, env);
        canContinue &= setDescription(params, root);
        canContinue &= setIcon(params, root);
        canContinue &= setUrl(params, newRelation, env);

        if (!canContinue )
            return FMTemplateSelector.select("EditSeries", "add", env, request);

        persistence.create(item);

        newRelation.setChild(item);
        persistence.create(newRelation);
        newRelation.getParent().addChildRelation(newRelation);
        TagTool.assignDetectedTags(item, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(newRelation));

        return null;
    }

    @ActionCheck(requireModifyRight = true, itemType = Item.SERIES)
    public String actionEdit() throws Exception {
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        params.put(PARAM_NAME, item.getTitle());
        Node node = root.element("description");
        if (node != null)
            params.put(PARAM_DESCRIPTION, node.getText());
        node = root.element("icon");
        if (node != null)
            params.put(PARAM_ICON, node.getText());

        env.put(VAR_EDIT_MODE, Boolean.TRUE);
        return FMTemplateSelector.select("EditSeries", "edit", env, request);
    }

    @ActionCheck(requireModifyRight = true, itemType = Item.SERIES, checkReferer = true, checkPost = true)
    public String actionEdit2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = setName(params, item, env);
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

    @ActionCheck(requireDeleteRight = true, itemType = Item.SERIES, checkTicket = true)
    public String actionRemove() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        Item seriesItem = (Item) relation.getChild().clone();
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

        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, UrlUtils.PREFIX_SERIES);
        return null;
    }

    public String actionAddArticle() {
        Persistence persistence = PersistenceFactory.getPersistence();
        Category category = (Category) persistence.findById(new Category(Constants.CAT_SERIES));
        List<Relation> series = category.getChildren();
        Sorters2.byName(series);
        env.put(VAR_SERIES_LIST, series);
        return FMTemplateSelector.select("EditSeries", "addArticle", env, request);
    }

    @ActionCheck(requireModifyRight = true, itemType = Item.SERIES, checkPost = true, checkReferer = true)
    public String actionAddArticle2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        Item seriesItem = (Item) relation.getChild().clone();
        Element seriesRoot = seriesItem.getData().getRootElement();
        List articles = seriesRoot.elements("article");
		
		if (!Tools.permissionsFor(user, relation).canModify())
			return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        List articleRelations = Tools.asList(params.get(PARAM_ARTICLE_RELATION));
        if ( articleRelations.size() == 0 )
            throw new MissingArgumentException("Chybí parametr "+PARAM_ARTICLE_RELATION+"!");

        for (Iterator iter = articleRelations.iterator(); iter.hasNext();) {
            String ridString = (String) iter.next();
            int rid = Misc.parseInt(ridString, 0);
            Relation articleRelation = (Relation) persistence.findById(new Relation(rid));
            persistence.synchronize(articleRelation.getChild());
            Item articleItem = (Item) articleRelation.getChild().clone();
			
			if (!Tools.permissionsFor(user, articleRelation).canModify())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
			
            Element articleRoot = articleItem.getData().getRootElement();

            if (articleRoot.element("series_rid") != null) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Článek '" + Tools.childName(articleRelation) +
                        "' už je přiřazen k nějakému seriálu!", env, request.getSession());
                continue;
            }

            addArticleToSeries(articleItem, articleRelation, articles);
            articleRoot.addElement("series_rid").setText(Integer.toString(relation.getId()));
            persistence.update(articleItem);
        }

        persistence.update(seriesItem);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));

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

    @ActionCheck(requireModifyRight = true, itemType = Item.SERIES)
    public String actionAddArticlesUrls() {
        return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
    }

    @ActionCheck(requireModifyRight = true, itemType = Item.SERIES, checkReferer = true, checkPost = true)
    public String actionAddArticlesUrls2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
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

            Relation relArticle = URLMapper.loadRelationFromUrl(url);
            if (relArticle == null) {
                ServletUtils.addError(PARAM_URL, "URL "+url+" nebylo nalezeno!", env, null);
                return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
            }

            GenericObject obj = persistence.findById(relArticle.getChild());
            if ( !(obj instanceof Item) || ((Item)obj).getType() != Item.ARTICLE) {
                ServletUtils.addError(PARAM_URL, "URL "+url+" nepatří článku!", env, null);
                return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
            }

            articleRelations.add(Integer.toString(relArticle.getId()));
        }

        if (articleRelations.size() == 0) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL adresu nejméně jednoho článku!", env, null);
            return FMTemplateSelector.select("EditSeries", "addArticlesUrls", env, request);
        }

        params.put(PARAM_ARTICLE_RELATION, articleRelations);
        return actionAddArticle2();
    }

    @ActionCheck(requireModifyRight = true, itemType = Item.SERIES, checkTicket = true)
    public String actionRmArticle() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        Item seriesItem = (Item) relation.getChild().clone();
        Element seriesRoot = seriesItem.getData().getRootElement();

        String ridString = (String) params.get(PARAM_ARTICLE_RELATION);
        if ( ridString == null )
            throw new MissingArgumentException("Chybí parametr "+PARAM_ARTICLE_RELATION+"!");

        int articleRid = Misc.parseInt(ridString, 0);
        Relation articleRelation = (Relation) persistence.findById(new Relation(articleRid));
        persistence.synchronize(articleRelation.getChild());
        Item articleItem = (Item) articleRelation.getChild().clone();
        Element articleRoot = articleItem.getData().getRootElement();

        Object obj = seriesRoot.selectObject("article[text()='"+articleRid+"']");
        Element articleElement;

        if (obj instanceof List)
            articleElement = (Element) ((List) obj).get(0);
        else
            articleElement = (Element) obj;

        if (articleElement == null)
            throw new MissingArgumentException("Seriál neobsahuje článek "+articleRid+"!");

        Element seriesElement = articleRoot.element("series_rid");
        seriesElement.detach();
        persistence.update(articleItem);

        articleElement.detach();
        persistence.update(seriesItem);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }


    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        if (tmp != null && tmp.length() > 0) {
            tmp = Misc.filterDangerousCharacters(tmp);
            item.setTitle(tmp);
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
