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
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.video.Thumbnailer;
import cz.abclinuxu.data.view.VideoServer;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.ImageTool;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author lubos
 */
public class EditVideo implements AbcAction, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditVideo.class);
    
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_URL = "url";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_REDIRECT = "redirect";
    
    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";
    public static final String ACTION_I_LIKE = "favourite";
    
    public static final String PREF_URLS = "urls";
    public static final String PREF_PLAYERS = "players";
    
    public static Map<String,VideoServer> videoServers;
    
    static {
        EditVideo instance = new EditVideo();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    /*
        http://www.vimeo.com/2010210
        http://www.vimeo.com/moogaloop/load/clip:2010210/ - neobsahuje maly nahled
     */
    
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);
        Relation relation = (Relation) InstanceUtils.instantiateParam(Constants.PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");
        
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        
        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);
        
        if (ACTION_I_LIKE.equals(action)) {
            ActionProtector.ensureContract(request, EditVideo.class, true, false, false, true);
            return actionILike(request, response, env);
        }
        
        boolean isBlogOwner = false;
        if (relation.getChild() instanceof Item) {
            Item item = (Item) relation.getChild();
            if (item.getType() == Item.BLOG && item.getOwner() == user.getId())
                isBlogOwner = true;
        }
        
        if (ACTION_ADD.equals(action)) {   
            if (!Tools.permissionsFor(user, relation).canCreate() && !isBlogOwner)
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return FMTemplateSelector.select("EditVideo", "add", env, request);
        }
        
        if (ACTION_ADD_STEP2.equals(action)) {
            if (!Tools.permissionsFor(user, relation).canCreate() && !isBlogOwner)
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            ActionProtector.ensureContract(request, EditVideo.class, true, true, true, false);
            return actionAddStep2(request, response, env, relation);
        }
        
        Item item = (Item) relation.getChild();
        if (!Tools.permissionsFor(user, relation).canModify() && item.getOwner() != user.getId())
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        
        if (ACTION_EDIT.equals(action))
            return actionEditStep1(request, response, env, relation);
        
        if (ACTION_EDIT_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditVideo.class, true, true, true, false);
            return actionEditStep2(request, response, env, relation);
        }
        
        isBlogOwner = false;
        if (relation.getParent() instanceof Item) {
            item = (Item) relation.getParent();
            if (item.getType() == Item.BLOG && item.getOwner() == user.getId())
                isBlogOwner = true;
        }
        
        if (ACTION_REMOVE.equals(action) || ACTION_REMOVE_STEP2.equals(action)) {
            if (!Tools.permissionsFor(user, relation).canDelete() && !isBlogOwner)
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            
            if (ACTION_REMOVE_STEP2.equals(action)) {
                ActionProtector.ensureContract(request, EditVideo.class, true, true, true, false);
                return actionRemove(request, response, env, relation);
            } else {
                return FMTemplateSelector.select("EditVideo", "remove", env, request);
            }
        }
        
        throw new MissingArgumentException("Chybí argument action!");
    }
    
    private static String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, Relation upperRelation) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        String urlRedirect = (String) params.get(PARAM_REDIRECT);
        
        Document documentItem = DocumentHelper.createDocument();
        Element root = documentItem.addElement("data");
        
        Item item = new Item(0, Item.VIDEO);
        item.setData(documentItem);
        item.setOwner(user.getId());
        
        if (upperRelation.getChild() instanceof GenericDataObject) {
            GenericDataObject gdo = (GenericDataObject) upperRelation.getChild();
            item.setGroup(gdo.getGroup());
        }
        
        Relation relation = new Relation(upperRelation.getChild(), item, upperRelation.getId());
        
        boolean canContinue;
        canContinue = setTitle(item, params, env);
        canContinue &= setUrl(item, root, params, env);
        canContinue &= setDescription(root, params, env);
        
        if (upperRelation.getChild() instanceof Category)
            canContinue &= setItemUrl(relation, item, persistence);
        
        if (!canContinue)
            return FMTemplateSelector.select("EditVideo", "add", env, request);
        
        persistence.create(item);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);
        
        if (upperRelation.getChild() instanceof Category) {
            if (!setThumbnail(relation, item, root, params))
                ServletUtils.addError(Constants.ERROR_GENERIC, "Nepodařilo se získat náhled videa", env, request.getSession());
            else
                persistence.update(item);
            
            VariableFetcher.getInstance().refreshVideos();
            EditDiscussion.createEmptyDiscussion(relation, user, persistence);
        }
        
        TagTool.assignDetectedTags(item, user);
        
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        
        if (Misc.empty(urlRedirect))
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        else
            urlUtils.redirect(response, urlRedirect);
        
        return null;
    }
    
    private static String actionEditStep1(HttpServletRequest request, HttpServletResponse response, Map env, Relation relation) throws Exception {
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        
        params.put(PARAM_TITLE, item.getTitle());
        Element elem = (Element) root.selectSingleNode("url");
        
        if (elem != null)
            params.put(PARAM_URL, elem.getText());
        
        elem = (Element) root.selectSingleNode("description");
        if (elem != null)
            params.put(PARAM_DESCRIPTION, elem.getText());
        
        return FMTemplateSelector.select("EditVideo", "edit", env, request);
    }
    
    private static String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env, Relation relation) throws Exception {
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        
        boolean canContinue;
        canContinue = setTitle(item, params, env);
        canContinue &= setUrl(item, root, params, env);
        canContinue &= setDescription(root, params, env);
        
        if (relation.getParent() instanceof Category) {
            if (!setThumbnail(relation, item, root, params))
                ServletUtils.addError(Constants.ERROR_GENERIC, "Nepodařilo se získat náhled videa", env, request.getSession());
        }
        
        if (!canContinue)
            return FMTemplateSelector.select("EditVideo", "edit", env, request);
        
        persistence.update(item);
        
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        
        return null;
    }
    
    private static String actionRemove(HttpServletRequest request, HttpServletResponse response, Map env, Relation relation) throws Exception {
        Relation upperRel = new Relation(relation.getUpper());
        Tools.sync(upperRel);
        Persistence persistence = PersistenceFactory.getPersistence();
        
        persistence.remove(relation);
        
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(upperRel));
        
        return null;
    }
    
    public String actionILike(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
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
    
    private static boolean setTitle(Item item, Map params, Map env) {
        String title = (String) params.get(PARAM_TITLE);
        if (Misc.empty(title)) {
            ServletUtils.addError(PARAM_TITLE, "Zadejte název!", env, null);
            return false;
        }
        
        if (title.indexOf('<') != -1) {
            ServletUtils.addError(PARAM_TITLE, "HTML zde není povoleno!", env, null);
            return false;
        }
        
        item.setTitle(title);
        return true;
    }
    
    private static boolean setUrl(Item item, Element root, Map params, Map env) {
        String url = (String) params.get(PARAM_URL);
        
        if (Misc.empty(url)) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL videa!", env, null);
            return false;
        }
        
        for (VideoServer server : videoServers.values()) {
            RE regexp = new RE(server.getUrlMatcher(), RE.MATCH_SINGLELINE);
            
            if (regexp.match(url)) {
                item.setSubType(server.getName());
                Element elem = DocumentHelper.makeElement(root, "code");
                elem.setText(regexp.getParen(1));
                
                elem = DocumentHelper.makeElement(root, "url");
                elem.setText(url);
                
                return true;
            }
        }
        
        ServletUtils.addError(PARAM_URL, "Nebylo rozpoznáno podporované URL!", env, null);
        return false;
    }
    
    private static boolean setThumbnail(Relation rel, Item item, Element root, Map params) throws Exception {
        String url = (String) params.get(PARAM_URL);
        Thumbnailer thumbnailer = Thumbnailer.getInstance(item.getSubType());
        Node node = root.selectSingleNode("thumbnail");
        
        if (node != null) {
            new File(node.getText()).delete();
            node.detach();
        }
        
        if (thumbnailer == null)
            return false;
        
        try {
            String path = "/images/videos/"+rel.getId()+".jpg";
            String fileName = AbcConfig.calculateDeployedPath(path.substring(1));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
            String source = thumbnailer.getThumbnailUrl(url);
            
            if (source == null)
                return false;
            
            URL thumb = new URL(source);
            InputStream in = thumb.openStream();

            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1)
                out.write(buffer, 0, numRead);

            out.close();
            in.close();
            
            File file = new File(fileName);
            ImageTool.createThumbnailMaxSize(file, file, 96, true);
            
            Element elem = DocumentHelper.makeElement(root, "thumbnail");
            elem.setText(path);
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
    
    private static boolean setDescription(Element root, Map params, Map env) {
        String desc = (String) params.get(PARAM_DESCRIPTION);
        
        if (Misc.empty(desc)) {
            Node node = root.selectSingleNode("description");
            if (node != null)
                node.detach();
            
            return true;
        }
        
        if (desc.indexOf('<') != -1) {
            ServletUtils.addError(PARAM_DESCRIPTION, "HTML zde není povoleno!", env, null);
            return false;
        }
        
        Element elem = DocumentHelper.makeElement(root, "description");
        desc = Tools.processLocalLinks(desc, null);
        elem.setText(desc);
        
        return true;
    }
    
    private static boolean setItemUrl(Relation relation, Item item, Persistence persistence) {
        String name = item.getTitle();
        Relation upper = new Relation(relation.getUpper());
        
        persistence.synchronize(upper);
        
        name = Misc.filterDangerousCharacters(name);
        name = upper.getUrl() + "/" + name;

        String url = URLManager.enforceAbsoluteURL(name);
        url = URLManager.protectFromDuplicates(url);
        relation.setUrl(url);
        return true;
    }
    
    public void configure(Preferences prefs) throws ConfigurationException {
        RECompiler reCompiler = new RECompiler();
        
        try {
            Preferences subprefs = prefs.node(PREF_URLS);
            String[] keys = subprefs.keys();
            Preferences subPlayers = prefs.node(PREF_PLAYERS);
            
            videoServers = new HashMap(keys.length);
            
            for (int i = 0; i < keys.length; i++) {
                VideoServer server = new VideoServer(keys[i]);
                
                server.setUrlMatcher(reCompiler.compile(subprefs.get(keys[i], null)));
                server.setCode(subPlayers.get(keys[i], null));
                
                videoServers.put(keys[i], server);
            }
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e.getMessage(), e.getCause());
        }
    }
}
