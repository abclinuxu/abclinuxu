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
import cz.abclinuxu.utils.PathGenerator;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.IOException;
import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

/**
 * @author literakl
 * @since 3.6.2006
 */
public class EditAttachment implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditAttachment.class);

    public static final String VAR_RELATION = "RELATION";
    public static final String PARAM_SCREENSHOT = "screenshot";
    public static final String PARAM_IMAGE_ID = "imageId";

    public static final String ACTION_ADD_SCREENSHOT = "addScreenshot";
    public static final String ACTION_ADD_SCREENSHOT_STEP2 = "addScreenshot2";
    public static final String ACTION_REMOVE_SCREENSHOT = "removeScreenshot";
    public static final String ACTION_REMOVE_SCREENSHOT_STEP2 = "removeScreenshot2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (action == null)
            throw new MissingArgumentException("Chybí parametr action!");

        if (action.equals(ACTION_ADD_SCREENSHOT))
            return FMTemplateSelector.select("EditSoftware", "addScreenshot", env, request);

        if (action.equals(ACTION_ADD_SCREENSHOT_STEP2))
            return actionAddScreenshotStep2(request, response, env);

        if (action.equals(ACTION_REMOVE_SCREENSHOT))
            return FMTemplateSelector.select("EditSoftware", "removeScreenshot", env, request);

        if (action.equals(ACTION_REMOVE_SCREENSHOT_STEP2))
            return actionRemoveScreenshotStep2(request, response, env);

        return null;
    }

    protected String actionAddScreenshotStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= addScreenshot(params, item, env);

        if (!canContinue) {
            return FMTemplateSelector.select("EditSoftware", "addScreenshot", env, request);
        }

        persistance.update(item);

        String url = relation.getUrl();
        if (url == null)
            url = "/software/show/" + relation.getId();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionRemoveScreenshotStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= removeScreenshot(params, item, env);

        // TODO ak sa nepodari vymazat screenshot, mozno uz neexistuje
        if (!canContinue) {
            return FMTemplateSelector.select("EditSoftware", "removeScreenshot", env, request);
        }

        persistance.update(item);

        String url = relation.getUrl();
        if (url == null)
            url = "/software/show/" + relation.getId();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Uploads new screenshot and creates a thumbnail (if needed). Changes are not synchronized with persistance.
     *
     * @param params map holding request's parameters
     * @param root   root element of record to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean addScreenshot(Map params, Item item, Map env) throws IOException {
        FileItem fileItem = (FileItem) params.get(PARAM_SCREENSHOT);
        PathGenerator pathGenerator = AbcConfig.getPathGenerator();
        Element root = item.getData().getRootElement();

        if (fileItem == null) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Vyberte soubor s va?ím screenshotem!", env, null);
            return false;
        }

        String suffix = getFileSuffix(fileItem.getName()).toLowerCase();
        if (!(suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif"))) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Soubor musí být typu JPG, GIF nebo JPEG!", env, null);
            return false;
        }

        int nextId = getNextScreenshotId(root);

        String fileName = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, nextId + "." + suffix);
        File file = new File(AbcConfig.calculateDeployedPath(fileName));
        try {
            fileItem.write(file);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_SCREENSHOT, "Chyba p?i zápisu na disk!", env, null);
            log.error("Neni mozne ulozit fotografii " + file.getAbsolutePath() + " na disk!", e);
            return false;
        }

        String thumbnailName = pathGenerator.getPath(item, PathGenerator.Type.SCREENSHOT, nextId + "_thumb.jpg");
        boolean thumbnail = cz.abclinuxu.utils.ImageTool.createThumbnail(AbcConfig.calculateDeployedPath(fileName),
                AbcConfig.calculateDeployedPath(thumbnailName));

        Element screenshots = DocumentHelper.makeElement(root, "screenshots");
        Element screenshot = screenshots.addElement("screenshot");
        screenshot.setAttributeValue("id", new Integer(nextId).toString());
        screenshot.addElement("image").setText(fileName);
        screenshots.setAttributeValue("nextId", new Integer(nextId + 1).toString());

        if (thumbnail) {
            screenshot.addElement("thumbnail").setText(thumbnailName);
        }

        return true;
    }

    /**
     * TODO ak sa mazanie nepodari, musi na to upozornit uzivatela
     * <p/>
     * Removes screenshot from software and deletes the image and thumbnail. Changes are not synchronized with persistance.
     *
     * @param params map holding request's parameters
     * @param root   root element of record to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean removeScreenshot(Map params, Item item, Map env) {
        String imageId = (String) params.get(PARAM_IMAGE_ID);
        Element root = item.getData().getRootElement();
        Node node = root.selectSingleNode("/data/screenshots/screenshot[@id='" + imageId + "']");

        if (node == null || !(node instanceof Element))
            return false;

        Element element = (Element) node;
        Element elementImage = element.element("image");
        Element elementThumbnail = element.element("thumbnail");

        if (elementImage != null)
            deleteFile(elementImage);

        if (elementThumbnail != null)
            deleteFile(elementThumbnail);

        node.detach();

        return true;
    }

    /**
     * Extracts text after last dot in string.
     *
     * @param name
     * @return
     */
    private String getFilePrefix(String name) {
        if (name == null)
            return "";
        int i = name.lastIndexOf('.');
        if (i == -1)
            return "";
        else
            return name.substring(0, i - 1);
    }

    /**
     * Extracts text after last dot in string.
     *
     * @param name
     * @return
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

    /**
     * Deletes a file, which filename is the content of <code>element</code>.
     *
     * @return true if the file was deleted
     * @param Element, which contains file name
     */
    private boolean deleteFile(Element element) {
        String fileName = element.getText();

        if (fileName == null)
            return false;

        File file = new File(AbcConfig.calculateDeployedPath(fileName));
        if (!file.delete()) {
            log.warn("File " + fileName + "couldn't be deleted.");
            return false;
        }

        return true;
    }

    /**
     * @return Id for next screenshot.
     */
    private int getNextScreenshotId(Element root) {
        Element screenshots = (Element) root.selectSingleNode("/data/screenshots");
        if (screenshots != null) {
            try {
                return new Integer(screenshots.attributeValue("nextId")).intValue();
            } catch (NumberFormatException e) {
                log.error(e);
            }
        }

        return 0;
    }
}
