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

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.safehtml.NewsGuard;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.htmlparser.util.ParserException;

/**
 * User: literakl
 * Date: 5.7.2005
 */
public class EditGuestBook implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditGuestBook.class);

    public static final String PARAM_NAME = "name";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_RELATION = "rid";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    public static final String VAR_RELATION = "RELATION";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);

        if (ACTION_ADD.equals(action) || action==null)
            return FMTemplateSelector.select("EditGuestBook", "add", env, request);

        if (ACTION_ADD_STEP2.equals(action))
            return actionGuestBookEntry(request, response, env);

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if (!user.hasRole(Roles.ROOT))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr rid!");

        relation = (Relation) persistance.findById(relation);
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        if (ACTION_EDIT.equals(action))
            return actionEditGuestBookEntry(request, relation, env);

        if (ACTION_EDIT_STEP2.equals(action))
            return actionEditGuestBookEntryStep2(request, response, relation, env);

        return null;
    }

    private String actionGuestBookEntry(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item(0, Item.GUESTBOOK);
        if (user != null)
            item.setOwner(user.getId());

        Document document = DocumentHelper.createDocument();
        item.setData(document);
        Element root = document.addElement("data");

        boolean canContinue = true;
        canContinue &= setAuthor(params, root, env);
        canContinue &= setMessage(params, root, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditGuestBook", "add", env, request);

        persistance.create(item);
        Relation relation = new Relation(new Category(Constants.CAT_GUESTBOOK), item, Constants.REL_GUESTBOOK);
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/kniha_navstev");
        return null;
    }

    private String actionEditGuestBookEntry(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Element element = root.element("author");
        params.put(PARAM_NAME, element.getText());
        element = root.element("message");
        params.put(PARAM_MESSAGE, element.getText());

        return FMTemplateSelector.select("EditGuestBook", "edit", env, request);
    }

    private String actionEditGuestBookEntryStep2(HttpServletRequest request, HttpServletResponse response, Relation relation, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setAuthor(params, root, env);
        canContinue &= setMessage(params, root, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditGuestBook", "edit", env, request);

        persistance.update(item);
        AdminLogger.logEvent(user, "edited guestbook entry "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/kniha_navstev");
        return null;
    }

    // setters

    /**
     * Sets message of the guest book entry. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   XML document
     * @return false, if there is a major error.
     */
    boolean setMessage(Map params, Element root, Map env) {
        String content = (String) params.get(PARAM_MESSAGE);
        if (Misc.empty(content)) {
            ServletUtils.addError(PARAM_MESSAGE, "Prosím zadejte vá¹ vzkaz.", env, null);
            return false;
        }

        try {
            NewsGuard.check(content);
        } catch (ParserException e) {
            log.error("ParseException on '" + content + "'", e);
            ServletUtils.addError(PARAM_MESSAGE, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_MESSAGE, e.getMessage(), env, null);
            return false;
        }

        Element tagContent = root.element("message");
        if (tagContent == null)
            tagContent = root.addElement("message");
        tagContent.setText(content);
        return true;
    }

    /**
     * Sets name of guest book entry author. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   XML document
     * @return false, if there is a major error.
     */
    boolean setAuthor(Map params, Element root, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_NAME, "Prosím zadejte va¹e jméno.", env, null);
            return false;
        }
        if (name.indexOf('<')!=-1) {
            ServletUtils.addError(PARAM_NAME, "Jméno nesmí obsahovat HTML znaèky.", env, null);
            return false;
        }

        Element tagContent = root.element("author");
        if (tagContent == null)
            tagContent = root.addElement("author");
        tagContent.setText(name);
        return true;
    }
}
