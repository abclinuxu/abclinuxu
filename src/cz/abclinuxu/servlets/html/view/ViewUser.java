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
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.extra.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.dom4j.Element;

/**
 * Profile of the user
 */
public class ViewUser implements AbcAction {

    public static final String VAR_PROFILE = "PROFILE";
    public static final String VAR_COUNTS = "COUNTS";

    public static final String PARAM_USER = "userId";
    public static final String PARAM_USER_SHORT = "uid";
    public static final String PARAM_URL = "url";

    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGIN2 = "login2";
    public static final String ACTION_SEND_EMAIL = "sendEmail";
    public static final String ACTION_SHOW_MY_PROFILE = "myPage";
    public static final String ACTION_SHOW_MY_OBJECTS = "objekty";
    public static final String ACTION_SEND_PASSWORD = "forgottenPassword";


    /**
     * Put your processing here. Return null, if you have redirected browser to another URL.
     * @param env holds all variables, that shall be available in template, when it is being processed.
     * It may also contain VAR_USER and VAR_PARAMS objects.
     * @return name of template to be executed or null
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_LOGIN.equals(action) )
            return handleLogin(request,env);

        if ( ACTION_LOGIN2.equals(action) )
            return handleLogin2(request,response,env);

        User profile = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);

        if ( ACTION_SHOW_MY_PROFILE.equals(action) ) {
            User user = (User) env.get(Constants.VAR_USER);
            if (profile==null) {
                if (user==null)
                    return FMTemplateSelector.select("ViewUser", "login", env, request);
                else
                    profile = user;
            } else
                profile = (User) persistance.findById(profile);

            env.put(VAR_PROFILE, profile);
            if (user==null || (user.getId()!=profile.getId() && !user.hasRole(Roles.USER_ADMIN)))
                return handleProfile(request, env);
            else
                return handleMyProfile(request,env);
        }

        if (profile == null)
            return ServletUtils.showErrorPage("Chybí parametr uid!", env, request);

        profile = (User) persistance.findById(profile);
        env.put(VAR_PROFILE, profile);

        if (ACTION_SHOW_MY_OBJECTS.equals(action))
            return handleMyObjects(request, env);

        if (ACTION_SEND_EMAIL.equals(action))
            return handleSendEmail(request, response, env);

        if (ACTION_SEND_PASSWORD.equals(action))
            return handleSendForgottenPassword(request, response, env);

        return handleProfile(request,env);
    }

    /**
     * shows profile for selected user
     */
    protected String handleProfile(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(VAR_PROFILE);

        Element settingsBlog = (Element) user.getData().selectSingleNode("/data/settings/blog");
        if (settingsBlog != null) {
            int id = Misc.parseInt(settingsBlog.getText(), 0);
            Category blog = (Category) persistance.findById(new Category(id));
            env.put(ViewBlog.VAR_BLOG, blog);

            Element element = (Element) blog.getData().selectSingleNode("//settings/page_size");
            int count = Misc.parseInt((element != null) ? element.getText() : null, ViewBlog.getDefaultPageSize());

            List qualifiers = new ArrayList();
            qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL, new Integer(user.getId())));
            qualifiers.add(Qualifier.SORT_BY_CREATED);
            qualifiers.add(Qualifier.ORDER_DESCENDING);
            qualifiers.add(new LimitQualifier(0, count));

            Qualifier[] qa = new Qualifier[qualifiers.size()];
            List stories = SQLTool.getInstance().findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
            Tools.syncList(stories);
            env.put(ViewBlog.VAR_STORIES, stories);
        }

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
        User user = (User) env.get(VAR_PROFILE);

        SQLTool sqlTool = SQLTool.getInstance();
        Map counts = new HashMap();
        counts.put("article", new Integer(sqlTool.countArticleRelationsByUser(user.getId())));
        counts.put("news", new Integer(sqlTool.countNewsRelationsByUser(user.getId())));
        counts.put("question", new Integer(sqlTool.countQuestionRelationsByUser(user.getId())));
        counts.put("comment", new Integer(sqlTool.countCommentRelationsByUser(user.getId())));
        counts.put("hardware", new Integer(sqlTool.countRecordRelationsWithUserAndType(user.getId(), Record.HARDWARE)));
        counts.put("software", new Integer(sqlTool.countRecordRelationsWithUserAndType(user.getId(), Record.SOFTWARE)));
        counts.put("dictionary", new Integer(sqlTool.countRecordRelationsWithUserAndType(user.getId(), Record.DICTIONARY)));
        env.put(VAR_COUNTS, counts);

        return FMTemplateSelector.select("ViewUser","counter",env,request);
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
     * Sends forgotten password.
     */
    protected String handleSendForgottenPassword(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(VAR_PROFILE);

        Map data = new HashMap();
        data.put(Constants.VAR_USER, user);
        data.put(EmailSender.KEY_FROM, "admin@abclinuxu.cz");
        data.put(EmailSender.KEY_TO, user.getEmail());
        data.put(EmailSender.KEY_SUBJECT, "Zapomenute heslo");
        data.put(EmailSender.KEY_TEMPLATE, "/mail/password.ftl");
        EmailSender.sendEmail(data);

        ServletUtils.addMessage("Heslo bylo odesláno na adresu "+user.getEmail()+".", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/");
        return null;
    }
}
