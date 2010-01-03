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
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Data;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.data.view.RowComment;
import cz.abclinuxu.utils.PathGenerator;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.ImageTool;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;

import org.apache.commons.fileupload.FileItem;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Node;
import static cz.abclinuxu.servlets.Constants.PARAM_RELATION;

/**
 * @author literakl
 * @since 3.6.2006
 */
public class EditAttachment implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditAttachment.class);

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ATTACHMENTS = "ATTACHMENTS";

    public static final String PARAM_SCREENSHOT = "screenshot";
    public static final String PARAM_ATTACHMENT = "attachment";
    public static final String PARAM_REMOVE = "remove";
    public static final String PARAM_SET_VISIBLE = "setVisible";
    public static final String PARAM_SET_HIDDEN = "setHidden";
    public static final String PARAM_THREAD = "threadId";

    public static final String ACTION_ADD_SCREENSHOT = "addScreenshot";
    public static final String ACTION_ADD_SCREENSHOT_STEP2 = "addScreenshot2";
    public static final String ACTION_ADD_FILE = "addFile";
    public static final String ACTION_ADD_FILE_STEP2 = "addFile2";
    public static final String ACTION_MANAGE = "manage";
    public static final String ACTION_MANAGE_STEP2 = "manage2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        Tools.sync(relation);
        env.put(VAR_RELATION, relation);
        GenericObject child = relation.getChild();
        GenericDataObject gdo = null;

        if (child instanceof GenericDataObject)
            gdo = (GenericDataObject) child;

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        boolean allowed = true;
        if (child instanceof Item) {
            Item item = (Item) child;
            int type = item.getType();

            if ( (type == Item.BLOG || type == Item.UNPUBLISHED_BLOG) && item.getOwner() != user.getId()) {
                if (gdo != null)
                    allowed = Tools.permissionsFor(user, relation).canModify();
                else
                    allowed = false;
            }
        }

        if ( ! allowed)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_ADD_SCREENSHOT))
            return FMTemplateSelector.select("EditAttachment", "addScreenshot", env, request);

        if (action.equals(ACTION_ADD_SCREENSHOT_STEP2)) {
            ActionProtector.ensureContract(request, EditAttachment.class, true, true, true, false);
            return actionAddScreenshotStep2(request, response, env);
        }

        if (gdo != null)
            allowed = Tools.permissionsFor(user, relation).canDelete();
        else
            allowed = false;

        if (child instanceof Item) {
            Item item = (Item) child;
            int type = item.getType();

            if ( (type == Item.BLOG || type == Item.UNPUBLISHED_BLOG
                    || type == Item.EVENT || type == Item.UNPUBLISHED_EVENT) && item.getOwner() == user.getId())
                allowed = true;
            else if (type == Item.DISCUSSION && user.hasRole(Roles.DISCUSSION_ADMIN))
                allowed = true;
        }

        if (!allowed)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action.equals(ACTION_MANAGE))
            return actionManageAttachments(request, env);

        if (action.equals(ACTION_MANAGE_STEP2)) {
            ActionProtector.ensureContract(request, EditAttachment.class, true, true, true, false);
            return actionManageAttachmentsStep2(request, response, env);
        }

        if (action.equals(ACTION_REMOVE))
            return actionRemoveAttachmentStep1(request, env);

        if (action.equals(ACTION_REMOVE_STEP2)) {
            ActionProtector.ensureContract(request, EditAttachment.class, true, true, true, false);
            return actionRemoveAttachmentStep2(response, env);
        }

        if (action.equals(ACTION_ADD_FILE))
            return FMTemplateSelector.select("EditAttachment", "addFile", env, request);
        if (action.equals(ACTION_ADD_FILE_STEP2)) {
            ActionProtector.ensureContract(request, EditAttachment.class, true, true, true, false);
            return actionAddFilesStep2(request, response, env);
        }

        return null;
    }

    protected String actionAddScreenshotStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);

        Data data = new Data();
        data.setType(Data.IMAGE);
        data.setOwner(user.getId());

        if (relation.getChild() instanceof GenericDataObject) {
            GenericDataObject gdo = (GenericDataObject) relation.getChild();
            data.setGroup(gdo.getGroup());
        }

        boolean canContinue = addScreenshot(params, data, relation, env);
        if (!canContinue)
            return FMTemplateSelector.select("EditAttachment", "addScreenshot", env, request);
        persistence.create(data);

        Relation dataRelation = new Relation(relation.getChild(), data, relation.getId());
        persistence.create(dataRelation);
        dataRelation.getParent().addChildRelation(dataRelation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    private String actionAddFilesStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Tools.sync(item);

        boolean success = storeAttachments(relation, env);

        if (success) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            return null;
        } else
            return FMTemplateSelector.select("EditAttachment", "addFile", env, request);
    }

    private String actionManageAttachments(HttpServletRequest request, Map env) throws Exception {
        Relation relation = (Relation) env.get(VAR_RELATION);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item item = (Item) relation.getChild();
        Map<String, List<Relation>> map = Tools.groupByType(item.getChildren(), "Data");
        int threadId = Misc.parseInt((String) params.get(EditDiscussion.PARAM_THREAD), 0);

        List<Relation> attachments = map.get(Constants.TYPE_DATA);

        if (threadId != 0 && item.getType() == Item.DISCUSSION && attachments != null) {
            // remove attachments not attached to the specified comment
            Persistence persistence = PersistenceFactory.getPersistence();
            Record record = getDiscussion(item, persistence);
            DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
            Comment comment = dizRecord.getComment(threadId);

            if (comment == null)
                throw new MissingArgumentException("Neplatne threadId!");

            Element commentRoot = comment.getData().getRootElement();

            for (int i = 0; i < attachments.size(); i++) {
                if (commentRoot.selectSingleNode("attachment[text()='"+attachments.get(i).getId()+"']") == null)
                    attachments.remove(i--);
            }
        }

        env.put(VAR_ATTACHMENTS, attachments);
        return FMTemplateSelector.select("EditAttachment", "manage", env, request);
    }

    private String actionRemoveAttachmentStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Map<String, List<Relation>> map = Tools.groupByType(item.getChildren(), "Data");
        List dataRelations = map.get(Constants.TYPE_DATA);

        Object param = params.get(PARAM_ATTACHMENT);
        if (param == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nevybrali jste žádnou přílohu na smazání.", env, null);
            return FMTemplateSelector.select("EditAttachment", "manage", env, request);
        }
        if (param instanceof String) {
            param = Collections.singletonList(param);
            params.put(PARAM_ATTACHMENT, param);
        }

        for (Iterator iter = dataRelations.iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            boolean found = false;
            for (Iterator iterIn = ((List) param).iterator(); iterIn.hasNext();) {
                String s = (String) iterIn.next();
                int id = Misc.parseInt(s, -1);
                if (rel.getId() == id) {
                    found = true;
                    break;
                }
            }
            if (! found)
                iter.remove();
        }

        env.put(VAR_ATTACHMENTS, dataRelations);

        return FMTemplateSelector.select("EditAttachment", "remove", env, request);
    }

    private String actionManageAttachmentsStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);

        if (params.containsKey(PARAM_REMOVE))
            return actionRemoveAttachmentStep2(response, env);

        boolean setHidden, setVisible;
        setHidden = params.containsKey(PARAM_SET_HIDDEN);
        setVisible = params.containsKey(PARAM_SET_VISIBLE);

        boolean bSuccess = false;
        if (setHidden || setVisible)
            bSuccess = setAttachmentVisibility(env, setHidden);

        if (bSuccess) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            return null;
        } else
            return FMTemplateSelector.select("EditAttachment", "manage", env, request);
    }

    private String actionRemoveAttachmentStep2(HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Comment comment = null;
        Record record = null;
        int threadId = Misc.parseInt((String) params.get(EditDiscussion.PARAM_THREAD), 0);

        if (threadId != 0 && item.getType() == Item.DISCUSSION) {
            // remove attachments not attached to the specified comment
            record = getDiscussion(item, persistence);
            DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();

            comment = dizRecord.getComment(threadId);

            if (comment == null)
                throw new MissingArgumentException("Neplatne threadId!");

            ((RowComment)comment).set_dirty(true);
        }

        List list = Tools.asList(params.get(PARAM_ATTACHMENT));
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            int rid = Misc.parseInt((String) iter.next(), -1);
            Relation dataRelation = (Relation) persistence.findById(new Relation(rid));
            String path = Tools.xpath(dataRelation.getChild(), "/data/object/@path");

            if (!Misc.empty(path))
                new File(path).delete();

            if (comment != null) {
                Node node = comment.getData().selectSingleNode("//attachment[text()='"+rid+"']");
                if (node != null)
                    node.detach();
            }

            persistence.remove(dataRelation);
            dataRelation.getParent().removeChildRelation(dataRelation);
            AdminLogger.logEvent(user, "smazal přílohu " + dataRelation);
        }

        if (record != null)
            persistence.update(record);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    private Record getDiscussion(Item discussion, Persistence persistence) {
        Map childrenMap = Tools.groupByType(discussion.getChildren(), "Record");
        List<Relation> recordRelations = (List<Relation>) childrenMap.get(Constants.TYPE_RECORD);
        Relation relation = recordRelations.get(0);
        Record record = (Record) persistence.findById(relation.getChild());

        return record;
    }

    /**
     * Uploads new screenshot and creates a thumbnail (if needed). Changes to Item are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param data data object to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean addScreenshot(Map params, Data data, Relation parentRelation, Map env) throws IOException {
        FileItem fileItem = (FileItem) params.get(PARAM_SCREENSHOT);
        PathGenerator pathGenerator = AbcConfig.getPathGenerator();

        if (fileItem == null) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Zadejte prosím cestu k souboru.", env, null);
            return false;
        }

        if (!checkImage(params, env))
            return false;

        data.setSubType(fileItem.getContentType());

        String suffix = Misc.getFileSuffix(fileItem.getName()).toLowerCase();
        if (!(suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif"))) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Soubor musí být typu PNG, GIF nebo JPEG.", env, null);
            return false;
        }

        Item item = (Item)parentRelation.getChild();
        String name = getFileName(item);
        File imageFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name, "." + suffix);
        try {
            fileItem.write(imageFile);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Chyba při zápisu na disk!", env, null);
            log.error("Není možné uložit obrázek " + imageFile.getAbsolutePath() + " na disk!", e);
            return false;
        }

        File thumbnailFile = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, name+"-mini", ".png");
        boolean thumbnail = ImageTool.createThumbnail(imageFile, thumbnailFile);

        Document document = DocumentHelper.createDocument();
        data.setData(document);

        Element root = document.addElement("data");
        String path = Misc.getWebPath(imageFile.getAbsolutePath());
        Element screenshot = root.addElement("object").addAttribute("path", path);
        screenshot.addElement("originalFilename").setText(fileItem.getName());
        screenshot.addElement("size").setText(Long.toString(fileItem.getSize()));

        if (thumbnail) {
            path = Misc.getWebPath(thumbnailFile.getAbsolutePath());
            screenshot.addElement("thumbnail").addAttribute("path", path);
        }

        return true;
    }

    /**
     * Creates valid file name generated from item's title.
     * @param item item
     * @return valid file name (lower case, no diacritics, slashes etc)
     */
    public static String getFileName(Item item) {
        Relation tmpRel = new Relation(0);
        tmpRel.setInitialized(true);
        tmpRel.setChild(item);
        item.setInitialized(true);
        String name = Tools.childName(tmpRel);
        // todo these checks are already in PathGeneratorImpl
        name = Misc.filterDangerousCharacters(name);
        name = Misc.removeDiacritics(name);
        name = name.toLowerCase();
        // TODO more checks like in URLManager.normalizeCharacters()
        return name;
    }

    /**
     * Creates valid normalized file name.
     * @param name
     * @return
     */
    public static String getNormalizedFileName(String name) {
        int i = name.lastIndexOf('/');
        if (i != -1)
            name = name.substring(i + 1);
//        name = Misc.filterDangerousCharacters(name);
//        name = DiacriticRemover.getInstance().removeDiacritics(name);
//        name = name.toLowerCase();
        return name;
    }

    /**
     * Checks an image, whether it is valid or not.
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
        } catch (IOException e) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Obrázek nelze načíst: " + e.getMessage(), env, null);
            return false;
        }
        return true;
    }

    /**
     * Stores generic attachments
     * @param relation
     * @param env
     * @return true if the operation was successful
     * @throws java.io.IOException
     */
    static boolean storeAttachments(Relation relation, Map env) throws IOException {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        PathGenerator pathGenerator = AbcConfig.getPathGenerator();
        Persistence persistence = PersistenceFactory.getPersistence();

        List<FileItem> files = (List<FileItem>) Tools.asList(params.get(PARAM_ATTACHMENT));

        for (FileItem fileItem : files) {

            if (fileItem.getSize() == 0)
                continue;

            Data data = new Data();
            Item item = (Item) relation.getChild();

            data.setSubType(fileItem.getContentType());
            data.setOwner(user.getId());
            data.setGroup(item.getGroup());

            String fileName = EditAttachment.getNormalizedFileName(fileItem.getName());
            int i = fileName.lastIndexOf('.');
            String prefix, suffix;
            if (i == -1) {
                prefix = fileName;
                suffix = "";
            } else {
                prefix = fileName.substring(0, i);
                suffix = fileName.substring(i + 1);
            }

            File file;

            if (item.getType() == Item.ARTICLE)
                file = pathGenerator.getPath(relation, PathGenerator.Type.ARTICLE_ATTACHMENT, prefix, "." + suffix);
            else
                file = pathGenerator.getPath(item, PathGenerator.Type.ATTACHMENT, prefix, "." + suffix);

            try {
                fileItem.write(file);
            } catch (Exception e) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Chyba při zápisu souboru " + fileName + " na disk!", env, null);
                log.error("Není možné uložit soubor " + file.getAbsolutePath() + " na disk!", e);
                return false;
            }

            Document document = DocumentHelper.createDocument();
            data.setData(document);
            Element root = document.addElement("data");
            String path = Misc.getWebPath(file.getAbsolutePath());
            Element attachment = root.addElement("object").addAttribute("path", path).addAttribute("hidden", "false");
            attachment.addElement("originalFilename").setText(fileName);
            attachment.addElement("size").setText(Long.toString(fileItem.getSize()));

            persistence.create(data);
            Relation dataRelation = new Relation(item, data, relation.getId());
            persistence.create(dataRelation);
            dataRelation.getParent().addChildRelation(dataRelation);
        }

        return true;
    }

    boolean setAttachmentVisibility(Map env, boolean hidden) {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        List<String> list = (List<String>) Tools.asList(params.get(PARAM_ATTACHMENT));
        for (String srid : list) {
            int rid = Misc.parseInt(srid, -1);
            Relation dataRelation = new Relation(rid);
            Tools.sync(dataRelation);

            GenericDataObject gdo = (GenericDataObject) dataRelation.getChild();
            Document doc = gdo.getData();

            Element object = (Element) doc.selectSingleNode("/data/object");
            Attribute attr = object.attribute("hidden");

            if (attr == null)
                object.addAttribute("hidden", String.valueOf(hidden));
            else
                attr.setValue(String.valueOf(hidden));
            persistence.update(gdo);
        }

        return true;
    }
}
