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
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.exceptions.DuplicateKeyException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.parser.safehtml.ProfileGuard;
import cz.abclinuxu.utils.parser.safehtml.NoHTMLGuard;
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
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.scheduler.UpdateLinks;
import org.apache.commons.fileupload.FileItem;
import org.apache.regexp.RE;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.htmlparser.util.ParserException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Class for manipulation with User.
 */
public class EditUser implements AbcAction, Configurable {
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
    public static final String PARAM_SIGNATURE = "signature";
    public static final String PARAM_COOKIE_VALIDITY = "cookieValid";
    public static final String PARAM_DISCUSSIONS_COUNT = "discussions";
    public static final String PARAM_NEWS_COUNT = "news";
    public static final String PARAM_STORIES_COUNT = "stories";
    public static final String PARAM_FOUND_PAGE_SIZE = "search";
    public static final String PARAM_FORUM_PAGE_SIZE = "forum";
    public static final String PARAM_BLACKLIST_USER = "bUid";
    public static final String PARAM_SUBSCRIBE_MONTHLY = "monthly";
    public static final String PARAM_SUBSCRIBE_WEEKLY = "weekly";
    public static final String PARAM_SUBSCRIBE_FORUM = "forum";
    public static final String PARAM_PHOTO = "photo";
    public static final String PARAM_RETURN_TO_FORUM = "moveback";
    public static final String PARAM_USER_ROLES = "roles";
    public static final String PARAM_USERS = "users";
    public static final String PARAM_URL_CSS = "css";
    public static final String PARAM_GUIDEPOST = "guidepost";
    public static final String PARAM_FEED = "feed";
    public static final String PARAM_TEMPLATE_FEED_SIZE = "feedSize";
    public static final String PARAM_INDEX_FEED_SIZE = "indexFeedSize";
    public static final String PARAM_URL = "url";

    public static final String VAR_MANAGED = "MANAGED";
    public static final String VAR_DEFAULT_DISCUSSION_COUNT = "DEFAULT_DISCUSSIONS";
    public static final String VAR_DEFAULT_NEWS_COUNT = "DEFAULT_NEWS";
    public static final String VAR_DEFAULT_STORIES_COUNT = "DEFAULT_STORIES";
    public static final String VAR_DEFAULT_FEED_LINKS_COUNT = "DEFAULT_LINKS";
    public static final String VAR_DEFAULT_TEMPLATE_FEED_LINKS_COUNT = "DEFAULT_TEMPLATE_LINKS";
    public static final String VAR_USERS = "USERS";
    public static final String VAR_SERVERS = "SERVERS";

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
    public static final String ACTION_EDIT_SETTINGS = "editSettings";
    public static final String ACTION_EDIT_SETTINGS_STEP2 = "editSettings2";
    public static final String ACTION_EDIT_BLACKLIST = "editBlacklist";
    public static final String ACTION_REMOVE_FROM_BLACKLIST = "fromBlacklist";
    public static final String ACTION_ADD_TO_BLACKLIST = "toBlacklist";
    public static final String ACTION_EDIT_SUBSCRIPTION = "subscribe";
    public static final String ACTION_EDIT_SUBSCRIPTION_STEP2 = "subscribe2";
    public static final String ACTION_GRANT_ROLES = "grant";
    public static final String ACTION_GRANT_ROLES_STEP2 = "grant2";
    public static final String ACTION_GRANT_ROLES_STEP3 = "grant3";
    public static final String ACTION_INVALIDATE_EMAIL = "invalidateEmail";
    public static final String ACTION_INVALIDATE_EMAIL2 = "invalidateEmail2";
    public static final String ACTION_ADD_GROUP_MEMBER = "addToGroup";

    public static final String PREF_INVALID_NICK_REGEXP = "regexp.invalid.login";
    private static REProgram reLoginInvalid;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new EditUser());
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_INVALID_NICK_REGEXP, null);
        reLoginInvalid = new RECompiler().compile(tmp);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        User managed = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);
        User user = (User) env.get(Constants.VAR_USER);
        if ( managed==null )
            managed = user;
        else
            managed = (User) PersistanceFactory.getPersistance().findById(managed);
        env.put(VAR_MANAGED, managed);

        // registration doesn't require user to be logged in
        if (  action==null || action.equals(ACTION_REGISTER) )
            return FMTemplateSelector.select("EditUser","register",env,request);
        else if ( action.equals(ACTION_REGISTER_STEP2) )
            return actionAddStep2(request,response,env);

        // all other actions require user to be logged in and to have rights for this action
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( ! (user.getId()==managed.getId() || user.hasRole(Roles.USER_ADMIN)) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_EDIT_BASIC) )
            return actionEditBasic(request, env);

        if ( action.equals(ACTION_EDIT_BASIC_STEP2) )
            return actionEditBasic2(request, response, env);

        if ( action.equals(ACTION_CHANGE_PASSWORD) )
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        if ( action.equals(ACTION_CHANGE_PASSWORD_STEP2) )
            return actionPassword2(request, response, env);

        if ( action.equals(ACTION_EDIT_PERSONAL) )
            return actionEditPersonal(request, env);

        if ( action.equals(ACTION_EDIT_PERSONAL_STEP2) )
            return actionEditPersonal2(request, response, env);

        if ( action.equals(ACTION_EDIT_PROFILE) )
            return actionEditProfile(request, env);

        if ( action.equals(ACTION_EDIT_PROFILE_STEP2) )
            return actionEditProfile2(request, response, env);

        if ( action.equals(ACTION_EDIT_SETTINGS) )
            return actionEditSettings(request, env);

        if ( action.equals(ACTION_EDIT_SETTINGS_STEP2) )
            return actionEditSettings2(request, response, env);

        if ( action.equals(ACTION_EDIT_BLACKLIST) )
            return actionEditBlacklist(request, env);

        if ( action.equals(ACTION_ADD_TO_BLACKLIST) )
            return actionAddToBlacklist(request, response, env);

        if ( action.equals(ACTION_REMOVE_FROM_BLACKLIST) )
            return actionRemoveFromBlacklist(request, response, env);

        if ( action.equals(ACTION_EDIT_SUBSCRIPTION) )
            return actionEditSubscription(request, env);

        if ( action.equals(ACTION_EDIT_SUBSCRIPTION_STEP2) )
            return actionEditSubscription2(request, response, env);

        if ( action.equals(ACTION_UPLOAD_PHOTO) )
            return FMTemplateSelector.select("EditUser", "uploadPhoto", env, request);

        if ( action.equals(ACTION_UPLOAD_PHOTO_STEP2) )
            return actionUploadPhoto2(request, response, env);

        // these actions are restricted to admin only

        if ( user.hasRole(Roles.USER_ADMIN) || user.hasRole(Roles.CAN_INVALIDATE_EMAILS) ) {
            if ( action.equals(ACTION_INVALIDATE_EMAIL) )
                return FMTemplateSelector.select("EditUser", "invalidateEmail", env, request);

            if ( action.equals(ACTION_INVALIDATE_EMAIL2) )
                return actionInvalidateEmail(request, response, env);
        }

        if ( ! user.hasRole(Roles.USER_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_GRANT_ROLES) )
            return actionGrant(request, env);

        if ( action.equals(ACTION_GRANT_ROLES_STEP2) )
            return actionGrant2(request, env);

        if ( action.equals(ACTION_GRANT_ROLES_STEP3) )
            return actionGrant3(request, response, env);

        if ( action.equals(ACTION_ADD_GROUP_MEMBER) )
            return actionAddToGroup(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Creates new user.
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = new User();
        Persistance persistance = PersistanceFactory.getPersistance();

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

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "register", env, request);

        try {
            persistance.create(managed);
        } catch (DuplicateKeyException e) {
            ServletUtils.addError(PARAM_LOGIN, "Pøihla¹ovací jméno nebo pøezdívka jsou ji¾ pou¾ívány!", env, null);
            return FMTemplateSelector.select("EditUser", "register", env, request);
        }

        HttpSession session = request.getSession();
        session.setAttribute(Constants.VAR_USER, managed);

        Map data = new HashMap();
        data.put(Constants.VAR_USER, managed);
        data.put(EmailSender.KEY_FROM, "admin@abclinuxu.cz");
        data.put(EmailSender.KEY_TO, managed.getEmail());
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
        Persistance persistance = PersistanceFactory.getPersistance();

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
            persistance.update(managed);
        } catch ( DuplicateKeyException e ) {
            ServletUtils.addError(PARAM_LOGIN, "Toto jméno je ji¾ pou¾íváno!", env, null);
            return FMTemplateSelector.select("EditUser","editBasic",env,request);
        }

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if ( managed.getId()==sessionUser.getId() ) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zmìny byly ulo¾eny.",env, request.getSession());
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
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        canContinue &= setPassword(params,managed,env);

        if ( ! canContinue )
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        persistance.update(managed);

        Cookie cookie = ServletUtils.getCookie(request, Constants.VAR_USER);
        if ( cookie!=null )
            ServletUtils.deleteCookie(cookie, response);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Heslo bylo zmìnìno.", env, request.getSession());
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
        node = document.selectSingleNode("/data/personal/signature");
        if ( node!=null )
            params.put(PARAM_SIGNATURE, node.getText());
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
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "editPersonal", env, request);

        canContinue &= setSignature(params, managed, env);
        canContinue &= setSex(params, managed, env);
        canContinue &= setBirthYear(params, managed, env);
        canContinue &= setCity(params, managed, env);
        canContinue &= setArea(params, managed, env);
        canContinue &= setCountry(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editPersonal", env, request);

        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zmìny byly ulo¾eny.", env, request.getSession());
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
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "editProfile", env, request);

        canContinue &= setMyPage(params, managed, env);
        canContinue &= setLinuxUserFrom(params, managed);
        canContinue &= setAbout(params, managed, env);
        canContinue &= setDistributions(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editProfile", env, request);

        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zmìny byly ulo¾eny.", env, request.getSession());
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
        Persistance persistance = PersistanceFactory.getPersistance();

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
        canContinue &= setGuidepost(params, managed);
        canContinue &= setDiscussionsSizeLimit(params, managed, env);
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

        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zmìny byly ulo¾eny.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    private void setDefaultValuesForEditSettings(Map env) {
        List maintainedServers = UpdateLinks.getMaintainedServers();
        List servers = new ArrayList(maintainedServers.size());
        Persistance persistance = PersistanceFactory.getPersistance();
        for (Iterator iter = maintainedServers.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            Server server = (Server) persistance.findById(new Server(id.intValue()));
            servers.add(server);
        }
        env.put(VAR_SERVERS, servers);

        Map defaultSizes = VariableFetcher.getInstance().getDefaultSizes();
        env.put(VAR_DEFAULT_DISCUSSION_COUNT, defaultSizes.get(VariableFetcher.KEY_QUESTION));
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

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId())
            sessionUser.synchronizeWith(managed);

    	ServletUtils.addMessage("Autor byl pøidán na seznam blokovaných u¾ivatelù.", env, request.getSession());
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

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId())
            sessionUser.synchronizeWith(managed);

        ServletUtils.addMessage("Autor byl odstranìn ze seznamu blokovaných u¾ivatelù.", env, request.getSession());
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
        Persistance persistance = PersistanceFactory.getPersistance();

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

        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zmìny byly ulo¾eny.", env, request.getSession());
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
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.hasRole(Roles.USER_ADMIN) )
            canContinue &= checkPassword(params, managed, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditUser", "uploadPhoto", env, request);

        canContinue &= setPhoto(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "uploadPhoto", env, request);

        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zmìny byly ulo¾eny.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&uid="+managed.getId());
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
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        canContinue &= checkPassword(params, user, env);
        canContinue &= setUserRoles(params, managed);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "grantRoles", env, request);

        persistance.update(managed);

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if (managed.getId() == sessionUser.getId()) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zmìny rolí u¾ivatele "+managed.getName()+" byly ulo¾eny.", env, request.getSession());
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
        Persistance persistance = PersistanceFactory.getPersistance();
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
                managed = (User) persistance.findById(new User(id));
                Element tagEmail = DocumentHelper.makeElement(managed.getData(), "/data/communication/email");
                tagEmail.attribute("valid").setText("no");
                persistance.update(managed);
                AdminLogger.logEvent(user, "zneplatnil email uzivateli "+managed.getName()+" - "+managed.getId());
                count++;
            } catch (Exception e) {
                sb.append("U¾ivatel "+id+" nebyl nalezen!<br>");
            }
        }

        if ( sb.length()>0 )
            ServletUtils.addError(Constants.ERROR_GENERIC, sb.toString(), env, request.getSession());
        ServletUtils.addMessage(count+" u¾ivatelùm byl zneplatnìn email.", env, request.getSession());
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
        Persistance persistance = PersistanceFactory.getPersistance();

        int group = Misc.parseInt((String) params.get(EditGroup.PARAM_GROUP), 0);
        if (group==0)
            return ServletUtils.showErrorPage("Chybí èíslo skupiny!",env,request);

        Element system = (Element) managed.getData().selectSingleNode("/data/system");
        system.addElement("group").setText(new Integer(group).toString());
        persistance.update(managed);

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
                ServletUtils.addError(paramName, e.getMessage(), env, null);
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
     * Updates password from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPassword(Map params, User user, Map env) {
        String password = (String) params.get(PARAM_PASSWORD);
        String password2 = (String) params.get(PARAM_PASSWORD2);

        if ( password==null || password.length()<4 ) {
            ServletUtils.addError(PARAM_PASSWORD, "Heslo je pøíli¹ krátké!", env, null);
            return false;
        }
        if ( !(password.equals(password2)) ) {
            ServletUtils.addError(PARAM_PASSWORD, "Hesla se li¹í!", env, null);
            return false;
        }
        user.setPassword(password);
        return true;
    }

    /**
     * Updates login from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setLogin(Map params, User user, Map env) {
        String login = (String) params.get(PARAM_LOGIN);
        if ( login==null || login.length()<3 ) {
            ServletUtils.addError(PARAM_LOGIN, "Pøihla¹ovací jméno musí mít nejménì tøi znaky!", env, null);
            return false;
        }
        if ( login.length()>16 ) {
            ServletUtils.addError(PARAM_LOGIN, "Pøihla¹ovací jméno nesmí mít více ne¾ 16 znakù!", env, null);
            return false;
        }
        if ( new RE(reLoginInvalid).match(login) ) {
            ServletUtils.addError(PARAM_LOGIN, "Pøihla¹ovací jméno smí obsahovat pouze písmena A a¾ Z, èíslice, pomlèku, teèku a podtr¾ítko!", env, null);
            return false;
        }
        user.setLogin(login);
        return true;
    }

    /**
     * Updates name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, User user, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()<4 ) {
            ServletUtils.addError(PARAM_NAME, "Jméno je pøíli¹ krátké!", env, null);
            return false;
        }

        if (! verifyGuard(NoHTMLGuard.class, name, PARAM_NAME, env))
            return false;

        user.setName(name);
        return true;
    }

    /**
     * Updates nick from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setNick(Map params, User user, Map env) {
        String nick = (String) params.get(PARAM_NICK);
        if ( nick==null || nick.trim().length()==0) {
            user.setNick(null);
            return true;
        }

        nick = nick.trim();
        if (nick.length()>20 ) {
            ServletUtils.addError(PARAM_NICK, "Pøezdívka je pøíli¹ dlouhá!", env, null);
            return false;
        }

        if (!verifyGuard(NoHTMLGuard.class, nick, PARAM_NICK, env))
            return false;

        user.setNick(nick);
        return true;
    }

    /**
     * Updates email from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setEmail(Map params, User user, Map env) {
        String email = (String) params.get(PARAM_EMAIL);
        if ( email==null || email.length()<6 || email.indexOf('@')==-1 ) {
            ServletUtils.addError(PARAM_EMAIL, "Neplatný email!", env, null);
            return false;
        }
        user.setEmail(email);
        Element tagEmail = DocumentHelper.makeElement(user.getData(), "/data/communication/email");
        tagEmail.attribute("valid").setText("yes");
        return true;
    }

    /**
     * Updates sex from parameters. Changes are not synchronized with persistance.
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
     * Updates birth year from parameters. Changes are not synchronized with persistance.
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
     * Updates city from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCity(Map params, User user, Map env) {
        String city = (String) params.get(PARAM_CITY);
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
     * Updates area from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setArea(Map params, User user, Map env) {
        String area = (String) params.get(PARAM_AREA);
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
     * Updates country from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCountry(Map params, User user, Map env) {
        String country = (String) params.get(PARAM_COUNTRY);
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
     * Updates home page from parameters. Changes are not synchronized with persistance.
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
     * Updates linuxUserFromYear from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setLinuxUserFrom(Map params, User user) {
        String year = (String) params.get(PARAM_LINUX_USER_FROM);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        if ( year==null || year.length()==0 ) {
            Node node = profile.element("linux_user_from_year");
            if (node!=null)
                profile.remove(node);
            return true;
        }
        DocumentHelper.makeElement(profile, "linux_user_from_year").setText(year);
        return true;
    }

    /**
     * Updates about from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setAbout(Map params, User user, Map env) {
        String about = (String) params.get(PARAM_ABOUT_ME);
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
     * Updates distributions from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setDistributions(Map params, User user, Map env) {
        List distros = (List) params.get(PARAM_DISTRIBUTION);
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
     * Updates signature from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setSignature(Map params, User user, Map env) {
        String signature = (String) params.get(PARAM_SIGNATURE);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( signature==null || signature.length()==0 ) {
            Node node = personal.element("signature");
            if ( node!=null )
                personal.remove(node);
            return true;
        }

        if (contentSize(signature)>100) {
            ServletUtils.addError(PARAM_SIGNATURE, "Maximální délka je 100 znakù!", env, null);
            return false;
        }

        if (!verifyGuard(SafeHTMLGuard.class, signature, PARAM_SIGNATURE, env))
            return false;

        DocumentHelper.makeElement(personal, "signature").setText(signature);
        return true;
    }

    /**
     * Updates URL with custom CSS from parameters. Changes are not synchronized with persistance.
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
     * Updates emoticons from parameters. Changes are not synchronized with persistance.
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
     * Updates signatures from parameters. Changes are not synchronized with persistance.
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
     * Updates guidepost from parameters. Changes are not synchronized with persistance.
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
     * Updates login cookie validity from parameters. Changes are not synchronized with persistance.
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
     * Updates size limit of discussions on main page from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setDiscussionsSizeLimit(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = ((Integer) maxSizes.get(VariableFetcher.KEY_QUESTION)).intValue();
        return setLimitedSize(params, PARAM_DISCUSSIONS_COUNT, user.getData(), "/data/settings/index_discussions", 0, max, env);
    }

    /**
     * Updates size limit for news in the template from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setNewsSizeLimit(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = ((Integer) maxSizes.get(VariableFetcher.KEY_NEWS)).intValue();
        return setLimitedSize(params, PARAM_NEWS_COUNT, user.getData(), "/data/settings/index_news", 0, max, env);
    }

    /**
     * Updates size limit for stories on index page from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setStoriesSizeLimit(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = ((Integer) maxSizes.get(VariableFetcher.KEY_STORY)).intValue();
        return setLimitedSize(params, PARAM_STORIES_COUNT, user.getData(), "/data/settings/index_stories", 0, max, env);
    }

    /**
     * Updates page size for found objects from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setFoundPageSize(Map params, User user, Map env) {
        return setLimitedSize(params, PARAM_FOUND_PAGE_SIZE, user.getData(), "/data/settings/found_size", 10, 100, env);
    }

    /**
     * Updates page size for forum from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setForumPageSize(Map params, User user, Map env) {
        return setLimitedSize(params, PARAM_FORUM_PAGE_SIZE, user.getData(), "/data/settings/forum_size", 10, 100, env);
    }

    /**
     * Adds a uid to users blacklist. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean addToBlacklist(Map params, User user, Map env) {
        String uid = (String) params.get(PARAM_BLACKLIST_USER);
        if ( uid==null ) {
            ServletUtils.addError(PARAM_BLACKLIST_USER, "Chybí parametr "+PARAM_BLACKLIST_USER+"!", env, null);
            return false;
        }

        Element blacklist = DocumentHelper.makeElement(user.getData(), "/data/settings/blacklist");
        Node node = blacklist.selectSingleNode("uid[text()=\"" + uid + "\"]");
        if (node != null)
            return true;

        blacklist.addElement("uid").setText(uid);
        return true;
    }

    /**
     * Removes a uid from users blacklist. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean removeFromBlacklist(Map params, User user, Map env) {
        List ids = null;
        Object o = params.get(PARAM_BLACKLIST_USER);
        if (o instanceof String) {
            ids = Collections.singletonList(o);
        } else if (o instanceof List)
            ids = (List) o;

        if ( ids==null || ids.size()==0 ) {
            ServletUtils.addError(PARAM_BLACKLIST_USER, "Nevybral jste ¾ádného u¾ivatele!", env, null);
            return false;
        }

        Element blacklist = (Element) user.getData().selectSingleNode("/data/settings/blacklist");
        for (Iterator iter = ids.iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            Node node = blacklist.selectSingleNode("uid[text()=\"" + s + "\"]");
            if (node != null)
                node.detach();
        }
        return true;
    }

    /**
     * Subscribes user to weekly summary from parameters. Changes are not synchronized with persistance.
     */
    private boolean setWeeklySummary(Map params, User user) {
        String subscription = (String) params.get(PARAM_SUBSCRIBE_WEEKLY);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/communication/email/weekly_summary");
        String value = ("yes".equals(subscription))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Subscribes user to monthly summary from parameters. Changes are not synchronized with persistance.
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
     * Subscribes user to email forum from parameters. Changes are not synchronized with persistance.
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
     * Sets page flow for admin, when he move discussion from parameters. Changes are not synchronized with persistance.
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
     * Sets user roles from parameters. Changes are not synchronized with persistance.
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
     * Sets selected feeds from parameters. Changes are not synchronized with persistance.
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
     * Overrides number of links per feed.  Changes are not synchronized with persistance.
     * @return false, if there is a major error.
     */
    private boolean setFeedSize(Map params, User user, Map env) {
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = ((Integer) maxSizes.get(VariableFetcher.KEY_INDEX_LINKS)).intValue();
        boolean result = setLimitedSize(params, PARAM_INDEX_FEED_SIZE, user.getData(), "/data/settings/index_links", 1, max, env);
        max = ((Integer) maxSizes.get(VariableFetcher.KEY_TEMPLATE_LINKS)).intValue();
        result &= setLimitedSize(params, PARAM_TEMPLATE_FEED_SIZE, user.getData(), "/data/settings/template_links", 1, max, env);
        return result;
    }

    /**
     * Uploads photo from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPhoto(Map params, User user, Map env) {
        FileItem fileItem = (FileItem) params.get(PARAM_PHOTO);
        if ( fileItem==null ) {
            ServletUtils.addError(PARAM_PHOTO, "Vyberte soubor s va¹í fotografií!", env, null);
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
            ServletUtils.addError(PARAM_PHOTO, "Chyba pøi zápisu na disk!", env, null);
            log.error("Neni mozne ulozit fotografii "+file.getAbsolutePath()+" na disk!",e);
            return false;
        }

        Element photo = DocumentHelper.makeElement(user.getData(), "/data/profile/photo");
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
                ServletUtils.addError(paramName, "Zadejte èíslo v rozsahu "+min+" - " + max + "!", env, null);
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
     * @return
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
}
