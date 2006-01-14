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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.scheduler.VariableFetcher;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Iterator;
import java.util.Date;

import freemarker.ext.dom.NodeModel;

/**
 * User: literakl
 * Date: 16.7.2005
 */
public class EditFaq implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditFaq.class);

    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_LINK_CAPTION = "caption";
    public static final String PARAM_LINK_URL = "link";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_FAQ_XML = "XML";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_ALTER_MONITOR = "monitor";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        String action = (String) params.get(PARAM_ACTION);

        if (relation != null) {
            Tools.sync(relation);
            env.put(VAR_RELATION, relation);
        } else if (!ACTION_ADD.equals(action))
            throw new MissingArgumentException("Chybí parametr rid!");


        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (ACTION_ADD.equals(action))
            return actionAddStep1(request, env);

        if (ACTION_ADD_STEP2.equals(action))
            return actionAddStep2(request, response, env, true);

        if (ACTION_EDIT.equals(action))
            return actionEditStep1(request, env);

        if (ACTION_EDIT_STEP2.equals(action))
            return actionEditStep2(request, response, env);

        if (ACTION_ALTER_MONITOR.equals(action))
            return actionAlterMonitor(request, response, env);

        throw new MissingArgumentException("Nepodporovaná hodnota parametru action!");
    }

    /**
     * Displays form to add question.
     */
    private String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("EditFaq", "add", env, request);
    }

    /**
     * Adds new FAQ entry to selected FAQ section.
     */
    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation parentRelation = (Relation) env.get(VAR_RELATION);
        Category section = (Category) persistance.findById(parentRelation.getChild());
        if (section.getType()!=Category.FAQ) {
            log.warn("Sekce " + section.getId() + " musí být typu FAQ - " + Category.FAQ);
            throw new InvalidInputException("Interní chyba - tato sekce není typu FAQ.");
        }

        Item faq = new Item(0, Item.FAQ);
        faq.setOwner(user.getId());
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        faq.setData(document);
        Relation relation = new Relation(parentRelation.getChild(), faq, parentRelation.getId());

        boolean canContinue = true;
        canContinue &= setQuestion(params, faq, root, env);
        canContinue &= setText(params, root, env);
        canContinue &= setLinks(params, root, env);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            faq.setInitialized(true);
            env.put(VAR_PREVIEW, faq);
            env.put(VAR_FAQ_XML, NodeModel.wrap((new DOMWriter().write(faq.getData()))));
            return FMTemplateSelector.select("EditFaq", "add", env, request);
        }

        persistance.create(faq);

        String title = root.elementText("title");
        String url = parentRelation.getUrl() + "/" + URLManager.enforceLastURLPart(title);
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);
        persistance.create(relation);

        // commit new version
        Misc.commitRelation(document.getRootElement(), relation, user);

        // refresh RSS
        FeedGenerator.updateFAQ();
        VariableFetcher.getInstance().refreshFaq();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, url);
        } else
            env.put(VAR_RELATION, relation);

        return null;
    }

    /**
     * Displays form to edit question.
     */
    private String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        Element root = item.getData().getRootElement();
        Element element = (Element) root.element("title");
        params.put(PARAM_TITLE, element.getText());
        element = (Element) root.element("text");
        params.put(PARAM_TEXT, element.getText());
        element = (Element) root.element("links");
        if (element!=null) {
            int i = 1;
            Iterator iterator = element.elementIterator("link");
            while (iterator.hasNext()) {
                Element link = (Element) iterator.next();
                params.put(PARAM_LINK_CAPTION+i, link.getText());
                params.put(PARAM_LINK_URL+i, link.attributeValue("url"));
                i++;
            }
        }
        return FMTemplateSelector.select("EditFaq", "edit", env, request);
    }

    /**
     * Modifies existing FAQ entry to selected FAQ section.
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item faq = (Item) relation.getChild();
        faq.setOwner(user.getId());
        Element root = faq.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setQuestion(params, faq, root, env);
        canContinue &= setText(params, root, env);
        canContinue &= setLinks(params, root, env);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            else
                env.put(VAR_PREVIEW, faq);
            env.put(VAR_FAQ_XML, NodeModel.wrap((new DOMWriter().write(faq.getData()))));
            return FMTemplateSelector.select("EditFaq", "edit", env, request);
        }

        persistance.update(faq);

        // commit new version
        Misc.commitRelation(root, relation, user);

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz" + relation.getUrl();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.FAQ, faq, absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        // refresh RSS
        FeedGenerator.updateFAQ();
        VariableFetcher.getInstance().refreshFaq();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    /**
     * Reverts current monitor state for the user on this faq.
     */
    protected String actionAlterMonitor(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item faq = (Item) persistance.findById(relation.getChild());
        User user = (User) env.get(Constants.VAR_USER);

        Date originalUpdated = faq.getUpdated();
        MonitorTools.alterMonitor(faq.getData().getRootElement(), user);
        persistance.update(faq);
        SQLTool.getInstance().setUpdatedTimestamp(faq, originalUpdated);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }


    // setters

    /**
     * Updates question from parameters. Changes are not synchronized with persistance.
     *
     * @param params map holding request's parameters
     * @param root   root element of discussion to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    static boolean setQuestion(Map params, Item faq, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TITLE);
        if (tmp != null && tmp.length() > 0) {
            if (tmp.indexOf("<") != -1) {
                params.put(PARAM_TITLE, "");
                ServletUtils.addError(PARAM_TITLE, "Pou¾ití HTML znaèek je zakázáno!", env, null);
                return false;
            }
            if (tmp.indexOf('\n') != -1)
                tmp = tmp.replace('\n', ' ');

            DocumentHelper.makeElement(root, "title").setText(tmp);
            faq.setSubType(Tools.limit(tmp, 30, ""));
        } else {
            ServletUtils.addError(PARAM_TITLE, "Zadejte titulek dotazu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates text of faq from parameters. Changes are not synchronized with persistance.
     *
     * @param params map holding request's parameters
     * @param root   root element of discussion to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    static boolean setText(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TEXT);
        if (tmp != null && tmp.length() > 0) {
            try {
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '" + tmp + "'", e);
                ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root, "text");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        } else {
            ServletUtils.addError(PARAM_TEXT, "Zadejte text odpovìdi!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates links from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setLinks(Map params, Element root, Map env) {
        Element links = (Element) root.element("links");
        if (links != null)
            links.detach();

        links = DocumentHelper.createElement("links");
        String url, title;
        boolean appendLinks = false;
        for (int i=1; i<=10; i++) {
            title = Misc.trimUndefined((String) params.get(PARAM_LINK_CAPTION+i));
            url = Misc.trimUndefined((String) params.get(PARAM_LINK_URL+i));
            if (title==null && url==null)
                break;
            if (title==null) {
                ServletUtils.addError(PARAM_LINK_CAPTION+i, "Zadejte titulek odkazu.", env, null);
                return false;
            }
            if (url==null) {
                ServletUtils.addError(PARAM_LINK_CAPTION+i, "Zadejte adresu odkazu.", env, null);
                return false;
            }

            appendLinks = true;
            Element link = links.addElement("link");
            link.setText(title);
            link.addAttribute("url", url);
        }
        if (appendLinks)
            root.add(links);
        return true;
    }
}
