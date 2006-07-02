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

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.utils.PathGenerator;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ImageTool;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.io.DOMWriter;
import freemarker.ext.dom.NodeModel;

/**
 * @author literakl
 * @since 3.6.2006
 */
public class EditAttachment implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditAttachment.class);

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_XML = "XML";

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_SCREENSHOT = "screenshot";
    public static final String PARAM_ATTACHMENT = "attachment";

    public static final String ACTION_ADD_SCREENSHOT = "addScreenshot";
    public static final String ACTION_ADD_SCREENSHOT_STEP2 = "addScreenshot2";
    public static final String ACTION_MANAGE = "manage";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");

        Tools.sync(relation);
        env.put(VAR_RELATION, relation);
        GenericObject child = relation.getChild();

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (action.equals(ACTION_ADD_SCREENSHOT))
            return FMTemplateSelector.select("EditAttachment", "addScreenshot", env, request);

        if (action.equals(ACTION_ADD_SCREENSHOT_STEP2))
            return actionAddScreenshotStep2(request, response, env);

        boolean allowed = false;
        if (child instanceof Item) {
            Item item = (Item) child;
            if (item.getType() == Item.BLOG && item.getOwner()==user.getId())
                allowed = true;
        }
        allowed |= user.hasRole(Roles.ATTACHMENT_ADMIN);
        if (!allowed)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_MANAGE))
            return actionManageAttachments(request, env);

        if (action.equals(ACTION_REMOVE))
            return actionRemoveAttachmentStep1(request, env);

        if (action.equals(ACTION_REMOVE_STEP2))
            return actionRemoveAttachmentStep2(request, response, env);

        return null;
    }

    protected String actionAddScreenshotStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = addScreenshot(params, item, env);
        if (!canContinue)
            return FMTemplateSelector.select("EditAttachment", "addScreenshot", env, request);

        persistance.update(item);

        // commit new version
        Misc.commitRelation(item.getData().getRootElement(), relation, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation, true));
        return null;
    }

    private String actionManageAttachments(HttpServletRequest request, Map env) throws Exception {
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        env.put(VAR_XML, NodeModel.wrap((new DOMWriter().write(item.getData()))));
        return FMTemplateSelector.select("EditAttachment", "manage", env, request);
    }

    private String actionRemoveAttachmentStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        env.put(VAR_XML, NodeModel.wrap((new DOMWriter().write(item.getData()))));

        Object param = params.get(PARAM_ATTACHMENT);
        if (param == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nevybrali jste ¾ádnou pøílohu na smazání.", env, null);
            return FMTemplateSelector.select("EditAttachment", "manage", env, request);
        }
        if (param instanceof String)
            params.put(PARAM_ATTACHMENT, Collections.singletonList(param));

        return FMTemplateSelector.select("EditAttachment", "remove", env, request);
    }

    private String actionRemoveAttachmentStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();

        Document document = (Document) item.getData();
        Element inset = (Element) document.selectSingleNode("/data/inset");
        List list = Tools.asList(params.get(PARAM_ATTACHMENT));
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            String path = (String) iter.next();
            Element element = (Element) inset.selectSingleNode("//*[.='"+path+"']");
            if (element == null) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Pøíloha '"+path+"' nebyla nalezena mezi daty!", env, null);
                return actionManageAttachments(request, env);
            }

            deleteAttachment(path, env, user, request);
            path = element.attributeValue("thumbnail");
            if (path != null)
                deleteAttachment(path, env, user, request);

            element.detach();
        }

        persistance.update(item);

        // commit new version
        Misc.commitRelation(item.getData().getRootElement(), relation, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation, true));
        return null;
    }

    /**
     * Uploads new screenshot and creates a thumbnail (if needed). Changes to Item are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean addScreenshot(Map params, Item item, Map env) throws IOException {
        FileItem fileItem = (FileItem) params.get(PARAM_SCREENSHOT);
        PathGenerator pathGenerator = AbcConfig.getPathGenerator();
        Element root = item.getData().getRootElement();

        if (fileItem == null) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Zadejte prosím cestu k souboru.", env, null);
            return false;
        }

        String suffix = getFileSuffix(fileItem.getName()).toLowerCase();
        if (!(suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif"))) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Soubor musí být typu PNG, GIF nebo JPEG.", env, null);
            return false;
        }

        Relation tmpRel = new Relation(0);
        tmpRel.setInitialized(true);
        tmpRel.setChild(item);
        String name = Tools.childName(tmpRel);

        File imageFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name, "." + suffix);
        try {
            fileItem.write(imageFile);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Chyba pøi zápisu na disk!", env, null);
            log.error("Není mo¾né ulo¾it obrázek " + imageFile.getAbsolutePath() + " na disk!", e);
            return false;
        }

        File thumbnailFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name+"-mini", ".jpg");
        boolean thumbnail = ImageTool.createThumbnail(imageFile, thumbnailFile);

        Element screenshots = DocumentHelper.makeElement(root, "inset/images");
        Element screenshot = screenshots.addElement("image");
        String path = getWebPath(imageFile.getAbsolutePath());
        screenshot.setText(path);

        if (thumbnail) {
            path = getWebPath(thumbnailFile.getAbsolutePath());
            screenshot.addAttribute("thumbnail", path);
        }

        return true;
    }

    /**
     * Deletes specified file from hard drive.
     * @param path absolute path within web application context
     * @param env environment
     * @param user user performing this operation
     * @param request
     */
    private void deleteAttachment(String path, Map env, User user, HttpServletRequest request) {
        File file = new File(AbcConfig.getDeployPath()+path);
        if (! file.exists()) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nepodaøilo se smazat soubor "+path, env, request.getSession());
            return;
        }
        if (! file.delete()) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nepodaøilo se smazat soubor " + path, env, request.getSession());
            log.warn("Nepodaøilo se smazat soubor "+file.getAbsolutePath());
            return;
        }
        AdminLogger.logEvent(user, "remove | attachment "+path);
    }

    /**
     * @return text after last dot in string.
     */
    private String getFileSuffix(String name) {
        if (name == null)
            return "";
        int i = name.lastIndexOf('.');
        if (i == -1)
            return "";
        else
            return name.substring(i + 1);
    }

    private String getWebPath(String absolutePath) {
        String deployPath = AbcConfig.getDeployPath();
        return absolutePath.substring(deployPath.length() - 1);
    }
}
