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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Attribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Node;
import static cz.abclinuxu.servlets.Constants.PARAM_NAME;
import static cz.abclinuxu.servlets.Constants.PARAM_DESCRIPTION;

/**
 * Used to define and edit advertisement areas and their codes.
 * @author literakl
 * @since 20.12.2006
 */
public class EditAdvertisement implements AbcAction, Configurable {
    public static final String PREF_IDENTIFIER_REGEXP = "regexp.valid.identifier";

    public static final String PARAM_IDENTIFIER = "identifier";
    public static final String PARAM_NEW_IDENTIFIER = "newIdentifier";
    public static final String PARAM_MAIN_CODE = "main_code";
    public static final String PARAM_CODE = "code";
    public static final String PARAM_HTML_CODE = "htmlCode";
    public static final String PARAM_REGEXP = "regexp";
    public static final String PARAM_DYNAMIC = "dynamic";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_VARIANT = "variant";
    public static final String PARAM_TAGS = "tags";
    public static final String PARAM_MODE = "mode";

    public static final String VAR_POSITION = "POSITION";
    public static final String VAR_POSITIONS = "POSITIONS";
    public static final String VAR_DEFAULT_CODE = "DEFAULT_CODE";
    public static final String VAR_CODES = "CODES";
    public static final String VAR_CODE = "CODE";
    public static final String VAR_VARIANTS = "VARIANTS";

    public static final String ACTION_LIST = "list";
    public static final String ACTION_ADD_POSITION = "addPosition";
    public static final String ACTION_ADD_POSITION_STEP2 = "addPosition2";
    public static final String ACTION_EDIT_POSITION = "editPosition";
    public static final String ACTION_EDIT_POSITION_STEP2 = "editPosition2";
    public static final String ACTION_REMOVE_POSITION = "rmPosition";
    public static final String ACTION_ACTIVATE_POSITION = "activatePosition";
    public static final String ACTION_DEACTIVATE_POSITION = "deactivatePosition";
    public static final String ACTION_SHOW_POSITION = "showPosition";
    public static final String ACTION_SHOW_CODE = "showCode";
    public static final String ACTION_ADD_VARIANT = "addVariant";
    public static final String ACTION_ADD_VARIANT_STEP2 = "addVariant2";
    public static final String ACTION_EDIT_VARIANT = "editVariant";
    public static final String ACTION_EDIT_VARIANT_STEP2 = "editVariant2";
    public static final String ACTION_REMOVE_VARIANT = "rmVariant";
    public static final String ACTION_TEST_URL = "testUrl";
    public static final String ACTION_ADD_CODE = "addCode";
    public static final String ACTION_ADD_CODE_STEP2 = "addCode2";
    public static final String ACTION_EDIT_CODE = "editCode";
    public static final String ACTION_EDIT_CODE_STEP2 = "editCode2";
    public static final String ACTION_REMOVE_CODE = "rmCode";
    public static final String ACTION_TOGGLE_VARIANT = "toggleVariant";

    static Pattern identifierPattern;
    static {
        EditAdvertisement instance = new EditAdvertisement();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);
        Persistence persistence = PersistenceFactory.getPersistence();

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (!user.hasRole(Roles.ADVERTISEMENT_ADMIN))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation != null) {
            relation = (Relation) Tools.sync(relation);
            env.put(ShowObject.VAR_RELATION, relation);
        }

        if (ACTION_LIST.equals(action) || Misc.empty(action))
            return actionShowMain(request, env);
        
        if (ACTION_ADD_POSITION.equals(action))
            return actionAddStep1(request, env);

        if (ACTION_ADD_POSITION_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionAddStep2(request, response, env);
        }
        
        if (relation == null)
            throw new MissingArgumentException("Chybi cislo relace!");

        if (ACTION_SHOW_POSITION.equals(action))
            return actionShowPosition(request, response, env);

        if (ACTION_EDIT_POSITION.equals(action) || params.containsKey(ACTION_EDIT_POSITION))
            return actionEditPositionStep1(request, response, env);

        if (ACTION_EDIT_POSITION_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionEditPositionStep2(request, response, env);
        }

        if (ACTION_ACTIVATE_POSITION.equals(action) || params.containsKey(ACTION_ACTIVATE_POSITION)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionChangePositionState(request, response, true, env);
        }

        if (ACTION_DEACTIVATE_POSITION.equals(action) || params.containsKey(ACTION_DEACTIVATE_POSITION)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionChangePositionState(request, response, false, env);
        }

        if (ACTION_REMOVE_POSITION.equals(action) || params.containsKey(ACTION_REMOVE_POSITION)) {
            return actionRemovePosition(request, response, env);
        }

        if (ACTION_ADD_CODE.equals(action) || params.containsKey(ACTION_ADD_CODE))
            return actionAddCodeStep1(request, env);

        if (ACTION_ADD_CODE_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionAddCodeStep2(request, response, env);
        }
        
        if (ACTION_ADD_VARIANT.equals(action))
            return FMTemplateSelector.select("EditAdvertisement", "addVariant", env, request);
        
        if (ACTION_ADD_VARIANT_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionAddVariantStep2(request, response, env);
        }
        
        if (ACTION_EDIT_VARIANT.equals(action))
            return actionEditVariant(request, response, env);
        
        if (ACTION_EDIT_VARIANT_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionEditVariantStep2(request, response, env);
        }
            
        if (ACTION_SHOW_CODE.equals(action))
            return actionShowCode(request, response, env);
        
        if (ACTION_REMOVE_VARIANT.equals(action)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, false, true);
            return actionRemoveVariant(request, response, env);
        }
        
        if (ACTION_TOGGLE_VARIANT.equals(action))
            return actionToggleVariant(request, response, env);
        
        if (ACTION_EDIT_CODE.equals(action) || params.containsKey(ACTION_EDIT_CODE))
            return actionEditCodeStep1(request, response, env);
        
        if (ACTION_EDIT_CODE_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, true, false);
            return actionEditCodeStep2(request, response, env);
        }
        
        if (ACTION_REMOVE_CODE.equals(action) || params.containsKey(ACTION_REMOVE_CODE)) {
            ActionProtector.ensureContract(request, EditAdvertisement.class, true, true, false, true);
            return actionRemoveCode(request, response, env);
        }
        
        throw new MissingArgumentException("Neplatny parametr action!");
    }

    public String actionShowMain(HttpServletRequest request, Map env) throws Exception {
        Category catAdvertisements = (Category) Tools.sync(new Category(Constants.CAT_ADVERTISEMENTS));
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String mode = (String) params.get(PARAM_MODE);
        boolean modeActive;
        
        if (Misc.empty(mode) || mode.equals("active"))
            modeActive = true;
        else
            modeActive = false;
        
        List<Relation> children = Tools.syncList(catAdvertisements.getChildren());
        
        for (Iterator<Relation> it = children.iterator(); it.hasNext();) {
            Item item = (Item) it.next().getChild();
            String active = Tools.xpath(item, "/data/active");
            
            if ("no".equals(active) == modeActive)
                it.remove();
        }
        
        Sorters2.byName(children);
        env.put(VAR_POSITIONS, children);
        
        List parents = new ArrayList(2);
        parents.add(new Link("Administrace", "/Admin", null));
        parents.add(new Link("Reklamy", "/EditAdvertisement", null));
        env.put(ShowObject.VAR_PARENTS, parents);
        
        return FMTemplateSelector.select("EditAdvertisement", "main", env, request);
    }

    public String actionShowPosition(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation rel = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) rel.getChild();
        
        List parents = new ArrayList(3);
        parents.add(new Link("Administrace", "/Admin", null));
        parents.add(new Link("Reklamy", "/EditAdvertisement", null));
        parents.add(new Link(item.getTitle(), "/EditAdvertisement/"+rel.getId()+"?action=showPosition", null));
        env.put(ShowObject.VAR_PARENTS, parents);

        env.put(VAR_POSITION, item);
        return FMTemplateSelector.select("EditAdvertisement", "showPosition", env, request);
    }

    public String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        params.clear();
        return FMTemplateSelector.select("EditAdvertisement", "addPosition", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item();
        item.setType(Item.ADVERTISEMENT);
        
        Category catUpper = (Category) persistence.findById(new Category(Constants.CAT_ADVERTISEMENTS));
        
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        String id = (String) params.get(PARAM_NEW_IDENTIFIER);
        
        item.setData(document);

        boolean canContinue = true;
        canContinue &= setName(params, item, env);
        canContinue &= setIdentifier(params, item, env);
        canContinue &= setPositionDescription(params, root);
        DocumentHelper.makeElement(root, "active").setText("yes");
        root.addElement("codes");

        if (! canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "addPosition", env, request);

        persistence.create(item);
        
        Relation relation = new Relation(catUpper, item, Constants.REL_ADVERTISEMENTS);
        persistence.create(relation);

        AdminLogger.logEvent(user, "pridal reklamni pozici " + id);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditAdvertisement?action=" + ACTION_SHOW_POSITION+"&"+PARAM_RELATION_SHORT+"=" + relation.getId());
        return null;
    }
    
    public String actionShowCode(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        
        List<Node> codes = item.getData().selectNodes("//code");
        int index = Misc.parseInt((String) params.get(PARAM_CODE), -1);
        
        if (index < 0 || index >= codes.size()) {
            urlUtils.redirect(response, "/EditAdvertisement?action=list");
            return null;
        }
        
        Element code = (Element) codes.get(index);
        List<Element> variants = code.selectNodes("variants/variant");
        List<Map> variantsData = new ArrayList<Map>(variants.size());
        
        for (Element elem : variants) {
            Map map = new HashMap();
            
            map.put("code", elem.getText());
            map.put("description", elem.attributeValue("description"));
            map.put("active", elem.attributeValue("active"));
            
            String tags = elem.attributeValue("tags");
            if (!Misc.empty(tags))
                map.put("tags", tags.split(" "));
            
            variantsData.add(map);
        }
        
        env.put(VAR_VARIANTS, variantsData);
        
        Map codeData = new HashMap();
        codeData.put("name", code.attributeValue("name"));
        codeData.put("description", code.attributeValue("description"));
        codeData.put("regexp", code.attributeValue("regexp"));
        
        env.put(VAR_CODE, codeData);
        
        List parents = new ArrayList(4);
        parents.add(new Link("Administrace", "/Admin", null));
        parents.add(new Link("Reklamy", "/EditAdvertisement", null));
        parents.add(new Link(item.getTitle(), "/EditAdvertisement/"+relation.getId()+"?action=showPosition", null));
        parents.add(new Link((String) codeData.get("name"), "/EditAdvertisement/"+relation.getId()+"?action=showCode&code="+params.get(PARAM_CODE), null));
        env.put(ShowObject.VAR_PARENTS, parents);
        
        return FMTemplateSelector.select("EditAdvertisement", "showCode", env, request);
    }
    

    public String actionEditPositionStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation rel = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) rel.getChild();

        Element root = item.getData().getRootElement();
        params.put(PARAM_NAME, item.getTitle());
        params.put(PARAM_NEW_IDENTIFIER, item.getString1());
        Element element = root.element("description");
        if (element != null)
            params.put(PARAM_DESCRIPTION, element.getText());

        return FMTemplateSelector.select("EditAdvertisement", "editPosition", env, request);
    }

    public String actionEditPositionStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation rel = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) rel.getChild();

        boolean canContinue = true;
        canContinue &= setName(params, item, env);
        canContinue &= setIdentifier(params, item, env);
        canContinue &= setPositionDescription(params, item.getData().getRootElement());

        if (! canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "editPosition", env, request);

        persistence.update(item);

        String id = (String) params.get(PARAM_IDENTIFIER);
        AdminLogger.logEvent(user, "upravil reklamni pozici " + id);

        urlUtils.redirect(response, "/EditAdvertisement?action=" + ACTION_SHOW_POSITION+"&"+PARAM_RELATION_SHORT+"=" + rel.getId());
        return null;
    }

    public String actionChangePositionState(HttpServletRequest request, HttpServletResponse response, boolean activate, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);

        Relation rel = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) rel.getChild();

        DocumentHelper.makeElement(item.getData(), "/data/active").setText(activate ? "yes":"no");
        AdminLogger.logEvent(user, (activate ? "":"de") + "aktivoval reklamni pozici " + item.getString1());

        persistence.update(item);

        String url = request.getHeader("Referer");
        if (url == null)
            url = "/EditAdvertisement";
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    public String actionRemovePosition(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);

        Relation rel = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) rel.getChild();
            
        AdminLogger.logEvent(user, "smazal reklamni pozici " + item.getString1());

        persistence.remove(rel);
        persistence.remove(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/EditAdvertisement?action=list");
        return null;
    }

    public String actionAddCodeStep1(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("EditAdvertisement", "addCode", env, request);
    }

    public String actionAddCodeStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element position = item.getData().getRootElement();

        boolean canContinue = true;
        Element codes = DocumentHelper.makeElement(position, "codes");
        Element code = codes.addElement("code");
        canContinue &= setCodeName(params, code, null, env);
        canContinue &= setCodeRegexp(params, code, env);
        canContinue &= setCodeDescription(params, code);
        
        Element variants = DocumentHelper.makeElement(code, "variants");
        Element defaultVariant = variants.addElement("variant");
        canContinue &= setVariantDynamicFlag(params, defaultVariant);
        canContinue &= setVariantCode(params, defaultVariant);
        defaultVariant.addAttribute("description", "defaultní");

        if (!canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "addCode", env, request);

        persistence.update(item);

        String id = item.getString1();
        AdminLogger.logEvent(user, "pridal reklamni kod k pozici " + id);

        urlUtils.redirect(response, "/EditAdvertisement?action=" + ACTION_SHOW_POSITION + "&" + PARAM_RELATION_SHORT + "=" + relation.getId());
        return null;
    }

    public String actionEditCodeStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showPosition");
            return null;
        }

        Attribute attribute = code.attribute("regexp");
        if (attribute != null)
            params.put(PARAM_REGEXP, attribute.getText());
        attribute = code.attribute("description");
        if (attribute != null)
            params.put(PARAM_DESCRIPTION, attribute.getText());
        attribute = code.attribute("name");
        if (attribute != null)
            params.put(PARAM_NAME, attribute.getText());
        
        return FMTemplateSelector.select("EditAdvertisement", "editCode", env, request);
    }

    public String actionEditCodeStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showPosition");
            return null;
        }
        
        List<Element> codes = item.getData().selectNodes("//code");
        
        boolean canContinue = true;
        canContinue &= setCodeRegexp(params, code, env);
        canContinue &= setCodeDescription(params, code);
        canContinue &= setCodeName(params, code, codes, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "editCode", env, request);

        persistence.update(item);

        String id = item.getString1();
        AdminLogger.logEvent(user, "upravil reklamni kod k pozici " + id);

        urlUtils.redirect(response, "/EditAdvertisement/" + relation.getId() + "?action=" + ACTION_SHOW_CODE + "&" + PARAM_CODE + "=" + params.get(PARAM_CODE));
        return null;
    }

    public String actionRemoveCode(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showPosition");
            return null;
        }

        code.detach();
        persistence.update(item);

        String id = item.getString1();
        AdminLogger.logEvent(user, "smazal reklamni kod k pozici " + id);

        urlUtils.redirect(response, "/EditAdvertisement/" + relation.getId() + "?action=" + ACTION_SHOW_POSITION);
        return null;
    }
    
    public String actionRemoveVariant(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showPosition");
            return null;
        }
        
        Element variant = getVariant(params, code);
        
        if (variant == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showCode&code="+params.get(PARAM_CODE));
            return null;
        }

        variant.detach();
        persistence.update(item);

        String id = item.getString1();
        AdminLogger.logEvent(user, "smazal variantu reklamniho kod k pozici " + id);

        urlUtils.redirect(response, "/EditAdvertisement/" + relation.getId() + "?action=" + ACTION_SHOW_CODE + "&code=" + params.get(PARAM_CODE));
        return null;
    }
    
    public String actionAddVariantStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showPosition");
            return null;
        }
        
        boolean canContinue = true;
        Element variants = DocumentHelper.makeElement(code, "variants");
        Element variant = variants.addElement("variant");
        
        canContinue &= setVariantDynamicFlag(params, variant);
        canContinue &= setVariantCode(params, variant);
        canContinue &= setVariantTags(params, variant);
        canContinue &= setVariantDescription(params, variant);
        
        variant.addAttribute("active", "no");
        
        if (!canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "addVariant", env, request);
        
        persistence.update(item);
        
        AdminLogger.logEvent(user, "pridal variantu reklamniho kod k pozici " + item.getString1());
        
        urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=" + ACTION_SHOW_CODE + "&code="+params.get(PARAM_CODE));
        return null;
    }
    
    public String actionEditVariant(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showPosition");
            return null;
        }
        
        Element variant = getVariant(params, code);
        if (variant == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showCode&code="+params.get(PARAM_CODE));
            return null;
        }
        
        Attribute attr = variant.attribute("description");
        if (attr != null)
            params.put(PARAM_DESCRIPTION, attr.getText());
        attr = variant.attribute("tags");
        if (attr != null)
            params.put(PARAM_TAGS, attr.getText());
        attr = variant.attribute("dynamic");
        if (attr != null)
            params.put(PARAM_DYNAMIC, attr.getText());
        params.put(PARAM_HTML_CODE, variant.getText());
        
        return FMTemplateSelector.select("EditAdvertisement", "editVariant", env, request);
    }
    
    public String actionEditVariantStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        User user = (User) env.get(Constants.VAR_USER);
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showPosition");
            return null;
        }
        
        Element variant = getVariant(params, code);
        if (variant == null) {
            urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showCode&code="+params.get(PARAM_CODE));
            return null;
        }
        
        boolean canContinue = true;
        
        canContinue &= setVariantDescription(params, variant);
        canContinue &= setVariantCode(params, variant);
        canContinue &= setVariantDynamicFlag(params, variant);
        canContinue &= setVariantTags(params, variant);
        
        if (!canContinue)
            return FMTemplateSelector.select("EditAdvertisement", "editVariant", env, request);
        
        AdminLogger.logEvent(user, "upravil variantu reklamniho kod k pozici " + item.getString1());
        
        persistence.update(item);
        
        urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showCode&code="+params.get(PARAM_CODE));
        return null;
    }
    
    public String actionToggleVariant(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        
        Element code = getCode(params, item.getData().getRootElement());
        
        if (code == null) {
            urlUtils.redirect(response, "/EditAdvertisement/" + relation.getId() + "?action=showPosition");
            return null;
        }
        
        Element variant = getVariant(params, code);
        
        if (variant == null) {
            urlUtils.redirect(response, "/EditAdvertisement/" + relation.getId() + "?action=showCode&code=" + params.get(PARAM_CODE));
            return null;
        }
        
        boolean nowActive = false;
        String active = variant.attributeValue("active");
        if (active != null) {
            if (active.equals("yes"))
                variant.addAttribute("active", "no");
            else {
                variant.addAttribute("active", "yes");
                nowActive = true;
            }
        } else
            variant.addAttribute("active", "no");
        
        String tags = variant.attributeValue("tags");
        List<String> myTags = Collections.EMPTY_LIST;
        
        if (!Misc.empty(tags))
            myTags = new ArrayList<String>(Arrays.asList(tags.split(" ")));
        
        if (nowActive) {
            boolean problem = false;
            List<Element> listVariants = code.selectNodes("//variant");
            
            if (myTags.size() > 0) {
                List<String> usedTags = new ArrayList<String>();

                for (Element var : listVariants) {
                    String ctags = var.attributeValue("tags");
                    if (ctags == null)
                        continue;
                    if ("no".equals(var.attributeValue("active")) || var == variant)
                        continue;

                    usedTags.addAll(Arrays.asList(ctags.split(" ")));
                }

                myTags.retainAll(usedTags);
                problem = myTags.size() > 0;
            } else {
                for (Element var : listVariants) {
                    if ("no".equals(var.attributeValue("active")) || var == variant)
                        continue;
                    
                    String ctags = var.attributeValue("tags");
                    if (Misc.empty(ctags) || ctags.trim().length() == 0) {
                        problem = true;
                        break;
                    }
                }
            }
            
            if (problem) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Varianta se překrývá s jinou aktivní variantou", env, request.getSession());
                urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showCode&code="+params.get(PARAM_CODE));
                return null;
            }
        }
        
        persistence.update(item);
        
        urlUtils.redirect(response, "/EditAdvertisement/"+relation.getId()+"?action=showCode&code="+params.get(PARAM_CODE));
        return null;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String regexp = prefs.get(PREF_IDENTIFIER_REGEXP, null);
        identifierPattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }

    // helper methods
    
    private Element getCode(Map params, Element root) {
        List<Node> codes = root.selectNodes("//code");
        int index = Misc.parseInt((String) params.get(PARAM_CODE), -1);
        
        if (index < 0 || index >= codes.size())
            return null;
        else
            return (Element) codes.get(index);
    }
    
    private Element getVariant(Map params, Element code) {
        List<Node> variants = code.selectNodes("variants/variant");
        int index = Misc.parseInt((String) params.get(PARAM_VARIANT), -1);
        
        if (index < 0 || index >= variants.size())
            return null;
        else
            return (Element) variants.get(index);
    }

    /**
     * Updates position name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param position position element to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_NAME, "Zadejte jméno.", env, null);
            return false;
        }

        item.setTitle(name);
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
    private boolean setIdentifier(Map params, Item item, Map env) {
        String id = (String) params.get(PARAM_NEW_IDENTIFIER);
        if (Misc.empty(id)) {
            ServletUtils.addError(PARAM_NEW_IDENTIFIER, "Zadejte identifikátor.", env, null);
            return false;
        }

        Matcher matcher = identifierPattern.matcher(id);
        if (!matcher.matches()) {
            ServletUtils.addError(PARAM_NEW_IDENTIFIER, "Neplatný identifikátor.", env, null);
            return false;
        }

        Item itemExisting = SQLTool.getInstance().findAdvertisementByString(id);
        if (itemExisting != null && itemExisting.getId() != item.getId()) {
            ServletUtils.addError(PARAM_NEW_IDENTIFIER, "Identifikátor se už používá!", env, null);
            return false;
        }

        item.setString1(id);
        return true;
    }

    /**
     * Updates code's regexp from parameters. Changes are not synchronized with persistance.
     * @param params   map holding request's parameters
     * @param code element to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setCodeRegexp(Map params, Element code, Map env) {
        String regexp = (String) params.get(PARAM_REGEXP);
        /*if (Misc.empty(regexp)) {
            ServletUtils.addError(PARAM_REGEXP, "Zadejte regulární výraz.", env, null);
            return false;
        }*/
        if (regexp == null)
            regexp = "";

        try {
            Pattern.compile(regexp);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_REGEXP, "Regulární výraz obsahuje chybu:"+e.getMessage(), env, null);
            return false;
        }

        Attribute attribute = code.attribute("regexp");
        if (attribute != null)
            attribute.setValue(regexp);
        else
            code.addAttribute("regexp", regexp);
        return true;
    }

    /**
     * Updates code's description from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param code element to be updated
     * @return false, if there is a major error.
     */
    private boolean setCodeDescription(Map params, Element code) {
        String desc = (String) params.get(PARAM_DESCRIPTION);
        Attribute attribute = code.attribute("description");
        if (Misc.empty(desc)) {
            if (attribute != null)
                attribute.detach();
        } else {
            if (attribute != null)
                attribute.setValue(desc);
            else
                code.addAttribute("description", desc);
        }
        return true;
    }
    
    private boolean setCodeName(Map params, Element code, List<Element> codes, Map env) {
        String name = (String) params.get(PARAM_NAME);
        
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_NAME, "Zadejte název!", env, null);
            return false;
        }
        
        if (codes != null) {
            for (Element elem : codes) {
                if (elem == code)
                    continue;
                if (elem.attributeValue("name").equals(name)) {
                    ServletUtils.addError(PARAM_NAME, "Kód s takovým názvem už existuje!", env, null);
                    return false;
                }
            }
        }
        
        code.addAttribute("name", name);

        return true;
    }

    /**
     * Updates code's dynamic flag from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param code element to be updated
     * @return false, if there is a major error.
     */
    private boolean setVariantDynamicFlag(Map params, Element variant) {
        String flag = (String) params.get(PARAM_DYNAMIC);
        Attribute attribute = variant.attribute("dynamic");
        if (Misc.empty(flag)) {
            if (attribute != null)
                attribute.detach();
        } else {
            if (attribute == null)
                variant.addAttribute("dynamic", "yes");
        }
        return true;
    }

    /**
     * Updates code's content from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param code element to be updated
     * @return false, if there is a major error.
     */
    private boolean setVariantCode(Map params, Element variant) {
        String s = (String) params.get(PARAM_HTML_CODE);
        if (Misc.empty(s))
            s = "<!-- empty -->";

        variant.setText(s);
        return true;
    }
    
    private boolean setVariantDescription(Map params, Element variant) {
        String description = (String) params.get(PARAM_DESCRIPTION);
        Attribute attr = variant.attribute("description");
        if (Misc.empty(description)) {
            if (attr != null)
                attr.detach();
        } else {
            if (attr != null)
                attr.setText(description);
            else
                variant.addAttribute("description", description);
        }
        
        return true;
    }
    
    private boolean setVariantTags(Map params, Element variant) {
        String tags = (String) params.get(PARAM_TAGS);
        variant.addAttribute("tags", tags);
        return true;
    }
}
