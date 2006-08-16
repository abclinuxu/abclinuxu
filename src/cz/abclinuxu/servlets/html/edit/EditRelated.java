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
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.view.RelatedDocument;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.net.URLDecoder;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.htmlparser.util.ParserException;

/**
 * @author literakl
 * @since 12.8.2006
 */
public class EditRelated implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRelated.class);

    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_URL = "url";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_DOCUMENT = "document";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_DOCUMENTS = "DOCUMENTS";

    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr rid!");

        Tools.sync(relation);
        env.put(VAR_RELATION, relation);
        String action = (String) params.get(PARAM_ACTION);

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (ACTION_ADD_STEP2.equals(action))
            return actionAddStep2(request, response, env, true);

        if (ACTION_EDIT.equals(action))
            return actionEditStep1(request, env);

        if (ACTION_EDIT_STEP2.equals(action))
            return actionEditStep2(request, response, env);

        if (ACTION_REMOVE.equals(action))
            return actionRemoveAttachmentStep1(request, env);

        if (ACTION_REMOVE_STEP2.equals(action))
            return actionRemoveAttachmentStep2(request, response, env);

        return actionManageRelated(request, env);
    }

    private String actionManageRelated(HttpServletRequest request, Map env) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);
        return FMTemplateSelector.select("EditRelated", "manage", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = insertDocument(params, root, env);
        if (! canContinue)
            return actionManageRelated(request, env);

        item.setOwner(user.getId());
        persistance.update(item);

        // commit new version
        Misc.commitRelation(item.getData().getRootElement(), relation, user);

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/EditRelated/" + relation.getId());
        }
        return null;
    }

    private String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Object param = params.get(PARAM_DOCUMENT);
        if (param == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chybí adresa dokumentu.", env, null);
            return actionManageRelated(request, env);
        }

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = (Element) item.getData().getRootElement();
        Element related = (Element) root.element("related");
        String url = (String) params.get(PARAM_DOCUMENT);
        url = URLDecoder.decode(url, "ISO-8859-2");
        Element element = (Element) related.selectSingleNode("//document[url='" + url + "']");
        if (element == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Dokument '" + url + "' nebyl nalezen mezi daty!", env, null);
            return actionManageRelated(request, env);
        }

        params.put(PARAM_URL, element.elementText("url"));
        params.put(PARAM_TITLE, element.elementText("title"));
        params.put(PARAM_DESCRIPTION, element.elementText("description"));

        return FMTemplateSelector.select("EditRelated", "edit", env, request);
    }

    private String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = editDocument(params, root, env);
        if (! canContinue)
            return FMTemplateSelector.select("EditRelated", "edit", env, request);

        item.setOwner(user.getId());
        persistance.update(item);

        // commit new version
        Misc.commitRelation(item.getData().getRootElement(), relation, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditRelated/" + relation.getId());
        return null;
    }

    private String actionRemoveAttachmentStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Object param = params.get(PARAM_DOCUMENT);
        if (param == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nevybrali jste ¾ádný dokument na smazání.", env, null);
            return actionManageRelated(request, env);
        }
        if (param instanceof String)
            params.put(PARAM_DOCUMENT, Collections.singletonList(param));

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = (Element) item.getData().getRootElement();
        Element related = (Element) root.element("related");
        List list = (List) params.get(PARAM_DOCUMENT);
        List documents = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            String url = (String) iter.next();
            url = URLDecoder.decode(url, "ISO-8859-2");
            Element element = (Element) related.selectSingleNode("//document[url='" + url + "']");
            if (element != null) {
                RelatedDocument document = new RelatedDocument(element.elementText("url"), element.attributeValue("type"));
                document.setTitle(element.elementText("title"));
                documents.add(document);
            }
        }
        env.put(VAR_DOCUMENTS, documents);

        return FMTemplateSelector.select("EditRelated", "remove", env, request);
    }

    private String actionRemoveAttachmentStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();

        Element root = (Element) item.getData().getRootElement();
        Element related = (Element) root.element("related");
        List list = Tools.asList(params.get(PARAM_DOCUMENT));
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            String url = (String) iter.next();
            url = URLDecoder.decode(url, "ISO-8859-2");
            Element element = (Element) related.selectSingleNode("//document[url='" + url + "']");
            if (element == null) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Dokument '" + url + "' nebyl nalezen mezi daty!", env, null);
                return actionManageRelated(request, env);
            }
            element.detach();
        }

        item.setOwner(user.getId());
        persistance.update(item);

        // commit new version
        Misc.commitRelation(item.getData().getRootElement(), relation, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditRelated/"+relation.getId());
        return null;
    }

    // setters

    /**
     * Inserts new related document from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    public static boolean insertDocument(Map params, Element root, Map env) {
        RelatedDocument relatedDocument = createDocument(params, env);
        if (relatedDocument == null)
            return false;

        Element related = DocumentHelper.makeElement(root, "/related");
        Element element = related.addElement("document");
        element.addElement("url").setText(relatedDocument.getUrl());
        element.addElement("title").setText(relatedDocument.getTitle());
        if (! Misc.empty(relatedDocument.getDescription()))
            element.addElement("description").setText(relatedDocument.getDescription());
        element.addAttribute("type", relatedDocument.getType());
        return true;
    }

    /**
     * Updates existing related document from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    public static boolean editDocument(Map params, Element root, Map env) throws Exception {
        RelatedDocument relatedDocument = createDocument(params, env);
        if (relatedDocument == null)
            return false;

        String url = (String) params.get(PARAM_DOCUMENT);
        url = URLDecoder.decode(url, "ISO-8859-2");

        Element related = (Element) root.element("related");
        Element element = (Element) related.selectSingleNode("//document[url='" + url + "']");
        if (element == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Dokument '" + url + "' nebyl nalezen mezi daty!", env, null);
            return false;
        }

        element.element("url").setText(relatedDocument.getUrl());
        element.element("title").setText(relatedDocument.getTitle());
        element.attribute("type").setText(relatedDocument.getType());

        Element desc = element.element("description");
        if (desc != null)
            desc.detach();
        if (! Misc.empty(relatedDocument.getDescription()))
            element.addElement("description").setText(relatedDocument.getDescription());
        return true;
    }

    /**
     * Creates new related document from parameters. If there is some error,
     * the message is stored into env and null is returned.
     * @return document
     */
    private static RelatedDocument createDocument(Map params, Map env) {
        String url = (String) params.get(PARAM_URL);
        if (Misc.empty(url)) {
            ServletUtils.addError(PARAM_URL, "Zadejte adresu dokumentu.", env, null);
            return null;
        }

        String title = (String) params.get(PARAM_TITLE);
        title = Misc.filterDangerousCharacters(title);

        String desc = (String) params.get(PARAM_DESCRIPTION);
        if (desc != null) {
            desc = Misc.filterDangerousCharacters(desc);
            try {
                SafeHTMLGuard.check(desc);
            } catch (ParserException e) {
                log.error("ParseException on '" + desc + "'", e);
                ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
                return null;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
                return null;
            }
        }

        String type = null;
        String domain = AbcConfig.getDomain();
        int domainPosition = url.indexOf(domain);
        if (domainPosition != -1) {
            int position = url.indexOf("/", domainPosition + domain.length());
            url = url.substring(position);
        }
        if (url.indexOf("://") != -1) {
            type = Constants.RELATED_EXTERNAL_DOCUMENT;
            if (Misc.empty(title)) {
                ServletUtils.addError(PARAM_TITLE, "Zadejte jméno pro externí dokument.", env, null);
                return null;
            }
        }

        if (type == null) {
            Persistance persistance = PersistanceFactory.getPersistance();
            Relation relation = URLMapper.loadRelationFromUrl(url);
            if (relation == null) {
                if (Misc.empty(title)) {
                    ServletUtils.addError(PARAM_TITLE, "Zadejte jméno pro tento nepodporovaný dokument.", env, null);
                    return null;
                }
            } else {
                GenericObject obj = persistance.findById(relation.getChild());
                if (obj instanceof Item) {
                    switch (((Item) obj).getType()) {
                        case Item.ARTICLE: type = Constants.RELATED_ARTICLE; break;
                        case Item.CONTENT: type = Constants.RELATED_CONTENT; break;
                        case Item.DICTIONARY: type = Constants.RELATED_DICTIONARY; break;
                        case Item.DISCUSSION: type = Constants.RELATED_DISCUSSION; break;
                        case Item.DRIVER: type = Constants.RELATED_DRIVER; break;
                        case Item.FAQ: type = Constants.RELATED_FAQ; break;
                        case Item.HARDWARE: type = Constants.RELATED_HARDWARE; break;
                        case Item.NEWS: type = Constants.RELATED_NEWS; break;
                        case Item.SOFTWARE: type = Constants.RELATED_SOFTWARE; break;
                        case Item.BLOG: type = Constants.RELATED_STORY;
                    }
                } else if (obj instanceof Category)
                    type = Constants.RELATED_SECTION;
                else if (obj instanceof Poll)
                    type = Constants.RELATED_POLL;

                if (Misc.empty(title))
                    title = Tools.childName(relation);
            }

            if (type == null)
                type = Constants.RELATED_OTHER;
        }

        RelatedDocument document = new RelatedDocument(url, type);
        document.setTitle(title);
        if (!Misc.empty(desc))
            document.setDescription(desc);
        return document;
    }
}
