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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to define and edit advertisement areas and their codes.
 * @author literakl
 * @since 20.12.2006
 */
public class EditAdvertisement implements AbcAction, Configurable {
    public static final String PREF_IDENTIFIER_REGEXP = "regexp.valid.identifier";

    public static final String PARAM_NAME = "name";
    public static final String PARAM_IDENTIFIER = "identifier";
    public static final String PARAM_NEW_IDENTIFIER = "newIdentifier";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_MAIN_CODE = "main_code";

    public static final String VAR_POSITION = "POSITION";
    public static final String VAR_POSITIONS = "POSITIONS";

    public static final String ACTION_ADD_POSITION = "addPosition";
    public static final String ACTION_ADD_POSITION_STEP2 = "addPosition2";
    public static final String ACTION_EDIT_POSITION = "editPosition";
    public static final String ACTION_EDIT_POSITION_STEP2 = "editPosition2";
    public static final String ACTION_REMOVE_POSITION = "rmPosition";
    public static final String ACTION_ACTIVATE_POSITION = "activatePosition";
    public static final String ACTION_DEACTIVATE_POSITION = "deactivatePosition";
    public static final String ACTION_SHOW_POSITION = "showPosition";
    public static final String ACTION_TEST_URL = "testUrl";
    public static final String ACTION_ADD_CODE = "addCode";
    public static final String ACTION_ADD_CODE_STEP2 = "addCode2";
    public static final String ACTION_EDIT_CODE = "editCode";
    public static final String ACTION_EDIT_CODE_STEP2 = "editCode2";
    public static final String ACTION_REMOVE_CODE = "rmCode";

    static Pattern identifierPattern;
    static {
        EditAdvertisement instance = new EditAdvertisement();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (!user.hasRole(Roles.ADVERTISEMENT_ADMIN))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_ADD_POSITION.equals(action) || params.containsKey(ACTION_ADD_POSITION))
            return actionAddStep1(request, env);

        if (ACTION_ADD_POSITION_STEP2.equals(action) || params.containsKey(ACTION_ADD_POSITION_STEP2))
            return actionAddStep2(request, response, env);

        if (ACTION_SHOW_POSITION.equals(action) || params.containsKey(ACTION_SHOW_POSITION))
            return actionShowPosition(request, env);

        if (ACTION_EDIT_POSITION.equals(action) || params.containsKey(ACTION_EDIT_POSITION))
            return actionEditPositionStep1(request, response, env);

        if (ACTION_EDIT_POSITION_STEP2.equals(action) || params.containsKey(ACTION_EDIT_POSITION_STEP2))
            return actionEditPositionStep2(request, response, env);

        if (ACTION_ACTIVATE_POSITION.equals(action) || params.containsKey(ACTION_ACTIVATE_POSITION))
            return actionChangePositionState(request, response, true, env);

        if (ACTION_DEACTIVATE_POSITION.equals(action) || params.containsKey(ACTION_DEACTIVATE_POSITION))
            return actionChangePositionState(request, response, false, env);

        if (ACTION_REMOVE_POSITION.equals(action) || params.containsKey(ACTION_REMOVE_POSITION))
            return actionRemovePosition(request, response, env);

        return actionShowMain(request, env);
    }

    public String actionShowMain(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
        Element element = (Element) item.getData().selectSingleNode("//advertisement");
        if (element != null) {
            List list = element.elements("position");
            if (list.size() > 0)
                env.put(VAR_POSITIONS, list);
        }
        return FMTemplateSelector.select("EditAdvertisement", "main", env, request);
    }

    public String actionShowPosition(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        String id = (String) params.get(PARAM_IDENTIFIER);
        if (Misc.empty(id)) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chybí povinný parametr "+PARAM_IDENTIFIER, env, null);
            return actionShowMain(request, env);
        }

        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
        Element element = (Element) item.getData().selectSingleNode("//advertisement/position[@id='" + id + "']");
        if (element == null)
            ServletUtils.addError(Constants.ERROR_GENERIC, "Pozice '" + id + "' nebyla nalezena!", env, null);

        env.put(VAR_POSITION, element);
        return FMTemplateSelector.select("EditAdvertisement", "showPosition", env, request);
    }

    public String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        params.clear();
        return FMTemplateSelector.select("EditAdvertisement", "addPosition", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION)).clone();
        Document document = item.getData();
        Element root = document.getRootElement();
        Element position = createNewPosition(params, root, env);
        if (position == null)
            return FMTemplateSelector.select("EditAdvertisement", "addPosition", env, request);
        String id = position.attributeValue("id");

        boolean canContinue = true;
        canContinue &= setName(params, position, env);
        canContinue &= setPositionDescription(params, position);
        canContinue &= setDefaultCode(params, position);

        if (! canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "addPosition", env, request);

        persistence.update(item);

        AdminLogger.logEvent(user, "pridal reklamni pozici " + id);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditAdvertisement?action=" + ACTION_SHOW_POSITION+"&"+PARAM_IDENTIFIER+"=" + id);
        return null;
    }

    public String actionEditPositionStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        String id = (String) params.get(PARAM_IDENTIFIER);
        if (id == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nebyl definován povinný parametr id!", env, request.getSession());
            urlUtils.redirect(response, "/EditAdvertisement");
            return null;
        }

        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
        Document document = item.getData();
        Element adsRoot = document.getRootElement().element("advertisement");
        Element position = (Element) adsRoot.selectSingleNode("position[@id='" + id + "']");
        if (position == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Pozice " + id + " nebyla nalezena!", env, request.getSession());
            urlUtils.redirect(response, "/EditAdvertisement");
            return null;
        }

        Element element = position.element("name");
        params.put(PARAM_NAME, element.getText());
        params.put(PARAM_NEW_IDENTIFIER, id);
        element = position.element("description");
        if (element != null)
            params.put(PARAM_DESCRIPTION, element.getText());
        element = (Element) position.selectSingleNode("code[string-length(@id)=0]");
        if (element != null)
            params.put(PARAM_MAIN_CODE, element.getText());

        return FMTemplateSelector.select("EditAdvertisement", "editPosition", env, request);
    }

    public String actionEditPositionStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        String id = (String) params.get(PARAM_IDENTIFIER);
        if (id == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nebyl definován povinný parametr id!", env, request.getSession());
            urlUtils.redirect(response, "/EditAdvertisement");
            return null;
        }

        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION)).clone();
        Document document = item.getData();
        Element adsRoot = document.getRootElement().element("advertisement");
        Element position = (Element) adsRoot.selectSingleNode("position[@id='" + id + "']");
        if (position == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Pozice " + id + " nebyla nalezena!", env, request.getSession());
            urlUtils.redirect(response, "/EditAdvertisement");
            return null;
        }

        boolean canContinue = true;
        canContinue &= setName(params, position, env);
        canContinue &= setIdentifier(params, adsRoot, position, env);
        canContinue &= setPositionDescription(params, position);
        canContinue &= setDefaultCode(params, position);

        if (! canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "editPosition", env, request);

        persistence.update(item);

        AdminLogger.logEvent(user, "upravil reklamni pozici " + id);

        urlUtils.redirect(response, "/EditAdvertisement?action=" + ACTION_SHOW_POSITION+"&"+PARAM_IDENTIFIER+"=" + id);
        return null;
    }

    public String actionChangePositionState(HttpServletRequest request, HttpServletResponse response, boolean activate, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION)).clone();
        Document document = item.getData();
        Element adsRoot = document.getRootElement().element("advertisement");

        List ids = Tools.asList(params.get(PARAM_IDENTIFIER));
        String id = null;
        for (Iterator iter = ids.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (!(o instanceof String))
                continue;
            id = (String) o;
            Element position = (Element) adsRoot.selectSingleNode("position[@id='" + id + "']");
            if (position == null) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Pozice "+id+" nebyla nalezena!", env, request.getSession());
                continue;
            }
            position.attribute("active").setText(activate ? "yes":"no");
            AdminLogger.logEvent(user, (activate ? "":"de") + "aktivoval reklamni pozici " + id);
        }

        persistence.update(item);

        String url = request.getHeader("Referer");
        if (url == null)
            url = "/EditAdvertisement";
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    public String actionRemovePosition(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION)).clone();
        Document document = item.getData();
        Element adsRoot = document.getRootElement().element("advertisement");

        List ids = Tools.asList(params.get(PARAM_IDENTIFIER));
        String id = null;
        for (Iterator iter = ids.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (!(o instanceof String))
                continue;
            id = (String) o;
            Element position = (Element) adsRoot.selectSingleNode("position[@id='" + id + "']");
            if (position == null) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Pozice " + id + " nebyla nalezena!", env, request.getSession());
                continue;
            }
            position.detach();
            AdminLogger.logEvent(user, "smazal reklamni pozici " + id);
        }

        persistence.update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditAdvertisement");
        return null;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String regexp = prefs.get(PREF_IDENTIFIER_REGEXP, null);
        identifierPattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }

    // setters

    /**
     * Creates new position element. It verifies that valid identifier exists and that it does
     * not conflict with other position. It returns null in case of error.
     * @return element with identifier set or null
     */
    private Element createNewPosition(Map params, Element root, Map env) {
        String id = (String) params.get(PARAM_IDENTIFIER);
        if (id == null) {
            ServletUtils.addError(PARAM_IDENTIFIER, "Zadejte identifikátor.", env, null);
            return null;
        }

        Matcher matcher = identifierPattern.matcher(id);
        if (! matcher.matches() ) {
            ServletUtils.addError(PARAM_IDENTIFIER, "Neplatný identifikátor.", env, null);
            return null;
        }

        Element adsRoot = DocumentHelper.makeElement(root, "advertisement");
        Element position = (Element) adsRoot.selectSingleNode("position[@id='"+id+"']");
        if (position != null) {
            ServletUtils.addError(PARAM_IDENTIFIER, "Tento identifikátor je ji¾ pou¾it, zadejte jiný.", env, null);
            return null;
        }

        position = adsRoot.addElement("position");
        position.addAttribute("id", id);
        position.addAttribute("active", "yes");
        return position;
    }

    /**
     * Updates position name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param position position element to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Element position, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_NAME, "Zadejte jméno.", env, null);
            return false;
        }

        DocumentHelper.makeElement(position, "name").setText(name);
        return true;
    }

    /**
     * Updates position name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param position position element to be updated
     * @return false, if there is a major error.
     */
    private boolean setDefaultCode(Map params, Element position) {
        String code = (String) params.get(PARAM_MAIN_CODE);
        if (Misc.empty(code))
            code = "<!-- empty -->";

        Element element = (Element) position.selectSingleNode("code[string-length(@id)=0]");
        if (element == null)
            element = position.addElement("code");
        element.setText(code);
        return true;
    }

    /**
     * Updates position description from parameters. Changes are not synchronized with persistance.
     * @param params   map holding request's parameters
     * @param position position element to be updated
     * @return false, if there is a major error.
     */
    private boolean setPositionDescription(Map params, Element position) {
        String name = (String) params.get(PARAM_DESCRIPTION);
        if (Misc.empty(name)) {
            Element element = position.element("description");
            if (element != null)
                element.detach();
        }

        DocumentHelper.makeElement(position, "description").setText(name);
        return true;
    }

    /**
     * Updates position identifier from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param adsRoot container for all positions
     * @param position position element to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setIdentifier(Map params, Element adsRoot, Element position, Map env) {
        String id = (String) params.get(PARAM_NEW_IDENTIFIER);
        if (id == null) {
            ServletUtils.addError(PARAM_NEW_IDENTIFIER, "Zadejte identifikátor.", env, null);
            return false;
        }

        Matcher matcher = identifierPattern.matcher(id);
        if (!matcher.matches()) {
            ServletUtils.addError(PARAM_NEW_IDENTIFIER, "Neplatný identifikátor.", env, null);
            return false;
        }

        Element existingPosition = (Element) adsRoot.selectSingleNode("position[@id='" + id + "']");
        if (! existingPosition.equals(position)) {
            ServletUtils.addError(PARAM_NEW_IDENTIFIER, "Tento identifikátor je ji¾ pou¾it pro pozici "+existingPosition.elementText("name")+".", env, null);
            return false;
        }

        position.attribute("id").setText(id);
        return true;
    }
}
