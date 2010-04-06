/*
 *  Copyright (C) 2008 Leos Literak
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
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author lubos
 */
public class EditEvent implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_REGION = "region";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_DATE_TO = "dateTo";
    public static final String PARAM_DESCRIPTION_SHORT = "descriptionShort";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_LOGO = "logo";
    public static final String PARAM_REMOVE_LOGO = "removeLogo";
    public static final String PARAM_SUBTYPE = "subtype";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_LOCATION = "location";
    public static final String PARAM_UID = "uid";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_REGISTER_STEP2 = "register2";
    public static final String ACTION_DEREGISTER_STEP2 = "deregister2";
    public static final String ACTION_APPROVE = "approve";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditEvent.class);

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation != null) {
            Tools.sync(relation);
            env.put(ShowObject.VAR_RELATION, relation);
        } else
            throw new MissingArgumentException("Chybí číslo relace!");

        if (action.equals(ACTION_REGISTER))
            return actionRegisterStep1(request, response, env);

        if (action.equals(ACTION_REGISTER_STEP2)) {
            ActionProtector.ensureContract(request, EditEvent.class, false, false, true, false);
            return actionRegisterStep2(request, response, env);
        }

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (action.equals(ACTION_DEREGISTER_STEP2)) {
            ActionProtector.ensureContract(request, EditEvent.class, false, false, true, false);
            return actionDeregisterStep2(request, response, env);
        }

        if (action.equals(ACTION_ADD)) {
            if (!Tools.permissionsFor(user, relation).canCreate())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            return FMTemplateSelector.select("EditEvent", "add", env, request);
        }

        if (action.equals(ACTION_ADD_STEP2)) {
            if (!Tools.permissionsFor(user, relation).canCreate())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            ActionProtector.ensureContract(request, EditEvent.class, true, false, true, false);
            return actionAddStep2(request, response, env, true);
        }

        Item item = (Item) relation.getChild();
        if (!Tools.permissionsFor(user, relation).canModify() && item.getOwner() != user.getId())
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_EDIT))
            return actionEditStep1(request, env);

        if (action.equals(ACTION_EDIT_STEP2)) {
            ActionProtector.ensureContract(request, EditEvent.class, true, false, true, false);
            return actionEditStep2(request, response, env);
        }

        if (!Tools.permissionsFor(user, relation).canModify())
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_APPROVE)) {
            ActionProtector.ensureContract(request, EditEvent.class, true, false, false, true);
            return actionApprove(request, response, env);
        }

        if (!Tools.permissionsFor(user, relation).canDelete())
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        return null;
    }

    private String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation parent = (Relation) env.get(ShowObject.VAR_RELATION);

        Document documentItem = DocumentHelper.createDocument();
        Element root = documentItem.addElement("data");
        Item item = new Item(0, Item.UNPUBLISHED_EVENT);
        item.setData(documentItem);
        item.setOwner(user.getId());

        Category cat = (Category) parent.getChild();
        item.setGroup(cat.getGroup());
        item.setPermissions(cat.getPermissions());

        Relation relation = new Relation(parent.getChild(), item, parent.getId());

        boolean canContinue;
        canContinue = setTitle(params, item, env);
        canContinue &= setRegion(params, item, env);
        canContinue &= setDate(params, item, env);
        canContinue &= setDateTo(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setShortDescription(params, root, env);
        canContinue &= setSubType(params, item, env);
        canContinue &= setLocation(params, root);
        canContinue &= checkImage(params, env);

        if (user.hasRole(Roles.ROOT))
            canContinue &= setOwner(params, item, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditEvent", "add", env, request);

        persistence.create(item);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        setLogo(params, relation, item, root, env);
        persistence.update(item);

        EditDiscussion.createEmptyDiscussion(relation, user, persistence);

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, UrlUtils.PREFIX_EVENTS + "/show/" + relation.getId());
        } else
            env.put(ShowObject.VAR_RELATION, relation);
        return null;
    }

    private String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        params.put(PARAM_TITLE, item.getTitle());
        params.put(PARAM_REGION, item.getString1());
        params.put(PARAM_SUBTYPE, item.getSubType());

        String date = Constants.isoFormat.format(item.getCreated());
        params.put(PARAM_DATE, date);

        if (item.getDate1() != null) {
            date = Constants.isoFormat.format(item.getDate1());
            params.put(PARAM_DATE_TO, date);
        }

        Element elem = root.element("description");
        if (elem != null)
            params.put(PARAM_DESCRIPTION, elem.getText());

        elem = root.element("descriptionShort");
        if (elem != null)
            params.put(PARAM_DESCRIPTION_SHORT, elem.getText());

        elem = root.element("location");
        if (elem != null)
            params.put(PARAM_LOCATION, elem.getText());

        return FMTemplateSelector.select("EditEvent", "edit", env, request);
    }

    private String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();
        User user = (User) env.get(Constants.VAR_USER);

        boolean canContinue;
        canContinue = setTitle(params, item, env);
        canContinue &= setRegion(params, item, env);
        canContinue &= setDate(params, item, env);
        canContinue &= setDateTo(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setShortDescription(params, root, env);
        canContinue &= setSubType(params, item, env);
        canContinue &= setLocation(params, root);
        canContinue &= checkImage(params, env);
        canContinue &= setLogo(params, relation, item, root, env);

        if (user.hasRole(Roles.ROOT))
            canContinue &= setOwner(params, item, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditEvent", "edit", env, request);

        persistence.update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    private String actionRegisterStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();

        if (user != null) {
            params.put(PARAM_EMAIL, user.getEmail());
            params.put(PARAM_NAME, user.getName());

            Node node = item.getData().selectSingleNode("/data/registrations/registration[@uid='"+user.getId()+"']");
            if (node != null) {

                ServletUtils.addError(Constants.ERROR_GENERIC, "Již jste se registroval!", env, request.getSession());

                UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
                urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
                return null;
            }
        }

        return FMTemplateSelector.select("EditEvent", "register", env, request);
    }

    private String actionRegisterStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        if (new Date().compareTo(item.getCreated()) > 0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nemůžete se registrovat k proběhlé události!", env, request.getSession());

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            return null;
        }

        if (user != null) {
            Node node = item.getData().selectSingleNode("/data/registrations/registration[@uid='"+user.getId()+"']");

            if (node != null) {

                ServletUtils.addError(Constants.ERROR_GENERIC, "Již jste se registroval!", env, request.getSession());

                UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
                urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
                return null;
            }
        }

        Element reg = DocumentHelper.createElement("registration");

        boolean canContinue = setRegistrationName(reg, params, env);
        canContinue &= setRegistrationEmail(reg, params, env);
        canContinue &= setRegistrationUser(reg, env);

        if(!canContinue)
            return FMTemplateSelector.select("EditEvent", "register", env, request);

        Element regs = DocumentHelper.makeElement(item.getData(), "/data/registrations");
        regs.add(reg);

        Date originalUpdated = item.getUpdated();
        persistence.update(item);
        SQLTool.getInstance().setUpdatedTimestamp(item, originalUpdated);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    private String actionDeregisterStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        Node node = item.getData().selectSingleNode("/data/registrations/registration[@uid='"+user.getId()+"']");

        if (node == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nejste registrován!", env, request.getSession());
        } else {
            node.detach();

            ServletUtils.addMessage("Vaše registrace byla zrušena.", env, request.getSession());

            Date originalUpdated = item.getUpdated();
            persistence.update(item);
            SQLTool.getInstance().setUpdatedTimestamp(item, originalUpdated);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    private String actionApprove(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        if (item.getType() == Item.EVENT) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Akce již byla schválena!", env, request.getSession());
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            return null;
        }

        setURL(relation, item, persistence);
        persistence.update(relation);

        Element element = DocumentHelper.makeElement(item.getData(), "/data/approved_by");
        element.setText(Integer.toString(user.getId()));

        item.setType(Item.EVENT);
        persistence.update(item);

        TagTool.assignDetectedTags(item, user);

        Relation section = Tools.getParentSubportal(relation);
        VariableFetcher.getInstance().refreshSubportalEvents(section);

        AdminLogger.logEvent(user, "  approve | event " + relation.getUrl());

        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    private boolean setTitle(Map params, Item item, Map env) {
        String title = (String) params.get(PARAM_TITLE);

        if (Misc.empty(title)) {
            ServletUtils.addError(PARAM_TITLE, "Zadejte název akce!", env, null);
            return false;
        }

        if (title.indexOf("<") != -1) {
            ServletUtils.addError(PARAM_TITLE, "HTML zde není povoleno!", env, null);
            return false;
        }

        item.setTitle(title);
        return true;
    }

    private boolean setRegion(Map params, Item item, Map env) {
        String region = (String) params.get(PARAM_REGION);

        if (Misc.empty(region)) {
            ServletUtils.addError(PARAM_REGION, "Vyberte kraj!", env, null);
            return false;
        }

        item.setString1(region);
        return true;
    }

    private boolean setOwner(Map params, Item item, Map env) {
        String uid = (String) params.get(PARAM_UID);
        if (Misc.empty(uid))
            return true;

        try {
            User owner = Tools.createUser(uid);
            item.setOwner(owner.getId());
        } catch (Exception e) {
            ServletUtils.addError(PARAM_UID, "Neplatné UID uživatele!", env, null);
            return false;
        }

        return true;
    }

    private boolean setDate(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_DATE);
        if (Misc.empty(tmp)) {
            ServletUtils.addError(PARAM_DATE, "Zadejte datum!", env, null);
            return false;
        }

        try {
            Date date;
            synchronized (Constants.isoFormat) {
                date = Constants.isoFormat.parse(tmp);
            }
            item.setCreated(date);
            return true;
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_DATE, "Chybný formát data!", env, null);
            return false;
        }
    }

    private boolean setDateTo(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_DATE_TO);
        if (Misc.empty(tmp)) {
            item.setDate1(null);
            return true;
        }

        try {
            Date date;
            synchronized (Constants.isoFormat) {
                date = Constants.isoFormat.parse(tmp);
            }
            item.setDate1(date);
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_DATE_TO, "Chybný formát data!", env, null);
            return false;
        }

        if (item.getDate1().before(item.getCreated())) {
            ServletUtils.addError(PARAM_DATE_TO, "Akce nemůže končit dříve než začínat!", env, null);
            return false;
        }

        return true;
    }

    private boolean setShortDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION_SHORT);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp == null || tmp.length() == 0) {
            ServletUtils.addError(PARAM_DESCRIPTION_SHORT, "Zadejte popis!", env, null);
            return false;
        }

        try {
            tmp = HtmlPurifier.clean(tmp);
            HtmlChecker.check(Rules.DEFAULT, tmp);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION_SHORT, e.getMessage(), env, null);
            return false;
        }

        Element element = DocumentHelper.makeElement(root, "descriptionShort");
        element.setText(tmp);

        return true;
    }

    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp == null || tmp.length() == 0) {
            Element element = root.element("description");
            if (element != null)
                element.detach();
            return true;
        }

        try {
            tmp = HtmlPurifier.clean(tmp);
            HtmlChecker.check(Rules.DEFAULT, tmp);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        }

        Element element = DocumentHelper.makeElement(root, "description");
        tmp = Tools.processLocalLinks(tmp, null);
        element.setText(tmp);

        return true;
    }

    private boolean setLocation(Map params, Element root) {
        String tmp = (String) params.get(PARAM_LOCATION);
        if (Misc.empty(tmp)) {
            Element element = root.element("location");
            if (element != null)
                element.detach();
            return true;
        }

        Element element = DocumentHelper.makeElement(root, "location");
        element.setText(tmp);

        return true;
    }

    private boolean setSubType(Map params, Item item, Map env) {
        String subtype = (String) params.get(PARAM_SUBTYPE);

        if (Misc.empty(subtype)) {
            ServletUtils.addError(PARAM_SUBTYPE, "Zadejte popis!", env, null);
            return false;
        }

        item.setSubType(subtype);
        return true;
    }

    private boolean checkImage(Map params, Map env) {
        FileItem fileItem = (FileItem) params.get(PARAM_LOGO);
        if ( fileItem == null || fileItem.getSize() == 0)
            return true;

        String suffix = EditSubportal.getFileSuffix(fileItem.getName()).toLowerCase();
        if ( ! (suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif")) ) {
            ServletUtils.addError(PARAM_LOGO, "Soubor musí být typu JPG, GIF nebo JPEG!", env, null);
            return false;
        }

        try {
            Iterator readers = ImageIO.getImageReadersBySuffix(suffix);
            ImageReader reader = (ImageReader) readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(fileItem.getInputStream());
            reader.setInput(iis, false);
            if (reader.getNumImages(true) > 1) {
                ServletUtils.addError(PARAM_LOGO, "Animované obrázky nejsou povoleny!", env, null);
                return false;
            }
            if (reader.getHeight(0) > 200 || reader.getWidth(0) > 250) {
                ServletUtils.addError(PARAM_LOGO, "Ikonka přesahuje povolené maximální rozměry!", env, null);
                return false;
            }
        } catch(Exception e) {
            ServletUtils.addError(PARAM_LOGO, "Nelze načíst obrázek!", env, null);
            return false;
        }
        return true;
    }

    private boolean setLogo(Map params, Relation rel, Item item, Element root, Map env) {
        if (params.containsKey(PARAM_REMOVE_LOGO)) {
            Node node = item.getData().selectSingleNode("/data/icon");
            if (node != null) {
                String localPath = AbcConfig.calculateDeployedPath(node.getText().substring(1));
                new File(localPath).delete();
                node.detach();
            }
            return true;
        }

        FileItem fileItem = (FileItem) params.get(PARAM_LOGO);
        if ( fileItem == null || fileItem.getSize() == 0)
            return true;

        String suffix = EditSubportal.getFileSuffix(fileItem.getName()).toLowerCase();

        String fileName = "images/events/" + rel.getId() + "." + suffix;
        File file = new File(AbcConfig.calculateDeployedPath(fileName));
        try {
            fileItem.write(file);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_LOGO, "Chyba při zápisu na disk!", env, null);
            log.error("Není možné uložit logo " + file.getAbsolutePath() + " na disk!",e);
            return false;
        }

        Element photo = DocumentHelper.makeElement(root, "icon");
        photo.setText("/"+fileName);

        return true;
    }

    private boolean setURL(Relation relation, Item item, Persistence persistence) {
        String name = item.getTitle();
        Relation upper = new Relation(relation.getUpper());

        persistence.synchronize(upper);

        name = Misc.filterDangerousCharacters(name);
        synchronized (Constants.isoSearchFormat) {
            name = upper.getUrl() + "/" + name + "-" + Constants.isoSearchFormat.format(item.getCreated());
        }

        String url = URLManager.enforceAbsoluteURL(name);
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);
        return true;
    }

    private boolean setRegistrationName(Element registration, Map params, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_NAME, "Zadejte své jméno!", env, null);
            return false;
        }

        registration.addAttribute("name", name);
        return true;
    }

    private boolean setRegistrationEmail(Element registration, Map params, Map env) {
        String email = (String) params.get(PARAM_EMAIL);
        if (Misc.empty(email)) {
            ServletUtils.addError(PARAM_EMAIL, "Zadejte svůj e-mail!", env, null);
            return false;
        }

        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            ServletUtils.addError(PARAM_EMAIL, "Neplatný email!", env, null);
            return false;
        }

        registration.addAttribute("email", email);
        return true;
    }

    private boolean setRegistrationUser(Element registration, Map env) {
        User user = (User) env.get(Constants.VAR_USER);

        if (user != null) {
            registration.addAttribute("uid", String.valueOf(user.getId()));
        } else {
            Attribute attr = registration.attribute("uid");
            if (attr != null)
                attr.detach();
        }

        return true;
    }
}
