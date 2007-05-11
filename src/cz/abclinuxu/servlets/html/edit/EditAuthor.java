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
import cz.abclinuxu.data.Category;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class is responsible for adding and
 * editing of software items and records.
 */
public class EditAuthor implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditAuthor.class);

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_SURNAME = "surname";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_NICKNAME = "nickname";
    public static final String PARAM_BIRTH_NUMBER = "birthNumber";
    public static final String PARAM_ACCOUNT_NUMBER = "accountNumber";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PHONE = "phone";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_UID = "uid";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_EDIT_MODE = "EDIT_MODE";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (action == null)
            throw new MissingArgumentException("ChybĂ­ parametr action!");

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (!user.hasRole(Roles.ARTICLE_ADMIN))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_ADD))
            return actionAddStep1(request, response, env);

        if (action.equals(ACTION_ADD_STEP2)) {
            ActionProtector.ensureContract(request, EditAuthor.class, true, true, true, false);
            return actionAddStep2(request, response, env, true);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("ChybĂ­ parametr relationId!");

        persistence.synchronize(relation);
        persistence.synchronize(relation.getChild());
        env.put(VAR_RELATION,relation);

        if (action.equals(ACTION_EDIT))
            return actionEditStep1(request, env);

        if (action.equals(ACTION_EDIT_STEP2)) {
            ActionProtector.ensureContract(request, EditAuthor.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        throw new MissingArgumentException("ChybĂ­ parametr action!");
    }

    public String actionAddStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return FMTemplateSelector.select("EditAuthor", "add", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.AUTHOR);
        item.setData(document);
        item.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setSurname(params, root, env);
        canContinue &= setFirstname(params, root);
        canContinue &= setNickname(params, root);
        canContinue &= setBirthNumber(params, root);
        canContinue &= setUserId(params, root);
        canContinue &= setAccountNumber(params, root);
        canContinue &= setEmail(params, root);
        canContinue &= setPhone(params, root);
        canContinue &= setAddress(params, root);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditAuthor", "add", env, request);
        }

        persistence.create(item);

        Relation relation = new Relation(new Category(Constants.CAT_AUTHORS), item, Constants.REL_AUTHORS);
        String url = proposeAuthorsUrl(root);
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);

        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        associateWithUser(root, persistence, relation);

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

        Node node = root.element("surname");
        params.put(PARAM_SURNAME, node.getText());
        node = root.element("firstname");
        if (node != null)
            params.put(PARAM_NAME, node.getText());
        node = root.element("nickname");
        if (node != null)
            params.put(PARAM_NICKNAME, node.getText());
        node = root.element("birthNumber");
        if (node != null)
            params.put(PARAM_BIRTH_NUMBER, node.getText());
        node = root.element("uid");
        if (node != null)
            params.put(PARAM_UID, node.getText());
        node = root.element("accountNumber");
        if (node != null)
            params.put(PARAM_ACCOUNT_NUMBER, node.getText());
        node = root.element("email");
        if (node != null)
            params.put(PARAM_EMAIL, node.getText());
        node = root.element("phone");
        if (node != null)
            params.put(PARAM_PHONE, node.getText());
        node = root.element("address");
        if (node != null)
            params.put(PARAM_ADDRESS, node.getText());

        env.put(VAR_EDIT_MODE, Boolean.TRUE);
        return FMTemplateSelector.select("EditAuthor", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setSurname(params, root, env);
        canContinue &= setFirstname(params, root);
        canContinue &= setNickname(params, root);
        canContinue &= setUserId(params, root);
        canContinue &= setBirthNumber(params, root);
        canContinue &= setAccountNumber(params, root);
        canContinue &= setEmail(params, root);
        canContinue &= setPhone(params, root);
        canContinue &= setAddress(params, root);

        if (! canContinue || params.get(PARAM_PREVIEW) != null) {
            if (! canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            env.put(VAR_EDIT_MODE, Boolean.TRUE);
            return FMTemplateSelector.select("EditAuthor", "edit", env, request);
        }

        persistence.update(item);

        String url = proposeAuthorsUrl(root);
        if (! url.equals(relation.getUrl())) {
            url = URLManager.protectFromDuplicates(url);
            sqlTool.insertOldAddress(relation.getUrl(), url, relation.getId());
            relation.setUrl(url);
            persistence.update(relation);
        }

        associateWithUser(root, persistence, relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    /* ******** setters ********* */

    /**
     * @return absolute url for this author
     */
    private String proposeAuthorsUrl(Element root) {
        StringBuffer sb = new StringBuffer();
        String name = root.elementTextTrim("firstname");
        if ( name != null )
            sb.append(name).append(' ');
        sb.append(root.elementTextTrim("surname"));

        String url = UrlUtils.PREFIX_AUTHORS + "/" + URLManager.enforceRelativeURL(sb.toString());
        return url;
    }

    /**
     * Creates link to user
     */
    private void associateWithUser(Element root, Persistence persistence, Relation relation) {
        String uid = root.elementText("uid");
        if (uid != null) {
            int id = Misc.parseInt(uid, 0);
            User associatedUser = (User) persistence.findById(new User(id));
            Document document = associatedUser.getData();
            Element element = (Element) document.selectSingleNode("/system/author_id");
            if (element == null) {
                element = DocumentHelper.makeElement(document, "/system/author_id");
                element.setText(Integer.toString(relation.getId()));
                persistence.update(associatedUser);
            }
        }
    }

    /**
     * Updates surname from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean setSurname(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_SURNAME);
        if (tmp != null && tmp.length() > 0) {
            tmp = Misc.filterDangerousCharacters(tmp);
            DocumentHelper.makeElement(root, "surname").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_SURNAME, "Zadejte pĹĂ­jmenĂ­!", env, null);
            return false;
        }
    }

    /**
     * Updates first name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @return false, if there is a major error.
     */
    private boolean setFirstname(Map params, Element root) {
        String tmp = (String) params.get(PARAM_NAME);
        Element element = root.element("firstname");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "firstname");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates nickname from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @return false, if there is a major error.
     */
    private boolean setNickname(Map params, Element root) {
        String tmp = (String) params.get(PARAM_NICKNAME);
        Element element = root.element("nickname");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "nickname");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates birth number from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setBirthNumber(Map params, Element root) {
        String tmp = (String) params.get(PARAM_BIRTH_NUMBER);
        Element element = root.element("birthNumber");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "birthNumber");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates account number from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setAccountNumber(Map params, Element root) {
        String tmp = (String) params.get(PARAM_ACCOUNT_NUMBER);
        Element element = root.element("accountNumber");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "accountNumber");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates user id from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setUserId(Map params, Element root) {
        String tmp = (String) params.get(PARAM_UID);
        Element element = root.element("uid");
        if (Misc.empty(tmp)) {
            if (element != null) {
                int id = Misc.parseInt(element.getText(), 0);
                element.detach();

                Persistence persistence = PersistenceFactory.getPersistance();
                try {
                    User associatedUser = (User) persistence.findById(new User(id));
                    Document document = associatedUser.getData();
                    element = (Element) document.selectSingleNode("/system/author_id");
                    if (element != null) {
                        element.detach();
                        persistence.update(associatedUser);
                    }
                } catch (NotFoundException e) {
                    // user has been probably deleted
                }
            }
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "uid");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates email address from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setEmail(Map params, Element root) {
        String tmp = (String) params.get(PARAM_EMAIL);
        Element element = root.element("email");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "email");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates phone number from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setPhone(Map params, Element root) {
        String tmp = (String) params.get(PARAM_PHONE);
        Element element = root.element("phone");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "phone");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }

    /**
     * Updates address from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setAddress(Map params, Element root) {
        String tmp = (String) params.get(PARAM_ADDRESS);
        Element element = root.element("address");
        if (Misc.empty(tmp)) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "address");

        tmp = Misc.filterDangerousCharacters(tmp);
        element.setText(tmp);
        return true;
    }
}
