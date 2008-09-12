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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.Screenshot;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.html.edit.EditBookmarks;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.freemarker.Tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.dom4j.Element;

/**
 * Profile of the user
 */
public class ViewUser implements AbcAction {

    public static final String VAR_PROFILE = "PROFILE";
    public static final String VAR_COUNTS = "COUNTS";
    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_LAST_DESKTOP = "LAST_DESKTOP";
    public static final String VAR_DESKTOPS = "DESKTOPS";
    public static final String VAR_VIDEOS = "VIDEOS";
    public static final String VAR_INVALID_EMAIL = "INVALID_EMAIL";
    public static final String VAR_SUBPORTALS = "SUBPORTALS";

    public static final String PARAM_USER = "userId";
    public static final String PARAM_USER_SHORT = "uid";
    public static final String PARAM_URL = "url";

    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGIN2 = "login2";
    public static final String ACTION_SEND_EMAIL = "sendEmail";
    public static final String ACTION_SHOW_MY_PROFILE = "myPage";
    public static final String ACTION_SHOW_MY_OBJECTS = "objekty";
    public static final String ACTION_BOOKMARKS = "zalozky";
    public static final String ACTION_GPG = "gpg";

    private Pattern reTagId = Pattern.compile(UrlUtils.PREFIX_PEOPLE + "/" + "([^/?]+)/?(\\w+)?");

    /**
     * Put your processing here. Return null, if you have redirected browser to another URL.
     * @param env holds all variables, that shall be available in template, when it is being processed.
     * It may also contain VAR_USER and VAR_PARAMS objects.
     * @return name of template to be executed or null
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_LOGIN.equals(action) )
            return handleLogin(request,env);

        if ( ACTION_LOGIN2.equals(action) )
            return handleLogin2(request,response,env);

        User profile = null;
        String url = (String) env.get(Constants.VAR_REQUEST_URI);
        Matcher matcher = reTagId.matcher(url);
        if (matcher.find()) {
            String login = matcher.group(1);
            Integer id = SQLTool.getInstance().getUserByLogin(login);
            if (id != null)
                profile = new User(id);

            if (action == null && matcher.group(2) != null)
                action = matcher.group(2);
        } else {
            profile = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);
        }

        if ( ACTION_SHOW_MY_PROFILE.equals(action) ) {
            User user = (User) env.get(Constants.VAR_USER);
            if (profile==null) {
                if (user==null)
                    return FMTemplateSelector.select("ViewUser", "login", env, request);
                else
                    profile = user;
            } else
                profile = (User) persistence.findById(profile);

            env.put(VAR_PROFILE, profile);
            if (user==null || (user.getId()!=profile.getId() && !user.hasRole(Roles.USER_ADMIN)))
                return handleProfile(request, env);
            else
                return handleMyProfile(request,env);
        }

        if (profile == null)
            return ServletUtils.showErrorPage("Uživatel nebyl nalezen nebo byl zadán nesprávný parametr!", env, request);

        profile = (User) persistence.findById(profile);

        if (ACTION_BOOKMARKS.equals(action))
            return EditBookmarks.processList(request, response, env, profile);

        env.put(VAR_PROFILE, profile);
        env.put(VAR_INVALID_EMAIL, ! Misc.hasValidEmail(profile));

        if (ACTION_SHOW_MY_OBJECTS.equals(action))
            return handleMyObjects(request, env);
        
        if (ACTION_GPG.equals(action))
            return handleGPG(request, response, env);

        if (ACTION_SEND_EMAIL.equals(action))
            return handleSendEmail(request, response, env);

        return handleProfile(request,env);
    }

    /**
     * shows profile for selected user
     */
    protected String handleProfile(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(VAR_PROFILE);
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();

        Element settingsBlog = (Element) user.getData().selectSingleNode("/data/settings/blog");
        if (settingsBlog != null) {
            int id = Misc.parseInt(settingsBlog.getText(), 0);
            Category blog = (Category) persistence.findById(new Category(id));
            env.put(ViewBlog.VAR_BLOG, blog);

            Element element = (Element) blog.getData().selectSingleNode("//settings/page_size");
            int count = Misc.parseInt((element != null) ? element.getText() : null, ViewBlog.getDefaultPageSize());

            List qualifiers = new ArrayList();
            qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL, user.getId()));
            qualifiers.add(Qualifier.SORT_BY_CREATED);
            qualifiers.add(Qualifier.ORDER_DESCENDING);
            qualifiers.add(new LimitQualifier(0, count));

            Qualifier[] qa = new Qualifier[qualifiers.size()];
            List stories = SQLTool.getInstance().findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
            Tools.syncList(stories);
            env.put(ViewBlog.VAR_STORIES, stories);
        }
        
        List<Relation> subportals = sqlTool.findSubportalMembership(user.getId());
        Tools.syncList(subportals);
        env.put(VAR_SUBPORTALS, subportals);

        Set<String> property = Collections.singleton(Integer.toString(user.getId()));
        Map<String, Set<String>> filters = Collections.singletonMap(Constants.PROPERTY_USED_BY, property);
        List<Relation> softwares = sqlTool.findItemRelationsWithTypeWithFilters(Item.SOFTWARE, null, filters);
        Tools.syncList(softwares);
        env.put(VAR_SOFTWARE, softwares);

        // find user's favourite desktop screenshots
        filters = Collections.singletonMap(Constants.PROPERTY_FAVOURITED_BY, property);
        List<Relation> desktops = sqlTool.findItemRelationsWithTypeWithFilters(Item.SCREENSHOT, null, filters);
        Tools.syncList(desktops);
        env.put(VAR_DESKTOPS, desktops);
        
        // find user's favourite videos
        filters = Collections.singletonMap(Constants.PROPERTY_FAVOURITED_BY, property);
        List<Relation> videos = sqlTool.findItemRelationsWithTypeWithFilters(Item.VIDEO, null, filters);
        Tools.syncList(videos);
        env.put(VAR_VIDEOS, videos);

        // find the last screenshot uploaded by this user
        setLastDesktop(user.getId(), env);

        return FMTemplateSelector.select("ViewUser","profile",env,request);
    }

    /**
     * shows profile of logged in user
     */
    protected String handleMyProfile(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("ViewUser","myProfile",env,request);
    }

    /**
     * shows numbers of objects
     */
    protected String handleMyObjects(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        User user = (User) env.get(VAR_PROFILE);

        Map counts = new HashMap();
        Qualifier[] qualifiers = new Qualifier[]{ new CompareCondition(Field.NUMERIC1, Operation.EQUAL, user.getId())};
        List<Relation> authors = sqlTool.findItemRelationsWithType(Item.AUTHOR, qualifiers);
        if (! authors.isEmpty()) {
            Relation author = (Relation) persistence.findById(authors.get(0));
            counts.put("article", sqlTool.countArticleRelationsByAuthor(author.getId()));
            env.put(VAR_AUTHOR, author);
        }

        Element settingsBlog = (Element) user.getData().selectSingleNode("/data/settings/blog");
        if (settingsBlog != null) {
            int id = Misc.parseInt(settingsBlog.getText(), 0);
            Category blog = (Category) persistence.findById(new Category(id));
            env.put(ViewBlog.VAR_BLOG, blog);

            Qualifier[] qa = new Qualifier[]{ new CompareCondition(Field.OWNER, Operation.EQUAL, user.getId()) };
            int stories = sqlTool.countItemRelationsWithType(Item.BLOG, qa);
            counts.put("story", stories);
        }

        counts.put("news", sqlTool.countNewsRelationsByUser(user.getId()));
        counts.put("question", sqlTool.countQuestionRelationsByUser(user.getId()));
        counts.put("comment", sqlTool.countCommentRelationsByUser(user.getId()));
        counts.put("wiki", sqlTool.countWikiRelationsByUser(user.getId()));
        env.put(VAR_COUNTS, counts);

        return FMTemplateSelector.select("ViewUser","counter",env,request);
    }
    
    /**
     * Sends the contents of the file with the GPG public key
     */
    protected String handleGPG(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(VAR_PROFILE);
        Element element = (Element) user.getData().selectSingleNode("/data/profile/gpg");
        
        if (element == null)
            return null;
        else {
            File file = new File(AbcConfig.calculateDeployedPath(element.getText()));
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            
            fis.read(data);
            response.getWriter().write(new String(data));
            return null;
        }
    }

    /**
     * shows login screen
     */
    protected String handleLogin(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String url = request.getHeader("Referer");
        params.put(PARAM_URL, url);
        return FMTemplateSelector.select("ViewUser","login",env,request);
    }

    /**
     * handle login submit
     */
    protected String handleLogin2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        if ( env.get(Constants.VAR_USER)!=null ) {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            String url = (String) params.get(PARAM_URL);
            if (url!=null) {
                if (url.indexOf("logout=true")!=-1)
                    url = "/";
                UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
                urlUtils.redirect(response, url);
                return null;
            }

            User user = (User)env.get(Constants.VAR_USER);
            env.put(VAR_PROFILE, user);
            String id = Integer.toString(user.getId());
            params.put(PARAM_USER,id);
            return handleProfile(request,env);
        }
        else
            return FMTemplateSelector.select("ViewUser","login",env,request);
    }

    /**
     * shows login screen
     */
    protected String handleSendEmail(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(VAR_PROFILE);
        request.getSession().setAttribute(SendEmail.PREFIX+EmailSender.KEY_TO, user.getEmail());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Mail?url=/Profile/"+user.getId());
        return null;
    }

    /**
     * Finds last published desktop for specified user and stores it in VAR_LAST_DESKTOP variable.
     * @param user searched user
     * @param env environment
     */
    public static void setLastDesktop(int user, Map env) {
        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{new CompareCondition(Field.OWNER, Operation.EQUAL, user),
                                                 Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 1)};
        List<Relation> desktops = sqlTool.findItemRelationsWithType(Item.SCREENSHOT, qualifiers);
        if (! desktops.isEmpty()) {
            Relation desktopRelation = desktops.get(0);
            Tools.sync(desktopRelation.getChild());
            env.put(VAR_LAST_DESKTOP, new Screenshot(desktopRelation));
        }

    }
}
