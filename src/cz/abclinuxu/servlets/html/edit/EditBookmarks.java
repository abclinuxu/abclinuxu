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
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Bookmark;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author lubos
 */
public class EditBookmarks implements AbcAction {
    public static final String PARAM_PATH = "path";
    public static final String PARAM_DIRECTORY_NAME = "directoryName";
    public static final String PARAM_RID = "rid";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_TARGET_PATH = "targetPath";
    public static final String PARAM_URL = "url";
    public static final String PARAM_REMOVE = "remove";
    public static final String PARAM_MOVE = "move";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_ORDER_BY = "orderBy";

    public static final String VAR_BOOKMARKS = "BOOKMARKS";
    public static final String VAR_MANAGED = "MANAGED";

    public static final String ACTION_TO_BOOKMARKS = "add";
    public static final String ACTION_ADD_LINK = "addLink";
    public static final String ACTION_ADD_LINK_STEP2 = "addLink2";
    public static final String ACTION_MANAGE_BOOKMARKS = "manage";
    public static final String ACTION_CREATE_DIRECTORY = "createDirectory";
    public static final String ACTION_REMOVE_DIRECTORY = "removeDirectory";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) InstanceUtils.instantiateParam(ViewUser.PARAM_USER_SHORT, User.class, params, request);
        String action = (String) params.get(PARAM_ACTION);

        if (managed == null)
            throw new MissingArgumentException("Chybi userId!");

        if (!Misc.empty(action)) {
            User user = (User) env.get(Constants.VAR_USER);

            if (user == null)
                return FMTemplateSelector.select("ViewUser", "login", env, request);

            managed = (User) PersistenceFactory.getPersistence().findById(managed);
            if (!user.hasRole(Roles.USER_ADMIN) && managed.getId() != user.getId())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            env.put(VAR_MANAGED, managed);
            if (action.equals(ACTION_ADD_LINK))
                return FMTemplateSelector.select("EditBookmarks", "add", env, request);

            ActionProtector.ensureContract(request, EditBookmarks.class, true, false, false, true);

            if (action.equals(ACTION_TO_BOOKMARKS))
                return actionAddToBookmarks(request, response, managed, env);

            if (action.equals(ACTION_ADD_LINK_STEP2))
                return actionAddLinkStep2(request, response, managed, env);

            if (action.equals(ACTION_MANAGE_BOOKMARKS))
                return actionManageBookmarks(request, response, env, managed);

            if (action.equals(ACTION_CREATE_DIRECTORY))
                return actionCreateDirectory(request, response, env, managed);

            if (action.equals(ACTION_REMOVE_DIRECTORY))
                return actionRemoveDirectory(request, response, env, managed);
        }

        return processList(request, env, managed);
    }

    public static String processList(HttpServletRequest request, Map env, User user) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String path = (String) params.get(PARAM_PATH);

        env.put(VAR_MANAGED, user);

        Element nodeLinks = (Element) resolveBookmarkDirectory(user, path);

        if (nodeLinks == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Cesta neexistuje nebo neexistují žádné záložky!", env, null);
        } else {

            List bookmarks = new ArrayList();
            List elements = nodeLinks.selectNodes("link");
            String title, prefix, type, url;
            Relation relation;
            Bookmark bookmark;
            Element element;
            if (elements.size() != 0) {
                List relations = new ArrayList(elements.size());
                for (Iterator iter = elements.iterator(); iter.hasNext();) {
                    element = (Element) iter.next();

                    type = element.elementText("type");
                    title = element.elementText("title");

                    if (type.equals(Constants.TYPE_EXTERNAL_DOCUMENT)) {
                        url = element.elementText("url");
                        bookmark = new Bookmark(url, title);
                    } else {
                        relation = new Relation(Integer.parseInt(element.elementText("rid")));
                        prefix = element.elementText("prefix");
                        bookmark = new Bookmark(relation, title, prefix, type);
                        relations.add(relation);
                    }
                    bookmarks.add(bookmark);
                }
                persistence.synchronizeList(relations, true);

                // now update titles for all valid items
                for (Iterator iter = bookmarks.iterator(); iter.hasNext();) {
                    Bookmark b = (Bookmark) iter.next();
                    Relation r = b.getRelation();
                    if (r != null && r.isInitialized())
                        b.setTitle(Tools.childName(r));
                }

                Comparator comparator;
                String order = (String) params.get(PARAM_ORDER_BY);

                if ("type".equals(order))
                    comparator = new Bookmark.TypeComparator();
                else if ("modified".equals(order))
                    comparator = new Bookmark.ModifiedComparator();
                else
                    comparator = new Bookmark.TitleComparator();

                Collections.sort(bookmarks, comparator);
            }
            env.put(VAR_BOOKMARKS, bookmarks);
        }

        return FMTemplateSelector.select("EditBookmarks", "editAll", env, request);
    }

    /**
     * Adds an internal object to the bookmarks.
     */
    protected String actionAddToBookmarks(HttpServletRequest request, HttpServletResponse response, User managed, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        String url = (String) params.get(PARAM_URL);
        if ( url == null || url.length() == 0)
            url = "/lide/" + managed.getLogin() + "/zalozky";

        if (addToBookmarks(request, params, managed, env))
                updateUser(managed, env);

        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Adds a link to the bookmarks
     */
    protected String actionAddLinkStep2(HttpServletRequest request, HttpServletResponse response, User managed, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String path = (String) params.get(PARAM_PATH);

        Element elem = (Element) resolveBookmarkDirectory(managed, path);
        if (elem == null)
            ServletUtils.addError(Constants.ERROR_GENERIC, "Cesta neexistuje!", env, request.getSession());
        else if(addLink(request, params, managed, env))
            updateUser(managed, env);
        else
            return FMTemplateSelector.select("EditBookmarks", "add", env, request);

        if (path == null)
            path = "";

        String url = "/lide/" + managed.getLogin() + "/zalozky?path=" + path;

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionCreateDirectory(HttpServletRequest request, HttpServletResponse response, Map env, User user) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String path = (String) params.get(PARAM_PATH);

        Element nodeLinks = (Element) resolveBookmarkDirectory(user, path);

        if (nodeLinks == null)
            ServletUtils.addError(Constants.ERROR_GENERIC, "Cesta neexistuje!", env, request.getSession());
        else if(addBookmarkDirectory(request, params, nodeLinks, env))
            updateUser(user, env);

        path = (String) params.get(PARAM_PATH);
        if (path == null)
            path = "";

        String url = "/lide/" + user.getLogin() + "/zalozky" + "?path=" + path;
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionRemoveDirectory(HttpServletRequest request, HttpServletResponse response, Map env, User user) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String path = (String) params.get(PARAM_PATH);

        Element nodeLinks = (Element) resolveBookmarkDirectory(user, path);

        if (nodeLinks == null)
            ServletUtils.addError(Constants.ERROR_GENERIC, "Cesta neexistuje!", env, request.getSession());
        else if (removeBookmarkDirectory(request, params, nodeLinks, env))
            updateUser(user, env);

        String url = "/lide/" + user.getLogin() + "/zalozky";
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionManageBookmarks(HttpServletRequest request, HttpServletResponse response, Map env, User managed) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String path = (String) params.get(PARAM_PATH);

        Element nodeLinks = (Element) resolveBookmarkDirectory(managed, path);

        if (nodeLinks == null)
            ServletUtils.addError(Constants.ERROR_GENERIC, "Cesta neexistuje!", env, request.getSession());
        else if (params.containsKey(PARAM_REMOVE))
            removeFromBookmarks(request, params, nodeLinks, env);
        else if (params.containsKey(PARAM_MOVE))
            moveBookmarks(request, params, managed, env);

        updateUser(managed, env);

        String url = "/lide/" + managed.getLogin() + "/zalozky" + "?path=" + path;
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Adds an internal object to the bookmarks.
     */
    protected static boolean addToBookmarks(HttpServletRequest request, Map params, User user, Map env) {
        Persistence persistence = PersistenceFactory.getPersistence();

        String rid = (String) params.get(PARAM_RID);
        String prefix = (String) params.get(PARAM_PREFIX);
        if (Misc.empty(rid) || prefix == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chybí parametr " + PARAM_RID + "!", env, request.getSession());
            return false;
        }

        String path = (String) params.get(PARAM_PATH);

        Element elem = (Element) resolveBookmarkDirectory(user, path);
        if (elem == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Cesta neexistuje!", env, request.getSession());
            return false;
        }

        Node node = elem.selectSingleNode("link/rid[text()=\"" + rid + "\"]");
        if (node != null)
            return true;

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RID, Relation.class, params, request);
        relation = (Relation) persistence.findById(relation);

        GenericObject obj = persistence.findById(relation.getChild());
        String type;
        if (obj instanceof Item)
           type = ((Item) obj).getTypeString();
        else if (obj instanceof Category)
            type = Constants.TYPE_SECTION;
        else if (obj instanceof Poll)
            type = Constants.TYPE_POLL;
        else
            type = Constants.TYPE_OTHER;

        Element link = elem.addElement("link");
        link.addElement("rid").setText(rid);
        link.addElement("title").setText(Tools.childName(relation));
        link.addElement("prefix").setText(prefix);
        link.addElement("type").setText(type);

        ServletUtils.addMessage("Stránka byla přidána do vašich záložek.", env, request.getSession());
        return true;

    }

    /**
     * Adds a link to the bookmarks, invokes addToBookmarks() if the link is internal
     */
    protected static boolean addLink(HttpServletRequest request, Map params, User user, Map env) {
        String url = (String) params.get(PARAM_URL);
        if (Misc.empty(url)) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL!", env, null);
            return false;
        }

        String path = (String) params.get(PARAM_PATH);
        Element elem = (Element) resolveBookmarkDirectory(user, path);

        if (elem == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Neplatná cesta!", env, null);
            return false;
        }

        String myurl;
        String domain = AbcConfig.getDomain();
        int domainPosition = url.indexOf(domain);
        if (domainPosition != -1) {
            int position = url.indexOf("/", domainPosition + domain.length());
            url = url.substring(position);
        }

        int hashPosition = url.indexOf('#');

        if (hashPosition != -1)
            myurl = url.substring(0, hashPosition);
        else
            myurl = url;

        Relation relation = null;

        if (!myurl.startsWith("http://"))
            relation = URLMapper.loadRelationFromUrl(myurl);

        if (relation == null) {
            String title = (String) params.get(PARAM_TITLE);
            if (Misc.empty(title)) {
                ServletUtils.addError(PARAM_TITLE, "Zadejte titulek!", env, null);
                return false;
            }

            Element link = elem.addElement("link");
            link.addElement("url").setText(url);
            link.addElement("title").setText(title);
            link.addElement("type").setText(Constants.TYPE_EXTERNAL_DOCUMENT);

            ServletUtils.addMessage("Stránka byla přidána do vašich záložek.", env, request.getSession());

            return true;
        } else {
            String prefix = UrlUtils.getPrefix(url);

            params.put(PARAM_PREFIX, prefix);
            params.put(PARAM_RID, String.valueOf(relation.getId()));

            return addToBookmarks(request, params, user, env);
        }

    }

    protected static boolean addBookmarkDirectory(HttpServletRequest request, Map params, Element parentDir, Map env) {
        String name = (String) params.get(PARAM_DIRECTORY_NAME);

        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_DIRECTORY_NAME, "Zadejte jméno adresáře!", env, request.getSession());
            return false;
        }

        if (name.indexOf('/') != -1) {
            ServletUtils.addError(PARAM_DIRECTORY_NAME, "Chybné jméno adresáře!", env, request.getSession());
            return false;
        }

        if (parentDir.selectSingleNode("dir[@name='"+name+"']") != null) {
            ServletUtils.addError(PARAM_DIRECTORY_NAME, "Adresář už existuje!", env, request.getSession());
            return false;
        }

        Element elemDir = parentDir.addElement("dir");
        elemDir.addAttribute("name", name);

        ServletUtils.addMessage("Adresář vytvořen", env, request.getSession());

        return true;
    }

    protected static boolean removeBookmarkDirectory(HttpServletRequest request, Map params, Element parentDir, Map env) {
        String path = (String) params.get(PARAM_PATH);

        if (Misc.empty(path) || path.equals("/")) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nelze smazat \"/\" !", env, request.getSession());
            return false;
        }

        parentDir.detach();
        params.put(PARAM_PATH, null);

        ServletUtils.addMessage("Adresář odstraněn", env, request.getSession());
        return true;
    }

    protected static Node resolveBookmarkDirectory(User user, String path) {
        Element nodeLinks = DocumentHelper.makeElement(user.getData(), "/data/links");

        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, "/");
            while (st.hasMoreTokens()) {
                String dir = st.nextToken();
                if (dir.length() == 0)
                    continue;
                nodeLinks = (Element) nodeLinks.selectSingleNode("dir[@name='"+dir+"']");

                if (nodeLinks == null)
                    return null;
            }
        }

        return nodeLinks;
    }

    /**
     * Removes a rids from user's bookmarks. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @return false, if there is a major error.
     */
    protected static boolean removeFromBookmarks(HttpServletRequest request, Map params, Element parentDir, Map env) {
        List rids = Tools.asList(params.get(PARAM_RID));
        List urls = Tools.asList(params.get(PARAM_URL));

        if (rids.isEmpty() && urls.isEmpty()) {
            ServletUtils.addError(PARAM_RID, "Nevybral jste žádné dokumenty.", env, request.getSession());
            return false;
        }

        for (Iterator iter = rids.iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            Node node = parentDir.selectSingleNode("link/rid[text()=\"" + s + "\"]");
            if (node != null) {
                node.getParent().detach();
            }
        }

        for (Iterator iter = urls.iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            Node node = parentDir.selectSingleNode("link/url[text()=\"" + s + "\"]");
            if (node != null) {
                node.getParent().detach();
            }
        }

        ServletUtils.addMessage("Vybrané stránky byly odstraněny ze záložek.", env, request.getSession());
        return true;
    }

    private boolean moveBookmarks(HttpServletRequest request, Map params, User user, Map env) {
        List rids = Tools.asList(params.get(PARAM_RID));
        if (rids.isEmpty()) {
            ServletUtils.addError(PARAM_RID, "Nevybral jste žádné dokumenty.", env, request.getSession());
            return false;
        }

        String sourcePath = (String) params.get(PARAM_PATH);
        String targetPath = (String) params.get(PARAM_TARGET_PATH);
        Element bookmarksSource = (Element) resolveBookmarkDirectory(user, sourcePath);
        Element bookmarksTarget = (Element) resolveBookmarkDirectory(user, targetPath);

        if (bookmarksSource == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Zdrojový adresář neexistuje!", env, request.getSession());
            return false;
        }

        if (bookmarksTarget == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Cílový adresář neexistuje!", env, request.getSession());
            return false;
        }

        for (Iterator iter = rids.iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            Node node = bookmarksSource.selectSingleNode("link/rid[text()=\"" + s + "\"]");
            if (node != null) {
                Node parent = node.getParent();
                parent.detach();
                bookmarksTarget.add(parent);
            }
        }

        ServletUtils.addMessage("Vybrané záložky byly přesunuty.", env, request.getSession());
        return true;
    }

    /**
     * Synchronizes the user with the persistence and with the session, if necessary
     * @param user
     */
    private static void updateUser(User user, Map env) {
        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.update(user);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (user.getId() == sessionUser.getId())
            sessionUser.synchronizeWith(user);
    }
}
