/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 8:22:28 AM
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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
    public static final String ACTION_SEND_PASSWORD = "forgottenPassword";


    /**
     * Put your processing here. Return null, if you have redirected browser to another URL.
     * @param env holds all variables, that shall be available in template, when it is being processed.
     * It may also contain VAR_USER and VAR_PARAMS objects.
     * @return name of template to be executed or null
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( action==null )
            return handleProfile(request, env);

        if ( action.equals(ACTION_LOGIN) )
            return handleLogin(request,env);

        if ( action.equals(ACTION_LOGIN2) )
            return handleLogin2(request,response,env);

        if ( action.equals(ACTION_SHOW_MY_PROFILE) ) {
            User user = (User) env.get(Constants.VAR_USER);
            User managed = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);
            if (managed==null) {
                if (user==null)
                    return FMTemplateSelector.select("ViewUser", "login", env, request);
                else
                    managed = user;
            } else
                managed = (User) PersistanceFactory.getPersistance().findById(managed);

            env.put(VAR_PROFILE, managed);
            if (user==null || (user.getId()!=managed.getId() && !user.hasRole(Roles.USER_ADMIN)))
                return handleProfile(request, env);
            else
                return handleMyProfile(request,env);
        }

        if (action.equals(ACTION_SEND_EMAIL))
            return handleSendEmail(request, response, env);

        if (action.equals(ACTION_SEND_PASSWORD))
            return handleSendForgottenPassword(request, response, env);

        return handleProfile(request,env);
    }

    /**
     * shows profile for selected user
     */
    protected String handleProfile(HttpServletRequest request, Map env) throws Exception {
        if (env.get(VAR_PROFILE)==null) {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            Persistance persistance = PersistanceFactory.getPersistance();

            User user = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);
            if ( user==null )
                return ServletUtils.showErrorPage("Chybí parametr uid!", env, request);
            user = (User) persistance.findById(user);
            env.put(VAR_PROFILE, user);

            SQLTool sqlTool = SQLTool.getInstance();
            Map counts = new HashMap();
            counts.put("article", new Integer(sqlTool.countArticleRelationsByUser(user.getId())));
            counts.put("news", new Integer(sqlTool.countNewsRelationsByUser(user.getId())));
            counts.put("question", new Integer(sqlTool.countQuestionRelationsByUser(user.getId())));
            counts.put("comment", new Integer(sqlTool.countCommentRelationsByUser(user.getId())));
            counts.put("hardware", new Integer(sqlTool.countRecordRelationsWithUserAndType(user.getId(), Record.HARDWARE)));
            counts.put("software", new Integer(sqlTool.countRecordRelationsWithUserAndType(user.getId(), Record.SOFTWARE)));
            env.put(VAR_COUNTS, counts);
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
     * shows login screen
     */
    protected String handleLogin(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("ViewUser","login",env,request);
    }

    /**
     * handle login submit
     */
    protected String handleLogin2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        if ( env.get(Constants.VAR_USER)!=null ) {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            String id = new Integer(((User)env.get(Constants.VAR_USER)).getId()).toString();
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
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        User user = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);
        user = (User) persistance.findById(user);
        request.getSession().setAttribute(SendEmail.PREFIX+EmailSender.KEY_TO, user.getEmail());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Mail");
        return null;
    }

    /**
     * Sends forgotten password.
     */
    protected String handleSendForgottenPassword(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        User user = (User) InstanceUtils.instantiateParam(PARAM_USER_SHORT, User.class, params, request);
        persistance.synchronize(user);

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
