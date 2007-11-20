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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ViewScreenshot;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.PathGenerator;
import cz.abclinuxu.utils.ImageTool;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.scheduler.VariableFetcher;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.apache.commons.fileupload.FileItem;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.File;

/**
 * Creates screenshot
 * @author literakl
 * @since 17.11.2007
 */
public class EditScreenshot implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditScreenshot.class);

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_SCREENSHOT = "screenshot";
    public static final String PARAM_DESCRIPTION = "desc";

    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_REMOVE_STEP2 = "rm2";
    public static final String ACTION_I_LIKE = "favourite";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);
        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (action.equals(ACTION_ADD))
            return FMTemplateSelector.select("Screenshot", "add", env, request);

        if (action.equals(ACTION_ADD_STEP2)) {
            ActionProtector.ensureContract(request, EditDictionary.class, true, false, true, false);
            return actionAddStep2(request, response, env, true);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        if (ACTION_I_LIKE.equals(action)) {
            ActionProtector.ensureContract(request, EditSoftware.class, true, false, false, true);
            return actionILike(request, response, env);
        }

        boolean allowed = user.hasRole(Roles.ATTACHMENT_ADMIN);
        allowed |= ((Item)relation.getChild()).getOwner() == user.getId();
        if (! allowed)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_EDIT))
            return actionEdit(request, env);

        if (action.equals(ACTION_EDIT_STEP2)) {
            ActionProtector.ensureContract(request, EditScreenshot.class, true, true, true, false);
            return actionEdit2(request, response, env);
        }

        allowed = user.hasRole(Roles.ATTACHMENT_ADMIN);
        if (!allowed)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_REMOVE_STEP2)) {
            ActionProtector.ensureContract(request, ViewScreenshot.class, true, true, false, true);
            return actionRemoveStep2(request, response, env);
        }

        return null;
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);

        Document documentItem = DocumentHelper.createDocument();
        Element root = documentItem.addElement("data");
        Item item = new Item(0, Item.SCREENSHOT);
        item.setData(documentItem);
        item.setOwner(user.getId());
        Relation relation = new Relation(new Category(Constants.CAT_SCREENSHOTS), item, Constants.REL_SCREENSHOTS);

        boolean canContinue = setName(params, root, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= checkImage(params, env);
        canContinue &= setURL(relation, user);
        if (!canContinue)
            return FMTemplateSelector.select("Screenshot", "add", env, request);

        persistence.create(item);
        createImage(params, item, env);
        persistence.update(item);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);
        EditDiscussion.createEmptyDiscussion(relation, user, persistence);

        FeedGenerator.updateScreenshots();
        VariableFetcher.getInstance().refreshScreenshots();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, relation.getUrl());
        } else
            env.put(VAR_RELATION, relation);
        return null;
    }

    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild();
        Document document = item.getData();
        Node node = document.selectSingleNode("data/title");
        params.put(PARAM_NAME, node.getText());
        Node desc = document.selectSingleNode("data/description");
        if (desc != null)
            params.put(PARAM_DESCRIPTION, desc.getText());

        return FMTemplateSelector.select("Screenshot", "edit", env, request);
    }

    protected String actionEdit2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = setName(params, root, env);
        canContinue &= setDescription(params, root, env);

        if (!canContinue)
            return FMTemplateSelector.select("Screenshot", "edit", env, request);

        persistence.update(item);

        FeedGenerator.updateScreenshots();
        VariableFetcher.getInstance().refreshScreenshots();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    private String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild();
        Element rootElement = item.getData().getRootElement();
        Element element = rootElement.element("image");
        String path = element.getText();
        EditAttachment.deleteAttachment(path, env, user, request);
        element = rootElement.element("listingThumbnail");
        path = element.getText();
        EditAttachment.deleteAttachment(path, env, user, request);
        element = rootElement.element("detailThumbnail");
        path = element.getText();
        EditAttachment.deleteAttachment(path, env, user, request);

        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);
        AdminLogger.logEvent(user, "  remove | screenshot " + relation.getUrl());

        FeedGenerator.updateScreenshots();
        VariableFetcher.getInstance().refreshScreenshots();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, UrlUtils.PREFIX_SCREENSHOTS);
        return null;
    }

    public String actionILike(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();
        Set<String> users = item.getProperty(Constants.PROPERTY_FAVOURITED_BY);

        // see whether user wants to remove or add himself
        String userid = Integer.toString(user.getId());
        if (!users.contains(userid))
            item.addProperty(Constants.PROPERTY_FAVOURITED_BY, userid);
        else
            item.removePropertyValue(Constants.PROPERTY_FAVOURITED_BY, userid);

        Date originalUpdated = item.getUpdated();
        persistence.update(item);
        SQLTool.getInstance().setUpdatedTimestamp(item, originalUpdated);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));

        return null;
    }




    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Element root, Map env) {
        String name = (String) params.get(PARAM_NAME);
        name = Misc.filterDangerousCharacters(name);
        if (name == null || name.length() == 0) {
            ServletUtils.addError(PARAM_NAME, "Nezadali jste titulek obrázku.", env, null);
            return false;
        }

        DocumentHelper.makeElement(root, "title").setText(name);
        return true;
    }

    /**
     * Updates explaination from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        tmp = Misc.filterDangerousCharacters(tmp);
        Element element = root.element("description");
        if (tmp == null || tmp.length() == 0) {
            if (element != null)
                element.detach();
            return true;
        }

        if (element == null)
            element = DocumentHelper.makeElement(root, "description");

        try {
            SafeHTMLGuard.check(tmp);
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
            return true;
        } catch (ParserException e) {
            log.error("ParseException on '" + tmp + "'", e);
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        }
    }

    /**
     * Updates URL, checks for duplicates.
     * Changes are not synchronized with persistence.
     * @param relation relation
     * @param user that is creating this screenshot
     * @return false, if there is a major error.
     */
    private boolean setURL(Relation relation, User user) {
        String name = user.getNick();
        if (name == null)
            name = user.getName();
        name = Misc.filterDangerousCharacters(name);
        synchronized (Constants.isoSearchFormat) {
            name = UrlUtils.PREFIX_SCREENSHOTS + "/" + name + "-" + Constants.isoSearchFormat.format(new Date());
        }

        String url = URLManager.enforceAbsoluteURL(name);
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);
        return true;
    }

    /**
     * Checks the screenshot, if the file is correct.
     * @param params map holding request's parameters
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean checkImage(Map params, Map env) {
        FileItem fileItem = (FileItem) params.get(PARAM_SCREENSHOT);
        if (fileItem == null) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Zadejte prosím cestu k souboru.", env, null);
            return false;
        }

        String suffix = Misc.getFileSuffix(fileItem.getName()).toLowerCase();
        if (!(suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif"))) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Soubor musí být typu PNG, GIF nebo JPEG.", env, null);
            return false;
        }

        // todo check that image has minimum dimension of 800x600

        return true;
    }

    /**
     * Uploads new screenshot and creates a thumbnail (if needed). Changes to Item are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean createImage(Map params, Item item, Map env) throws IOException {
        FileItem fileItem = (FileItem) params.get(PARAM_SCREENSHOT);
        PathGenerator pathGenerator = AbcConfig.getPathGenerator();
        Element root = item.getData().getRootElement();
        String suffix = Misc.getFileSuffix(fileItem.getName()).toLowerCase();
        String name = EditAttachment.getFileName(item);
        File imageFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name, "." + suffix);
        String path = Misc.getWebPath(imageFile.getAbsolutePath());
        try {
            fileItem.write(imageFile);
            root.addElement("image").setText(path);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Chyba při zápisu na disk!", env, null);
            log.error("Není možné uložit obrázek " + imageFile.getAbsolutePath() + " na disk!", e);
            return false;
        }

        File thumbnailListingFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name + "-listing", ".png");
        ImageTool.createThumbnailMaxSize(imageFile, thumbnailListingFile, 200, true);
        path = Misc.getWebPath(thumbnailListingFile.getAbsolutePath());
        root.addElement("listingThumbnail").setText(path);

        File thumbnailDetailFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name + "-detail", ".png");
        ImageTool.createThumbnailMaxSize(imageFile, thumbnailDetailFile, 500, false);
        path = Misc.getWebPath(thumbnailDetailFile.getAbsolutePath());
        root.addElement("detailThumbnail").setText(path);

        return true;
    }
}