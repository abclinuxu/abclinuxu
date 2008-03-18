/*
 *  Copyright (C) 2005 Leos Literak
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

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.view.Bookmark;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.exceptions.DuplicateKeyException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.ldap.LdapUserManager;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.safehtml.ProfileGuard;
import cz.abclinuxu.utils.parser.safehtml.NoHTMLGuard;
import cz.abclinuxu.utils.parser.safehtml.SignatureHTMLGuard;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.email.forum.SubscribedUsers;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.scheduler.UpdateLinks;
import org.apache.commons.fileupload.FileItem;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.htmlparser.util.ParserException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.prefs.Preferences;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Class for manipulation with User.
 */
public class EditUser implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditUser.class);

    public static final String PARAM_USER = ViewUser.PARAM_USER;
    public static final String PARAM_USER_SHORT = ViewUser.PARAM_USER_SHORT;
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_PASSWORD2 = "password2";
    public static final String PARAM_NICK = "nick";
    public static final String PARAM_SEX = "sex";
    public static final String PARAM_CITY = "city";
    public static final String PARAM_AREA = "area";
    public static final String PARAM_COUNTRY = "country";
    public static final String PARAM_BIRTH_YEAR = "birth";
    public static final String PARAM_HOME_PAGE = "www";
    public static final String PARAM_LINUX_USER_FROM = "linuxFrom";
    public static final String PARAM_DISTRIBUTION = "distribution";
    public static final String PARAM_ABOUT_ME = "about";
    public static final String PARAM_EMOTICONS = "emoticons";
    public static final String PARAM_SIGNATURES = "signatures";
    public static final String PARAM_AVATARS = "avatars";
    public static final String PARAM_DISPLAY_BANNED_STORIES = "bannedStories";
    public static final String PARAM_SIGNATURE = "signature";
    public static final String PARAM_COOKIE_VALIDITY = "cookieValid";
    public static final String PARAM_DISCUSSIONS_COUNT = "discussions";
    public static final String PARAM_SCREENSHOTS_COUNT = "screenshots";
    public static final String PARAM_NEWS_COUNT = "news";
    public static final String PARAM_STORIES_COUNT = "stories";
    public static final String PARAM_FOUND_PAGE_SIZE = "search";
    public static final String PARAM_FORUM_PAGE_SIZE = "forum";
    public static final String PARAM_BLACKLIST_USER = "bUid";
    public static final String PARAM_BLACKLIST_NAME = "bName";
    public static final String PARAM_SUBSCRIBE_MONTHLY = "monthly";
    public static final String PARAM_SUBSCRIBE_WEEKLY = "weekly";
    public static final String PARAM_SUBSCRIBE_FORUM = "forum";
    public static final String PARAM_PHOTO = "photo";
    public static final String PARAM_AVATAR = "avatar";
    public static final String PARAM_REMOVE_AVATAR = "remove_avatar";
    public static final String PARAM_REMOVE_PHOTO = "remove_photo";
    public static final String PARAM_RETURN_TO_FORUM = "moveback";
    public static final String PARAM_USER_ROLES = "roles";
    public static final String PARAM_USERS = "users";
    public static final String PARAM_URL_CSS = "css";
    public static final String PARAM_GUIDEPOST = "guidepost";
    public static final String PARAM_FEED = "feed";
    public static final String PARAM_TEMPLATE_FEED_SIZE = "feedSize";
    public static final String PARAM_INDEX_FEED_SIZE = "indexFeedSize";
    public static final String PARAM_URL = "url";
    public static final String PARAM_RID = "rid";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_UID1 = "uid1";
    public static final String PARAM_UID2 = "uid2";

    public static final String VAR_MANAGED = "MANAGED";
    public static final String VAR_DEFAULT_DISCUSSION_COUNT = "DEFAULT_DISCUSSIONS";
    public static final String VAR_DEFAULT_SCREENSHOT_COUNT = "DEFAULT_SCREENSHOTS";
    public static final String VAR_DEFAULT_NEWS_COUNT = "DEFAULT_NEWS";
    public static final String VAR_DEFAULT_STORIES_COUNT = "DEFAULT_STORIES";
    public static final String VAR_DEFAULT_FEED_LINKS_COUNT = "DEFAULT_LINKS";
    public static final String VAR_DEFAULT_TEMPLATE_FEED_LINKS_COUNT = "DEFAULT_TEMPLATE_LINKS";
    public static final String VAR_USERS = "USERS";
    public static final String VAR_SERVERS = "SERVERS";
    public static final String VAR_BOOKMARKS = "BOOKMARKS";
    public static final String VAR_USER1 = "USER1";
    public static final String VAR_USER2 = "USER2";

    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_REGISTER_STEP2 = "register2";
    public static final String ACTION_EDIT_BASIC = "editBasic";
    public static final String ACTION_EDIT_BASIC_STEP2 = "editBasic2";
    public static final String ACTION_CHANGE_PASSWORD = "changePassword";
    public static final String ACTION_CHANGE_PASSWORD_STEP2 = "changePassword2";
    public static final String ACTION_EDIT_PERSONAL = "editPersonal";
    public static final String ACTION_EDIT_PERSONAL_STEP2 = "editPersonal2";
    public static final String ACTION_EDIT_PROFILE = "editProfile";
    public static final String ACTION_EDIT_PROFILE_STEP2 = "editProfile2";
    public static final String ACTION_UPLOAD_PHOTO = "uploadPhoto";
    public static final String ACTION_UPLOAD_PHOTO_STEP2 = "uploadPhoto2";
    public static final String ACTION_UPLOAD_AVATAR = "uploadAvatar";
    public static final String ACTION_UPLOAD_AVATAR_STEP2 = "uploadAvatar2";
    public static final String ACTION_EDIT_SETTINGS = "editSettings";
    public static final String ACTION_EDIT_SETTINGS_STEP2 = "editSettings2";
    public static final String ACTION_EDIT_BLACKLIST = "editBlacklist";
    public static final String ACTION_REMOVE_FROM_BLACKLIST = "fromBlacklist";
    public static final String ACTION_ADD_TO_BLACKLIST = "toBlacklist";
    public static final String ACTION_EDIT_BOOKMARKS = "editBookmarks";
    public static final String ACTION_ADD_TO_BOOKMARKS = "toBookmarks";
    public static final String ACTION_REMOVE_FROM_BOOKMARKS = "fromBookmarks";
    public static final String ACTION_EDIT_SUBSCRIPTION = "subscribe";
    public static final String ACTION_EDIT_SUBSCRIPTION_STEP2 = "subscribe2";
    public static final String ACTION_GRANT_ROLES = "grant";
    public static final String ACTION_GRANT_ROLES_STEP2 = "grant2";
    public static final String ACTION_GRANT_ROLES_STEP3 = "grant3";
    public static final String ACTION_INVALIDATE_EMAIL = "invalidateEmail";
    public static final String ACTION_INVALIDATE_EMAIL2 = "invalidateEmail2";
    public static final String ACTION_ADD_GROUP_MEMBER = "addToGroup";
    public static final String ACTION_REMOVE_MERGE = "removeMerge";
    public static final String ACTION_REMOVE_MERGE_STEP2 = "removeMerge2";
    public static final String ACTION_REMOVE_MERGE_STEP3 = "removeMerge3";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        User managed = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);
        User user = (User) env.get(Constants.VAR_USER);
        if (managed == null)
            managed = user;
        else
            managed = (User) PersistenceFactory.getPersistence().findById(managed);
        env.put(VAR_MANAGED, managed);

        // registration doesn't require user to be logged in
        if (  action==null || action.equals(ACTION_REGISTER) )
            return FMTemplateSelector.select("EditUser","register",env,request);
        else if ( action.equals(ACTION_REGISTER_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, false, true, true, false);
            return actionAddStep2(request,response,env);
        }

        // all other actions require user to be logged in and to have rights for this action
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( ! (user.getId()==managed.getId() || user.hasRole(Roles.USER_ADMIN)) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_EDIT_BASIC) )
            return actionEditBasic(request, env);

        if ( action.equals(ACTION_EDIT_BASIC_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionEditBasic2(request, response, env);
        }

        if ( action.equals(ACTION_CHANGE_PASSWORD) )
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        if ( action.equals(ACTION_CHANGE_PASSWORD_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionPassword2(request, response, env);
        }

        if ( action.equals(ACTION_EDIT_PERSONAL) )
            return actionEditPersonal(request, env);

        if ( action.equals(ACTION_EDIT_PERSONAL_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionEditPersonal2(request, response, env);
        }

        if ( action.equals(ACTION_EDIT_PROFILE) )
            return actionEditProfile(request, env);

        if ( action.equals(ACTION_EDIT_PROFILE_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionEditProfile2(request, response, env);
        }

        if ( action.equals(ACTION_EDIT_SETTINGS) )
            return actionEditSettings(request, env);

        if ( action.equals(ACTION_EDIT_SETTINGS_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionEditSettings2(request, response, env);
        }

        if ( action.equals(ACTION_EDIT_BLACKLIST) )
            return actionEditBlacklist(request, env);

        if ( action.equals(ACTION_ADD_TO_BLACKLIST) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, false, false, true);
            return actionAddToBlacklist(request, response, env);
        }

        if ( action.equals(ACTION_REMOVE_FROM_BLACKLIST) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, false, false, true);
            return actionRemoveFromBlacklist(request, response, env);
        }

        if ( action.equals(ACTION_EDIT_BOOKMARKS) )
            return actionEditBookmarks(request, env);

        if ( action.equals(ACTION_ADD_TO_BOOKMARKS) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, false, false, true);
            return actionAddToBookmarks(request, response, env);
        }

        if ( action.equals(ACTION_REMOVE_FROM_BOOKMARKS) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, false, false, true);
            return actionRemoveFromBookmarks(request, response, env);
        }

        if ( action.equals(ACTION_EDIT_SUBSCRIPTION) )
            return actionEditSubscription(request, env);

        if ( action.equals(ACTION_EDIT_SUBSCRIPTION_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionEditSubscription2(request, response, env);
        }

        if ( action.equals(ACTION_UPLOAD_PHOTO) )
            return FMTemplateSelector.select("EditUser", "uploadPhoto", env, request);

        if ( action.equals(ACTION_UPLOAD_PHOTO_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionUploadPhoto2(request, response, env);
        }

        if ( action.equals(ACTION_UPLOAD_AVATAR) )
            return FMTemplateSelector.select("EditUser", "uploadAvatar", env, request);

        if ( action.equals(ACTION_UPLOAD_AVATAR_STEP2) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionUploadAvatar2(request, response, env);
        }

        // these actions are restricted to admin only

        if ( user.hasRole(Roles.USER_ADMIN) || user.hasRole(Roles.CAN_INVALIDATE_EMAILS) ) {
            if ( action.equals(ACTION_INVALIDATE_EMAIL) )
                return FMTemplateSelector.select("EditUser", "invalidateEmail", env, request);

            if ( action.equals(ACTION_INVALIDATE_EMAIL2) ) {
                ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
                return actionInvalidateEmail(request, response, env);
            }
        }

        if ( ! user.hasRole(Roles.USER_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_GRANT_ROLES) )
            return actionGrant(request, env);

        if ( action.equals(ACTION_GRANT_ROLES_STEP2) )
            return actionGrant2(request, env);

        if ( action.equals(ACTION_GRANT_ROLES_STEP3) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionGrant3(request, response, env);
        }

        if ( action.equals(ACTION_ADD_GROUP_MEMBER) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, false, false, true);
            return actionAddToGroup(request, response, env);
        }

        if ( action.equals(ACTION_REMOVE_MERGE) ) {
            return FMTemplateSelector.select("EditUser", "removeMerge", env, request);
        }

        if ( action.equals(ACTION_REMOVE_MERGE_STEP2) ) {
            return actionRemoveMerge2(request, env);
        }

        if ( action.equals(ACTION_REMOVE_MERGE_STEP3) ) {
            ActionProtector.ensureContract(request, EditUser.class, true, true, true, false);
            return actionRemoveMerge3(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Creates new user.
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = new User();
        Persistence persistence = PersistenceFactory.getPersistence();

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document, "/data/settings");
        Element system = DocumentHelper.makeElement(document, "/data/system");
        String date;
        synchronized (Constants.isoFormat) {
            date = Constants.isoFormat.format(new Date());
        }
        system.addElement("registration_date").setText(date);
        Element email = DocumentHelper.makeElement(document, "/data/communication/email");
        email.addAttribute("valid", "yes");
        managed.setData(document);

        boolean canContinue = true;
        canContinue &= setLogin(params, managed, env);
        canContinue &= setPassword(params, managed, env);
        canContinue &= setName(params, managed, env);
        canContinue &= setNick(params, managed, env);
        canContinue &= setEmail(params, managed, env);
        canContinue &= setSex(params, managed, env);
        canContinue &= setWeeklySummary(params, managed);
        canContinue &= setMonthlySummary(params, managed);
        managed.addProperty(Constants.PROPERTY_TICKET, generateTicket(managed.getId()));

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "register", env, request);

        try {
            persistence.create(managed);
        } catch (DuplicateKeyException e) {
            ServletUtils.addError(PARAM_LOGIN, "Přihlašovací jméno nebo přezdívka jsou již používány.", env, null);
            return FMTemplateSelector.select("EditUser", "register", env, request);
        }

        HttpSession session = request.getSession();
        session.setAttribute(Constants.VAR_USER, managed);

        Map data = new HashMap();
        data.put(Constants.VAR_USER, managed);
        data.put(EmailSender.KEY_FROM, "admin@abclinuxu.cz");
        data.put(EmailSender.KEY_TO, managed.getEmail());
        data.put(EmailSender.KEY_RECEPIENT_UID, Integer.toString(managed.getId()));
        data.put(EmailSender.KEY_SUBJECT, "Privitani na portalu www.abclinuxu.cz");
        data.put(EmailSender.KEY_TEMPLATE, "/mail/registrace.ftl");
        EmailSender.sendEmail(data);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?registrace=true&action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of basic information about account.
     */
    protected String actionEditBasic(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        params.put(PARAM_LOGIN,managed.getLogin());
        params.put(PARAM_NAME,managed.getName());
        params.put(PARAM_NICK,managed.getNick());
        params.put(PARAM_EMAIL,managed.getEmail());

        return FMTemplateSelector.select("EditUser","editBasic",env,request);
    }

    /**
     * Updates basic information about account.
     */
    protected String actionEditBasic2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "editBasic", env, request);

        canContinue &= setLogin(params, managed, env);
        canContinue &= setName(params, managed, env);
        canContinue &= setNick(params, managed, env);
        canContinue &= setEmail(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editBasic", env, request);

        try {
            persistence.update(managed);
        } catch ( DuplicateKeyException e ) {
            ServletUtils.addError(PARAM_LOGIN, "Login nebo přezdívka je již používána", env, null);
            return FMTemplateSelector.select("EditUser","editBasic",env,request);
        }

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny byly uloženy.",env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
        return null;
    }

    /**
     * Changes password of the user.
     */
    protected String actionPassword2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        canContinue &= setPassword(params,managed,env);

        if ( ! canContinue )
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        persistence.update(managed);

        Cookie cookie = ServletUtils.getCookie(request, Constants.VAR_USER);
        if ( cookie!=null )
            ServletUtils.deleteCookie(cookie, response);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Heslo bylo změněno.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of basic information about account.
     */
    protected String actionEditPersonal(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/personal/sex");
        if ( node!=null )
            params.put(PARAM_SEX, node.getText());
        node = document.selectSingleNode("/data/personal/birth_year");
        if ( node!=null )
            params.put(PARAM_BIRTH_YEAR, node.getText());
        node = document.selectSingleNode("/data/personal/city");
        if ( node!=null )
            params.put(PARAM_CITY, node.getText());
        node = document.selectSingleNode("/data/personal/area");
        if ( node!=null )
            params.put(PARAM_AREA, node.getText());
        node = document.selectSingleNode("/data/personal/country");
        if ( node!=null )
            params.put(PARAM_COUNTRY, node.getText());

        return FMTemplateSelector.select("EditUser", "editPersonal", env, request);
    }

    /**
     * Updates basic information about account.
     */
    protected String actionEditPersonal2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "editPersonal", env, request);

        canContinue &= setSex(params, managed, env);
        canContinue &= setBirthYear(params, managed, env);
        canContinue &= setCity(params, managed, env);
        canContinue &= setArea(params, managed, env);
        canContinue &= setCountry(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editPersonal", env, request);

        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny byly uloženy.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of basic information about account.
     */
    protected String actionEditProfile(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/profile/home_page");
        if ( node!=null )
            params.put(PARAM_HOME_PAGE, node.getText());
        node = document.selectSingleNode("/data/profile/linux_user_from_year");
        if ( node!=null )
            params.put(PARAM_LINUX_USER_FROM, node.getText());
        node = document.selectSingleNode("/data/personal/signature");
        if (node != null)
            params.put(PARAM_SIGNATURE, node.getText());
        node = document.selectSingleNode("/data/profile/about_myself");
        if ( node!=null )
            params.put(PARAM_ABOUT_ME, node.getText());
        List nodes = Tools.xpaths(document,"/data/profile/distributions/distribution");
        if ( nodes.size()>0 )
            params.put(PARAM_DISTRIBUTION, nodes);

        return FMTemplateSelector.select("EditUser", "editProfile", env, request);
    }

    /**
     * Updates basic information about account.
     */
    protected String actionEditProfile2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "editProfile", env, request);

        canContinue &= setMyPage(params, managed, env);
        canContinue &= setLinuxUserFrom(params, managed, env);
        canContinue &= setSignature(params, managed, env);
        canContinue &= setAbout(params, managed, env);
        canContinue &= setDistributions(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editProfile", env, request);

        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny byly uloženy.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of account's settings.
     */
    protected String actionEditSettings(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        setDefaultValuesForEditSettings(env);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/settings/emoticons");
        if ( node!=null )
            params.put(PARAM_EMOTICONS, node.getText());

        node = document.selectSingleNode("/data/settings/signatures");
        if ( node!=null )
            params.put(PARAM_SIGNATURES, node.getText());

        node = document.selectSingleNode("/data/settings/avatars");
        if ( node!=null )
            params.put(PARAM_AVATARS, node.getText());
        node = document.selectSingleNode("/data/settings/hp_all_stories");
        if ( node!=null )
            params.put(PARAM_DISPLAY_BANNED_STORIES, node.getText());

        node = document.selectSingleNode("/data/settings/guidepost");
        if ( node!=null )
            params.put(PARAM_GUIDEPOST, node.getText());

        node = document.selectSingleNode("/data/settings/css");
        if ( node!=null )
            params.put(PARAM_URL_CSS, node.getText());

        node = document.selectSingleNode("/data/settings/cookie_valid");
        if ( node!=null )
            params.put(PARAM_COOKIE_VALIDITY, node.getText());

        node = document.selectSingleNode("/data/settings/index_discussions");
        if ( node!=null )
            params.put(PARAM_DISCUSSIONS_COUNT, node.getText());

        node = document.selectSingleNode("/data/settings/index_screenshots");
        if ( node!=null )
            params.put(PARAM_SCREENSHOTS_COUNT, node.getText());

        node = document.selectSingleNode("/data/settings/index_news");
        if ( node!=null )
            params.put(PARAM_NEWS_COUNT, node.getText());

        node = document.selectSingleNode("/data/settings/index_stories");
        if ( node!=null )
            params.put(PARAM_STORIES_COUNT, node.getText());

        node = document.selectSingleNode("/data/settings/found_size");
        if ( node!=null )
            params.put(PARAM_FOUND_PAGE_SIZE, node.getText());

        node = document.selectSingleNode("/data/settings/forum_size");
        if ( node!=null )
            params.put(PARAM_FORUM_PAGE_SIZE, node.getText());

        node = document.selectSingleNode("/data/settings/template_links");
        if ( node!=null )
            params.put(PARAM_TEMPLATE_FEED_SIZE, node.getText());

        node = document.selectSingleNode("/data/settings/index_links");
        if ( node!=null )
            params.put(PARAM_INDEX_FEED_SIZE, node.getText());

        node = document.selectSingleNode("/data/settings/return_to_forum");
        if ( node!=null )
            params.put(PARAM_RETURN_TO_FORUM, node.getText());

        List maintainedServers = UpdateLinks.getMaintainedServers();
        Element element = (Element) document.selectSingleNode("/data/settings/feeds");
        if (element != null) {
            StringTokenizer stk = new StringTokenizer(element.getText(), ",");
            while (stk.hasMoreTokens()) {
                String tmp = stk.nextToken();
                Integer id = new Integer(tmp);
                if (maintainedServers.indexOf(id) != -1)
                    params.put(PARAM_FEED + id, Boolean.TRUE);
            }
        }

        return FMTemplateSelector.select("EditUser", "editSettings", env, request);
    }

    /**
     * Updates account's settings.
     */
    protected String actionEditSettings2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue) {
            setDefaultValuesForEditSettings(env);
            return FMTemplateSelector.select("EditUser", "editSettings", env, request);
        }

        canContinue &= setCssUrl(params, managed);
        canContinue &= setCookieValidity(params, managed);
        canContinue &= setEmoticons(params, managed);
        canContinue &= setSignatures(params, managed);
        canContinue &= setBannedStories(params, managed);
        canContinue &= setAvatars(params, managed);
        canContinue &= setGuidepost(params, managed);
        canContinue &= setDiscussionsSizeLimit(params, managed, env);
        canContinue &= setScreenshotsSizeLimit(params, managed, env);
        canContinue &= setNewsSizeLimit(params, managed, env);
        canContinue &= setStoriesSizeLimit(params, managed, env);
        canContinue &= setFoundPageSize(params, managed, env);
        canContinue &= setForumPageSize(params, managed, env);
        canContinue &= setReturnBackToForum(params, managed);
        canContinue &= setFeeds(params, managed);
        canContinue &= setFeedSize(params, managed, env);

        if ( !canContinue ) {
            setDefaultValuesForEditSettings(env);
            return FMTemplateSelector.select("EditUser", "editSettings", env, request);
        }

        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny byly uloženy.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    private void setDefaultValuesForEditSettings(Map env) {
        List maintainedServers = UpdateLinks.getMaintainedServers();
        List servers = new ArrayList(maintainedServers.size());
        Persistence persistence = PersistenceFactory.getPersistence();
        for (Iterator iter = maintainedServers.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            Server server = (Server) persistence.findById(new Server(id));
            servers.add(server);
        }
        env.put(VAR_SERVERS, servers);

        Map defaultSizes = VariableFetcher.getInstance().getDefaultSizes();
        env.put(VAR_DEFAULT_DISCUSSION_COUNT, defaultSizes.get(VariableFetcher.KEY_QUESTION));
        env.put(VAR_DEFAULT_SCREENSHOT_COUNT, defaultSizes.get(VariableFetcher.KEY_SCREENSHOT));
        env.put(VAR_DEFAULT_NEWS_COUNT, defaultSizes.get(VariableFetcher.KEY_NEWS));
        env.put(VAR_DEFAULT_STORIES_COUNT, defaultSizes.get(VariableFetcher.KEY_STORY));
        env.put(VAR_DEFAULT_FEED_LINKS_COUNT, defaultSizes.get(VariableFetcher.KEY_TEMPLATE_LINKS));
        env.put(VAR_DEFAULT_TEMPLATE_FEED_LINKS_COUNT, defaultSizes.get(VariableFetcher.KEY_INDEX_LINKS));
    }

    protected String actionEditBlacklist(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("EditUser", "editBlacklist", env, request);
    }

    /**
     * Adds selected user to the blacklist.
     */
    protected String actionAddToBlacklist(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

	    String url = (String) params.get(PARAM_URL);
        if ( url==null || url.length()==0)
            url = "/EditUser/"+managed.getId()+"?action="+ACTION_EDIT_BLACKLIST;

    	boolean canContinue = addToBlacklist(params, managed, env);
        if ( !canContinue ) {
            urlUtils.redirect(response, url);
            return null;
        }

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId())
            sessionUser.synchronizeWith(managed);

    	ServletUtils.addMessage("Uživatel byl přidán na seznam blokovaných uživatelů.", env, request.getSession());
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Removes selected user from the blacklist.
     */
    protected String actionRemoveFromBlacklist(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

    	String url = (String) params.get(PARAM_URL);
        if (url == null || url.length() == 0)
            url = "/EditUser/" + managed.getId() + "?action=" + ACTION_EDIT_BLACKLIST;

        boolean canContinue = removeFromBlacklist(params, managed, env);
        if ( !canContinue ) {
            urlUtils.redirect(response, url);
            return null;
        }

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId())
            sessionUser.synchronizeWith(managed);

        ServletUtils.addMessage("Uživatel byl odstraněn ze seznamu blokovaných uživatelů.", env, request.getSession());
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionEditBookmarks(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(VAR_MANAGED);

        List bookmarks = new ArrayList();
        List elements = user.getData().selectNodes("/data/links/link");
        String title, prefix, type;
        Relation relation;
        Bookmark bookmark;
        Element element;
        if (elements.size() != 0) {
            List relations = new ArrayList(elements.size());
            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                element = (Element) iter.next();
                relation = new Relation(Integer.parseInt(element.elementText("rid")));
                title = element.elementText("title");
                prefix = element.elementText("prefix");
                type = element.elementText("type");
                bookmark = new Bookmark(relation, title, prefix, type);
                relations.add(relation);
                bookmarks.add(bookmark);
            }
            persistence.synchronizeList(relations, true);

            // now update titles for all valid items
            for (Iterator iter = bookmarks.iterator(); iter.hasNext();) {
                Bookmark b = (Bookmark) iter.next();
                Relation r = b.getRelation();
                if (r.isInitialized())
                    b.setTitle(Tools.childName(r));
            }
            Collections.sort(bookmarks);
        }
        env.put(VAR_BOOKMARKS, bookmarks);

        return FMTemplateSelector.select("EditUser", "editBookmarks", env, request);
    }

    /**
     * Removes selected item from bookmarks.
     */
    protected String actionRemoveFromBookmarks(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
    	String url = "/EditUser/" + managed.getId() + "?action=" + ACTION_EDIT_BOOKMARKS;

        boolean canContinue = removeFromBookmarks(params, managed, env);
        if ( ! canContinue ) {
            urlUtils.redirect(response, url);
            return null;
        }

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId())
            sessionUser.synchronizeWith(managed);

        ServletUtils.addMessage("Vybrané stránky byly odstraněny ze záložek.", env, request.getSession());
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Adds an object to bookmarks.
     */
    protected String actionAddToBookmarks(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        String url = (String) params.get(PARAM_URL);
        if ( url == null || url.length() == 0)
            url = "/EditUser/" + managed.getId() + "?action=" + ACTION_EDIT_BOOKMARKS;

        boolean canContinue = addToBookmarks(request, params, managed, env);
        if ( ! canContinue ) {
            urlUtils.redirect(response, url);
            return null;
        }

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId())
            sessionUser.synchronizeWith(managed);

        ServletUtils.addMessage("Stránka byla přidána do vašich záložek.", env, request.getSession());
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Shows form for editing of account's subscription.
     */
    protected String actionEditSubscription(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/communication/email/weekly_summary");
        if ( node!=null )
            params.put(PARAM_SUBSCRIBE_WEEKLY, node.getText());
        node = document.selectSingleNode("/data/communication/email/newsletter");
        if ( node!=null )
            params.put(PARAM_SUBSCRIBE_MONTHLY, node.getText());
        node = document.selectSingleNode("/data/communication/email/forum");
        if ( node!=null )
            params.put(PARAM_SUBSCRIBE_FORUM, node.getText());

        return FMTemplateSelector.select("EditUser", "editSubscription", env, request);
    }

    /**
     * Updates account's subscription.
     */
    protected String actionEditSubscription2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "editSubscription", env, request);

        if (canContinue) {
            canContinue &= setWeeklySummary(params, managed);
            canContinue &= setMonthlySummary(params, managed);
            canContinue &= setForumByEmail(params, managed);
        }

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editSubscription", env, request);

        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny byly uloženy.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    /**
     * Uploads photo.
     */
    protected String actionUploadPhoto2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( ! user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "uploadPhoto", env, request);

        canContinue &= setPhoto(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "uploadPhoto", env, request);

        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny byly uloženy.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
        return null;
    }

    /**
     * Uploads photo.
     */
    protected String actionUploadAvatar2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        if ( ! user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if ( ! canContinue)
            return FMTemplateSelector.select("EditUser", "uploadAvatar", env, request);

        canContinue &= setAvatar(params, managed, env);

        if ( ! canContinue )
            return FMTemplateSelector.select("EditUser", "uploadAvatar", env, request);

        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny byly uloženy.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
        return null;
    }

    /**
     * Remove/merge accounts, step 2: safety checks.
     */
    protected String actionRemoveMerge2(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user1 = null, user2 = null;

        int uid1 = Misc.parseInt((String) params.get(PARAM_UID1), 0);
        int uid2 = Misc.parseInt((String) params.get(PARAM_UID2), 0);

        if (uid1 == 0) {
            ServletUtils.addError(PARAM_UID1, "Vyplňte UID!", env, null);
            return FMTemplateSelector.select("EditUser", "removeMerge", env, request);
        }

        if (uid1 == uid2) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "UID jsou shodná!", env, null);
            return FMTemplateSelector.select("EditUser", "removeMerge", env, request);
        }

        try {
            user1 = Tools.createUser(uid1);
            if (uid2 != 0)
                user2 = Tools.createUser(uid2);
        } catch (Exception e) {
            if (user1 == null) {
                ServletUtils.addError(PARAM_UID1, "První UID je neplatné!", env, null);
                return FMTemplateSelector.select("EditUser", "removeMerge", env, request);
            } else {
                ServletUtils.addError(PARAM_UID2, "Druhé UID je neplatné!", env, null);
                return FMTemplateSelector.select("EditUser", "removeMerge", env, request);
            }
        }

        if (user2 == null) {
            // verify that the user account is unused
            boolean unused = true;
            SQLTool sqlTool = SQLTool.getInstance();

            Set<String> property = Collections.singleton(Integer.toString(user1.getId()));
            Map<String, Set<String>> filters = Collections.singletonMap(Constants.PROPERTY_USER, property);
            List<Relation> authors = sqlTool.findItemRelationsWithTypeWithFilters(Item.AUTHOR, null, filters);

            unused &= authors.isEmpty();
            unused &= sqlTool.countNewsRelationsByUser(user1.getId()) == 0;
            unused &= sqlTool.countQuestionRelationsByUser(user1.getId()) == 0;
            unused &= sqlTool.countCommentRelationsByUser(user1.getId()) == 0;
            unused &= sqlTool.countWikiRelationsByUser(user1.getId()) == 0;
            unused &= sqlTool.countPropertiesByUser(user1.getId()) == 0;

            if ( ! unused) {
                ServletUtils.addError(PARAM_UID1, "Tento uživatel má záznamy v systému! Musíte je nejdříve smazat.", env, null);
                return FMTemplateSelector.select("EditUser", "removeMerge", env, request);
            }
        }

        env.put(VAR_USER1, user1);
        env.put(VAR_USER2, user2);

        return FMTemplateSelector.select("EditUser", "removeMerge2", env, request);
    }

    /**
     * Remove/merge accounts, step 3: execute the operation.
     */
    protected String actionRemoveMerge3(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        SQLTool sqlTool = SQLTool.getInstance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        int uid1 = Misc.parseInt((String) params.get(PARAM_UID1), 0);
        int uid2 = Misc.parseInt((String) params.get(PARAM_UID2), 0);
        User user1 = Tools.createUser(uid1);

        if (uid2 != 0)
            sqlTool.mergeUsers(uid1, uid2);
        sqlTool.deleteUser(uid1);
        PersistenceFactory.getPersistence().clearCache();

        if (uid2 != 0) {
            ServletUtils.addMessage("Uživatelé byli sloučeni.", env, request.getSession());
            AdminLogger.logEvent(user, "sloucil uzivatele " + uid1 + "(" + user1.getEmail() + ") s uzivatelem " + uid2);
        } else {
            ServletUtils.addMessage("Uživatel byl odstraněn.", env, request.getSession());;
            AdminLogger.logEvent(user, "smazal uzivatele " + uid1 + "(" + user1.getEmail() + ")");
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Admin");

        return null;
    }

    /**
     * Shows form for granting roles to the user.
     */
    protected String actionGrant(HttpServletRequest request, Map env) throws Exception {
        env.put(VAR_USERS,SQLTool.getInstance().findUsersWithRoles(null));
        return FMTemplateSelector.select("EditUser", "grantSelect", env, request);
    }

    /**
     * Shows form for granting roles to the user.
     */
    protected String actionGrant2(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        List nodes = document.selectNodes("/data/roles/role");
        if ( nodes!=null && nodes.size()>0 ) {
            List roles = new ArrayList(nodes.size());
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Node node = (Node) iter.next();
                roles.add(node.getText());
            }
            params.put(PARAM_USER_ROLES,roles);
        }

        return FMTemplateSelector.select("EditUser", "grantRoles", env, request);
    }

    /**
     * Manages roles of the user.
     */
    protected String actionGrant3(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        boolean canContinue = true;
        canContinue &= checkPassword(params, user, env);
        canContinue &= setUserRoles(params, managed);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "grantRoles", env, request);

        persistence.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Změny rolí uživatele "+managed.getName()+" byly uloženy.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    /**
     * Invalidates emails of set of users.
     */
    protected String actionInvalidateEmail(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();
        StringBuffer sb = new StringBuffer();
        int count = 0;

        String tmp = (String) params.get(PARAM_USER_SHORT);
        if (tmp==null)
            tmp = (String) params.get(PARAM_USERS);
        if ( tmp==null ) tmp = "";
        StringTokenizer stk = new StringTokenizer(tmp,"\r\n");
        while (stk.hasMoreTokens()) {
            int id = Misc.parseInt(stk.nextToken(),0);
            if (id==0)
                continue;

            User managed = null;
            try {
                managed = (User) persistence.findById(new User(id));
                Element tagEmail = DocumentHelper.makeElement(managed.getData(), "/data/communication/email");
                tagEmail.attribute("valid").setText("no");
                persistence.update(managed);
                AdminLogger.logEvent(user, "zneplatnil email uzivateli "+managed.getName()+" - "+managed.getId());
                count++;
            } catch (Exception e) {
                sb.append("Uživatel ").append(id).append(" nebyl nalezen!<br>");
            }
        }

        if (sb.length() > 0)
            ServletUtils.addError(Constants.ERROR_GENERIC, sb.toString(), env, request.getSession());
        ServletUtils.addMessage(count+" uživatelům byl zneplatněn email.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Admin");
        return null;
    }

    /**
     * Adds selected user to given group.
     */
    protected String actionAddToGroup(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        Persistence persistence = PersistenceFactory.getPersistence();

        int group = Misc.parseInt((String) params.get(EditGroup.PARAM_GROUP), 0);
        if (group==0)
            return ServletUtils.showErrorPage("Chybí číslo skupiny!",env,request);

        Element system = (Element) managed.getData().selectSingleNode("/data/system");
        system.addElement("group").setText(Integer.toString(group));
        persistence.update(managed);

        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user,"vlozil uzivatele "+managed.getId()+" do skupiny "+group);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Group?action=members&gid="+group);
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    //                          Setters                                      //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Executes given guard.
     * @param guard class of the guard. It must implement static method check(String)
     * @param text text to be checked
     * @param paramName error message will be associated with this parameter
     * @return true if everything is ok, false if guard throws exception
     */
    private boolean verifyGuard(Class guard, String text, String paramName, Map env) {
        try {
            Method method = guard.getMethod("check", new Class[] {String.class});
            method.invoke(null, new Object[] {text});
        } catch(InvocationTargetException e) {
            Throwable e1 = e.getCause();
            if (e1 instanceof ParserException) {
                log.error("ParseException on '" + text + "'", e);
                ServletUtils.addError(paramName, e.getMessage(), env, null);
                return false;
            } else {
                ServletUtils.addError(paramName, e1.getMessage(), env, null);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to run HTML guard "+guard.getName(), e);
        }
        return true;
    }

    /**
     * Checks password from parameters.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if password is missing or is not correct.
     */
    private boolean checkPassword(Map params, User user, Map env) {
        String password = (String) params.get(ServletUtils.PARAM_LOG_PASSWORD);

        if ( password==null || password.length()==0 ) {
            ServletUtils.addError(ServletUtils.PARAM_LOG_PASSWORD, "Zadejte heslo!", env, null);
            return false;
        }
        if ( ! user.validatePassword(password) ) {
            ServletUtils.addError(ServletUtils.PARAM_LOG_PASSWORD, "Nesprávné heslo!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates password from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPassword(Map params, User user, Map env) {
        String password = (String) params.get(PARAM_PASSWORD);
        String password2 = (String) params.get(PARAM_PASSWORD2);

        if ( password==null || password.length()<4 ) {
            ServletUtils.addError(PARAM_PASSWORD, "Heslo je příliš krátké!", env, null);
            return false;
        }
        if ( !(password.equals(password2)) ) {
            ServletUtils.addError(PARAM_PASSWORD, "Hesla se liší!", env, null);
            return false;
        }
        user.setPassword(password);
        return true;
    }

    /**
     * Updates login from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setLogin(Map params, User user, Map env) {
        String login = (String) params.get(PARAM_LOGIN);
        if ( login==null || login.length()<3 ) {
            ServletUtils.addError(PARAM_LOGIN, "Přihlašovací jméno musí mít nejméně tři znaky!", env, null);
            return false;
        }
        if ( login.length()>16 ) {
            ServletUtils.addError(PARAM_LOGIN, "Přihlašovací jméno nesmí mít více než 16 znaků!", env, null);
            return false;
        }
        Matcher matcher = LdapUserManager.reLoginInvalid.matcher(login);
        if ( matcher.find() ) {
            ServletUtils.addError(PARAM_LOGIN, "Přihlašovací jméno smí obsahovat pouze písmena A až Z, číslice, pomlčku, tečku a podtržítko!", env, null);
            return false;
        }

        List<Integer> users = SQLTool.getInstance().findUsersWithLogin(login, null);
        if (! (users.isEmpty() || users.contains(user.getId()))) {
            ServletUtils.addError(PARAM_LOGIN, "Toto přihlašovací jméno je již používáno. Zkuste jiné.", env, null);
            return false;
        }

        user.setLogin(login);
        return true;
    }

    /**
     * Updates name from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, User user, Map env) {
        String name = (String) params.get(PARAM_NAME);
        name = Misc.filterDangerousCharacters(name);
        if ( name==null || name.length()<4 ) {
            ServletUtils.addError(PARAM_NAME, "Jméno je příliš krátké!", env, null);
            return false;
        }

        if (! verifyGuard(NoHTMLGuard.class, name, PARAM_NAME, env))
            return false;

        user.setName(name);
        return true;
    }

    /**
     * Updates nick from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setNick(Map params, User user, Map env) {
        String nick = (String) params.get(PARAM_NICK);
        nick = Misc.filterDangerousCharacters(nick);
        if ( nick==null || nick.trim().length()==0) {
            user.setNick(null);
            return true;
        }

        nick = nick.trim();
        if (nick.length()>20 ) {
            ServletUtils.addError(PARAM_NICK, "Přezdívka je příliš dlouhá!", env, null);
            return false;
        }

        if (!verifyGuard(NoHTMLGuard.class, nick, PARAM_NICK, env))
            return false;

        List<Integer> users = SQLTool.getInstance().findUsersWithNick(nick, null);
        if (!(users.isEmpty() || users.contains(user.getId()))) {
            ServletUtils.addError(PARAM_NICK, "Tato přezdívka je již používána. Zkuste jinou.", env, null);
            return false;
        }

        user.setNick(nick);
        return true;
    }

    /**
     * Updates email from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setEmail(Map params, User user, Map env) {
        String email = (String) params.get(PARAM_EMAIL);
        if ( !isEmailValid(email) ) {
            ServletUtils.addError(PARAM_EMAIL, "Neplatný email!", env, null);
            return false;
        }
        user.setEmail(email);
        Element tagEmail = DocumentHelper.makeElement(user.getData(), "/data/communication/email");
        tagEmail.attribute("valid").setText("yes");
        return true;
    }

    /**
     * Validate email.
     * @return false, if email isnt right. Expected format is: yourname@yourdomain.com!
     */
    private boolean isEmailValid(String email) {
        if (email == null)
            return false;

        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }


    /**
     * Updates sex from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setSex(Map params, User user, Map env) {
        String sex = (String) params.get(PARAM_SEX);
        if ( !("man".equals(sex) || "woman".equals(sex)) ) {
            ServletUtils.addError(PARAM_SEX, "Zadejte své pohlaví!", env, null);
            return false;
        }
        Node node = DocumentHelper.makeElement(user.getData(),"/data/personal/sex");
        node.setText(sex);
        return true;
    }

    /**
     * Updates birth year from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setBirthYear(Map params, User user, Map env) {
        String year = (String) params.get(PARAM_BIRTH_YEAR);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( year==null || year.length()==0 ) {
            Node node = personal.element("birth_year");
            if (node!=null)
                personal.remove(node);
            return true;
        }
        int valid = Misc.parseInt(year, -1);
        if ( valid==-1 ) {
            ServletUtils.addError(PARAM_BIRTH_YEAR, "Zadejte platný rok!", env, null);
            return false;
        }
        DocumentHelper.makeElement(personal, "birth_year").setText(year);
        return true;
    }

    /**
     * Updates city from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCity(Map params, User user, Map env) {
        String city = (String) params.get(PARAM_CITY);
        city = Misc.filterDangerousCharacters(city);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( city==null || city.length()==0 ) {
            Node node = personal.element("city");
            if (node!=null)
                personal.remove(node);
            return true;
        }

        if (!verifyGuard(NoHTMLGuard.class, city, PARAM_CITY, env))
            return false;

        DocumentHelper.makeElement(personal, "city").setText(city);
        return true;
    }

    /**
     * Updates area from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setArea(Map params, User user, Map env) {
        String area = (String) params.get(PARAM_AREA);
        area = Misc.filterDangerousCharacters(area);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( area==null || area.length()==0 ) {
            Node node = personal.element("area");
            if (node!=null)
                personal.remove(node);
            return true;
        }

        if (!verifyGuard(NoHTMLGuard.class, area, PARAM_AREA, env))
            return false;

        DocumentHelper.makeElement(personal, "area").setText(area);
        return true;
    }

    /**
     * Updates country from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCountry(Map params, User user, Map env) {
        String country = (String) params.get(PARAM_COUNTRY);
        country = Misc.filterDangerousCharacters(country);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( country==null || country.length()==0 ) {
            Node node = personal.element("country");
            if (node!=null)
                personal.remove(node);
            return true;
        }

        if (!verifyGuard(NoHTMLGuard.class, country, PARAM_COUNTRY, env))
            return false;

        DocumentHelper.makeElement(personal, "country").setText(country);
        return true;
    }

    /**
     * Updates home page from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setMyPage(Map params, User user, Map env) {
        String page = (String) params.get(PARAM_HOME_PAGE);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        if ( page==null || page.length()==0 ) {
            Node node = profile.element("home_page");
            if (node!=null)
                profile.remove(node);
            return true;
        }

        if ( ! page.startsWith("http://") ) {
            ServletUtils.addError(PARAM_HOME_PAGE, "Neplatné URL!", env, null);
            return false;
        }

        if (!verifyGuard(NoHTMLGuard.class, page, PARAM_HOME_PAGE, env))
            return false;

        DocumentHelper.makeElement(profile, "home_page").setText(page);
        return true;
    }

    /**
     * Updates linuxUserFromYear from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setLinuxUserFrom(Map params, User user, Map env) {
        String s = (String) params.get(PARAM_LINUX_USER_FROM);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        if (s == null || s.trim().length() == 0) {
            Node node = profile.element("linux_user_from_year");
            if (node != null)
                node.detach();
            return true;
        }
        int year = Misc.parseInt(s, -1);
        if (year < 1991) {
            ServletUtils.addError(PARAM_LINUX_USER_FROM, "Očekáváno je číslo větší nebo rovno 1991.", env, null);
            return false;
        }
        DocumentHelper.makeElement(profile, "linux_user_from_year").setText(s);
        return true;
    }

    /**
     * Updates about from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setAbout(Map params, User user, Map env) {
        String about = (String) params.get(PARAM_ABOUT_ME);
        about = Misc.filterDangerousCharacters(about);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        if ( about==null || about.length()==0 ) {
            Node node = profile.element("about_myself");
            if (node!=null)
                profile.remove(node);
            return true;
        }

        if (!verifyGuard(ProfileGuard.class, about, PARAM_ABOUT_ME, env))
            return false;

        Element element = DocumentHelper.makeElement(profile, "about_myself");
        element.setText(about);
        Format format = FormatDetector.detect(about);
        element.addAttribute("format", Integer.toString(format.getId()));
        return true;
    }

    /**
     * Updates distributions from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setDistributions(Map params, User user, Map env) {
        List distros = Tools.asList(params.get(PARAM_DISTRIBUTION));
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        Node node = profile.element("distributions");
        if ( node!=null )
            profile.remove(node);
        if (distros==null || distros.size()==0)
            return true;
        Element distributions = DocumentHelper.makeElement(profile, "distributions");
        for ( Iterator iter = distros.iterator(); iter.hasNext(); ) {
            String distro = (String) iter.next();
            if (!verifyGuard(NoHTMLGuard.class, distro, PARAM_DISTRIBUTION, env))
                return false;

            Element element = DocumentHelper.createElement("distribution");
            element.setText(distro);
            distributions.add(element);
        }
        return true;
    }

    /**
     * Updates signature from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setSignature(Map params, User user, Map env) throws ParserException {
        String signature = (String) params.get(PARAM_SIGNATURE);
        signature = Misc.filterDangerousCharacters(signature);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( signature == null || signature.length() == 0 ) {
            Node node = personal.element("signature");
            if ( node!=null )
                personal.remove(node);
            return true;
        }

        if (contentSize(signature) > 120) {
            ServletUtils.addError(PARAM_SIGNATURE, "Maximální délka je 120 znaků!", env, null);
            return false;
        }

        if (!verifyGuard(SignatureHTMLGuard.class, signature, PARAM_SIGNATURE, env))
            return false;

        signature = Misc.addRelNofollowToLink(signature);
        DocumentHelper.makeElement(personal, "signature").setText(signature);
        return true;
    }

    /**
     * Updates URL with custom CSS from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user   user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCssUrl(Map params, User user) {
        String url = (String) params.get(PARAM_URL_CSS);
        if (url == null || url.length() == 0) {
            Node node = user.getData().selectSingleNode("/data/settings/css");
            if (node != null)
                node.detach();
        } else {
            Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/css");
            element.setText(url);
        }
        return true;
    }

    /**
     * Updates emoticons from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setEmoticons(Map params, User user) {
        String emoticons = (String) params.get(PARAM_EMOTICONS);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/emoticons");
        String value = ("yes".equals(emoticons))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Updates signatures from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setSignatures(Map params, User user) {
        String emoticons = (String) params.get(PARAM_SIGNATURES);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/signatures");
        String value = ("yes".equals(emoticons))? "yes":"no";
        element.setText(value);
        return true;
    }

     /**
     * Updates avatars from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setAvatars(Map params, User user) {
        String avatars = (String) params.get(PARAM_AVATARS);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/avatars");
        String value = ("yes".equals(avatars))? "yes":"no";
        element.setText(value);
        return true;
    }

    /** Updates banned blogs view settings from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setBannedStories(Map params, User user) {
        String showBanned = (String) params.get(PARAM_DISPLAY_BANNED_STORIES);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/hp_all_stories");
        String value = ("yes".equals(showBanned))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
    /**
     * Updates guidepost from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setGuidepost(Map params, User user) {
        String guidepost = (String) params.get(PARAM_GUIDEPOST);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/guidepost");
        String value = ("yes".equals(guidepost))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Updates login cookie validity from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCookieValidity(Map params, User user) {
        String validity = (String) params.get(PARAM_COOKIE_VALIDITY);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/cookie_valid");
        element.setText(validity);
        return true;
    }

    /**
     * Updates size limit of discussions on main page from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setDiscussionsSizeLimit(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = (Integer) maxSizes.get(VariableFetcher.KEY_QUESTION);
        return setLimitedSize(params, PARAM_DISCUSSIONS_COUNT, user.getData(), "/data/settings/index_discussions", 0, max, env);
    }

    /**
     * Updates size limit of screenshots on main page from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setScreenshotsSizeLimit(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = (Integer) maxSizes.get(VariableFetcher.KEY_SCREENSHOT);
        return setLimitedSize(params, PARAM_SCREENSHOTS_COUNT, user.getData(), "/data/settings/index_screenshots", 0, max, env);
    }

    /**
     * Updates size limit for news in the template from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setNewsSizeLimit(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = (Integer) maxSizes.get(VariableFetcher.KEY_NEWS);
        return setLimitedSize(params, PARAM_NEWS_COUNT, user.getData(), "/data/settings/index_news", 0, max, env);
    }

    /**
     * Updates size limit for stories on index page from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setStoriesSizeLimit(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = (Integer) maxSizes.get(VariableFetcher.KEY_STORY);
        return setLimitedSize(params, PARAM_STORIES_COUNT, user.getData(), "/data/settings/index_stories", 0, max, env);
    }

    /**
     * Updates page size for found objects from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setFoundPageSize(Map params, User user, Map env) {
        return setLimitedSize(params, PARAM_FOUND_PAGE_SIZE, user.getData(), "/data/settings/found_size", 10, 100, env);
    }

    /**
     * Updates page size for forum from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setForumPageSize(Map params, User user, Map env) {
        return setLimitedSize(params, PARAM_FORUM_PAGE_SIZE, user.getData(), "/data/settings/forum_size", 10, 100, env);
    }

    private boolean addToBookmarks(HttpServletRequest request, Map params, User user, Map env) {
        Persistence persistence = PersistenceFactory.getPersistence();

        String rid = (String) params.get(PARAM_RID);
        String prefix = (String) params.get(PARAM_PREFIX);
        if (rid == null || prefix == null) {
            ServletUtils.addError(PARAM_RID, "Chybí parametr " + PARAM_RID + "!", env, null);
            return false;
        }

        Element elem = DocumentHelper.makeElement(user.getData(), "/data/links");
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

        return true;

    }

    /**
     * Removes a rids from user's bookmarks. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean removeFromBookmarks(Map params, User user, Map env) {
        List rids = Tools.asList(params.get(PARAM_RID));
        if (rids.isEmpty()) {
            ServletUtils.addError(PARAM_RID, "Nevybral jste žádné dokumenty.", env, null);
            return false;
        }

        Element bookmarks = (Element) user.getData().selectSingleNode("/data/links");
        for (Iterator iter = rids.iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            Node node = bookmarks.selectSingleNode("link/rid[text()=\"" + s + "\"]");
            if (node != null) {
                node.getParent().detach();
            }
        }

        return true;
    }

    /**
     * Adds a uid to users blacklist. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean addToBlacklist(Map params, User user, Map env) {
        boolean paramFound = false;
        Element blacklist = DocumentHelper.makeElement(user.getData(), "/data/settings/blacklist");

        String uid = (String) params.get(PARAM_BLACKLIST_USER);
        if (uid != null) {
            paramFound = true;
            Node node = blacklist.selectSingleNode("uid[text()=\"" + uid + "\"]");
            if (node != null)
                return true;
            blacklist.addElement("uid").setText(uid);
        }

        String name = (String) params.get(PARAM_BLACKLIST_NAME);
        if (name != null) {
            paramFound = true;
            Node node = blacklist.selectSingleNode("name[text()=\"" + uid + "\"]");
            if (node != null)
                return true;
            blacklist.addElement("name").setText(name);
        }

        if (! paramFound) {
            ServletUtils.addError(PARAM_BLACKLIST_USER, "Chybí parametr " + PARAM_BLACKLIST_USER + "!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Removes a uid from users blacklist. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean removeFromBlacklist(Map params, User user, Map env) {
        boolean paramFound = false;
        List uids = Tools.asList(params.get(PARAM_BLACKLIST_USER));
        Element blacklist = (Element) user.getData().selectSingleNode("/data/settings/blacklist");
        for (Iterator iter = uids.iterator(); iter.hasNext();) {
            paramFound = true;
            String s = (String) iter.next();
            Node node = blacklist.selectSingleNode("uid[text()=\"" + s + "\"]");
            if (node != null)
                node.detach();
        }

        List names = Tools.asList(params.get(PARAM_BLACKLIST_NAME));
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            paramFound = true;
            String s = (String) iter.next();
            Node node = blacklist.selectSingleNode("name[text()=\"" + s + "\"]");
            if (node != null)
                node.detach();
        }

        if (! paramFound) {
            ServletUtils.addError(PARAM_BLACKLIST_USER, "Nevybral jste žádného uživatele.", env, null);
            return false;
        }
        return true;
    }

    /**
     * Subscribes user to weekly summary from parameters. Changes are not synchronized with persistence.
     */
    private boolean setWeeklySummary(Map params, User user) {
        String subscription = (String) params.get(PARAM_SUBSCRIBE_WEEKLY);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/communication/email/weekly_summary");
        String value = ("yes".equals(subscription))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Subscribes user to monthly summary from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setMonthlySummary(Map params, User user) {
        String subscription = (String) params.get(PARAM_SUBSCRIBE_MONTHLY);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/communication/email/newsletter");
        String value = ("yes".equals(subscription))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Subscribes user to email forum from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setForumByEmail(Map params, User user) {
        String subscription = (String) params.get(PARAM_SUBSCRIBE_FORUM);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/communication/email/forum");
        String value = ("yes".equals(subscription))? "yes":"no";
        element.setText(value);
        if("yes".equals(value))
            SubscribedUsers.getInstance().addUser(user.getId(), user.getEmail());
        else
            SubscribedUsers.getInstance().removeUser(user.getId());
        return true;
    }

    /**
     * Sets page flow for admin, when he move discussion from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setReturnBackToForum(Map params, User user) {
        String tmp = (String) params.get(PARAM_RETURN_TO_FORUM);
        if ( tmp==null || tmp.length()==0 )
            return true;
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/return_to_forum");
        String value = ("yes".equals(tmp))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Sets user roles from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setUserRoles(Map params, User user) {
        Element nodeRoles = (Element) user.getData().selectSingleNode("/data/roles");
        if ( nodeRoles!=null )
            nodeRoles.detach();

        Object tmp = params.get(PARAM_USER_ROLES);
        if ( tmp==null || (tmp instanceof List && ((List)tmp).size()==0) ) {
            return true;
        }
        if ( tmp instanceof String ) {
            List list = new ArrayList(1);
            list.add(tmp);
            tmp = list;
        }

        nodeRoles = DocumentHelper.makeElement(user.getData(), "/data/roles");
        for ( Iterator iter = ((List) tmp).iterator(); iter.hasNext(); ) {
            String role = (String) iter.next();
            nodeRoles.addElement("role").setText(role);
        }
        return true;
    }

    /**
     * Sets selected feeds from parameters. Changes are not synchronized with persistence.
     * @return false, if there is a major error.
     */
    private boolean setFeeds(Map params, User user) {
        Element element = (Element) user.getData().selectSingleNode("/data/settings/feeds");
        if (element != null)
            element.detach();

        StringBuffer sb = new StringBuffer();
        List maintainedServers = UpdateLinks.getMaintainedServers();
        for (Iterator iter = maintainedServers.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            String param = (String) params.get(PARAM_FEED + id);
            if (param != null && param.length() > 0)
                sb.append(id).append(',');
        }

        if (sb.length() == 0)
            return true;

        sb.setLength(sb.length() - 1); // remove last comma
        DocumentHelper.makeElement(user.getData(), "/data/settings/feeds").setText(sb.toString());
        return true;
    }

    /**
     * Overrides number of links per feed.  Changes are not synchronized with persistence.
     * @return false, if there is a major error.
     */
    private boolean setFeedSize(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = (Integer) maxSizes.get(VariableFetcher.KEY_INDEX_LINKS);
        boolean result = setLimitedSize(params, PARAM_INDEX_FEED_SIZE, user.getData(), "/data/settings/index_links", 1, max, env);
        max = (Integer) maxSizes.get(VariableFetcher.KEY_TEMPLATE_LINKS);
        result &= setLimitedSize(params, PARAM_TEMPLATE_FEED_SIZE, user.getData(), "/data/settings/template_links", 1, max, env);
        return result;
    }

    /**
     * Uploads photo from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPhoto(Map params, User user, Map env) {
        if (params.containsKey(PARAM_REMOVE_PHOTO)) {
            Node node = user.getData().selectSingleNode("/data/profile/photo");
            if (node != null) {
                String localPath = AbcConfig.calculateDeployedPath(node.getText().substring(1));
                new File(localPath).delete();
                node.detach();
            }
            return true;
        }

        FileItem fileItem = (FileItem) params.get(PARAM_PHOTO);
        if ( fileItem==null ) {
            ServletUtils.addError(PARAM_PHOTO, "Vyberte soubor s vaší fotografií!", env, null);
            return false;
        }

        String suffix = getFileSuffix(fileItem.getName()).toLowerCase();
        if ( !(suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif")) ) {
            ServletUtils.addError(PARAM_PHOTO, "Soubor musí být typu JPG, GIF nebo JPEG!", env, null);
            return false;
        }

        String fileName = "images/faces/"+user.getId()+"."+suffix;
        File file = new File(AbcConfig.calculateDeployedPath(fileName));
        try {
            fileItem.write(file);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_PHOTO, "Chyba při zápisu na disk!", env, null);
            log.error("Neni mozne ulozit fotografii "+file.getAbsolutePath()+" na disk!",e);
            return false;
        }

        Element photo = DocumentHelper.makeElement(user.getData(), "/data/profile/photo");
        photo.setText("/"+fileName);
        return true;
    }

   /**
     * Uploads photo from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setAvatar(Map params, User user, Map env) {
        if (params.containsKey(PARAM_REMOVE_AVATAR)) {
            Node node = user.getData().selectSingleNode("/data/profile/avatar");
            if (node != null) {
                String localPath = AbcConfig.calculateDeployedPath(node.getText().substring(1));
                new File(localPath).delete();
                node.detach();
            }
            return true;
        }

        FileItem fileItem = (FileItem) params.get(PARAM_AVATAR);
        if ( fileItem == null ) {
            ServletUtils.addError(PARAM_AVATAR, "Vyberte soubor s avatarem!", env, null);
            return false;
        }

        String suffix = getFileSuffix(fileItem.getName()).toLowerCase();
        if ( ! (suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif")) ) {
            ServletUtils.addError(PARAM_AVATAR, "Soubor musí být typu JPG, GIF nebo JPEG!", env, null);
            return false;
        }

        try {
            Iterator readers = ImageIO.getImageReadersBySuffix(suffix);
            ImageReader reader = (ImageReader) readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(fileItem.getInputStream());
            reader.setInput(iis, false);
            if (reader.getNumImages(true) > 1) {
                ServletUtils.addError(PARAM_AVATAR, "Animované obrázky nejsou povoleny!", env, null);
                return false;
            }
            if (reader.getHeight(0) > 50 || reader.getWidth(0) > 50) {
                ServletUtils.addError(PARAM_AVATAR, "Avatar přesahuje povolené maximální rozměry!", env, null);
                return false;
            }
        } catch(Exception e) {
            ServletUtils.addError(PARAM_AVATAR, "Nelze načíst obrázek!", env, null);
            return false;
        }

        String fileName = "images/avatars/" + user.getId() + "." + suffix;
        File file = new File(AbcConfig.calculateDeployedPath(fileName));
        try {
            fileItem.write(file);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_AVATAR, "Chyba při zápisu na disk!", env, null);
            log.error("Není možné uložit avatar " + file.getAbsolutePath() + " na disk!",e);
            return false;
        }

        Element photo = DocumentHelper.makeElement(user.getData(), "/data/profile/avatar");
        photo.setText("/"+fileName);
        return true;
    }

    /**
     * Sets user setting (number).
     * @param params map of params
     * @param paramName param name to be searched
     * @param document document to be updated
     * @param xpath xpath to be updated within document
     * @param min minimum allowed valued
     * @param max maximum allowed valued
     * @param env possible error message wil be stored there
     * @return false in case of major error
     */
    private boolean setLimitedSize(Map params, String paramName, Document document, String xpath, int min, int max, Map env) {
        String limit = (String) params.get(paramName);
        if (limit == null || limit.length() == 0) {
            Node node = document.selectSingleNode(xpath);
            if (node != null)
                node.detach();
        } else {
            int tmp = Misc.parseInt(limit, min - 1);
            if (tmp < min || tmp > max) {
                ServletUtils.addError(paramName, "Zadejte číslo v rozsahu "+min+" - " + max + "!", env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, xpath);
            element.setText(limit);
        }
        return true;
    }

    /**
     * Extracts text after last dot in string.
     * @param name
     * @return text after last dot
     */
    private String getFileSuffix(String name) {
        if ( name==null )
            return "";
        int i = name.lastIndexOf('.');
        if ( i==-1 )
            return "";
        else
            return name.substring(i+1);
    }

    /**
     * @param html non-empty string that may contain HTML
     * @return number of text characters (length of string stripped of HTML tags)
     */
    private int contentSize(String html) {
        String stripped = Tools.removeTags(html);
        return stripped.length();
    }

    /**
     * @return random alphanumeric string of length defined in configuration
     */
    public static String generateTicket(int userId) {
        StringBuffer sb = new StringBuffer();
        Random rand = new Random(userId + System.currentTimeMillis());
        int position, ticketLength = AbcConfig.getTicketLength();
        char c;
        for (int i = 0; i < ticketLength; i++ ) {
            position = rand.nextInt(62);
            if (position < 26)
                c = (char)('A' + position);
            else if (position < 52)
                c = (char)('a' + (position - 26));
            else
                c = (char)('0' + (position - 52));
            sb.append(c);
        }
        return sb.toString();
    }
}
