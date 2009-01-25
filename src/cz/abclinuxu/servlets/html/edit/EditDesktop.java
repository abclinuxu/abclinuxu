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
import cz.abclinuxu.data.Data;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ViewDesktop;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.PathGenerator;
import cz.abclinuxu.utils.ImageTool;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.scheduler.VariableFetcher;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;

/**
 * Creates screenshot
 * @author literakl
 * @since 17.11.2007
 */
public class EditDesktop implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDesktop.class);

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_SCREENSHOT = "screenshot";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_THEME_URL = "theme";

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
		Relation parent = new Relation(Constants.REL_DESKTOPS);

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (action.equals(ACTION_ADD)) {
			if (!Tools.permissionsFor(user, parent).canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            return FMTemplateSelector.select("Desktop", "add", env, request);
		}

        if (action.equals(ACTION_ADD_STEP2)) {
			if (!Tools.permissionsFor(user, parent).canCreate())
				return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            ActionProtector.ensureContract(request, EditDesktop.class, true, false, true, false);
            return actionAddStep2(request, response, env, true);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        if (ACTION_I_LIKE.equals(action)) {
            ActionProtector.ensureContract(request, EditDesktop.class, true, false, false, true);
            return actionILike(response, env);
        }

        boolean allowed = Tools.permissionsFor(user, relation).canModify();
        allowed |= ((Item)relation.getChild()).getOwner() == user.getId();
        if (! allowed)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_EDIT))
            return actionEdit(request, env);

        if (action.equals(ACTION_EDIT_STEP2)) {
            ActionProtector.ensureContract(request, EditDesktop.class, true, true, true, false);
            return actionEdit2(request, response, env);
        }

        if (Misc.containsForeignComments((Item) relation.getChild())) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tento desktop není možné smazat, neboť obsahuje cizí komentáře.", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_SCREENSHOTS));
            return null;
        }

        if (action.equals(ACTION_REMOVE_STEP2)) {
            ActionProtector.ensureContract(request, ViewDesktop.class, true, false, false, true);
            return actionRemoveStep2(response, env);
        }

        return null;
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
		Relation parent = new Relation(Constants.REL_DESKTOPS);

		Tools.sync(parent);

        Document documentItem = DocumentHelper.createDocument();
        Element root = documentItem.addElement("data");
        Item item = new Item(0, Item.DESKTOP);
        item.setData(documentItem);
        item.setOwner(user.getId());
		item.setGroup( ((Category) parent.getChild()).getGroup() );

        Relation relation = new Relation(parent.getChild(), item, parent.getId());

        boolean canContinue = setName(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setThemeUrl(params, root, env);
        canContinue &= checkImage(params, env);
        canContinue &= setURL(relation, user);
        if (!canContinue)
            return FMTemplateSelector.select("Desktop", "add", env, request);

        persistence.create(item);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        Data data = new Data();
        data.setOwner(user.getId());
        createImage(params, data, item, env);
        persistence.create(data);

        Relation dataRelation = new Relation(item, data, relation.getId());
        persistence.create(dataRelation);
        dataRelation.getParent().addChildRelation(dataRelation);
        TagTool.assignDetectedTags(item, user);

        EditDiscussion.createEmptyDiscussion(relation, user, persistence);

        FeedGenerator.updateDesktops();
        VariableFetcher.getInstance().refreshDesktops();

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
        params.put(PARAM_NAME, item.getTitle());
        Node desc = document.selectSingleNode("data/description");
        if (desc != null)
            params.put(PARAM_DESCRIPTION, desc.getText());
        desc = document.selectSingleNode("data/theme_url");
        if (desc != null)
            params.put(PARAM_THEME_URL, desc.getText());

        return FMTemplateSelector.select("Desktop", "edit", env, request);
    }

    protected String actionEdit2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Element root = item.getData().getRootElement();

        boolean canContinue = setName(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setThemeUrl(params, root, env);

        if (!canContinue)
            return FMTemplateSelector.select("Desktop", "edit", env, request);

        persistence.update(item);

        FeedGenerator.updateDesktops();
        VariableFetcher.getInstance().refreshDesktops();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    private String actionRemoveStep2(HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);

        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);
        AdminLogger.logEvent(user, "  remove | screenshot " + relation.getUrl());

        FeedGenerator.updateDesktops();
        VariableFetcher.getInstance().refreshDesktops();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, UrlUtils.PREFIX_SCREENSHOTS);
        return null;
    }

    public String actionILike(HttpServletResponse response, Map env) throws Exception {
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
     * @param item item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Map env) {
        String name = (String) params.get(PARAM_NAME);
        name = Misc.filterDangerousCharacters(name);
        if (name == null || name.length() == 0) {
            ServletUtils.addError(PARAM_NAME, "Nezadali jste titulek obrázku.", env, null);
            return false;
        }

        item.setTitle(name);
        return true;
    }

    /**
     * Updates explanation from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
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
            tmp = HtmlPurifier.clean(tmp);
            HtmlChecker.check(Rules.DEFAULT, tmp);
            tmp = Tools.processLocalLinks(tmp, null);
            element.setText(tmp);
            return true;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        }
    }

    /**
     * Updates theme url from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
     * @return false, if there is a major error.
     */
    private boolean setThemeUrl(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_THEME_URL);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            if (tmp.indexOf('<') != -1) {
                ServletUtils.addError(PARAM_THEME_URL, "HTML značky nejsou povoleny v URL!", env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root, "theme_url");
            element.setText(tmp);
        } else {
            Element element = root.element("theme_url");
            if (element != null)
                element.detach();
        }
        return true;
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
    static boolean checkImage(Map params, Map env) {
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

        try {
            BufferedImage img = ImageIO.read(fileItem.getInputStream());
            if (img == null) {
                ServletUtils.addError(PARAM_SCREENSHOT, "Obrázek nelze načíst, nepodporovaný formát.", env, null);
                return false;
            }
            if (img.getWidth() < 640) {
                ServletUtils.addError(PARAM_SCREENSHOT, "Obrázek musí mít rozměry nejméně 640x480 pixelů.", env, null);
                return false;
            }
            if (img.getHeight() < 480) {
                ServletUtils.addError(PARAM_SCREENSHOT, "Obrázek musí mít rozměry nejméně 640x480 pixelů.", env, null);
                return false;
            }
        } catch (IOException e) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Obrázek nelze načíst: " + e.getMessage(), env, null);
            return false;
        }
        return true;
    }

    /**
     * Uploads new screenshot and creates a thumbnail (if needed). Changes to Item are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param data data to be updated
     * @param item parent item
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean createImage(Map params, Data data, Item item, Map env) throws IOException {
        FileItem fileItem = (FileItem) params.get(PARAM_SCREENSHOT);
        data.setSubType(fileItem.getContentType());
        Document document = DocumentHelper.createDocument();
        data.setData(document);

        PathGenerator pathGenerator = AbcConfig.getPathGenerator();
        String suffix = Misc.getFileSuffix(fileItem.getName()).toLowerCase();
        String name = EditAttachment.getFileName(item);
        File imageFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name, "." + suffix);
        String path = Misc.getWebPath(imageFile.getAbsolutePath());
        try {
            fileItem.write(imageFile);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Chyba při zápisu na disk!", env, null);
            log.error("Není možné uložit obrázek " + imageFile.getAbsolutePath() + " na disk!", e);
            return false;
        }

        Element root = document.addElement("data");
        Element screenshot = root.addElement("object").addAttribute("path", path);
        screenshot.addElement("originalFilename").setText(fileItem.getName());
        screenshot.addElement("size").setText(Long.toString(fileItem.getSize()));

        File thumbnailListingFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name + "-listing", ".png");
        ImageTool.createThumbnailMaxSize(imageFile, thumbnailListingFile, 250, false);
        path = Misc.getWebPath(thumbnailListingFile.getAbsolutePath());
        screenshot.addElement("thumbnail").addAttribute("path", path).addAttribute("useType", "listing");

        File thumbnailDetailFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name + "-detail", ".png");
        ImageTool.createThumbnailMaxSize(imageFile, thumbnailDetailFile, 500, false);
        path = Misc.getWebPath(thumbnailDetailFile.getAbsolutePath());
        screenshot.addElement("thumbnail").addAttribute("path", path).addAttribute("useType", "detail");

        return true;
    }
}
