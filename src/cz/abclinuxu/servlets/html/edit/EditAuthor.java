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
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import static cz.abclinuxu.servlets.Constants.PARAM_RELATION;
import static cz.abclinuxu.servlets.Constants.PARAM_NAME;

/**
 * This class is responsible for adding and
 * editing of software items and records.
 */
public class EditAuthor implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditAuthor.class);

    public static final String PARAM_SURNAME = "surname";
    public static final String PARAM_NICKNAME = "nickname";
    public static final String PARAM_BIRTH_NUMBER = "birthNumber";
    public static final String PARAM_ACCOUNT_NUMBER = "accountNumber";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PHONE = "phone";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_UID = "uid";
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_EDIT_MODE = "EDIT_MODE";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);
		Relation parent = new Relation(Constants.REL_AUTHORS);

		persistence.synchronize(parent);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

		Permissions permissions = Tools.permissionsFor(user, parent);

        if (action.equals(ACTION_ADD)) {
			if (!permissions.canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

			return actionAddStep1(request, response, env);
		}

        if (action.equals(ACTION_ADD_STEP2)) {
			if (!permissions.canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

			ActionProtector.ensureContract(request, EditAuthor.class, true, true, true, false);
			return actionAddStep2(request, response, env, true);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relationId!");

        persistence.synchronize(relation);
        persistence.synchronize(relation.getChild());
        env.put(VAR_RELATION,relation);

		// since we're editing an existing item
		// we'll check permissions against that
		permissions = Tools.permissionsFor(user, relation);

		if (!permissions.canModify())
			return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_EDIT))
            return actionEditStep1(request, env);

        if (action.equals(ACTION_EDIT_STEP2)) {
            ActionProtector.ensureContract(request, EditAuthor.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    public String actionAddStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return FMTemplateSelector.select("EditAuthor", "add", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
		Relation parent = (Relation) persistence.findById(new Relation(Constants.REL_AUTHORS));

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.AUTHOR);
        item.setData(document);
        item.setOwner(user.getId());

		Category cat = (Category) parent.getChild();
		item.setGroup(cat.getGroup());
		item.setPermissions(cat.getPermissions());

        boolean canContinue = true;
        canContinue &= setSurname(params, item, env);
        canContinue &= setFirstname(params, item);
        canContinue &= setNickname(params, root);
        canContinue &= setBirthNumber(params, root);
        canContinue &= setUserId(params, item, env);
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

        item.setTitle(Tools.getPersonName(item));
        persistence.create(item);

        Relation relation = new Relation(parent.getChild(), item, parent.getId());
        String url = proposeAuthorsUrl(item);
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);

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
        Author author = BeanFetcher.fetchAuthorFromItem((Item)relation.getChild(), FetchType.PROCESS_NONATOMIC);
                
        params.put(PARAM_SURNAME, author.getSurname());
        params.put(PARAM_NAME, author.getName());
        params.put(PARAM_NICKNAME, author.getNickname());
        params.put(PARAM_BIRTH_NUMBER, author.getBirthNumber());
        params.put(PARAM_LOGIN, author.getLogin());
        params.put(PARAM_ACCOUNT_NUMBER, author.getAccountNumber());
        params.put(PARAM_EMAIL, author.getEmail());
        params.put(PARAM_PHONE, author.getPhone());
        params.put(PARAM_ADDRESS, author.getAddress());

        env.put(VAR_EDIT_MODE, Boolean.TRUE);
        return FMTemplateSelector.select("EditAuthor", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setSurname(params, item, env);
        canContinue &= setFirstname(params, item);
        canContinue &= setNickname(params, root);
        canContinue &= setUserId(params, item, env);
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

        item.setTitle(Tools.getPersonName(item));
        persistence.update(item);

        String url = proposeAuthorsUrl(item);
        if (! url.equals(relation.getUrl())) {
            url = URLManager.protectFromDuplicates(url);
            sqlTool.insertOldAddress(relation.getUrl(), url, relation.getId());
            relation.setUrl(url);
            persistence.update(relation);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    /* ******** setters ********* */

    /**
     * @return absolute url for this author
     */
    private String proposeAuthorsUrl(Item item) {
    	StringBuilder sb = new StringBuilder();
		if(item.getString1()!=null) sb.append(item.getString1()).append(" ");
		if(item.getString2()!=null) sb.append(item.getString2());
		
        String url = UrlUtils.PREFIX_AUTHORS + "/" + URLManager.enforceRelativeURL(sb.toString());
        return url;
    }

    /**
     * Updates surname from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean setSurname(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_SURNAME);
        if (!Misc.empty(tmp)) {
            tmp = Misc.filterDangerousCharacters(tmp);
            item.setString2(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_SURNAME, "Zadejte příjmení!", env, null);
            return false;
        }
    }

    /**
     * Updates first name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @return false, if there is a major error.
     */
    private boolean setFirstname(Map params, Item item) {
        String tmp = (String) params.get(PARAM_NAME);        
        if (Misc.empty(tmp)) {
        	item.setString1(null);
        }
        else {
        	tmp = Misc.filterDangerousCharacters(tmp);
        	item.setString1(tmp);
        }
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
     * @param item author's item to be updated
     * @return false, if there is a major error.
     */
    private boolean setUserId(Map params, Item item, Map env) {
        String login = (String) params.get(PARAM_LOGIN);
        if (Misc.empty(login)) {
            item.setNumeric1(null);
        } else {
            Integer uid = SQLTool.getInstance().getUserByLogin(login);
            if (uid == null) {
                ServletUtils.addError(PARAM_LOGIN, "Zadejte login!", env, null);
                return false;
            }
            item.setNumeric1(uid);
        }
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
